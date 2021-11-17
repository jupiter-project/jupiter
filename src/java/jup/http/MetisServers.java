/*
 * Copyright © 2020-2021 Jupiter Project Developers
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package jup.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jup.Appendix;
import jup.Block;
import jup.BlockchainProcessor;
import jup.Jup;
import jup.Transaction;
import jup.TransactionProcessor;
import jup.TransactionType;
import jup.util.Convert;
import jup.util.JSON;
import jup.util.Listeners;
import jup.util.Logger;
import jup.util.QueuedThreadPool;
import jup.util.ThreadPool;

public final class MetisServers {
	
	private static final int DEFAULT_METIS_PORT = 8083;
	
	private static final TransactionProcessor transactionProcessor = Jup.getTransactionProcessor();
	private static final BlockchainProcessor blockchainProcessor = Jup.getBlockchainProcessor();
	
    public enum Event {
    	ADDED_ACTIVE_METISSERVER, CHANGED_ACTIVE_METISSERVER, DEACTIVATE, REMOVE, NEW_METIS_SERVER
    }
    
    private static final Listeners<MetisServer,Event> listeners = new Listeners<>();
    private static final ConcurrentMap<String, MetisServer> metisServers = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> selfAnnouncedAddresses = new ConcurrentHashMap<>();
    
    static final ExecutorService metisService = new QueuedThreadPool(2, 15);
    private static final ExecutorService sendingService = Executors.newFixedThreadPool(10);

    private MetisServers() {} 
    
    public static void init() {
    	transactionProcessor.addListener(transactions -> {
    		if (!metisServers.isEmpty()) {
     			send(transactions);
     		}
         }, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
     	
     	blockchainProcessor.addListener(block -> {
     		if (!metisServers.isEmpty()) {
     			send(block);
     		}
          }, BlockchainProcessor.Event.BLOCK_PUSHED);
    }
    
    static void notifyListeners(MetisServer peer, Event eventType) {
    	MetisServers.listeners.notify(peer, eventType);
    }
    
    static MetisServer createMetisServer(String protocol, String port, String host) {
    	String announcedAddress;
    	if (port != null && !port.isEmpty()) {
    		announcedAddress = protocol + "://" + host + ":" + port;
    	} else {
    		announcedAddress = protocol + "://" + host;
    	}
    	return findOrCreateMetisServer(announcedAddress, true);
    }
    
    static MetisServer findOrCreateMetisServer(String announcedAddress, boolean create) {
    	if (announcedAddress == null) {
            return null;
        }
        announcedAddress = announcedAddress.trim().toLowerCase();
        MetisServer metisServer;
        if ((metisServer = metisServers.get(announcedAddress)) != null) {
            return metisServer;
        }
        String host = selfAnnouncedAddresses.get(announcedAddress);
        if (host != null && (metisServer = metisServers.get(host)) != null) {
            return metisServer;
        }
        
        try {
        	URI uri;
        	if(announcedAddress.indexOf("ws://") != -1 || announcedAddress.indexOf("wss://") != -1) {
        		uri = new URI(announcedAddress);
        	} else {
        		uri = new URI("ws://" + announcedAddress);
        	}
            
            host = uri.getHost();
            if (host == null) {
                return null;
            }
            if ((metisServer = metisServers.get(host)) != null) {
                return metisServer;
            }
            String host2 = selfAnnouncedAddresses.get(host);
            if (host2 != null && (metisServer = metisServers.get(host2)) != null) {
                return metisServer;
            }
            InetAddress inetAddress = InetAddress.getByName(host);
            return findOrCreateMetisServer(inetAddress, announcedAddress, create);
        } catch (URISyntaxException | UnknownHostException e) {
            Logger.logDebugMessage("Invalid metis address: " + announcedAddress + ", " + e.toString());
            return null;
        }
    }
    
    static MetisServer findOrCreateMetisServer(final InetAddress inetAddress, final String announcedAddress, final boolean create) {

        String host = inetAddress.getHostAddress();
        //re-add the [] to ipv6 addresses lost in getHostAddress() above
        if (host.split(":").length > 2) {
            host = "[" + host + "]";
        }

        MetisServer metisServer;
        if ((metisServer = metisServers.get(host)) != null) {
            return metisServer;
        }
        
        if (!create) {
            return null;
        }

        metisServer = new MetisServer(host, announcedAddress);
        Logger.logDebugMessage("Added Metis Server, host " + host + ", announcedAddress " + announcedAddress);
        return metisServer;
    }
    
    public static boolean add(MetisServer metisServer) {
        if (metisServers.put(metisServer.getHost(), (MetisServer) metisServer) == null) {
            listeners.notify(metisServer, Event.NEW_METIS_SERVER);
            return true;
        }
        return false;
    }
    
    public static int getDefaultMetisPort() {
        return DEFAULT_METIS_PORT;
    }
    
    public static MetisServer removeMetisServer(MetisServer metisServer) {
    	 if (metisServer.getAnnouncedAddress() != null) {
             selfAnnouncedAddresses.remove(metisServer.getAnnouncedAddress());
         }
    	 
    	 if(metisServers.get(metisServer.getHost()) != null) {
    		 Logger.logDebugMessage("Removed Metis Server, host " + metisServer.getHost() + ", announcedAddress " + metisServer.getAnnouncedAddress());
    		 MetisServers.notifyListeners(metisServers.get(metisServer.getHost()), MetisServers.Event.REMOVE);
             return metisServers.remove(metisServer.getHost());
    	 } else {
    		 return null;
    	 }
    }
    
    public static List<MetisServer> getMetisServers() {
    	List<MetisServer> result = new ArrayList<>();
        for (MetisServer metisServer : metisServers.values()) {
            result.add(metisServer);
        }
        return result;
    }
    
    public static void shutdown() {
        ThreadPool.shutdownExecutor("sendingService", sendingService, 2);
        ThreadPool.shutdownExecutor("metisService", metisService, 5);
    }
    
    private static final int sendTransactionsBatchSize = 10;
    private static void send(List<? extends Transaction> transactions) {
        int nextBatchStart = 0;
        while (nextBatchStart < transactions.size()) {
            JSONObject request = new JSONObject();
            JSONArray transactionsData = new JSONArray();
            
            for (int i = nextBatchStart; i < nextBatchStart + sendTransactionsBatchSize && i < transactions.size(); i++) {
            	Transaction tx = transactions.get(i);
            	transactionsData.add(getSmallTransactionJSON(tx));
            }
            
            request.put("requestType", "unconfirmedTransactions");
            request.put("transactions", transactionsData);
            send(request);
            nextBatchStart += sendTransactionsBatchSize;
        }
    }
    
    private static void send(Block block) {
    	if(metisServers.isEmpty()) {
    		return;
    	}
    	
        JSONObject request = new JSONObject();
        request.put("requestType", "acceptedBlock");
        request.put("timestamp", block.getTimestamp());
        request.put("previousBlock", Long.toUnsignedString(block.getPreviousBlockId()));
        request.put("heigh", block.getHeight());
        JSONArray transactionsData = new JSONArray();
        block.getTransactions().forEach(transaction -> {
        	if (Byte.compare(transaction.getType().getType(), TransactionType.TYPE_MESSAGING) == 0 && 
        			(Byte.compare(transaction.getType().getSubtype(), TransactionType.SUBTYPE_MESSAGING_ARBITRARY_MESSAGE) == 0 || 
        			 Byte.compare(transaction.getType().getSubtype(), TransactionType.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT) == 0 ||
        			 Byte.compare(transaction.getType().getSubtype(), TransactionType.SUBTYPE_MESSAGING_METIS_ACCOUNT_INFO) == 0 ||
        			 Byte.compare(transaction.getType().getSubtype(), TransactionType.SUBTYPE_MESSAGING_METIS_CHANNEL_INVITATION) == 0 ||
        			 Byte.compare(transaction.getType().getSubtype(), TransactionType.SUBTYPE_MESSAGING_METIS_CHANNEL_MEMBER) == 0)) {
        		transactionsData.add(getSmallTransactionJSON(transaction));
        	}
        });
        request.put("transactions", transactionsData);
        
        send(request);
    }
    
    private static JSONObject getSmallTransactionJSON(Transaction tx) {
    	JSONObject txJSON = new JSONObject();
    	txJSON.put("transactionId", Long.toUnsignedString(tx.getId()));
    	txJSON.put("type", tx.getType().getType());
    	txJSON.put("subtype", tx.getType().getSubtype());
    	txJSON.put("ecBlockHeight", tx.getECBlockHeight());
    	txJSON.put("senderId", Long.toUnsignedString(tx.getSenderId()));
    	txJSON.put("senderRS", Convert.rsAccount(tx.getSenderId()));
    	txJSON.put("recipientId", Long.toUnsignedString(tx.getRecipientId()));
    	txJSON.put("recipientRS", Convert.rsAccount(tx.getRecipientId()));
    	JSONObject attachmentJSON = new JSONObject();
        for (Appendix appendage : tx.getAppendages()) {
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (!attachmentJSON.isEmpty()) {
        	txJSON.put("attachment", attachmentJSON);
        }
    	return txJSON;
    }
    
    private static void send(final JSONObject request) {
        sendingService.submit(() -> {
            for (final MetisServer metis : metisServers.values()) {
               metis.send(JSON.prepareRequest(request));
            }
        });
    }
}
