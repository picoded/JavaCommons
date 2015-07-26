package picoded.webUtils._RequestHttp;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import picoded.conv.StringEscape;
import picoded.enums.HttpRequestType;
import picoded.webUtils.ResponseHttp;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

/// Support the RequestHTTP api via apache
/// Note that apache requests are synchrnous, and not async
public class RequestHttp_apache {
	
	///////////////////////////////////////////////////////////////////////
	// Utility / Helper functions
	///////////////////////////////////////////////////////////////////////
	
	/// Returns the apache client HttpUriRequest, based on its type
	protected static HttpRequestBase HttpUriRequest_fromRequestType( HttpRequestType reqType, String reqURL ) {
		switch (reqType) {
			case GET:
				return new HttpGet(reqURL);
			case POST:
				return new HttpPost(reqURL);
			case PUT:
				return new HttpPut(reqURL);
			case DELETE:
				return new HttpDelete(reqURL);
			case OPTION:
				return new HttpOptions(reqURL);
		}
		
		throw new RuntimeException("Invalid request type not supported: "+reqType);
		//return null;
	}
	
	/// Adds the cookies parameters to cookieJar
	protected static BasicCookieStore addCookiesIntoCookieJar(BasicCookieStore cookieJar, Map<String, String[]> cookieMap){
		if(cookieMap != null){
			for(Map.Entry<String, String[]> entry : cookieMap.entrySet()){
				for( String val : entry.getValue() ) {
					cookieJar.addCookie(new BasicClientCookie(entry.getKey(), val));
				}
			}
		}
		return cookieJar;
	}
	
	/// Adds the header parameters to request
	protected static HttpRequestBase addHeadersIntoRequest(HttpRequestBase reqBase, Map<String, String[]> headersMap){
		if(headersMap != null){
			for(Map.Entry<String, String[]> entry : headersMap.entrySet()){
				for( String val : entry.getValue() ) {
					reqBase.addHeader( entry.getKey(), val );
				}
			}
		}
		return reqBase;
	}
	
	/// Ammends the GET request URL with the parameters if needed
	protected static String appendGetParameters(String reqURL, Map<String,String[]> parametersMap) {
		if(parametersMap != null && parametersMap.size() > 0) {
			reqURL = reqURL.trim();
			StringBuilder req = new StringBuilder(reqURL);
			
			if(reqURL.endsWith("?")) {
				//does nothing
			} else if(reqURL.indexOf('?') >= 0) {
				req.append("&"); //add to previous paremeters
			} else {
				req.append("?"); //start of parameters
			}
			
			boolean first = true;
			for(Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
				for( String val : entry.getValue() ) {
					if(!first) {
						req.append("&"); //add to previous paremeters
					}
					
					req.append( StringEscape.encodeURI(entry.getKey()) );
					req.append( "=" );
					req.append( StringEscape.encodeURI(val) );
					
					first = false;
				}
			}
			
			return req.toString();
		}
		return reqURL;
	}
	
	/// Helps convert a Map<String, String[]> to a List<NameValuePair>, for put and post
	public static List<NameValuePair> parametersToNameValuePairs( Map<String, String[]> parametersMap ) {
		// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();
		if(parametersMap != null){
			for(Map.Entry<String, String[]> entry : parametersMap.entrySet()){
				for( String val : entry.getValue() ) {
					listNameValuePairs.add( new BasicNameValuePair(entry.getKey(), val) );
				}
			}
		}
		
		return listNameValuePairs;
	}
	
	///////////////////////////////////////////////////////////////////////
	// HTTP Request call handling
	///////////////////////////////////////////////////////////////////////
	
	/// Request executor taking a HttpRequestBase and returning its response
	protected static ResponseHttp_apache callRequest( //
		HttpRequestBase httpRequest, //
		Map<String,String[]> cookieMap //
	){
		try {
			// Prepares the HTTPClient, with a cookieJar =D
			BasicCookieStore cookieJar = addCookiesIntoCookieJar( new BasicCookieStore(), cookieMap );
			HttpClient apacheClient = HttpClientBuilder.create().setDefaultCookieStore(cookieJar).build();
			
			// Executes request
			HttpResponse apacheResponse = apacheClient.execute(httpRequest);

			// The response object to return is created
			ResponseHttp_apache returnResponse = new ResponseHttp_apache();
			returnResponse._cookieJar = cookieJar;
			returnResponse._apacheResponse = apacheResponse;
			returnResponse._inputStream = apacheResponse.getEntity().getContent();
			
			// And Return
			return returnResponse;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Executes the request, given the type and parmeters
	/// Note that post/put request parameters are sent as http form entities
	public static ResponseHttp callRequest( //
		HttpRequestType reqType, //
		String reqURL, //
		Map<String,String[]> parametersMap, //
		Map<String,String[]> cookiesMap, //
		Map<String,String[]> headersMap //
	) {
		// append get parameters if needed
		if(reqType == HttpRequestType.GET) { 
			reqURL = appendGetParameters(reqURL, parametersMap);
		}
		
		// prepare request with headers
		HttpRequestBase reqBase = HttpUriRequest_fromRequestType(reqType, reqURL);
		reqBase = addHeadersIntoRequest(reqBase, headersMap);
		
		
		// POST and PUT parameters 
		// note that its sent as a form entity
		if(reqType == HttpRequestType.POST || reqType == HttpRequestType.PUT ) {
			try {
				((HttpEntityEnclosingRequestBase)reqBase).setEntity( new UrlEncodedFormEntity( parametersToNameValuePairs(parametersMap) ) );
				
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// calls request with cookies
		return callRequest(reqBase, cookiesMap);
	}
}
