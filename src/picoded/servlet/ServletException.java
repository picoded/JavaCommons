package picoded.servlet;

/// Extendsion of javax.servlet.ServletException. This is to let the exception to be included via picoded.servlet.*
class ServletException extends javax.servlet.ServletException {
	// Serialize version ID
	static final long serialVersionUID = 1L;
	
	public ServletException(String message) {
		super(message);
	}
	
	public ServletException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServletException(Throwable cause) {
		super("JStackException", cause);
	}
	
}
