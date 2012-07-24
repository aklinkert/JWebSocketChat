package listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Client.Client;

public class InputKeyListener implements KeyListener {

	private Client client;

	public InputKeyListener( Client client ) {
		this.client = client;
	}
	public void keyTyped( KeyEvent event ) {

	}

	public void keyPressed( KeyEvent event ) {
		if( event.getKeyCode() == KeyEvent.VK_ENTER )
			client.sendMessage();
	}

	public void keyReleased( KeyEvent event ) {

	}

}
