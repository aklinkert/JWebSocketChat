package exceptions;

public class OperationNotAllowedException extends Exception {

	private static final long serialVersionUID = 233775919636873943L;

	public OperationNotAllowedException() {
		super();
	}

	public OperationNotAllowedException( String arg0 ) {
		super( arg0 );
	}

	public OperationNotAllowedException( Throwable arg0 ) {
		super( arg0 );
	}

	public OperationNotAllowedException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public OperationNotAllowedException( String arg0 , Throwable arg1 , boolean arg2 , boolean arg3 ) {
		super( arg0, arg1, arg2, arg3 );
	}

}
