package exceptions;

public class ConnectionLostException extends RuntimeException {

	private static final long serialVersionUID = 543631858133703046L;

	public ConnectionLostException() {
		super();
	}

	public ConnectionLostException( String message ) {
		super( message );
	}

	public ConnectionLostException( String message , Throwable cause , boolean enableSuppression , boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}

	public ConnectionLostException( String message , Throwable cause ) {
		super( message, cause );
	}

	public ConnectionLostException( Throwable cause ) {
		super( cause );
	}
}
