import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.java_websocket.WebSocket;

public class ConnectionEventHandler implements Runnable {

	public static final class ErrorCodes {

		public static final int NOTNAMED = 0;
		public static final int USERALLREADYEXIST = 1;
	}

	public static final class Commands {

		public static final String FAIL = "fail";
		public static final String JOIN = "join";
		public static final String LEFT = "left";
		public static final String NAME = "name";
		public static final String USER = "user";
		public static final String INKA = "inka";
		public static final String UMSG = "umsg";
	}

	// private SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss:SSS" );
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );
	private Pattern commandpattern = Pattern.compile( "([a-zA-Z]{4}) (.+)", Pattern.DOTALL );
	private Server server;
	private WebSocket conn;
	private ConnectionContext context;
	private List<String> users;
	private String message;
	private String event;

	public ConnectionEventHandler( Server server , WebSocket conn , ConnectionContext context , List<String> users , String event , String message ) {
		super();
		this.server = server;
		this.conn = conn;
		this.context = context;
		this.users = users;
		this.event = event;
		this.message = message;
	}

	public void run() {
		if( event.equalsIgnoreCase( "message" ) )
			onMessage();
		else if( event.equalsIgnoreCase( "open" ) )
			onOpen();
		else if( event.equalsIgnoreCase( "close" ) )
			onClose();
	}

	private void onOpen() {
		// void Method for later events
	}

	private void onMessage() {
		if( message == null )
			return;

		Matcher m = commandpattern.matcher( message );

		boolean found = m.find();

		if( !context.isNameSet() && !found ) {
			sendFail( conn, ErrorCodes.NOTNAMED );
			return;
		}

		if( found ) {
			String cmd = m.group( 1 );

			if( ConnectionEventHandler.Commands.NAME.equalsIgnoreCase( cmd ) ) {

				if( !context.isNameSet() ) {
					String name = m.group( 2 );

					if( users.contains( name ) ) {
						server.send( conn, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST );
						conn.close( 0, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST );
						return;
					}
					context.setName( name );

					server.dispatch( ConnectionEventHandler.Commands.JOIN + " " + name );

					System.out.println( name + " joined" );
					users.add( name );
				} else {
					String name = m.group( 2 );

					if( users.contains( name ) ) {
						server.send( conn, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST );
						return;
					}

					users.remove( context.getName() );
					users.add( name );

					server.dispatch( ConnectionEventHandler.Commands.INKA + " " + context.getName() + ":" + name );

					context.setName( name );
				}

			} else if( ConnectionEventHandler.Commands.USER.equalsIgnoreCase( cmd ) )
				server.send( conn, "user " + getUsers() );
			else if( ConnectionEventHandler.Commands.UMSG.equalsIgnoreCase( cmd ) )
				server.dispatch( ConnectionEventHandler.Commands.UMSG + " " + dateFormat.format( new Date() ) + " " + context.getName() + ": " + m.group( 2 ) );
			else
				System.err.println( "Error with incoming message: Unknown command Type " + cmd + "!" );
		} else
			System.err.println( "Error with incoming message: No command used!" );
	}

	private void onClose() {
		if( context.isNameSet() ) {
			String name = context.getName();
			server.dispatch( ConnectionEventHandler.Commands.LEFT + " " + name );
			users.remove( name );
			server.log( name + " left." );
		}
	}

	public void sendFail( WebSocket conn, int code ) {
		server.send( conn, ConnectionEventHandler.Commands.FAIL + " " + code );
	}

	private String getUsers() {
		// return users.toString();

		if( users.isEmpty() )
			return "0";

		Iterator<String> it = users.iterator();
		String userList = it.next();

		while ( it.hasNext() ) {
			userList += "§§" + it.next();
		}

		return userList;
	}
}
