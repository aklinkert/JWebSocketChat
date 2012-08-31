package Client;

import exceptions.ConnectionLostException;
import exceptions.OperationNotAllowedException;
import gui.Window;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import listener.ClearListener;
import listener.CloseListener;
import listener.ConnectListener;
import listener.InputKeyListener;
import listener.NameChangeListener;
import listener.SendListener;
import Server.ConnectionEventHandler;

public class Client {

	public static void main( String[] args ) {
		new Client();
	}

	private Window window;
	private Connection conn;
	private Boolean connected = false;

	private SimpleDateFormat dateformat = new SimpleDateFormat( "kk:mm:ss" );

	private String username = "";
	private ArrayList<String> userlist = new ArrayList<String>();
	private Boolean sendNameChangeRequest = false;

	private Pattern commandpattern = Pattern.compile( "([a-zA-Z]{4}) (.+)", Pattern.DOTALL );
	private String noUserString = new String( "No users in chat." );

	public Client() {
		window = new Window();
		window.draw();

		window.addSendListener( new SendListener( this ) );
		window.addClearListener( new ClearListener( this ) );
		window.addCloseListener( new CloseListener( this ) );
		window.addConnectListener( new ConnectListener( this ) );
		window.addInputListener( new InputKeyListener( this ) );
		window.addNameChangeListener( new NameChangeListener( this ) );
	}

	public void addOutput( String text ) {
		window.addOutputText( getTimeStamp() + " " + text );
	}

	private void addUserListEntry( String name ) {
		userlist.add( name );
		window.setUserlist( userlist );
	}

	private void checkConnection() {
		if( conn == null )
			throw new ConnectionLostException( "Lost Connection." );
	}

	public void clear() {
		window.clearOutput();
	}

	public void close() {
		disconnect();
		System.exit( 0 );
	}

	public void connect() {

		URI uri = null;
		String input = window.getConnectionInput();

		if( input == null || input.trim().equals( "" ) ) {
			logError( "Please insert a connection URI!" );
			return;
		}

		try {
			uri = new URI( input );

			conn = new Connection( this, uri );
			conn.connect();

		} catch ( URISyntaxException e ) {
			logError( e );
			return;
		} catch ( Exception e ) {
			logError( e );
			return;
		}
	}

