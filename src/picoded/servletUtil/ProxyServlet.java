package picoded.servletUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.enums.HttpRequestType;
import picoded.servlet.CorePage;
import picoded.webUtils.RequestHttp;
import picoded.webUtils.ResponseHttp;

public class ProxyServlet extends CorePage {
	
	/// Serialization UID.
	protected static final long serialVersionUID = 1L;
	
	/// Key for redirect location header.
	protected static final String LOCATION_HEADER = "Location";
	
	/// Key for content type header.
	protected static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
	
	/// Key for content length header.
	protected static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";
	
	/// Key for host header
	protected static final String HOST_HEADER_NAME = "Host";
	
	/// Key for host header
	protected static final String ORIGIN_HEADER_NAME = "Origin";
	
	/// The directory to use to temporarily store uploaded files
	protected static final File UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
	
	///////////////////////////////////////////////////////////
	// Proxy host params
	///////////////////////////////////////////////////////////
	
	/// The host paramter to proxy request to
	protected String proxyHost = "127.0.0.1";
	
	/// The port on the proxy host to wihch we are proxying requests. Default value is 80.	
	protected int proxyPort = 80;
	
	/// The (optional) path on the proxy host to wihch we are proxying requests. Default value is "".
	protected String proxyPath = "";
	
	/// The maximum size for uploaded files in bytes. Default value is 100MB.
	protected int maxFileUploadSize = 100 * 1024 * 1024;
	
	/// The configured proxy target scheme
	protected String proxyScheme = null;
	
	///////////////////////////////////////////////////////////
	// Proxy host params PUT/GET
	///////////////////////////////////////////////////////////
	
	public String getProxyScheme() {
		return proxyScheme;
	}
	
	public void setProxyScheme(String scheme) {
		proxyScheme = scheme;
	}
	
	public String getProxyHostAndPort() {
		if (getProxyPort() == 80) {
			return getProxyHost();
		} else {
			return getProxyHost() + ":" + getProxyPort();
		}
	}
	
	public String getProxyHost() {
		return proxyHost;
	}
	
	public void setProxyHost(String stringProxyHostNew) {
		proxyHost = stringProxyHostNew;
	}
	
	public int getProxyPort() {
		return proxyPort;
	}
	
	public void setProxyPort(int intProxyPortNew) {
		proxyPort = intProxyPortNew;
	}
	
	public String getProxyPath() {
		return proxyPath;
	}
	
	public void setProxyPath(String stringProxyPathNew) {
		proxyPath = stringProxyPathNew;
	}
	
	public int getMaxFileUploadSize() {
		return maxFileUploadSize;
	}
	
	public void setMaxFileUploadSize(int intMaxFileUploadSizeNew) {
		maxFileUploadSize = intMaxFileUploadSizeNew;
	}
	
	///////////////////////////////////////////////////////////
	// Loading of proxy host config values from web.xml (if applicable)
	///////////////////////////////////////////////////////////
	
	/// Initialize the <code>ProxyServlet</code>
	/// @param servletConfig The Servlet configuration passed in by the servlet conatiner
	@Override
	public void initSetup(CorePage original, ServletConfig servletConfig) {
		super.initSetup(original, servletConfig);
		
		ProxyServlet ori = (ProxyServlet) original;
		
		// Load original values?
		proxyHost = ori.proxyHost;
		proxyPort = ori.proxyPort;
		proxyPath = ori.proxyPath;
		proxyScheme = ori.proxyScheme;
		maxFileUploadSize = ori.maxFileUploadSize;
		
		if (servletConfig == null) {
			return;
		}
		
		// Get the proxy scheme
		String newProxyScheme = servletConfig.getInitParameter("proxyScheme");
		if (newProxyScheme != null && (newProxyScheme = newProxyScheme.trim()).length() > 0) {
			setProxyScheme(newProxyScheme);
		}
		
		// Get the proxy host
		String newProxyHost = servletConfig.getInitParameter("proxyHost");
		if (newProxyHost != null && (newProxyHost = newProxyHost.trim()).length() > 0) {
			setProxyHost(newProxyHost);
		}
		
		// Get the proxy port if specified
		String newProxyPort = servletConfig.getInitParameter("proxyPort");
		if (newProxyPort != null && (newProxyPort = newProxyHost.trim()).length() > 0) {
			setProxyPort(Integer.parseInt(newProxyPort));
		}
		
		// Get the proxy path if specified
		String newProxyPath = servletConfig.getInitParameter("proxyPath");
		if (newProxyPath != null && (newProxyPath = newProxyPath.trim()).length() > 0) {
			setProxyPath(newProxyPath);
		}
		
		// Get the maximum file upload size if specified
		String newMaxFileUploadSize = servletConfig.getInitParameter("maxFileUploadSize");
		if (newMaxFileUploadSize != null && (newMaxFileUploadSize = newMaxFileUploadSize.trim()).length() > 0) {
			setMaxFileUploadSize(Integer.parseInt(newMaxFileUploadSize));
		}
	}
	
