package picoded.JSql;

import java.lang.String;
import java.lang.Exception;

//import java.io.IOException;

/// JSql base exception class : This is used for BOTH sqlite and mysql
public class JSqlException extends Exception {
	protected static final long serialVersionUID = 1L;
	
	public static String invalidDatabaseImplementationException = "Invalid JSql implementation. Please use the resepctive database implementations, and avoid initiating the JSql class directly";
	
	public static String oracleNameSpaceWarning = "Table/Index/View/Column name should not be more then 30 char (due to ORACLE support): ";
	
	public JSqlException(String message) {
		super(message);
	}
	
	public JSqlException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public JSqlException(Throwable cause) {
		super("JSqlException", cause);
	}
}
