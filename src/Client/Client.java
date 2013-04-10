package client;

import exceptions.ConnectionLostException;
import exceptions.OperationNotAllowedException;
import gui.Window;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import listener.ClearListener;
import listener.CloseListener;
import listener.ConnectListener;
import listener.InputKeyListener;
import listener.NameChangeListener;
import listener.SendListener;
import Server.ConnectionEventHandler;

public class Client {

	public static void main(String[] args) {
		new Client();
	}

	private Window window;
	private Connection conn;
	private Boolean connected = false;

	private SimpleDateFormat dateformat = new SimpleDateFormat("kk:mm:ss");

	private String username = "";
	private ArrayList<String> userlist = new ArrayList<String>();
	private Boolean sendNameChangeRequest = false;

	private Pattern commandpattern = Pattern.compile("([a-zA-Z]{4}) (.+)", Pattern.DOTALL);
	private String noUserString = new String("No users in chat.");

	public Client() {
		this.window = new Window();
		this.window.draw();

		this.window.addSendListener(new SendListener(this));
		this.window.addClearListener(new ClearListener(this));
		this.window.addCloseListener(new CloseListener(this));
		this.window.addConnectListener(new ConnectListener(this));
		this.window.addInputListener(new InputKeyListener(this));
		this.window.addNameChangeListener(new NameChangeListener(this));
	}

	public void addOutput(String text) {
		this.window.addOutputText(getTimeStamp() + " " + text);
	}

	private void addUserListEntry(String name) {
		this.userlist.add(name);
		this.window.setUserlist(this.userlist);
	}

	private void checkConnection() {
		if (this.conn == null) {
			throw new ConnectionLostException("Lost Connection.");
		}
	}

	public void clear() {
		this.window.clearOutput();
	}

	public void close() {
		disconnect();
		System.exit(0);
	}

	public void connect() {

		URI uri = null;
		String input = this.window.getConnectionInput();

		if (input == null || input.trim().equals("")) {
			logError("Please insert a connection URI!");
			return;
		}

		try {
			uri = new URI(input);

			this.conn = new Connection(this, uri);
			this.conn.connect();

		} catch (URISyntaxException e) {
			logError(e);
			return;
		} catch (Exception e) {
			logError(e);
			return;
		}
	}

