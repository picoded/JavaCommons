package picoded.jCache;

import java.lang.String;
import java.lang.Exception;
//import java.io.IOException;

/// JSql base exception class : This is used for BOTH sqlite and mysql
public class JCacheException extends Exception {
	protected static final long serialVersionUID = 1L;
	
	/// Default exception, for function calls on invalid implementation (done by base class)
	public static String invalidDatastoreImplementationException = "Invalid JCache implementation. Please use the resepctive data storage implementations, and avoid initiating the JCache class directly";
	
	/// Exception used for function calls that are done, while "disposed"
	public static String isAlreadyDisposed = "Invalid function call: JCache client connection is already 'disposed'";
	
	public JCacheException(String message) {
		super(message);
	}
	
	public JCacheException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public JCacheException(Throwable cause) {
		super("JCacheException", cause);
	}
}
