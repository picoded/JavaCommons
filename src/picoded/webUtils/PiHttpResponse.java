package picoded.webUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

public class PiHttpResponse {
	
	private Map<String, String> _header;
	private Map<String, String> _cookie;
	private InputStream _responseBody;
	
	public PiHttpResponse(Map<String, String> header, Map<String, String> cookie, InputStream responseBody){
		_header = header;
		_cookie = cookie;
		_responseBody = responseBody;
	}
	
	public Map<String, String> getHeaders(){
		return _header;
	}
	
	public Map<String, String> getCookies(){
		return _cookie;
	}
	
	public InputStream getResponseBody(){
		return _responseBody;
	}
}