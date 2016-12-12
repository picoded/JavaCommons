package picoded.webUtils._RequestHttp;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import picoded.struct.HashMapList;

/// Extends the basic implmentation for the Apache Http Client
public class ResponseHttp_apache extends ResponseHttp_basic {
	
	//////////////////////////////////////////////////////
	// Apache HTTP response additional info containers
	//////////////////////////////////////////////////////
	
	/// The response raw values
	protected BasicCookieStore _cookieJar = null;
	protected HttpResponse _apacheResponse = null;
	
	/// Cached copy
	protected Map<String, String[]> _headersMap = null;
	protected Map<String, String[]> _cookiesMap = null;
	
	/// Returns the status code
	public int statusCode() {
		return _apacheResponse.getStatusLine().getStatusCode();
	}
	
	/// Gets the header map
	public Map<String, String[]> headersMap() {
		// Returns cached copy if exists
		if (_headersMap != null) {
			return _headersMap;
		}
		
		// Transfer the headers into the HashMapList
		HashMapList<String, String> mapList = new HashMapList<String, String>();
		for (Header header : _apacheResponse.getAllHeaders()) {
			mapList.append(header.getName(), header.getValue());
		}
		
		return (_headersMap = mapList.toMapArray(new String[0]));
	}
	
	/// Gets the cookies map
	public Map<String, String[]> cookiesMap() {
		// Returns cached copy if exists
		if (_cookiesMap != null) {
			return _cookiesMap;
		}
		
		// Gets the cookies from the cookie jar =D
		HashMapList<String, String> mapList = new HashMapList<String, String>();
		for (Cookie cookie : _cookieJar.getCookies()) {
			mapList.append(cookie.getName(), cookie.getValue());
		}
		
		return (_cookiesMap = mapList.toMapArray(new String[0]));
	}
	
}
