package Server;
public class ConnectionContext {
	private String name = null;
	private boolean nameSet = false;

	public ConnectionContext() {

	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
		this.nameSet = true;
	}

	public boolean isNameSet() {
		return this.nameSet;
	}

}