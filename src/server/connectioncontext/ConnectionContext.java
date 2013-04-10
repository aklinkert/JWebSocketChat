package server.connectioncontext;

public final class ConnectionContext {
	private boolean isNull = false;

	private String name = null;

	protected boolean isNull() {
		return this.isNull;
	}

	protected void setNull(final boolean isNull) {
		this.isNull = isNull;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}