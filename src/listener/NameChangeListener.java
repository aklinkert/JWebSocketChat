package listener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import client.Client;


public class NameChangeListener implements FocusListener {

	Client client;

	public NameChangeListener( Client client ) {
		this.client = client;
	}

	public void focusGained( FocusEvent event ) {

	}

	public void focusLost( FocusEvent event ) {
		if( client.isConnected() )
			client.sendNameChangeRequest();
	}

}
