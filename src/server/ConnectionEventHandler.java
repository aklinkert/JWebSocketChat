package server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.java_websocket.WebSocket;

import server.connectioncontext.ConnectionContext;
import server.connectioncontext.ConnectionContextHandler;

public class ConnectionEventHandler implements Runnable {

	public enum ConnectionEvent {
		OPEN, MESSAGE, CLOSE
	}

	public static final class ErrorCodes {

		public static final int INVALIDCOMMAND = -1;
		public static final int NOTNAMED = 0;
		public static final int USERALLREADYEXIST = 1;
	}

	public static final class Commands {

		public static final String FAIL = "fail";
		public static final String JOIN = "join";
		public static final String LEFT = "left";
		public static final String NAME = "name";
		public static final String USER = "user";
		public static final String INKA = "inka";
		public static final String UMSG = "umsg";
	}

	// private SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss:SSS" );
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private final Pattern commandpattern = Pattern.compile("([a-zA-Z]{4}) (.+)", Pattern.DOTALL);
	private final Server server;
	private final WebSocket conn;
	private final List<String> users;
	private final String message;
	private final ConnectionEvent event;

	public ConnectionEventHandler(final Server server, final WebSocket conn, final List<String> users, final ConnectionEvent event, final String message) {
		super();
		this.server = server;
		this.conn = conn;
		this.users = users;
		this.event = event;
		this.message = message;
	}

	@Override
	public void run() {
		ConnectionContextHandler contextHandler = ConnectionContextHandler.getInstance();
		Future<ConnectionContext> future = contextHandler.getContextFuture(this.conn);
		ConnectionContext context = null;
		try {
			context = future.get();

			if (null == context) {
				throw new IllegalStateException("Failed to get Context.");
			}

			if (ConnectionEvent.OPEN == this.event) {
				onOpen(context);
			} else if (ConnectionEvent.MESSAGE == this.event) {
				onMessage(context);
			} else if (ConnectionEvent.CLOSE == this.event) {
				onClose(context);
			}

		} catch (InterruptedException e) {
			System.err.println("Error while waiting for ConnectionContextFuture result:");
			e.printStackTrace();
		} catch (ExecutionException e) {
			System.err.println("Error while waiting for ConnectionContextFuture result:");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			System.err.println("Error while getting ConnectionContextFuture result:");
			e.printStackTrace();
		} finally {
			if (null == context) {
				contextHandler.notifyNext(this.conn);
			} else {
				contextHandler.giveBack(this.conn, context);
			}
		}

	}

	private void onOpen(final ConnectionContext context) {
		// empty Method for later events
	}

	private void onMessage(final ConnectionContext context) {
		if (this.message == null) {
			return;
		}

		Matcher m = this.commandpattern.matcher(this.message);

		boolean found = m.find();

		if (null == context.getName() && !found) {
			sendFail(this.conn, ErrorCodes.NOTNAMED);
			return;
		}

		if (!found) {
			System.err.println("Error with incoming message: No command used!");
			sendFail(this.conn, ErrorCodes.INVALIDCOMMAND);
			return;
		}

		String cmd = m.group(1);

		if (null == context.getName() && !ConnectionEventHandler.Commands.NAME.equalsIgnoreCase(cmd)) {
			sendFail(this.conn, ErrorCodes.NOTNAMED);
			return;
		}

		if (ConnectionEventHandler.Commands.NAME.equalsIgnoreCase(cmd)) {

			if (null == context.getName()) {
				String name = m.group(2);

				if (this.users.contains(name)) {
					this.server.send(this.conn, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST);
					this.conn.closeConnection(0, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST);
					return;
				}

				this.users.add(name);
				this.server.dispatch(ConnectionEventHandler.Commands.JOIN + " " + name);

				System.out.println(name + " joined");
				context.setName(name);

			} else {
				String name = m.group(2);

				if (this.users.contains(name)) {
					this.server.send(this.conn, ConnectionEventHandler.Commands.FAIL + " " + ConnectionEventHandler.ErrorCodes.USERALLREADYEXIST);
					return;
				}

				this.users.remove(context.getName());
				this.users.add(name);

				this.server.dispatch(ConnectionEventHandler.Commands.INKA + " " + context.getName() + ":" + name);

				context.setName(name);
			}

		} else if (ConnectionEventHandler.Commands.USER.equalsIgnoreCase(cmd)) {
			this.server.send(this.conn, "user " + getUsers());
		} else if (ConnectionEventHandler.Commands.UMSG.equalsIgnoreCase(cmd)) {
			this.server.dispatch(ConnectionEventHandler.Commands.UMSG + " " + this.dateFormat.format(new Date()) + " " + context.getName() + ": " + m.group(2));
		} else {
			System.err.println("Error with incoming message: Unknown command Type " + cmd + "!");
		}

	}

	private void onClose(final ConnectionContext context) {
		String name = context.getName();

		if (null != name) {
			this.users.remove(name);
			this.server.dispatch(ConnectionEventHandler.Commands.LEFT + " " + name);
			this.server.log(name + " left.");
		}

	}

	public void sendFail(final WebSocket conn, final int code) {
		this.server.send(conn, ConnectionEventHandler.Commands.FAIL + " " + code);
	}

	private String getUsers() {
		// return users.toString();

		if (this.users.isEmpty()) {
			return "0";
		}

		Iterator<String> it = this.users.iterator();
		String userList = it.next();

		while (it.hasNext()) {
			userList += "§§" + it.next();
		}

		return userList;
	}
}
