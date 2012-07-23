package gui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class Window {

	private JFrame frame;
	private TextArea output = new TextArea(), userlist = new TextArea();
	private JTextField connInputField = new JTextField(), nameInputField = new JTextField(), messageInputField = new JTextField();
	private JButton sendButton = new JButton( "Send" ), closeButton = new JButton( "Close" ), clearButton = new JButton( "Clear" ), connectButton = new JButton( "connect" );
	private JPanel connectionPanel = new JPanel(), MessageInputPanel = new JPanel(), outputPanel = new JPanel();

	private int windowHeight = 525, windowWidth = 1000;

	public Window() {

		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( Exception e ) {
		}

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
		frame.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		Container cont = frame.getContentPane();
		cont.setLayout( new FlowLayout() );

		// Panel for output TextAreas
		outputPanel.add( setSize( new JScrollPane( output, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), 650, 400 ) );
		outputPanel.add( setSize( new JScrollPane( userlist, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), 300, 400 ) );
		cont.add( setSize( outputPanel, 960, 410 ) );

		// Panel for Input TextField and send-Button
		messageInputField.setColumns( 80 );
		MessageInputPanel.add( setSize( messageInputField, 850, 28 ) );
		MessageInputPanel.add( setSize( sendButton, 65, 26 ) );
		cont.add( setSize( MessageInputPanel, 960, 35 ) );

		// Panel for Connection Options
		nameInputField.setColumns( 30 );
		nameInputField.setText( "Your Name" );
		connInputField.setColumns( 30 );
		connInputField.setText( "ws://osgiliath.arda-network.de:80" );
		connectionPanel.add( setSize( nameInputField, 850, 28 ) );
		connectionPanel.add( setSize( connInputField, 850, 28 ) );
		connectionPanel.add( setSize( connectButton, 130, 26 ) );
		connectionPanel.add( clearButton );
		connectionPanel.add( closeButton );
		cont.add( setSize( connectionPanel, 960, 35 ) );

		setDisconnected();

		frame.pack();
		frame.setVisible( true );
	}
	private Component setSize( Component comp, int width, int height ) {
		Dimension d = new Dimension( width, height );

		comp.setSize( d );
		comp.setPreferredSize( d );
		comp.setMaximumSize( d );
		comp.setMinimumSize( d );

		return comp;
	}

	public void addSendListener( ActionListener l ) {
		sendButton.addActionListener( l );
	}

	public void addCloseListener( ActionListener l ) {
		closeButton.addActionListener( l );
	}

	public void addClearListener( ActionListener l ) {
		clearButton.addActionListener( l );
	}

	public void addConnectListener( ActionListener l ) {
		connectButton.addActionListener( l );
	}

	public void addInputListener( KeyListener l ) {
		messageInputField.addKeyListener( l );
	}

	public void clearOutput() {
		output.setText( "" );
	}

	public void addOutputText( String text ) {
		output.append( "\n" + text );
	}

	public String getConnectionInput() {
		return connInputField.getText();
	}

	public String getInputText() {
		return messageInputField.getText();
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
	}
}
