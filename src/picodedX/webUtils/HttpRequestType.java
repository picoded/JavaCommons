package picoded.webUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

/// @TODO Migrate to picoded.enums.HttpRequestType
public enum HttpRequestType {
	GET((byte) 0), POST((byte) 1), PUT((byte) 2), DELETE((byte) 3), OPTION((byte) 4);
	
	public static final int totalTypeCount = 5;
	
	public static class HttpRequestTypeMap {
		
	}
	
	/// @TODO To check if its needed?
	///-----------------------------------------------
	public byte _value;
	
	private HttpRequestType(byte val) {
		this._value = val;
	}
	
	public byte value() {
		return _value;
	}
	
	public static HttpRequestType getCorrectHttpRequestType(byte val) {
		switch (val) {
		case 0:
			return HttpRequestType.GET;
		case 1:
			return HttpRequestType.POST;
		case 2:
			return HttpRequestType.PUT;
		case 3:
			return HttpRequestType.DELETE;
		case 4:
			return HttpRequestType.OPTION;
		}
		return null;
	}
}
