package Server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import Server.ConnectionEventHandler.ConnectionEvent;

public class Server extends WebSocketServer {

	private List<String> users = Collections.synchronizedList(new ArrayList<String>());
	private ExecutorService exec = Executors.newCachedThreadPool();

	private boolean started = false;

	public Server(InetSocketAddress address) {
		super(address);

		System.out.println("Server ready for Connections.");
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("Connection opened.");

		this.exec.submit(new ConnectionEventHandler(this, conn, this.users, ConnectionEvent.OPEN, ""));
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("Connection closed.");

		this.exec.submit(new ConnectionEventHandler(this, conn, this.users, ConnectionEvent.CLOSE, ""));
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Message recieved: " + message);
		this.exec.submit(new ConnectionEventHandler(this, conn, this.users, ConnectionEvent.MESSAGE, message));
	}

	@Override
	public void start() {
		if (this.started) {
			return;
		}
		this.started = true;
		new Thread(this).start();
	}

	public void dispatch(String message) {
		System.out.println("Dispatching: " + message);
		try {
			sendToAll(message);
		} catch (InterruptedException e) {
			logException(e);
		}
	}

	public void sendToAll(String text) throws InterruptedException {

		Collection<WebSocket> connections = connections();

		for (WebSocket c : connections) {
			System.out.println("[Dispatch] Sending message " + text + " to host " + c.getRemoteSocketAddress());
			if (!(c.isClosed() || c.isClosing())) {
				c.send(text);
			}
		}

	}

	@Override
	public void onError(WebSocket conn, Exception e) {
		System.err.println("");
		logException(e);
	}

	private void logException(Exception e) {
		e.printStackTrace();
	}

	public void log(String message) {
		System.out.println(message);
	}

	public void send(WebSocket conn, String message) {
		System.out.println("Sending: " + message + " to " + conn.getRemoteSocketAddress().toString());

		try {
			conn.send(message);
		} catch (NotYetConnectedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		InetSocketAddress adress = null;

		try {
			adress = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), new Integer(args[0]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		Server server = new Server(adress);
		server.start();

	}
}