	public void disconnect() {
		try {
			if( conn != null && conn.getConnection().isOpen() )
				conn.close();
			connected = false;
		} catch ( RuntimeException e ) {
			e.printStackTrace();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public String getTimeStamp() {
		return dateformat.format( new Date() );
	}

	public void getUserlist() {
		checkConnection();

		conn.send( "user all" );
	}

	public void handleMessage( String message ) {
		try {
			Matcher m = commandpattern.matcher( message );

			if( m.find() ) {
				String cmd = m.group( 1 );

				if( ConnectionEventHandler.Commands.UMSG.equalsIgnoreCase( cmd ) ) {

					window.addOutputText( m.group( 2 ).trim() );

				} else if( ConnectionEventHandler.Commands.JOIN.equalsIgnoreCase( cmd ) ) {
					String name = m.group( 2 ).trim();

					if( sendNameChangeRequest && ( name.equals( username ) || username.equals( "" ) ) ) {
						setUserName( name );
						sendNameChangeRequest = false;
						window.setNameInputEnabled( true );
					} else {

						if( userlist.size() == 1 && userlist.get( 0 ).equals( noUserString ) )
							removeUserListEntry( noUserString );

						addUserListEntry( name );
						addOutput( name + " joined." );
					}

				} else if( ConnectionEventHandler.Commands.LEFT.equalsIgnoreCase( cmd ) ) {
					String name = m.group( 2 ).trim();

					removeUserListEntry( name );
					if( userlist.isEmpty() )
						addUserListEntry( noUserString );

					addOutput( name + " left." );

				} else if( ConnectionEventHandler.Commands.USER.equalsIgnoreCase( cmd ) ) {
					String userstr = m.group( 2 ).trim();

					if( userstr.equals( "0" ) || userstr.equals( username ) ) {
						if( sendNameChangeRequest == true ) {
							window.setNameInputEnabled( true );
							sendNameChangeRequest = false;
						}
						return;
					}

					if( userlist.size() == 1 && userlist.get( 0 ).equals( noUserString ) )
						removeUserListEntry( noUserString );

					if( !userstr.contains( "§§" ) ) {
						addUserListEntry( userstr.trim() );
						return;
					}

					String[] users = userstr.split( "§§" );
					for( String user : users )
						if( !username.equals( user.trim() ) )
							addUserListEntry( user.trim() );

				} else if( ConnectionEventHandler.Commands.FAIL.equalsIgnoreCase( cmd ) ) {
					switch ( Integer.parseInt( m.group( 2 ) ) ) {
						case 1:

							logError( "The username you chose is already in user list!" );

							sendNameChangeRequest = false;
							if( !username.equals( "" ) )
								setUserName( username );

							window.setNameInputEnabled( true );

							break;
						default :
							logError( "Failed: " + m.group( 2 ) );
							break;
					}

				} else if( ConnectionEventHandler.Commands.INKA.equalsIgnoreCase( cmd ) ) {
					String[] names = m.group( 2 ).trim().split( ":" );

					addOutput( names[ 0 ] + " is now known as " + names[ 1 ] + "." );

					if( names[ 0 ].trim().equals( username ) ) {
						setUserName( names[ 1 ].trim() );
						sendNameChangeRequest = false;
						window.setNameInputEnabled( true );
						return;
					}

					removeUserListEntry( names[ 0 ].trim() );
					addUserListEntry( names[ 1 ].trim() );

				} else if( ConnectionEventHandler.Commands.NAME.equalsIgnoreCase( cmd ) ) {
					throw new OperationNotAllowedException( "The recieved command \"name\" is not valid in this context!" );
				} else {
					throw new OperationNotAllowedException( "The recieved command \"" + cmd + "\"is not valid!" );
				}
			}
		} catch ( OperationNotAllowedException ex ) {
			logError( ex );
		}
	}

	public Boolean isConnected() {
		return connected;
	}

	public void logError( String msg ) {
		JOptionPane.showMessageDialog( null, msg, "Error", JOptionPane.OK_OPTION );
	}

	public void logError( Exception ex ) {
		StringBuilder bui = new StringBuilder();
		bui.append( ex.getMessage() );

		for( StackTraceElement el : ex.getStackTrace() )
			bui.append( ( bui.length() == 0 ? "" : "\n" ) + el.toString() );

		logError( bui.toString() );
		ex.printStackTrace( System.err );
	}

	public void onConnected() {
		connected = true;
		addOutput( "connected" );
		addUserListEntry( noUserString );
		sendNameChangeRequest();
		window.setConnected();
		getUserlist();
	}

	public void onDisconnected() {
		conn = null;
		connected = false;
		window.setDisconnected();
		addOutput( "Connection lost. " );

		username = "";
		userlist.clear();
		window.setUserlist( userlist );
	}

	private void removeUserListEntry( String name ) {
		userlist.remove( name );
		window.setUserlist( userlist );
	}

	public void sendMessage() {
		checkConnection();

		String msg = window.getMessageInputText();

		if( msg.trim().equals( "" ) )
			return;

		conn.send( "umsg " + msg );
		window.clearMessageInput();
	}

	public void sendNameChangeRequest() {
		checkConnection();

		String name = window.getNameInputText();

		if( name.trim().equals( "" ) ) {
			logError( "Please insert a name!" );
			return;
		}

		if( name.equals( username ) || sendNameChangeRequest ) {
			return;
		}

		window.setNameInputEnabled( false );
		conn.send( "name " + name );
		sendNameChangeRequest = true;
	}

	private void setUserName( String name ) {
		username = name;
		window.setNameInputText( name );
	}
}
