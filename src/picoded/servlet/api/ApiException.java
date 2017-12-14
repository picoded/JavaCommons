package picoded.servlet.api;

public class ApiException extends RuntimeException {
	
	private int status = 500;
	private String errorCode = "";
	
	public ApiException(int status, String errorCode, String message) {
		super(message);
		this.status = status;
		this.errorCode = errorCode;
	}
	
	public ApiException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
