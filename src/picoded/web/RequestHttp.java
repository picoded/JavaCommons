package picoded.web;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.io.InputStream;
import java.io.File;
import java.util.function.Consumer;

import picoded.set.HttpRequestType;
import picoded.set.EmptyArray;
import picoded.core.conv.GenericConvert;
import picoded.web._RequestHttp.RequestHttp_apache;
import picoded.web._RequestHttp.ResponseHttp_websocket;

/**
* Sometimes you just want to do a simple HTTP Request and response
* that JUST WORKS. Without needing to handle like a dozen over import types,
* or complex setup (staring at you apache).
*
* Aka: KISS (for) the user programmer (using) this class (api)
*
* KISS: Keep It Simple Stupid.
*
* @TODO : A Map<String,String>, String[] String[], Map<String,Object> request version
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
*
* // You can use your relevent URL string that you want
* String requestURL = "http://localhost:8080"
*
* // Get request in its simplest form
* String getResult = RequestHttp.get( requestURL ).toString()
*
* // Or with parameters
* Map<String,Object> requestParams = new HashMap<String,Object>();
* requestParams.put("test","one");
*
* // Get request with parameters
* getResult = RequestHttp.get( requestURL, requestParams ).toString();
*
* // That can also be made as a POST request
* getResult = RequestHttp.post( requestURL, requestParams ).toString();
*
* // Or other less commonly used types
* getResult = RequestHttp.put( requestURL, requestParams ).toString();
* getResult = RequestHttp.delete( requestURL, requestParams ).toString();
*
* //
* // You may also want to get additional possible values
* //
* HttpResponse response = RequestHttp.get( requestURL );
*
* // Such as HTTP Code
* response.statusCode();
*
* // Its headers and cookies map
* response.headersMap();
* response.cookiesMap();
*
* // Oh similarly you can use that in your request too, with Map<String, String[]>
* RequestHttp.get( requestURL, requestParams, cookies, headers );
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
**/
public class RequestHttp {

	//--------------------------------------------------------
	// Utility function
	//--------------------------------------------------------

	/**
	* Takes a standard Map<String,Object> to Map<String,String[]> format
	*
	* Note this does a "dumb" conversion of stringifying every object provided
	* and placing it as a single element in a String[]
	*
	* @param  Simplified request parameters
	*
	* @return Simplified as Map<String,Object>
	**/
	public static Map<String,String[]> simpleParameterConversion(Map<String,Object> inMap) {
		// Null check
		if( inMap == null ) {
			return null;
		}

		// Setup and duplicate over
		Map<String,String[]> ret = new HashMap<String,String[]>();
		Set<String> keySet = inMap.keySet();

		// Enforce conversion to string, and store each value
		for(String key : keySet) {
			ret.put(key, new String[] { GenericConvert.toString(inMap.get(key)) });
		};

		// Return the converted params
		return ret;
	}

	//--------------------------------------------------------
	// simple GET / POST / PUT / DELETE
	//--------------------------------------------------------

	/**
	* Performs GET request : with parameters, appended to the requestURL
	*
	* Parameters here are in a strict 1 to 1 pair, and is parsed to string as such
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp simpleGet(String requestURL, Map<String,Object> parametersMap) {
		return get(requestURL, simpleParameterConversion(parametersMap));
	}

	/**
	* Performs POST request : with parameters
	*
	* Parameters here are in a strict 1 to 1 pair, and is parsed to string as such
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp simplePost(String requestURL, Map<String,Object> parametersMap) {
		return post(requestURL, simpleParameterConversion(parametersMap));
	}

	/**
	* Performs PUT request : with parameters
	*
	* Parameters here are in a strict 1 to 1 pair, and is parsed to string as such
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp simplePut(String requestURL, Map<String,Object> parametersMap) {
		return put(requestURL, simpleParameterConversion(parametersMap));
	}

	/**
	* Performs DELETE request : with parameters
	*
	* Parameters here are in a strict 1 to 1 pair, and is parsed to string as such
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp simpleDelete(String requestURL, Map<String,Object> parametersMap) {
		return delete(requestURL, simpleParameterConversion(parametersMap));
	}

	//--------------------------------------------------------
	// GET request operations
	//--------------------------------------------------------

	/**
	* Performs GET request : in the most basic form
	*
	* @param   Request URL to call
	*
	* @return  The ResponseHttp object, use it to get more info
	**/
	public static ResponseHttp get(String requestURL) {
		return byType(HttpRequestType.GET, requestURL, null, null, null);
	}

