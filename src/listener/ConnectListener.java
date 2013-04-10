package listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import client.Client;


public class ConnectListener implements ActionListener {

	private Client client;

	public ConnectListener( Client client ) {
		this.client = client;
	}

	public void actionPerformed( ActionEvent arg0 ) {
		if( client.isConnected() )
			client.disconnect();
		else
			client.connect();
	}
}