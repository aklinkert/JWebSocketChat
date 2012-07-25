package gui;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class Window extends GUI {

	{
		try {
			UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private JFrame frame;
	private TextArea output = new TextArea(), userlist = new TextArea();
	private JTextField connInputField = new JTextField(), nameInputField = new JTextField(), messageInputField = new JTextField();
	private JButton sendButton = new JButton( "Send" ), closeButton = new JButton( "Close" ), clearButton = new JButton( "Clear" ), connectButton = new JButton( "connect" );
	private JPanel connectionPanel = new JPanel(), messageInputPanel = new JPanel(), outputPanel = new JPanel();

	private int windowHeight = 525, windowWidth = 1000;

	public void addClearListener( ActionListener l ) {
		clearButton.addActionListener( l );
	}

	public void addCloseListener( ActionListener l ) {
		closeButton.addActionListener( l );
	}

	public void addConnectListener( ActionListener l ) {
		connectButton.addActionListener( l );
	}

	public void addInputListener( KeyListener l ) {
		messageInputField.addKeyListener( l );
	}

	public void addNameChangeListener( FocusListener l ) {
		nameInputField.addFocusListener( l );
	}

	public void addOutputText( String text ) {

		output.append( ( output.getText().length() > 0 ? "\n" : "" ) + text );
	}

	public void addSendListener( ActionListener l ) {
		sendButton.addActionListener( l );
	}

	public void clearMessageInput() {
		messageInputField.setText( "" );
	}

	public void clearOutput() {
		output.setText( "" );
	}

	public void draw() {
		Dimension framesize = new Dimension( windowWidth, windowHeight );

		// Frame Settings
		frame = new JFrame();
		frame.setTitle( "JWebSocketChat Client" );
		frame.setLocation( windowWidth / 2, windowHeight / 2 );
		frame.setSize( framesize );
		frame.setPreferredSize( framesize );
		frame.setMaximumSize( framesize );
		frame.setMinimumSize( framesize );
		frame.setResizable( false );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		Container cont = frame.getContentPane();
		cont.setLayout( new FlowLayout() );
		cont.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );

		// Panel for output TextAreas
		outputPanel.setLayout( new FlowLayout() );
		outputPanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		outputPanel.add( setSize( new JScrollPane( output, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), 650, 400 ) );
		outputPanel.add( setSize( new JScrollPane( userlist, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), 300, 400 ) );
		cont.add( setSize( outputPanel, 960, 410 ) );

		// Panel for Input TextField and send-Button
		messageInputPanel.setLayout( new FlowLayout() );
		messageInputPanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		messageInputField.setColumns( 75 );
		messageInputPanel.add( setSize( messageInputField, 850, 28 ) );
		messageInputPanel.add( setSize( sendButton, 120, 28 ) );
		cont.add( setSize( messageInputPanel, 960, 35 ) );

		// Panel for Connection Options
		nameInputField.setColumns( 25 );
		nameInputField.setText( "Name" );
		connInputField.setColumns( 30 );
		connInputField.setText( "ws://localhost:80" );
		connectionPanel.setLayout( new FlowLayout() );
		connectionPanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		connectionPanel.add( setSize( nameInputField, 850, 28 ) );
		connectionPanel.add( setSize( connInputField, 850, 28 ) );
		connectionPanel.add( setSize( connectButton, 120, 28 ) );
		connectionPanel.add( setSize( clearButton, 100, 28 ) );
		connectionPanel.add( setSize( closeButton, 100, 28 ) );
		cont.add( setSize( connectionPanel, 960, 35 ) );

		setDisconnected();

		frame.pack();
		frame.setVisible( true );
	}

	public String getConnectionInput() {
		return connInputField.getText();
	}

	public String getMessageInputText() {
		return messageInputField.getText();
	}

	public String getNameInputText() {
		return nameInputField.getText();
	}

	public void setConnected() {
		output.setEditable( false );
		userlist.setEditable( false );

		messageInputField.setEnabled( true );
		sendButton.setEnabled( true );

		connInputField.setEnabled( false );
		connectButton.setText( "Disconnect" );
	}

	public void setDisconnected() {
		output.setEditable( false );
		userlist.setEditable( false );

		messageInputField.setEnabled( false );
		sendButton.setEnabled( false );

		connInputField.setEnabled( true );
		connectButton.setText( "Connect" );

		userlist.setText( "" );
	}

	public void setFocusOnMessageInput() {
		messageInputField.requestFocus();
	}

	public void setFocusOnNameInput() {
		nameInputField.requestFocus();
	}

	public void setNameInputEnabled( Boolean status ) {
		nameInputField.setEnabled( status );
	}

	public void setNameInputText( String name ) {
		nameInputField.setText( name );
	}

	public void setUserlist( ArrayList<String> list ) {
		userlist.setText( "" );

		for( String s : list ) {
			if( !userlist.getText().equals( "" ) )
				userlist.append( "\n" );
			userlist.append( s );
		}
	}
}
