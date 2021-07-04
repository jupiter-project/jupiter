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

package nxt.http;

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

import nxt.Block;
import nxt.BlockchainProcessor;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionProcessor;
import nxt.util.JSON;
import nxt.util.Listeners;
import nxt.util.Logger;
import nxt.util.QueuedThreadPool;
import nxt.util.ThreadPool;

public final class MetisServers {
	
	private static final int DEFAULT_METIS_PORT = 8083;
	
	private static final TransactionProcessor transactionProcessor = Nxt.getTransactionProcessor();
	private static final BlockchainProcessor blockchainProcessor = Nxt.getBlockchainProcessor();
	
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
        	String type = "" + transaction.getType().getType();
        	String subtype = "" + transaction.getType().getSubtype();
        	if (type.equals("1") && (subtype.equals("0") || subtype.equals("1") || subtype.equals("10"))) {
        		transactionsData.add(getSmallTransactionJSON(transaction));
        	}
        });
        request.put("transactions", transactionsData);
        
        send(request);
    }
    
    private static JSONObject getSmallTransactionJSON(Transaction tx) {
    	JSONObject txJSON = new JSONObject();
    	txJSON.put("transactionId", tx.getId());
    	txJSON.put("type", tx.getType().getType());
    	txJSON.put("subtype", tx.getType().getSubtype());
    	txJSON.put("ecBlockHeight", tx.getECBlockHeight());
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
