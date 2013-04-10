/**
 * 
 */
package server.connectioncontext;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander 'Bodo06' Pinnecke <alexander.pinnecke]at[googlemail.com>
 * 
 */
public final class ConnectionContextFuture implements Callable<ConnectionContext> {

	private final Lock lock = new ReentrantLock();
	private final Condition contextSetCond = this.lock.newCondition();

	private ConnectionContext context;

	@Override
	public ConnectionContext call() throws Exception {
		if (null != this.context) {
			return this.context;
		}

		try {
			this.lock.lock();
			this.contextSetCond.await();

			return this.context;
		} finally {
			this.lock.unlock();
		}

	}

	public void setContext(final ConnectionContext context) {
		if (null != this.context) {
			throw new IllegalStateException("Operation not allowed, ConnectionContext is allready set!");
		}

		this.context = context;

		if (this.lock.tryLock()) {
			try {
				this.contextSetCond.signal();
			} finally {
				this.lock.unlock();
			}
		} else {
			this.contextSetCond.signal();
		}
	}
}
