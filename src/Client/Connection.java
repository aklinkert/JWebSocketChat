package client;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class Connection extends WebSocketClient {

	private Client client;

	public Connection(Client client, URI serverURI) {
		super(serverURI);

		if (client == null) {
			throw new IllegalArgumentException("The given Argument \"client\" is null!");
		}
		this.client = client;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		this.client.onConnected();
	}

	@Override
	public void onMessage(String message) {
		this.client.handleMessage(message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		this.client.onDisconnected();
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
	}

}
