package picoded.servlet.api;

import java.util.HashMap;
import java.util.Map;

public class ApiException extends RuntimeException {
	
	private int status = 500;
	private String errorCode = "";
	private Map<String, Object> metaData = new HashMap<>();
	
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
	
	public Map<String, Object> getMetaData() {
		return metaData;
	}
	
	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}
	
	public void putMetaData(String key, Object value) {
		this.metaData.put(key, value);
	}
	
}