	///////////////////////////////////////////////////////////
	// Utility functions for Request / Response
	///////////////////////////////////////////////////////////
	
	/// Gets and returns the target proxy URL given the httpServletReqeust
	protected String getProxyURL(HttpServletRequest httpServletRequest) {
		// Set the protocol to HTTP
		String scheme = (getProxyScheme() != null) ? getProxyScheme() : httpServletRequest.getScheme();
		
		String stringProxyURL = scheme + "://" + getProxyHostAndPort();
		// Check if we are proxying to a path other that the document root
		if (!getProxyPath().equalsIgnoreCase("")) {
			stringProxyURL += getProxyPath();
		}
		
		// Handle the path given to the servlet
		stringProxyURL += httpServletRequest.getPathInfo();
		
		// Handle the query string
		if (httpServletRequest.getQueryString() != null) {
			stringProxyURL += "?" + httpServletRequest.getQueryString();
		}
		return stringProxyURL;
	}
	
	/// Takes a servlet request, and extract the headers, filtering out the uneeded items
	protected Map<String, String[]> filterRequestHeaderMap(CorePage reqPage) {
		Map<String, String[]> map = reqPage.requestHeaderMap();
		
		String[] oriKeys = map.keySet().toArray(new String[map.size()]);
		String[] proxyHostAndPort = new String[] { getProxyHostAndPort() };
		
		for (String key : oriKeys) {
			if (key.equalsIgnoreCase(CONTENT_LENGTH_HEADER_NAME)) {
				String val = map.remove(key)[0];
				map.put(CONTENT_LENGTH_HEADER_NAME, new String[] { val }); //normalize the key value
			}
			
			if (key.equalsIgnoreCase(HOST_HEADER_NAME) || key.equalsIgnoreCase(ORIGIN_HEADER_NAME)) {
				map.put(key, proxyHostAndPort);
			}
		}
		
		return map;
	}
	
