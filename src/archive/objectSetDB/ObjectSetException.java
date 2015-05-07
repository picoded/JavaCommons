package picoded.objectSetDB;

/// ObjectSetDB base exception class
public class ObjectSetException extends Exception {
	protected static final long serialVersionUID = 1L;
	
	public static String invalidDataStack = "Invalid data stack setup";
	
	public ObjectSetException(String message) {
		super(message);
	}
	
	public ObjectSetException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ObjectSetException(Throwable cause) {
		super("ObjectSetException", cause);
	}
}
