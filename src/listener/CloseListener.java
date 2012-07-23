package listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloseListener implements ActionListener {
	public void actionPerformed( ActionEvent arg0 ) {
		System.exit( 0 );
	}
}