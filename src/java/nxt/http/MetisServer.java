package nxt.http;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.simple.JSONStreamAware;

import nxt.peer.Peer.State;
import nxt.peer.PeerWebSocket;
import nxt.util.Logger;

public class MetisServer {

	private final PeerWebSocket webSocket;
	private volatile boolean useWebSocket;
	private volatile String announcedAddress;
	private volatile State state;
	private volatile int port;
	private final String host;
	
	MetisServer(String host, String announcedAddress){
		this.host = host;
		this.announcedAddress = announcedAddress;
		try {
	        this.port = new URI("http://" + announcedAddress).getPort();
		} catch (URISyntaxException ignore) {}
		this.webSocket = new PeerWebSocket();
		this.useWebSocket = true;
		this.state = State.NON_CONNECTED;
		try {
		   this.port = new URI(host).getPort();
		} catch (URISyntaxException ignore) {}
	}
	
	public String getHost() {
        return host;
    }
	
	public State getState() {
        return state;
    }

    void setState(State state) {
        if (state != State.CONNECTED)
            webSocket.close();
        if (this.state == state) {
            return;
        }
        if (this.state == State.NON_CONNECTED) {
            this.state = state;
            MetisServers.notifyListeners(this, MetisServers.Event.ADDED_ACTIVE_METISSERVER);
        } else if (state != State.NON_CONNECTED) {
            this.state = state;
            MetisServers.notifyListeners(this, MetisServers.Event.CHANGED_ACTIVE_METISSERVER);
        } else {
            this.state = state;
        }
    }
    
    public int getPort() {
        return port <= 0 ? MetisServers.getDefaultMetisPort() : port;
    }
    
    public void deactivate() {
        if (state == State.CONNECTED) {
            setState(State.DISCONNECTED);
        } else {
            setState(State.NON_CONNECTED);
        }
        MetisServers.notifyListeners(this, MetisServers.Event.DEACTIVATE);
    }
    
    public void remove() {
        webSocket.close();
        MetisServers.removeMetisServer(this);
        MetisServers.notifyListeners(this, MetisServers.Event.REMOVE);
    }
    
    public String getAnnouncedAddress() {
        return announcedAddress;
    }
    
    public void send(final JSONStreamAware request) {
        try {
        	//String metisServerUrl = "ws://" + host+ "/jupiter";
        	String metisServerUrl = "ws://localhost:8083";
        	//String metisServerUrl = "ws://3eb945e75133.ngrok.io/jupiter";
        	
            // Create a new WebSocket session if we don't have one
            if (!webSocket.isOpen())
            	useWebSocket = webSocket.startClient(URI.create(metisServerUrl));

            if (useWebSocket) {
            	// Send the request using the WebSocket session
                StringWriter wsWriter = new StringWriter(1000);
                request.writeJSONString(wsWriter);
                webSocket.doMetisPost(wsWriter.toString());
                Logger.logDebugMessage("Metis Server " + metisServerUrl + " have been notified");
            }
        } catch (RuntimeException | IOException e) {
        	Logger.logDebugMessage("Deactivating metis server due to send exception, " + e.getMessage());
            deactivate();
        }
    }
}