	/// Does the proxy request with the given headers, and servlet response
	protected void proxyRequest(HttpRequestType reqType, String targetURL, Map<String, String[]> filteredHeaders,
		InputStream requestStream, HttpServletRequest req, HttpServletResponse res) {
		try {
			
			// Performs the request
			//-----------------------------------------------------------------------------
			
			// Gets the request length
			String reqLength = null;
			if (filteredHeaders.get(CONTENT_LENGTH_HEADER_NAME) != null) {
				reqLength = filteredHeaders.get(CONTENT_LENGTH_HEADER_NAME)[0];
			}
			
			// The request responese object
			ResponseHttp respHttpObj = null;
			
			// Handles post or put
			if (requestStream != null && (reqType == HttpRequestType.POST || reqType == HttpRequestType.PUT)) {
				// Pass input stream if required
				if (reqLength != null && reqLength.equals("0")) {
					// does standard request if 0
					respHttpObj = RequestHttp.byType(reqType, targetURL, null, null, filteredHeaders);
				} else {
					//if( reqLength != null && reqLength.length() == 0 ) {
					//	filteredHeaders.remove(CONTENT_LENGTH_HEADER_NAME);
					//}
					
					// Apache RequestHttp varient, only support completed input stream =[
					// This line should not be removed, to support streaming
					filteredHeaders.remove(CONTENT_LENGTH_HEADER_NAME);
					
					// Passes input stream
					respHttpObj = RequestHttp.byType(reqType, targetURL, null, null, filteredHeaders, requestStream);
				}
			} else {
				//Handles as get / etc request
				respHttpObj = RequestHttp.byType(reqType, targetURL, null, null, filteredHeaders);
			}
			
			// Forward the response code back to the client
			//-----------------------------------------------------------------------------
			int intProxyResponseCode = respHttpObj.statusCode();
			res.setStatus(intProxyResponseCode);
			
			/// Forward response headers back to the client
			//-----------------------------------------------------------------------------
			Map<String, String[]> headersMap = respHttpObj.headersMap();
			if (headersMap != null) {
				for (Map.Entry<String, String[]> entry : headersMap.entrySet()) {
					for (String val : entry.getValue()) {
						res.addHeader(entry.getKey(), val);
					}
				}
			}
			
			// Check if the proxy response is a redirect
			//
			// The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
			// Hooray for open source software
			//-----------------------------------------------------------------------------
			if (intProxyResponseCode >= HttpServletResponse.SC_MULTIPLE_CHOICES // 300 
				&& intProxyResponseCode < HttpServletResponse.SC_NOT_MODIFIED //304
			) {
				// Gets the string location, and check it
				String stringLocation = headersMap.get(LOCATION_HEADER)[0];
				if (stringLocation == null || stringLocation.length() <= 0) {
					throw new RuntimeException("Recieved status code: " + Integer.toString(intProxyResponseCode)
						+ " but no " + LOCATION_HEADER + " header was found in the response");
				}
				
				// The proxy hostname and port
				String stringMyHostName = req.getServerName();
				if (req.getServerPort() != 80) {
					stringMyHostName += ":" + req.getServerPort();
				}
				// stringMyHostName += req.getContextPath();
				
				// Replace the target host path, with the proxy host path,
				// and sends the redirect
				res.sendRedirect(stringLocation.replace(getProxyHostAndPort() + getProxyPath(), stringMyHostName));
				return;
			} else if (intProxyResponseCode == HttpServletResponse.SC_NOT_MODIFIED) {
				// 304 needs special handling.  See:
				// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
				// We get a 304 whenever passed an 'If-Modified-Since'
				// header and the data on disk has not changed; server
				// responds w/ a 304 saying I'm not going to send the
				// body because the file has not changed.
				res.setIntHeader(CONTENT_LENGTH_HEADER_NAME, 0);
				return;
			}
			
			// Send the content to the client
			//-----------------------------------------------------------------------------
			OutputStream outputStreamClientResponse = res.getOutputStream();
			InputStream inputStreamProxyResponse = respHttpObj.inputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			int outputNextByte;
			int bytesToRead = 0;
			
			while ((outputNextByte = bufferedInputStream.read()) != -1) {
				outputStreamClientResponse.write(outputNextByte);
			}
			outputStreamClientResponse.flush();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Performs a proxy redirect using the given CorePage instance
	public boolean proxyCorePageRequest(CorePage page) throws Exception {
		try {
			HttpServletRequest req = page.getHttpServletRequest();
			proxyRequest( //
				page.requestType(), //
				getProxyURL(req), //
				filterRequestHeaderMap(page), //
				req.getInputStream(), //
				req, //
				page.getHttpServletResponse() //
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// Execute the proxy request
		return true;
	}
	
	///////////////////////////////////////////////////////////
	// Core page overrides
	///////////////////////////////////////////////////////////
	
	/// Performs an output request, with special handling of POST / PUT
	@Override
	public boolean outputRequest(Map<String, Object> templateData, PrintWriter output) throws Exception {
		return proxyCorePageRequest(this);
	}
	
}
