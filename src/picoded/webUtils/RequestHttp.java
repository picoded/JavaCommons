package picoded.webUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

import picoded.enums.HttpRequestType;
import picoded.webUtils._RequestHttp.RequestHttp_apache;
import picoded.webUtils._RequestHttp.ResponseHttp_websocket;

///
/// Sometimes you just want to do a simple HTTP Request and response
/// that JUST WORKS. Without needing to handle like a dozen over import types,
/// or complex setup (staring at you apache).
///
/// Aka: KISS (for) the user programmer (using) this class (api)
///
/// KISS: Keep It Simple Stupid
///
public class RequestHttp {
	
	//--------------------------------------------------------
	// X request operations
	//--------------------------------------------------------
	
	/// Performs GET request with parameters, cookies and headers
	public static ResponseHttp byType( //
		HttpRequestType requestType, //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap // 
	) { //
		return RequestHttp_apache.callRequest(requestType, requestURL, parametersMap, cookiesMap,
			headersMap, null);
	}
	
	/// Performs GET request with parameters, cookies and headers
	public static ResponseHttp byType( //
		HttpRequestType requestType, //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap, // 
		InputStream requestStream //
	) { //
		return RequestHttp_apache.callRequest(requestType, requestURL, parametersMap, cookiesMap,
			headersMap, requestStream);
	}
	
	//--------------------------------------------------------
	// GET request operations
	//--------------------------------------------------------
	
	/// Performs GET request : in the most basic form
	public static ResponseHttp get(String requestURL) {
		return byType(HttpRequestType.GET, requestURL, null, null, null);
	}
	
	/// Performs GET request : with parameters
	public static ResponseHttp get(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.GET, requestURL, parametersMap, null, null);
	}
	
	/// Performs GET request with parameters, cookies and headers
	public static ResponseHttp get( //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap // 
	) { //
		return byType(HttpRequestType.GET, requestURL, parametersMap, cookiesMap, headersMap);
	}
	
	//--------------------------------------------------------
	// POST request operations
	//--------------------------------------------------------
	
	/// Performs (form) POST request : with parameters
	public static ResponseHttp post(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.POST, requestURL, parametersMap, null, null);
	}
	
	/// Performs (form) POST request with parameters, cookies and headers
	public static ResponseHttp post( //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap // 
	) { //
		return byType(HttpRequestType.POST, requestURL, parametersMap, cookiesMap, headersMap);
	}
	
	//--------------------------------------------------------
	// PUT request operations
	//--------------------------------------------------------
	
	/// Performs (form) PUT request : with parameters
	public static ResponseHttp put(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.PUT, requestURL, parametersMap, null, null);
	}
	
	/// Performs (form) PUT request with parameters, cookies and headers
	public static ResponseHttp put( //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap // 
	) { //
		return byType(HttpRequestType.PUT, requestURL, parametersMap, cookiesMap, headersMap);
	}
	
	//--------------------------------------------------------
	// DELETE request operations
	//--------------------------------------------------------
	
	/// Performs delete request
	public static ResponseHttp delete(String requestURL) {
		return byType(HttpRequestType.DELETE, requestURL, null, null, null);
	}
	
	/// Performs DELETE request : with parameters
	/// Note: parameters are treated the same way as GET request
	public static ResponseHttp delete(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.DELETE, requestURL, parametersMap, null, null);
	}
	
	/// Performs delete request with parameters, cookies and headers
	/// Note: parameters are treated the same way as GET request
	public static ResponseHttp delete( //
		String requestURL, //
		Map<String, String[]> parametersMap, // 
		Map<String, String[]> cookiesMap, // 
		Map<String, String[]> headersMap // 
	) { //
		return byType(HttpRequestType.DELETE, requestURL, parametersMap, cookiesMap, headersMap);
	}
	
	//--------------------------------------------------------
	// Websocket operations
	//--------------------------------------------------------
	
	/// Creates a basic websocket connection
	public static ResponseHttp websocket(String requestURL) {
		return new ResponseHttp_websocket(requestURL, null);
	}
	
	/// Creates a basic websocket connection
	public static ResponseHttp websocket(String requestURL, Consumer<String> handler) {
		return new ResponseHttp_websocket(requestURL, handler);
	}
}
