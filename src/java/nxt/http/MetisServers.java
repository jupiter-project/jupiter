/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Block;
import nxt.Transaction;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Listeners;
import nxt.util.Logger;
import nxt.util.QueuedThreadPool;
import nxt.util.ThreadPool;
import nxt.util.UPnP;

public final class MetisServers {
	
	private static final int DEFAULT_METIS_PORT = 8083;

    public enum Event {
    	ADDED_ACTIVE_METISSERVER, CHANGED_ACTIVE_METISSERVER, DEACTIVATE, REMOVE, NEW_METIS_SERVER
    }
    
    private static final Listeners<MetisServer,Event> listeners = new Listeners<>();
    private static final ConcurrentMap<String, MetisServer> metisServers = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> selfAnnouncedAddresses = new ConcurrentHashMap<>();
    
    static final ExecutorService metisService = new QueuedThreadPool(2, 15);
    private static final ExecutorService sendingService = Executors.newFixedThreadPool(10);

    private MetisServers() {} // never
    
    static void notifyListeners(MetisServer peer, Event eventType) {
    	MetisServers.listeners.notify(peer, eventType);
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
            URI uri = new URI("http://" + announcedAddress);
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
            return findOrCreateMetisServer(inetAddress, addressWithPort(announcedAddress), create);
        } catch (URISyntaxException | UnknownHostException e) {
            Logger.logDebugMessage("Invalid metis address: " + announcedAddress + ", " + e.toString());
            return null;
        }
    }
    
    static MetisServer findOrCreateMetisServer(final InetAddress inetAddress, final String announcedAddress, final boolean create) {

        if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
            return null;
        }

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
        return metisServers.remove(metisServer.getHost());
    }
    
    static String addressWithPort(String address) {
        if (address == null) {
            return null;
        }
        try {
            URI uri = new URI("http://" + address);
            String host = uri.getHost();
            int port = uri.getPort();
            return port > 0 && port != MetisServers.getDefaultMetisPort() ? host + ":" + port : host;
        } catch (URISyntaxException e) {
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
    public static void send(List<? extends Transaction> transactions) {
        int nextBatchStart = 0;
        while (nextBatchStart < transactions.size()) {
            JSONObject request = new JSONObject();
            JSONArray transactionsData = new JSONArray();
            
            for (int i = nextBatchStart; i < nextBatchStart + sendTransactionsBatchSize && i < transactions.size(); i++) {
            	Transaction tx = transactions.get(i);
            	transactionsData.add(getSmallTransactionJSON(tx));
            }
            
            request.put("requestType", "processTransactions");
            request.put("transactions", transactionsData);
            send(request);
            nextBatchStart += sendTransactionsBatchSize;
        }
    }
    
    public static void send(Block block) {
        JSONObject request = block.getJSONObject();
        request.put("requestType", "processBlock");
        request.put("timestamp", block.getTimestamp());
        request.put("previousBlock", Long.toUnsignedString(block.getPreviousBlockId()));
        request.put("heigh", block.getHeight());
        JSONArray transactionsData = new JSONArray();
        block.getTransactions().forEach(transaction -> transactionsData.add(getSmallTransactionJSON(transaction)));
        request.put("transactions", transactionsData);
        send(request);
    }
    
    private static JSONObject getSmallTransactionJSON(Transaction tx) {
    	JSONObject txJSON = new JSONObject();
    	//txJSON.put("event", eventType.name());
    	txJSON.put("transactionId", tx.getId());
    	txJSON.put("type", tx.getType().getType());
    	txJSON.put("subtype", tx.getType().getSubtype());
    	txJSON.put("ecBlockHeight", tx.getECBlockHeight());
    	return txJSON;
    }
    
    private static void send(final JSONObject request) {
        sendingService.submit(() -> {
            final JSONStreamAware jsonRequest = JSON.prepareRequest(request);

            List<Future<JSONObject>> expectedResponses = new ArrayList<>();
            for (final MetisServer metis : metisServers.values()) {

                if (metis.getState() == Peer.State.CONNECTED && metis.getAnnouncedAddress() != null) {
                    Future<JSONObject> futureResponse = metisService.submit(() -> metis.send(jsonRequest));
                    expectedResponses.add(futureResponse);
                }
                for (Future<JSONObject> future : expectedResponses) {
                    try {
                        JSONObject response = future.get();
                        if (response.get("error") != null) {
                            Logger.logErrorMessage("Error sending to Metis " + (String)response.get("error"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        Logger.logDebugMessage("Error in sendToSomePeers", e);
                    }

                }
            }
        });
    }
}
