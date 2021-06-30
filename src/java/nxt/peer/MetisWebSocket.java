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

package nxt.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import nxt.util.Logger;
import nxt.util.QueuedThreadPool;

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
            peerClient = new WebSocketClient();
            peerClient.getPolicy().setIdleTimeout(Peers.webSocketIdleTimeout);
            peerClient.getPolicy().setMaxBinaryMessageSize(Peers.MAX_MESSAGE_SIZE);
            peerClient.setConnectTimeout(Peers.connectTimeout);
            peerClient.start();
        } catch (Exception exc) {
            Logger.logErrorMessage("Unable to start WebSocket client", exc);
            peerClient = null;
        }
    }

    /** Thread pool for server request processing */
    private static final ExecutorService threadPool = new QueuedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 4);

    /** Negotiated WebSocket message version */
    private int version = VERSION;
    
    /** WebSocket session */
    private volatile Session session;

    /** WebSocket lock */
    private final ReentrantLock lock = new ReentrantLock();
    
    /** Next POST request identifier */
    private long nextRequestId = 0;

    /** WebSocket connection timestamp */
    private long connectTime = 0;

    /**
     * Create a client socket
     */
    public MetisWebSocket() {}

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
        	Logger.logDebugMessage("Error connecting to metis websocket, " + uri + " response " + exc.getMessage());
            if (exc.getCause() instanceof UpgradeException) {
                // We will use HTTP
            } else if (exc.getCause() instanceof IOException) {
                // Report I/O exception
                throw (IOException)exc.getCause();
            } else {
                // We will use HTTP
                Logger.logDebugMessage(String.format("WebSocket connection to %s failed", address), exc);
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
        Logger.logDebugMessage(String.format("WebSocket connection with %s completed",
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
    public String doPost(String request) throws IOException {
        // Send the POST request
        //
    	long requestId;
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
            session.getRemote().sendBytes(buf);
        } catch (WebSocketException exc) {
            throw new SocketException(exc.getMessage());
        } finally {
            lock.unlock();
        }
        
        // Get the response
        String response;
        try {
            PostRequest postRequest = new PostRequest();
            response = postRequest.get(Peers.readTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exc) {
            throw new SocketTimeoutException("WebSocket POST interrupted");
        }
        return response;
    }

    /**
     * Send POST response
     *
     * This method is used by the connection acceptor to return the POST response
     *
     * @param   requestId           Request identifier
     * @param   response            Response message
     * @throws  IOException         I/O error occurred
     */
    public void sendResponse(long requestId, String response) throws IOException {
        lock.lock();
        try {
            if (session != null && session.isOpen()) {
                byte[] responseBytes = response.getBytes("UTF-8");
                ByteBuffer buf = ByteBuffer.allocate(responseBytes.length);
                buf.put(responseBytes).flip();
                session.getRemote().sendBytes(buf);
            }
        } catch (WebSocketException exc) {
            throw new SocketException(exc.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Process a metis websocket message
     *
     * @param   inbuf               Message buffer
     * @param   off                 Starting offset
     * @param   len                 Message length
     */
    @OnWebSocketMessage
    public void onMessage(byte[] inbuf, int off, int len) {
        lock.lock();
        try {
            ByteBuffer buf = ByteBuffer.wrap(inbuf, off, len);
            byte[] msgBytes = new byte[buf.remaining()];
            buf.get(msgBytes);
            String message = new String(msgBytes, "UTF-8");
            System.out.println(message);
        } catch (Exception exc) {
            Logger.logDebugMessage("Exception while processing WebSocket message", exc);
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
            	Logger.logDebugMessage(String.format("WebSocket connection with %s closed",
                        session.getRemoteAddress().getHostString()));
                session = null;
            }
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

    /**
     * POST request
     */
    private class PostRequest {

        /** Request latch */
        private final CountDownLatch latch = new CountDownLatch(1);

        /** Response message */
        private volatile String response;

        /** Socket exception */
        private volatile IOException exception;

        /**
         * Create a post request
         */
        public PostRequest() {}

        /**
         * Wait for the response
         *
         * The caller must hold the lock for the request condition
         *
         * @param   timeout                 Wait timeout
         * @param   unit                    Time unit
         * @return                          Response message
         * @throws  InterruptedException    Wait interrupted
         * @throws  IOException             I/O error occurred
         */
        public String get(long timeout, TimeUnit unit) throws InterruptedException, IOException {
            if (!latch.await(timeout, unit)) {
                throw new SocketTimeoutException("WebSocket read timeout exceeded");
            }
            if (exception != null) {
                throw exception;
            }
            return response;
        }

        /**
         * Complete the request with a response message
         *
         * The caller must hold the lock for the request condition
         *
         * @param   response                Response message
         */
        public void complete(String response) {
            this.response = response;
            latch.countDown();
        }

        /**
         * Complete the request with an exception
         *
         * The caller must hold the lock for the request condition
         *
         * @param   exception             I/O exception
         */
        public void complete(IOException exception) {
            this.exception = exception;
            latch.countDown();
        }
    }
}