	/**
	* Performs GET request : with parameters, appended to the requestURL
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp get(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.GET, requestURL, parametersMap, null, null);
	}

	/**
	* Performs GET request : with parameters, appended to the requestURL
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	* @param   [Optional] Cookie map to send values
	*
	* @return  The ResponseHttp object
	**/
	public static ResponseHttp get( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap) { //
		return byType(HttpRequestType.GET, requestURL, parametersMap, cookiesMap, null);
	}

	/**
	* Performs GET request : with parameters, appended to the requestURL
	*
	* @param   Request URL to call
	* @param   [Optional] Parameters to add to the request
	* @param   [Optional] Cookie map to send values
	* @param   [Optional] Headers map to send values
	*
	* @return  The ResponseHttp object
	**/
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

	/**
	* Performs (form) POST request : with parameters
	**/
	public static ResponseHttp post(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.POST, requestURL, parametersMap, null, null);
	}

	/**
	* Performs (form) POST request with parameters, cookies and headers
	**/
	public static ResponseHttp post( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) { //
		return byType(HttpRequestType.POST, requestURL, parametersMap, cookiesMap, headersMap);
	}

	/**
	* Performs (form) POST request : with parameters and files
	**/
	public static ResponseHttp post(String requestURL, Map<String, String[]> parametersMap,
		Map<String, File[]> filesMap) {
		return byType(HttpRequestType.POST, requestURL, parametersMap, null, null, filesMap, null);
	}

	/**
	* Performs (form) POST request with parameters, files, cookies and headers
	**/
	public static ResponseHttp post( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, File[]> filesMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) { //
		return byType(HttpRequestType.POST, requestURL, parametersMap, cookiesMap, headersMap,
			filesMap, null);
	}

	//--------------------------------------------------------
	// PUT request operations
	//--------------------------------------------------------

	/**
	* Performs (form) PUT request : with parameters
	**/
	public static ResponseHttp put(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.PUT, requestURL, parametersMap, null, null);
	}

	/**
	* Performs (form) PUT request with parameters, cookies and headers
	**/
	public static ResponseHttp put( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) { //
		return byType(HttpRequestType.PUT, requestURL, parametersMap, cookiesMap, headersMap);
	}

	/**
	* Performs (form) PUT request : with parameters and files
	**/
	public static ResponseHttp put(String requestURL, Map<String, String[]> parametersMap,
		Map<String, File[]> filesMap) {
		return byType(HttpRequestType.PUT, requestURL, parametersMap, null, null, filesMap, null);
	}

	/**
	* Performs (form) PUT request with parameters, files, cookies and headers
	**/
	public static ResponseHttp put( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, File[]> filesMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) { //
		return byType(HttpRequestType.PUT, requestURL, parametersMap, cookiesMap, headersMap,
			filesMap, null);
	}

	//--------------------------------------------------------
	// DELETE request operations
	//--------------------------------------------------------

	/**
	* Performs delete request
	**/
	public static ResponseHttp delete(String requestURL) {
		return byType(HttpRequestType.DELETE, requestURL, null, null, null);
	}

	/**
	* Performs DELETE request : with parameters
	* Note: parameters are treated the same way as GET request
	**/
	public static ResponseHttp delete(String requestURL, Map<String, String[]> parametersMap) {
		return byType(HttpRequestType.DELETE, requestURL, parametersMap, null, null);
	}

	/**
	* Performs delete request with parameters, cookies and headers
	* Note: parameters are treated the same way as GET request
	**/
	public static ResponseHttp delete( //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) { //
		return byType(HttpRequestType.DELETE, requestURL, parametersMap, cookiesMap, headersMap);
	}

	//--------------------------------------------------------
	// X request operations
	//--------------------------------------------------------

	/**
	* Performs X request with parameters, cookies and headers
	**/
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

	/**
	* Performs X request with parameters, cookies and headers
	**/
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

	/**
	* Performs X request with parameters, cookies and headers
	**/
	public static ResponseHttp byType( //
		HttpRequestType requestType, //
		String requestURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap, //
		Map<String, File[]> filesMap, //
		InputStream requestStream //
	) { //
		return RequestHttp_apache.callRequest(requestType, requestURL, parametersMap, cookiesMap,
			headersMap, filesMap, requestStream);
	}

	//--------------------------------------------------------
	// Websocket operations
	//--------------------------------------------------------

	/**
	* Creates a basic websocket connection
	**/
	public static ResponseHttp websocket(String requestURL) {
		return new ResponseHttp_websocket(requestURL, null);
	}

	/**
	* Creates a basic websocket connection
	**/
	public static ResponseHttp websocket(String requestURL, Consumer<String> handler) {
		return new ResponseHttp_websocket(requestURL, handler);
	}
}