	public void disconnect() {
		try {
			if (this.conn != null && this.conn.getConnection().isOpen()) {
				this.conn.close();
			}
			this.connected = false;
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTimeStamp() {
		return this.dateformat.format(new Date());
	}

	public void getUserlist() {
		checkConnection();

		this.conn.send("user all");
	}

	public void handleMessage(String message) {
		System.out.println("Recieving Message: " + message);

		try {
			Matcher m = this.commandpattern.matcher(message);

			if (m.find()) {
				String cmd = m.group(1);

				if (ConnectionEventHandler.Commands.UMSG.equalsIgnoreCase(cmd)) {

					this.window.addOutputText(m.group(2).trim());

				} else if (ConnectionEventHandler.Commands.JOIN.equalsIgnoreCase(cmd)) {
					String name = m.group(2).trim();

					if (this.sendNameChangeRequest && (name.equals(this.username) || this.username.equals(""))) {
						getUserlist();
						setUserName(name);
						this.sendNameChangeRequest = false;
						this.window.setNameInputEnabled(true);
					} else {

						if (this.userlist.size() == 1 && this.userlist.get(0).equals(this.noUserString)) {
							removeUserListEntry(this.noUserString);
						}

						addUserListEntry(name);
						addOutput(name + " joined.");
					}

				} else if (ConnectionEventHandler.Commands.LEFT.equalsIgnoreCase(cmd)) {
					String name = m.group(2).trim();

					removeUserListEntry(name);
					if (this.userlist.isEmpty()) {
						addUserListEntry(this.noUserString);
					}

					addOutput(name + " left.");

				} else if (ConnectionEventHandler.Commands.USER.equalsIgnoreCase(cmd)) {
					String userstr = m.group(2).trim();

					if (userstr.equals("0") || userstr.equals(this.username)) {
						if (this.sendNameChangeRequest == true) {
							this.window.setNameInputEnabled(true);
							this.sendNameChangeRequest = false;
						}
						return;
					}

					if (this.userlist.size() == 1 && this.userlist.get(0).equals(this.noUserString)) {
						removeUserListEntry(this.noUserString);
					}

					if (!userstr.contains("§§")) {
						addUserListEntry(userstr.trim());
						return;
					}

					String[] users = userstr.split("§§");
					for (String user : users) {
						if (!this.username.equals(user.trim())) {
							addUserListEntry(user.trim());
						}
					}

				} else if (ConnectionEventHandler.Commands.FAIL.equalsIgnoreCase(cmd)) {
					switch (Integer.parseInt(m.group(2))) {
					case 1:

						logError("The username you chose is already in user list!");

						this.sendNameChangeRequest = false;
						if (!this.username.equals("")) {
							setUserName(this.username);
						}

						this.window.setNameInputEnabled(true);

						break;
					default:
						logError("Failed: " + m.group(2));
						break;
					}

				} else if (ConnectionEventHandler.Commands.INKA.equalsIgnoreCase(cmd)) {
					String[] names = m.group(2).trim().split(":");

					addOutput(names[0] + " is now known as " + names[1] + ".");

					if (names[0].trim().equals(this.username)) {
						setUserName(names[1].trim());
						this.sendNameChangeRequest = false;
						this.window.setNameInputEnabled(true);
						return;
					}

					removeUserListEntry(names[0].trim());
					addUserListEntry(names[1].trim());

				} else if (ConnectionEventHandler.Commands.NAME.equalsIgnoreCase(cmd)) {
					throw new OperationNotAllowedException("The recieved command \"name\" is not valid in this context!");
				} else {
					throw new OperationNotAllowedException("The recieved command \"" + cmd + "\"is not valid!");
				}
			}
		} catch (OperationNotAllowedException ex) {
			logError(ex);
		}
	}

	public Boolean isConnected() {
		return this.connected;
	}

	public void logError(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.OK_OPTION);
	}

	public void logError(Exception ex) {
		StringBuilder bui = new StringBuilder();
		bui.append(ex.getMessage());

		for (StackTraceElement el : ex.getStackTrace()) {
			bui.append((bui.length() == 0 ? "" : "\n") + el.toString());
		}

		logError(bui.toString());
		ex.printStackTrace(System.err);
	}

	public void onConnected() {
		this.connected = true;
		addOutput("connected");
		addUserListEntry(this.noUserString);
		sendNameChangeRequest();
		this.window.setConnected();
	}

	public void onDisconnected() {
		this.conn = null;
		this.connected = false;
		this.window.setDisconnected();
		addOutput("Connection lost. ");

		this.username = "";
		this.userlist.clear();
		this.window.setUserlist(this.userlist);
	}

	private void removeUserListEntry(String name) {
		this.userlist.remove(name);
		this.window.setUserlist(this.userlist);
	}

	public void sendMessage() {
		checkConnection();

		String msg = this.window.getMessageInputText();

		if (msg.trim().equals("")) {
			return;
		}

		this.conn.send("umsg " + msg);
		this.window.clearMessageInput();
	}

	public void sendNameChangeRequest() {
		checkConnection();

		String name = this.window.getNameInputText();

		if (name.trim().equals("")) {
			logError("Please insert a name!");
			return;
		}

		if (name.equals(this.username) || this.sendNameChangeRequest) {
			return;
		}

		this.window.setNameInputEnabled(false);
		this.sendNameChangeRequest = true;
		this.conn.send("name " + name);
	}

	private void setUserName(String name) {
		this.username = name;
		this.window.setNameInputText(name);
	}
}
