package Client;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class Connection extends WebSocketClient {

	private Client client;
	public Connection( Client client , URI serverURI ) {
		super( serverURI );

		if( client == null )
			throw new IllegalArgumentException( "The given Argument \"client\" is null!" );
		this.client = client;
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		client.addOutput( "connected" );
	}

	@Override
	public void onMessage( String message ) {
		client.handleMessage( message );
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		client.disconnect();
	}

	@Override
	public void onError( Exception ex ) {
		client.logError( ex.getMessage(), ex.getStackTrace().toString() );
	}

}
