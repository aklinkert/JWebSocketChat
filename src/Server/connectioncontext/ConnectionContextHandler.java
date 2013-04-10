/**
 * 
 */
package Server.connectioncontext;

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

	private Lock lock = new ReentrantLock();
	private Condition cond = this.lock.newCondition();
	private boolean inUse = false;

	private static ConnectionContextHandler instance;
	private HashMap<WebSocket, ConnectionContext> contexts = new HashMap<WebSocket, ConnectionContext>();
	private HashMap<WebSocket, LinkedList<ConnectionContextFuture>> waiters = new HashMap<WebSocket, LinkedList<ConnectionContextFuture>>();

	private ExecutorService exec = Executors.newCachedThreadPool();

	public static ConnectionContextHandler getInstance() {
		if (null == instance) {
			instance = new ConnectionContextHandler();
		}

		return instance;
	}

	public Future<ConnectionContext> getContextFuture(WebSocket s) {
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

	public void notifyNext(WebSocket s) {
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

	public void giveBack(WebSocket s, ConnectionContext context) {
		context.setNull(false);
		notifyNext(s);
	}
}
