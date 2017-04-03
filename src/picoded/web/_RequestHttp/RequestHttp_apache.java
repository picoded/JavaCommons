package picoded.web._RequestHttp;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;

import picoded.conv.StringEscape;
import picoded.set.HttpRequestType;
import picoded.web.ResponseHttp;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/// Support the RequestHTTP api via apache
/// Note that apache requests are synchrnous, and not async
public class RequestHttp_apache {
	
	///////////////////////////////////////////////////////////////////////
	// Utility / Helper functions
	///////////////////////////////////////////////////////////////////////
	
	/// Returns the apache client HttpUriRequest, based on its type
	protected static HttpRequestBase HttpUriRequest_fromRequestType(HttpRequestType reqType,
		String reqURL) {
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
		
		throw new RuntimeException("Invalid request type not supported: " + reqType);
		//return null;
	}
	
	/// Adds the cookies parameters to cookieJar
	protected static BasicCookieStore addCookiesIntoCookieJar(String domain,
		BasicCookieStore cookieJar, Map<String, String[]> cookieMap) {
		if (cookieMap != null) {
			for (Map.Entry<String, String[]> entry : cookieMap.entrySet()) {
				String[] values = entry.getValue();
				for (String val : values) {
					BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), val);
					
					//
					// T_T
					//
					// Bloody hell! Domains, and path needs to be explicitely set.
					// When i just want to set cookie policy as SEND ALL.
					// (it is a brand new cookie-jar per request anyway!!!)
					//
					// You can put this as default behaviour, fine, but let the 
					// developer decide how they want to control it. (SOMEWHERE??)
					//
					// And no, setting headers directly is not a good idea.
					// as it will be error prone, etc. Damn you apache!
					//
					// I hate this nearly as much as i hate java type erasure
					// (and that is hard to match)
					//
					// PS: this whole comment block is used to indicate 
					// why this useless step is there, in case someone 
					// (probably me in the future) would think its good
					// to just optimize it out... And break everything.
					//
					// ~ eugene@picoded.com
					//
					cookie.setDomain(domain);
					cookie.setPath("/");
					//
					// Q_Q
					//
					
					cookieJar.addCookie(cookie);
				}
			}
		}
		return cookieJar;
	}
	
	/// Adds the header parameters to request
	protected static HttpRequestBase addHeadersIntoRequest(HttpRequestBase reqBase,
		Map<String, String[]> headersMap) {
		if (headersMap != null) {
			for (Map.Entry<String, String[]> entry : headersMap.entrySet()) {
				String[] values = entry.getValue();
				for (String val : values) {
					reqBase.addHeader(entry.getKey(), val);
				}
			}
		}
		return reqBase;
	}
	
	/// Ammends the GET request URL with the parameters if needed
	protected static String appendGetParameters(String reqURL, Map<String, String[]> parametersMap) {
		if (parametersMap != null && parametersMap.size() > 0) {
			reqURL = reqURL.trim();
			StringBuilder req = new StringBuilder(reqURL);
			
			if (reqURL.endsWith("?")) {
				//does nothing
			} else if (reqURL.indexOf('?') >= 0) {
				req.append("&"); //add to previous paremeters
			} else {
				req.append("?"); //start of parameters
			}
			
			boolean first = true;
			for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
				for (String val : entry.getValue()) {
					if (!first) {
						req.append("&"); //add to previous paremeters
					}
					
					req.append(StringEscape.encodeURI(entry.getKey()));
					req.append("=");
					req.append(StringEscape.encodeURI(val));
					
					first = false;
				}
			}
			
			return req.toString();
		}
		return reqURL;
	}
	
	/// Helps convert a Map<String, String[]> to a List<NameValuePair>, for put and post
	public static List<NameValuePair> parametersToNameValuePairs(Map<String, String[]> parametersMap) {
		// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();
		if (parametersMap != null) {
			for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
				for (String val : entry.getValue()) {
					listNameValuePairs.add(new BasicNameValuePair(entry.getKey(), val));
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
		String reqURL, HttpRequestBase httpRequest, //
		Map<String, String[]> cookieMap //
	) {
		try {
			// gets the hostname
			String host = new URL(reqURL).getHost();
			
			// Prepares the HTTPClient, with a cookieJar =D
			BasicCookieStore cookieJar = addCookiesIntoCookieJar(host, new BasicCookieStore(),
				cookieMap);
			HttpClient apacheClient = HttpClientBuilder.create().setDefaultCookieStore(cookieJar)
				.build();
			
			// Executes request
			HttpResponse apacheResponse = apacheClient.execute(httpRequest);
			
			// The response object to return is created
			ResponseHttp_apache returnResponse = new ResponseHttp_apache();
			returnResponse._cookieJar = cookieJar;
			returnResponse._apacheResponse = apacheResponse;
			returnResponse._inputStream = apacheResponse.getEntity().getContent();
			
			// And Return
			return returnResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Executes the request, given the type and parmeters
	/// Note that post/put request parameters are sent as http form entities
	public static ResponseHttp callRequest( //
		HttpRequestType reqType, //
		String reqURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap //
	) {
		return callRequest(reqType, reqURL, parametersMap, cookiesMap, headersMap, null);
	}
	
	/// Executes the request, given the type and parmeters
	/// Note that post/put request parameters are sent using
	/// the input stream if given, else it uses the parameter map
	public static ResponseHttp callRequest( //
		HttpRequestType reqType, //
		String reqURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap, //
		InputStream requestStream //
	) {
		return callRequest(reqType, reqURL, parametersMap, cookiesMap, headersMap, null,
			requestStream);
	}
	
	/// Executes the request, given the type and parmeters
	/// Note that post/put request parameters are sent using
	/// the input stream if given, else it uses the parameter map
	public static ResponseHttp callRequest( //
		HttpRequestType reqType, //
		String reqURL, //
		Map<String, String[]> parametersMap, //
		Map<String, String[]> cookiesMap, //
		Map<String, String[]> headersMap, //
		Map<String, File[]> filesMap, //
		InputStream requestStream //
	) {
		// append get parameters if needed
		if (reqType == HttpRequestType.GET || reqType == HttpRequestType.DELETE) {
			reqURL = appendGetParameters(reqURL, parametersMap);
		}
		
		// prepare request with headers
		HttpRequestBase reqBase = HttpUriRequest_fromRequestType(reqType, reqURL);
		reqBase = addHeadersIntoRequest(reqBase, headersMap);
		
		// POST and PUT parameters 
		if (reqType == HttpRequestType.POST || reqType == HttpRequestType.PUT) {
			try {
				if (requestStream != null) {
					// note that its sent as a raw stream (set headers manually please)
					((HttpEntityEnclosingRequestBase) reqBase).setEntity(new InputStreamEntity(
						requestStream));
				} else if (filesMap != null) {
					// does a multipart encoding request
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					
					// Process the file map, not using lambda as it does not auto rethrow the IOException
					for (Map.Entry<String, File[]> part : filesMap.entrySet()) {
						String filePath = part.getKey();
						for (File fileObj : part.getValue()) {
							builder.addBinaryBody(filePath, new FileInputStream(fileObj),
								ContentType.APPLICATION_OCTET_STREAM, fileObj.getName());
						}
					}
					;
					
					// Process the text map
					if (parametersMap != null) {
						parametersMap.forEach((key, valArr) -> {
							for (String val : valArr) {
								builder.addTextBody(key, val, ContentType.TEXT_PLAIN);
							}
						});
					}
					
					// And submit it
					HttpEntity multipart = builder.build();
					((HttpEntityEnclosingRequestBase) reqBase).setEntity(multipart);
				} else if (parametersMap != null) {
					// note that its sent as a form entity
					((HttpEntityEnclosingRequestBase) reqBase).setEntity(new UrlEncodedFormEntity(
						parametersToNameValuePairs(parametersMap)));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// calls request with cookies
		return callRequest(reqURL, reqBase, cookiesMap);
	}
}
