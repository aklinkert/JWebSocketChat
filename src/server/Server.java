package server;

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

import server.ConnectionEventHandler.ConnectionEvent;

public class Server extends WebSocketServer {

	private final List<String> users = Collections.synchronizedList(new ArrayList<String>());
	private final ExecutorService exec = Executors.newCachedThreadPool();

	private boolean started = false;

	public Server(final InetSocketAddress address) {
		super(address);

		System.out.println("Server ready for Connections.");
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		System.out.println("Connection opened.");

		this.exec.submit(new ConnectionEventHandler(this, conn, this.users, ConnectionEvent.OPEN, ""));
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		System.out.println("Connection closed.");

		this.exec.submit(new ConnectionEventHandler(this, conn, this.users, ConnectionEvent.CLOSE, ""));
	}

	@Override
	public void onMessage(final WebSocket conn, final String message) {
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

	public void dispatch(final String message) {
		System.out.println("Dispatching: " + message);
		try {
			sendToAll(message);
		} catch (InterruptedException e) {
			logException(e);
		}
	}

	public void sendToAll(final String text) throws InterruptedException {

		Collection<WebSocket> connections = connections();

		synchronized (connections) {
			for (WebSocket c : connections) {
				System.out.println("[Dispatch] Sending message " + text + " to host " + c.getRemoteSocketAddress());
				if (!(c.isClosed() || c.isClosing())) {
					c.send(text);
				}
			}
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception e) {
		System.err.println("");
		logException(e);
	}

	private void logException(final Exception e) {
		e.printStackTrace();
	}

	public void log(final String message) {
		System.out.println(message);
	}

	public void send(final WebSocket conn, final String message) {
		System.out.println("Sending: " + message + " to " + conn.getRemoteSocketAddress().toString());

		try {
			conn.send(message);
		} catch (NotYetConnectedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {

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
