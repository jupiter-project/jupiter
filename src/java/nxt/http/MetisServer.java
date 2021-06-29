package nxt.http;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import nxt.peer.PeerWebSocket;
import nxt.peer.Peer.State;
import nxt.util.JSON;
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
    
    public JSONObject send(final JSONStreamAware request) {
        JSONObject response = null;

        try {
            //
            // Create a new WebSocket session if we don't have one
            //
            if (useWebSocket && !webSocket.isOpen())
                useWebSocket = webSocket.startClient(URI.create("ws://" + host + ":" + getPort() + "/nxt"));
            //
            // Send the request and process the response
            //
            if (useWebSocket) {
                //
                // Send the request using the WebSocket session
                //
                StringWriter wsWriter = new StringWriter(1000);
                request.writeJSONString(wsWriter);
                String wsRequest = wsWriter.toString();
                String wsResponse = webSocket.doPost(wsRequest);
                response = (JSONObject)JSONValue.parseWithException(wsResponse);
            } 
            //
            // Check for an error response
            //
            if (response != null && response.get("error") != null) {
                deactivate();
                Logger.logDebugMessage("Failed to notify to metis server, returned error: " +
                        response.toJSONString() + ", request was: " + JSON.toString(request) +
                        ", disconnecting");
            }
        } catch (RuntimeException|ParseException|IOException e) {
            deactivate();
        }

        return response;
    }
}
