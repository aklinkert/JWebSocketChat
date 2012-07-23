package Client;

import gui.Window;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import listener.ClearListener;
import listener.CloseListener;
import listener.ConnectListener;
import listener.InputKeyListener;
import listener.SendListener;

public class Client {

	private Window window;
	private Connection conn;
	private SimpleDateFormat dateformat = new SimpleDateFormat( "kk:mm:ss" );

	// private String username;
	// private ArrayList<String> userlist = new ArrayList<String>();

	public Client() {
		this.window = new Window();

		window.addSendListener( new SendListener( this ) );
		window.addClearListener( new ClearListener( this ) );
		window.addCloseListener( new CloseListener() );
		window.addConnectListener( new ConnectListener( this ) );
		window.addInputListener( new InputKeyListener( this ) );
	}

	public void logError( String msg, String details ) {
		System.err.println( "\nError: " + msg + "\nDetails: " + details );
		JOptionPane.showMessageDialog( null, msg, "Error", JOptionPane.OK_OPTION );
	}

	public void sendMessage() {
		String msg = window.getInputText();

		window.addOutputText( getTimeStamp() + " " + msg );
	}

	public void clear() {
		window.clearOutput();
	}

	public void connect() {

		URI uri = null;
		String input = window.getConnectionInput();

		if( input == null || input.trim().equals( "" ) ) {
			logError( "Please insert a connection URI!", "" );
			return;
		}

		try {
			uri = new URI( input );
		} catch ( URISyntaxException e ) {
			logError( "Failed to convert String to URI. ", e.getMessage() );
			return;
		}

		try {
			conn = new Connection( this, uri );
			conn.connect();
			window.setConnected();
		} catch ( Exception e ) {
			logError( "Failed to connect to URI.", e.getMessage() );
		}

	}

	public void disconnect() {
		if( conn.getReadyState() == 1 )
			conn.close();
		window.setDisconnected();
		addOutput( "Connection lost. " );
	}

	public void addOutput( String text ) {
		window.addOutputText( getTimeStamp() + " " + text );
	}

	public String getTimeStamp() {
		return dateformat.format( new Date() );
	}

	public void handleMessage( String message ) {

	}

	public static void main( String[] args ) {
		new Client();
	}
}
