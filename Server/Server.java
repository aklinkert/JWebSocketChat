package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;

public class Server extends WebSocketServer {

	private HashMap<WebSocket,ConnectionContext> connectionContexts = new HashMap<WebSocket,ConnectionContext>();
	private List<String> users = Collections.synchronizedList( new ArrayList<String>() );
	private ExecutorService exec = Executors.newCachedThreadPool();

	private boolean started = false;

	public Server( InetSocketAddress address ) {
		super( address );

		System.out.println( "Server ready for Connections." );
	}

	public void onOpen( WebSocket conn, ClientHandshake handshake ) {

		connectionContexts.put( conn, new ConnectionContext() );

		exec.submit( new ConnectionEventHandler( this, conn, null, users, "open", "" ) );
	}

	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {

		exec.submit( new ConnectionEventHandler( this, conn, connectionContexts.get( conn ), users, "close", "" ) );

		connectionContexts.remove( conn );
	}

	public void onMessage( WebSocket conn, String message ) {
		exec.submit( new ConnectionEventHandler( this, conn, connectionContexts.get( conn ), users, "message", message ) );
	}

	public void start() {
		if( started )
			return;
		started = true;
		new Thread( this ).start();
	}

	public void dispatch( String message ) {
		try {
			this.sendToAll( message );
		} catch ( InterruptedException e ) {
			logException( e );
		}
	}

	public void sendToAll( String text ) throws InterruptedException {
		for( WebSocket c : connections() ) {
			c.send( text );
		}
	}

	public void onError( WebSocket conn, Exception e ) {
		logException( e );
	}

	private void logException( Exception e ) {
		e.printStackTrace();
	}

	public void log( String message ) {
		System.out.println( message );
	}

	public void send( WebSocket conn, String message ) {
		try {
			conn.send( message );
		} catch ( NotYetConnectedException e ) {
			e.printStackTrace();
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) {

		InetSocketAddress adress = null;

		try {
			adress = new InetSocketAddress( InetAddress.getByName( args[ 0 ] ), new Integer( args[ 1 ] ) );
		} catch ( NumberFormatException e ) {
			e.printStackTrace();
			System.exit( 1 );
		} catch ( UnknownHostException e ) {
			e.printStackTrace();
			System.exit( 1 );
		}

		Server server = new Server( adress );
		server.start();

	}
}
