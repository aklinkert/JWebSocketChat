/**
 * 
 */
package server.connectioncontext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.java_websocket.WebSocket;

/**
 * @author Alexander 'Bodo06' Pinnecke <alexander.pinnecke]at[googlemail.com>
 * 
 */
public class ConnectionContextHandler {

	private final Lock lock = new ReentrantLock();
	private final Condition cond = this.lock.newCondition();
	private final boolean inUse = false;

	private static ConnectionContextHandler instance;
	private final HashMap<WebSocket, ConnectionContext> contexts = new HashMap<WebSocket, ConnectionContext>();
	private final HashMap<WebSocket, LinkedList<ConnectionContextFuture>> waiters = new HashMap<WebSocket, LinkedList<ConnectionContextFuture>>();

	private final ExecutorService exec = Executors.newCachedThreadPool();

	public static ConnectionContextHandler getInstance() {
		if (null == instance) {
			instance = new ConnectionContextHandler();
		}

		return instance;
	}

	public Future<ConnectionContext> getContextFuture(final WebSocket s) {
		if (this.inUse) {
			try {
				this.cond.await();
			} catch (InterruptedException e) {
				System.err.println("Error while waiting for unlocking ConnectionContextFuture condition:");
				e.printStackTrace();
			}
		}

		this.lock.lock();
		try {
			ConnectionContext context;

			if (!this.contexts.containsKey(s)) {
				context = new ConnectionContext();
				this.contexts.put(s, context);
			} else {
				context = this.contexts.get(s);
			}

			ConnectionContextFuture contextFuture = new ConnectionContextFuture();

			if (!this.waiters.containsKey(s)) {
				this.waiters.put(s, new LinkedList<ConnectionContextFuture>());
			}

			this.waiters.get(s).push(contextFuture);

			Future<ConnectionContext> future = this.exec.submit(contextFuture);

			if (!context.isNull()) {
				context.setNull(true);
				contextFuture.setContext(context);
			}

			return future;
		} finally {
			this.cond.signal();
			this.lock.unlock();
		}

	}

	public void notifyNext(final WebSocket s) {
		if (this.inUse) {
			try {
				this.cond.await();
			} catch (InterruptedException e) {
				System.err.println("Error while waiting for unlocking ConnectionContextFuture condition:");
				e.printStackTrace();
			}
		}

		this.lock.lock();
		try {
			if (!this.waiters.containsKey(s) || !this.contexts.containsKey(s)) {
				return;
			}

			ConnectionContextFuture future = this.waiters.get(s).poll();
			ConnectionContext context = this.contexts.get(s);

			if (null != future) {
				future.setContext(context);
			}

			context.setNull(true);
		} finally {
			this.cond.signal();
			this.lock.unlock();
		}
	}

	public void giveBack(final WebSocket s, final ConnectionContext context) {
		context.setNull(false);
		notifyNext(s);
	}
}
