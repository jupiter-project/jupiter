/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2020 Sigwo Technologies
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

package nxt.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import nxt.util.Logger;

/**
 * PeerWebSocket represents an HTTP/HTTPS upgraded connection
 */
@WebSocket
public class MetisWebSocket {

    /** Our WebSocket message version */
    private static final int VERSION = 1;

    /** Create the WebSocket client */
    private static WebSocketClient peerClient;
    static {
        try {
        	SslContextFactory sslContextFactory = new SslContextFactory();
    		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    				return new X509Certificate[0];
    			}

    			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    			}

    			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    			}
    		} };
        	
        	SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			sslContextFactory.setSslContext(sc);
        	
            peerClient = new WebSocketClient(sslContextFactory);
            peerClient.getPolicy().setIdleTimeout(Peers.webSocketIdleTimeout);
            peerClient.getPolicy().setMaxBinaryMessageSize(Peers.MAX_MESSAGE_SIZE);
            peerClient.setConnectTimeout(Peers.connectTimeout);
            peerClient.start();
        } catch (Exception exc) {
            Logger.logErrorMessage("Unable to start WebSocket client", exc);
            peerClient = null;
        }
    }

    /** Negotiated WebSocket message version */
    private int version = VERSION;

    /** WebSocket session */
    private volatile Session session;

    /** WebSocket endpoint - set for an accepted connection */
    private final PeerServlet peerServlet;

    /** WebSocket lock */
    private final ReentrantLock lock = new ReentrantLock();

    /** Next POST request identifier */
    private long nextRequestId = 0;

    /** WebSocket connection timestamp */
    private long connectTime = 0;

    /**
     * Create a client socket
     */
    public MetisWebSocket() {
        peerServlet = null;
    }

    /**
     * Create a server socket
     *
     * @param   peerServlet         Servlet for request processing
     */
    public MetisWebSocket(PeerServlet peerServlet) {
        this.peerServlet = peerServlet;
    }

    /**
     * Start a client session
     *
     * @param   uri                 Server URI
     * @return                      TRUE if the WebSocket connection was completed
     * @throws  IOException         I/O error occurred
     */
    public boolean startClient(URI uri) throws IOException {
        if (peerClient == null) {
            return false;
        }
        String address = String.format("%s:%d", uri.getHost(), uri.getPort());
        boolean useWebSocket = false;
        //
        // Create a WebSocket connection.  We need to serialize the connection requests
        // since the NRS server will issue multiple concurrent requests to the same peer.
        // After a successful connection, the subsequent connection requests will return
        // immediately.  After an unsuccessful connection, a new connect attempt will not
        // be done until 60 seconds have passed.
        //
        lock.lock();
        try {
            if (session != null) {
                useWebSocket = true;
            } else if (System.currentTimeMillis() > connectTime + 10 * 1000) {
                connectTime = System.currentTimeMillis();
                ClientUpgradeRequest req = new ClientUpgradeRequest();
                Future<Session> conn = peerClient.connect(this, uri, req);
                conn.get(Peers.connectTimeout + 100, TimeUnit.MILLISECONDS);
                useWebSocket = true;
            }
        } catch (ExecutionException exc) {
        	Logger.logDebugMessage(String.format("WebSocket connection to %s failed", address), exc);
            if (exc.getCause() instanceof UpgradeException) {
                // We will use HTTP
            } else if (exc.getCause() instanceof IOException) {
                // Report I/O exception
                throw (IOException)exc.getCause();
            } 
        } catch (TimeoutException exc) {
            throw new SocketTimeoutException(String.format("WebSocket connection to %s timed out", address));
        } catch (IllegalStateException exc) {
            if (! peerClient.isStarted()) {
                Logger.logDebugMessage("WebSocket client not started or shutting down");
                throw exc;
            }
            Logger.logDebugMessage(String.format("WebSocket connection to %s failed", address), exc);
        } catch (Exception exc) {
            Logger.logDebugMessage(String.format("WebSocket connection to %s failed", address), exc);
        } finally {
            if (!useWebSocket) {
                close();
            }
            lock.unlock();
        }
        return useWebSocket;
    }

    /**
     * WebSocket connection complete
     *
     * @param   session             WebSocket session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        Logger.logDebugMessage(String.format("Metis WebSocket connection with %s completed",
                session.getRemoteAddress().getHostString()));
    }

    /**
     * Check if we have a WebSocket connection
     *
     * @return                      TRUE if we have a WebSocket connection
     */
    public boolean isOpen() {
        Session s;
        return ((s=session) != null && s.isOpen());
    }

    /**
     * Return the remote address for this connection
     *
     * @return                      Remote address or null if the connection is closed
     */
    public InetSocketAddress getRemoteAddress() {
        Session s;
        return ((s=session) != null && s.isOpen() ? s.getRemoteAddress() : null);
    }

    /**
     * Process a POST request by sending the request message and then
     * waiting for a response.  This method is used by the connection
     * originator.
     *
     * @param   request             Request message
     * @return                      Response message
     * @throws  IOException         I/O error occurred
     */
    public void doMetisPost(String request) throws IOException {
        long requestId;
        //
        // Send the POST request
        //
        lock.lock();
        try {
            if (session == null || !session.isOpen()) {
                throw new IOException("WebSocket session is not open");
            }
            requestId = nextRequestId++;
            byte[] requestBytes = request.getBytes("UTF-8");
            int requestLength = requestBytes.length;
            int flags = 0;
            ByteBuffer buf = ByteBuffer.allocate(requestBytes.length + 20);
            buf.putInt(version)
               .putLong(requestId)
               .putInt(flags)
               .putInt(requestLength)
               .put(requestBytes)
               .flip();
            if (buf.limit() > Peers.MAX_MESSAGE_SIZE) {
                throw new ProtocolException("POST request length exceeds max message size");
            }
            session.getRemote().sendBytes(buf);
        } catch (WebSocketException exc) {
        	Logger.logDebugMessage("Metis websocket exceptions " + exc.getMessage());
            throw new SocketException(exc.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * WebSocket session has been closed
     *
     * @param   statusCode          Status code
     * @param   reason              Reason message
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        lock.lock();
        try {
            if (session != null) {
                if ((Peers.communicationLoggingMask & Peers.LOGGING_MASK_200_RESPONSES) != 0) {
                    Logger.logDebugMessage(String.format("%s WebSocket connection with %s closed",
                            peerServlet != null ? "Inbound" : "Outbound",
                            session.getRemoteAddress().getHostString()));
                }
                session = null;
            }
            SocketException exc = new SocketException("Metis WebSocket connection closed");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Close the WebSocket
     */
    public void close() {
        lock.lock();
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception exc) {
            Logger.logDebugMessage("Exception while closing WebSocket", exc);
        } finally {
            lock.unlock();
        }
    }
}
