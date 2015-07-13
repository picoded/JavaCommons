package picoded.webUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

/// @TODO Migrate to picoded.enums.HttpRequestType
public enum HttpRequestType {
	TYPE_GET((byte)0),
	TYPE_POST((byte)1),
	TYPE_PUT((byte)2),
	TYPE_DELETE((byte)3),
	TYPE_OPTION((byte)4);
	
	public static final int totalTypeCount = 5;
	
	public static class HttpRequestTypeMap {
		
	}
	
	
	/// @TODO To check if its needed?
	///-----------------------------------------------
	public byte _value;
	
	private HttpRequestType(byte val){
		this._value = val;
	}
	
	public byte value(){
		return _value;
	}
	
	public static HttpRequestType getCorrectHttpRequestType(byte val){
		switch(val){
			case 0: return HttpRequestType.TYPE_GET;
			case 1: return HttpRequestType.TYPE_POST;
			case 2: return HttpRequestType.TYPE_PUT;
			case 3: return HttpRequestType.TYPE_DELETE;
			case 4: return HttpRequestType.TYPE_OPTION;
		}
		return null;
	}
}
