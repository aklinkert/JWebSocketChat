package listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import client.Client;


public class SendListener implements ActionListener {
	private Client client;

	public SendListener( Client client ) {
		this.client = client;
	}

	public void actionPerformed( ActionEvent arg0 ) {
		client.sendMessage();
	}
}