package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public abstract class GUI {
	public abstract void addClearListener( ActionListener l );

	public abstract void addCloseListener( ActionListener l );

	public abstract void addConnectListener( ActionListener l );

	public abstract void addInputListener( KeyListener l );

	public abstract void addNameChangeListener( FocusListener l );

	public abstract void addOutputText( String text );

	public abstract void addSendListener( ActionListener l );

	public abstract void clearMessageInput();

	public abstract void clearOutput();

	public abstract void draw();

	public abstract String getConnectionInput();

	public abstract String getMessageInputText();

	public abstract String getNameInputText();

	public abstract void setConnected();

	public abstract void setDisconnected();

	public abstract void setFocusOnMessageInput();

	public abstract void setFocusOnNameInput();

	public abstract void setNameInputEnabled( Boolean status );

	public abstract void setNameInputText( String name );

	public abstract void setUserlist( ArrayList<String> list );

	public Component setSize( Component comp, int width, int height ) {
		Dimension d = new Dimension( width, height );

		comp.setPreferredSize( d );
		comp.setSize( d );
		comp.setMaximumSize( d );
		comp.setMinimumSize( d );

		return comp;
	}

}
