package picoded.webUtils;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.util.*;

import org.apache.commons.io.IOUtils;

import picoded.conv.*;

/// Performs a RawRequestHttp request, to get the input / output streams. Currently only supports GET and not POST
///
/// @TODO support POST parameters / files
///
public class RawRequestHttp {
	
	////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////

	/// Creates an instance with the target base URL
	public RawRequestHttp(String inRequestURL) {
		try {
			requestUrl = new URL(inRequestURL);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		commonSetup();
	}
	
	/// Creates an instance with the target base URL
	public RawRequestHttp(URL inRequestURL) {
		requestUrl = inRequestURL;
		commonSetup();
	}
	
	////////////////////////////////////////////////////
	// Protected common setup / functions and variables
	////////////////////////////////////////////////////

	/// The actual URL
	protected URL requestUrl = null;
	
	/// HTTP request type
	protected String requestType = "GET";
	
	/// Encryption on/off
	protected boolean encrypted = false;
	
	/// Timeout 
	protected int timeout = 30 * 1000;
	
	/// OutputStream is present
	protected boolean doOutputStream = false;
	
	/// Setup the default paramters, as per URL
	protected void commonSetup() {
		String scheme = requestUrl.getProtocol().toLowerCase();
		
		//requestUrl.setRequestProperty(String key, String value)
		//requestUrl.setIfModifiedSince(long unixtime);
		
		// Protocol specific values
		//-----------------------------------------------------
		
		// Websocket extended timeout
		if( scheme.startsWith("ws") ) {
			/// 15 minutes timeout
			timeout = 15 * 60 * 1000;
			doOutputStream = true;
		}
		
		// HTTPS Encryption
		if( scheme.startsWith("wss") || scheme.startsWith("https") ) {
			encrypted = true;
		}
	}
	
	/// InputStream handler
	protected InputStream inStream = null;
	
	/// OutputStream handler
	protected OutputStream outStream = null;
	
	/// headers
	protected Map<String,List<String>> requestHeaders = null;
	
	/// Sets the map
	public RawRequestHttp setHeaderMap(Map<String, List<String>> inMap) {
		requestHeaders = inMap;
		return this;
	}
	
	////////////////////////////////////////////////////
	// Connection request
	////////////////////////////////////////////////////
	
	protected HttpURLConnection urlConnection = null;
	
	/// Applies the configuration, and connect to server
	public RawRequestHttp connect() throws IOException {
		urlConnection = (HttpURLConnection)requestUrl.openConnection();
		
		// Apply config values
		urlConnection.setAllowUserInteraction(false);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		
		urlConnection.setConnectTimeout(timeout);
		urlConnection.setReadTimeout(timeout);
		urlConnection.setDoOutput(doOutputStream);
		
		//Iterate headers if present, and sets them
		if( requestHeaders != null ) {
			for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
				for (String val : entry.getValue()) {
					urlConnection.addRequestProperty( entry.getKey(),val);
				}
			}
		}
		
		urlConnection.connect();
		return this;
	}
	
	/// Disconnect and closes the connection
	public RawRequestHttp disconnect() {
		urlConnection.disconnect();
		return this;
	}
	
	////////////////////////////////////////////////////
	// Stream and result handling
	////////////////////////////////////////////////////
	
	public InputStream inputStream() {
		if(inStream != null) {
			return inStream;
		}
		try {
			return (inStream = urlConnection.getInputStream());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public OutputStream outputStream() {
		if(!doOutputStream) {
			return null;
		}
		if(outStream != null) {
			return outStream;
		}
		try {
			return (outStream = urlConnection.getOutputStream());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public int statusCode() {
		try {
			return urlConnection.getResponseCode();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, List<String>> headerMap() {
		try {
			return urlConnection.getHeaderFields();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	////////////////////////////////////////////////////
	// Utitlity functions
	////////////////////////////////////////////////////
	
	protected String _cachedString = null;
	
	/// Gets the response content as a string
	/// @TODO get the response encoding type, and pass "toString"
	public String toString() {
		if( _cachedString != null ) {
			return _cachedString;
		}
		
		try {
			return (_cachedString = IOUtils.toString(inputStream())); //, encoding 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Converts the result string into a map, via JSON's
	public Map<String,Object> toMap() {
		String r = toString();
		if( r == null || r.length() <= 1 ) {
			return null;
		}
		return ConvertJSON.toMap( r );
	}
}
