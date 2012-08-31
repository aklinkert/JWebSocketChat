package listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Client.Client;

public class CloseListener implements ActionListener {
	Client client;

	public CloseListener( Client client ) {
		this.client = client;
	}

	public void actionPerformed( ActionEvent arg0 ) {
		client.close();
	}
}