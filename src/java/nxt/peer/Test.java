package nxt.peer;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class Test extends WebSocketAdapter {

	public static void main(String[] args) {
		String url = "wss://metis-dev-5qk3s.ondigitalocean.app/jupiter"; // or
		// "wss://echo.websocket.org"
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
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			sslContextFactory.setSslContext(sc);
			WebSocketClient client = new WebSocketClient(sslContextFactory);

			client.start();
			Future<Session> fut = client.connect(new Test(), URI.create(url));
			Session session = fut.get();
			session.getRemote().sendString("Hello");
			session.getRemote().sendString("155-questions-active");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		System.out.println("Socket Connected: " + sess);
	}

	@Override
	public void onWebSocketText(String message) {
		super.onWebSocketText(message);
		System.out.println("Received TEXT message: " + message);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		System.out.println("Socket Closed: [" + statusCode + "] " + reason);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		cause.printStackTrace(System.err);
	}
}