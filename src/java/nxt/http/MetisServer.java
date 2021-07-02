package nxt.http;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.json.simple.JSONStreamAware;

import nxt.peer.MetisWebSocket;
import nxt.peer.Peer.State;
import nxt.peer.PeerWebSocket;
import nxt.util.Logger;

public class MetisServer {

	private final MetisWebSocket webSocket;
	private volatile boolean useWebSocket;
	private volatile String announcedAddress;
	private volatile State state;
	private String protocol;
	private final String host;
	
	MetisServer(String host, String announcedAddress){
		
		this.host = host;
		this.announcedAddress = announcedAddress;
		
		if(announcedAddress.indexOf("wss://") != -1) {
    		this.protocol = "wss";
    	}else {
    		this.protocol = "ws";
    	}
		
		this.webSocket = new MetisWebSocket();
		this.useWebSocket = true;
		this.state = State.NON_CONNECTED;
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
        	
        	//String metisServerUrl = protocol + "://" + host+ "/jupiter";
        	String metisServerUrl = announcedAddress + "/jupiter";
        	
            // Create a new WebSocket session if we don't have one
            if (!webSocket.isOpen())
            	useWebSocket = webSocket.startClient(URI.create(metisServerUrl));

            if (useWebSocket) {
            	setState(State.CONNECTED);
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