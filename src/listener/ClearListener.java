package listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Client.Client;

public class ClearListener implements ActionListener {
	Client client;

	public ClearListener( Client client ) {
		this.client = client;
	}

	public void actionPerformed( ActionEvent arg0 ) {
		client.clear();
	}
}