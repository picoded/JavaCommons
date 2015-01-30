package picoded.jSql;

import java.lang.String;
import java.lang.Exception;

/// JSql base exception class : This is used for BOTH sqlite and mysql
public class JSqlException extends Exception {
	protected static final long serialVersionUID = 1L;
	
	public static String invalidDatabaseImplementationException = "Invalid JSql implementation. Please use the resepctive database implementations, and avoid initiating the JSql class directly";
	
	public JSqlException(String message){
		super(message);
	}
	public JSqlException(String message, Throwable cause) {
		super(message, cause);
	}
	public JSqlException(Throwable cause) {
		super("JSqlException", cause);
	}
}