package picoded.servlet.api.internal;

public class HaltException extends RuntimeException {
	
	public HaltException() {
		super();
	}
	
	public HaltException(String s) {
		super(s);
	}
}
