package picoded.JStack;

import java.lang.String;
import java.lang.Exception;

/// JStack base exception class
public class JStackException extends Exception {
	protected static final long serialVersionUID = 1L;
	
	public JStackException(String message) {
		super(message);
	}
	
	public JStackException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public JStackException(Throwable cause) {
		super("JStackException", cause);
	}
}
