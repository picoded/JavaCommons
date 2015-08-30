package picoded.webUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.cookie.*;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.protocol.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.amazonaws.util.StringUtils;

import picoded.servlet.CorePage;
import picoded.enums.HttpRequestType;
import picoded.conv.StringEscape;

import com.ning.http.client.*;
import com.ning.http.client.AsyncHttpClientConfig.Builder;

/// Utility functions for various RequestHttp functions shared with proxyPage.
///
/// Note: See Async Http API for more details on underlying implementation
/// http://www.javadoc.io/doc/com.ning/async-http-client/1.9.29
///
public class RequestHttpUtils {
	
	/// Returns the apache client HttpUriRequest, based on its type
	public static org.apache.http.client.methods.HttpUriRequest apache_HttpUriRequest_fromRequestType( HttpRequestType reqType, String reqURL ) {
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
	
	/// Returns the async http client, based on its type
	protected static AsyncHttpClient.BoundRequestBuilder asyncHttpClient_fromRequestType( HttpRequestType reqType, String reqURL, boolean setFollowRedirect ) {
		AsyncHttpClientConfig config = (new AsyncHttpClientConfig.Builder()).setFollowRedirect( setFollowRedirect ).build();
		AsyncHttpClient httpClient = new AsyncHttpClient( config );
		
		switch (reqType) {
			case GET:
				return httpClient.prepareGet(reqURL);
			case POST:
				return httpClient.preparePost(reqURL);
			case PUT:
				return httpClient.preparePut(reqURL);
			case DELETE:
				return httpClient.prepareDelete(reqURL);
			case OPTION:
				return httpClient.prepareOptions(reqURL);
		}
		
		throw new RuntimeException("Invalid request type not supported: "+reqType);
		//return null;
	}
	
	/// Helps convert a Map<String, String[]> to a List<NameValuePair>
	public static List<NameValuePair> parameterMapToList( Map<String, String[]> inMap ) {
		// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();

		// Iterate the parameter names
		for (String stringParameterName : inMap.keySet()) {
			// Iterate the values for each parameter name
			String[] stringArrayParameterValues = inMap.get(stringParameterName);
			for (String stringParamterValue : stringArrayParameterValues) {
				// Create a NameValuePair and store in list
				NameValuePair nameValuePair = new BasicNameValuePair(stringParameterName, stringParamterValue);
				listNameValuePairs.add(nameValuePair);
			}
		}
		
		return listNameValuePairs;
	}
	
}
