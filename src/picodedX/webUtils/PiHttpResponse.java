package picoded.webUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

public class PiHttpResponse {
	
	private Map<String, String> _headers;
	private Map<String, String> _cookies;
	private InputStream _responseBody;
	
	public PiHttpResponse(Map<String, String> headers, Map<String, String> cookies, InputStream responseBody){
		_headers = headers;
		_cookies = cookies;
		_responseBody = responseBody;
	}
	
	public Map<String, String> getHeaders(){
		return _headers;
	}
	
	public Map<String, String> getCookies(){
		return _cookies;
	}
	
	public InputStream getResponseBody(){
		return _responseBody;
	}
}