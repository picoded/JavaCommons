package picoded.servletUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.servlet.*;
import picoded.webUtils.*;
import picoded.enums.HttpRequestType;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.cookie.*;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.protocol.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;


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
		if(getProxyPort() == 80) {
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
	public void initSetup( CorePage original, ServletConfig servletConfig ) {
		super.initSetup(original, servletConfig);
		
		ProxyServlet ori = (ProxyServlet)original;
		
		// Load original values?
		proxyHost = ori.proxyHost;
		proxyPort = ori.proxyPort;
		proxyPath = ori.proxyPath;
		proxyScheme = ori.proxyScheme;
		maxFileUploadSize = ori.maxFileUploadSize;
		
		if( servletConfig == null ) {
			return;
		}
		
		// Get the proxy scheme
		String newProxyScheme = servletConfig.getInitParameter("proxyScheme");
		if (newProxyScheme != null && (newProxyScheme = newProxyScheme.trim()).length() > 0 ) { 
			setProxyScheme( newProxyScheme );
		}

		// Get the proxy host
		String newProxyHost = servletConfig.getInitParameter("proxyHost");
		if (newProxyHost != null && (newProxyHost = newProxyHost.trim()).length() > 0 ) { 
			setProxyHost( newProxyHost );
		}

		// Get the proxy port if specified
		String newProxyPort = servletConfig.getInitParameter("proxyPort");
		if (newProxyPort != null && (newProxyPort = newProxyHost.trim()).length() > 0 ) {
			setProxyPort( Integer.parseInt(newProxyPort) );
		}

		// Get the proxy path if specified
		String newProxyPath = servletConfig.getInitParameter("proxyPath");
		if (newProxyPath != null && (newProxyPath = newProxyPath.trim()).length() > 0 ) {
			setProxyPath( newProxyPath );
		}

		// Get the maximum file upload size if specified
		String newMaxFileUploadSize = servletConfig.getInitParameter("maxFileUploadSize");
		if(newMaxFileUploadSize != null &&  (newMaxFileUploadSize = newMaxFileUploadSize.trim()).length() > 0) {
			setMaxFileUploadSize( Integer.parseInt(newMaxFileUploadSize) );
		}
	}
	
	///////////////////////////////////////////////////////////
	// Utility functions for Request / Response
	///////////////////////////////////////////////////////////
	
	/// Gets and returns the target proxy URL given the httpServletReqeust
	protected String getProxyURL(HttpServletRequest httpServletRequest) {
		// Set the protocol to HTTP
		String scheme = (getProxyScheme() != null)? getProxyScheme() : httpServletRequest.getScheme();
		
		String stringProxyURL = scheme + "://" + getProxyHostAndPort();
		// Check if we are proxying to a path other that the document root
		if(!getProxyPath().equalsIgnoreCase("")){
			stringProxyURL += getProxyPath();
		}
		
		// Handle the path given to the servlet
		stringProxyURL += httpServletRequest.getPathInfo();
		
		// Handle the query string
		if(httpServletRequest.getQueryString() != null) {
			stringProxyURL += "?" + httpServletRequest.getQueryString();
		}
		return stringProxyURL;
	}
	
	/// Takes a servlet request, and extract the headers, filtering out the uneeded items
	protected Map<String,String[]> filterRequestHeaderMap(CorePage reqPage) {
		Map<String,String[]> map = reqPage.requestHeaderMap();
		
		for( String key : map.keySet() ) {
			if( key.equalsIgnoreCase(CONTENT_LENGTH_HEADER_NAME) ) {
				map.remove(key);
			}
			
			if(key.equalsIgnoreCase(HOST_HEADER_NAME) || key.equalsIgnoreCase(ORIGIN_HEADER_NAME)) {
				map.put(key, new String[] { getProxyHostAndPort() } );
			}
		}
		
		return map;
	}
	
	/// Does the proxy request with the given headers, and servlet response
	protected void proxyRequest(HttpRequestType reqType, String targetURL, Map<String, String[]> filteredHeaders, HttpServletResponse res ) {
		try {
			
			// Performs the request
			//-----------------------------------------------------------------------------
			ResponseHttp respHttpObj = RequestHttp.byType( reqType, targetURL, null, null, filteredHeaders );
			
			// Forward the response code back to the client
			//-----------------------------------------------------------------------------
			int intProxyResponseCode = respHttpObj.statusCode();
			res.setStatus(intProxyResponseCode);
			
			/// Forward response headers back to the client
			//-----------------------------------------------------------------------------
			Map<String,String[]> headersMap = respHttpObj.headersMap();
			if( headersMap != null ) {
				for (Map.Entry<String, String[]> entry : headersMap.entrySet() ) {
					for (String val : entry.getValue()) {
						res.addHeader( entry.getKey(), val );	
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
				String stringLocation = headerMap.get(LOCATION_HEADER)[0];
				if(stringLocation == null || stringLocation.length <= 0) {
					throw new RuntimeException("Recieved status code: " + Integer.toString(intProxyResponseCode) 
					+ " but no " +  LOCATION_HEADER + " header was found in the response");
				}
				
				// Modify the redirect to go to this proxy servlet rather that the proxied host, if applicable
				String stringMyHostName = req.getServerName();
				if(req.getServerPort() != 80) {
					stringMyHostName += ":" + req.getServerPort();
				}
				stringMyHostName += req.getContextPath();
				
				res.sendRedirect(stringLocation.replace(getProxyHostAndPort() + getProxyPath(), stringMyHostName));
				return;
			} else if(intProxyResponseCode == HttpServletResponse.SC_NOT_MODIFIED) {
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
			OutputStream outputStreamClientResponse = res.getOutputStream();
			InputStream inputStreamProxyResponse = getRequest.inputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			// 
			// /// Send via a second thread?
			// Thread outputStreamClientResponse_thread = new Thread(
			// 	new Runnable(){
			// 		public void run(){
			// 			int b;
			// 			try {
			// 				while ( ( b = bufferedInputStream.read() ) != -1 ) {
			// 					outputStreamClientResponse.write(b);
			// 					outputStreamClientResponse.flush();
			// 				}
			// 			} catch(Exception e) {
			// 				// INTENTIONALLY SILENCED, to handle socket close events
			// 				//throw new RuntimeException(e);
			// 				return;
			// 			}
			// 		}
			// 	}
			// );
			// outputStreamClientResponse_thread.start();
			// 
			// OutputStream streamToTarget = getRequest.outputStream();
			// InputStream streamFromReq = req.getInputStream();
			// BufferedInputStream bisFromReq = new BufferedInputStream(streamFromReq);
			// 
			// Thread socketInputStream_thread = new Thread(
			// 	new Runnable(){
			// 		public void run(){
			// 			int b;
			// 			
			// 			try {
			// 				while ( streamToTarget != null && ( b = bisFromReq.read() ) != -1 ) {
			// 					streamToTarget.write(b);
			// 					streamToTarget.flush();
			// 				}
			// 			} catch(Exception e) {
			// 				// INTENTIONALLY SILENCED, to handle socket close events
			// 				//throw new RuntimeException(e);
			// 				return;
			// 			}
			// 		}
			// 	}
			// );
			// 
			// while( (outputStreamClientResponse_thread != null && outputStreamClientResponse_thread.isAlive())  || 
			//        (socketInputStream_thread != null && socketInputStream_thread.isAlive()) 
			// ) {
			// 	Thread.sleep(1);
			// }
			
			int outputNextByte;
			int bytesToRead = 0;
			
			//uses blocking call instead?
			while ( ( outputNextByte = bufferedInputStream.read() ) != -1 ) {
				outputStreamClientResponse.write(outputNextByte);
				outputStreamClientResponse.flush();
			}
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Performs a proxy redirect using the given CorePage instance
	public boolean proxyCorePageRequest(CorePage page) throws Exception {
		
		try {
			HttpRequestType rType = page.requestType();
			HttpServletRequest sReq = page.getHttpServletRequest();
			HttpServletResponse sRes = page.getHttpServletResponse();
			
			// if( rType == HttpRequestType.GET ) {
			// 	getRequestExecute(sReq, sRes);
			// 	return true;
			// }
			// 
			// // Create the respective request URL based on requestType and URL
			// HttpUriRequest methodToProxyRequest = RequestHttpUtils.apache_HttpUriRequest_fromRequestType(rType, getProxyURL(sReq));
			// 
			// // Forward the request headers
			// setProxyRequestHeaders(sReq, methodToProxyRequest);
			// 
			// InputStream socketInputStream = null;
			// 
			// // Handles post or put
			// if( rType == HttpRequestType.POST || rType == HttpRequestType.PUT ) {
			// 	// is not needed?
			// 	if( false && ServletFileUpload.isMultipartContent( sReq )) {
			// 		handleMultipartPost( (HttpEntityEnclosingRequestBase)methodToProxyRequest, sReq);
			// 	} else {
			// 		//	handleStandardPost( (HttpEntityEnclosingRequestBase)methodToProxyRequest, sReq);
			// 		
			// 		String reqLength = sReq.getHeader(CONTENT_LENGTH_HEADER_NAME);
			// 		
			// 		if( reqLength != null && reqLength.equals("0") ) {
			// 			// does nothing if req is 0
			// 		//} else if( reqLength == null || reqLength.length() <= 0 ) {
			// 		//	//Streaming request?
			// 		//	socketInputStream = sReq.getInputStream();
			// 		} else {
			// 			// Gets as raw input stream, and passes it
			// 			try {
			// 				((HttpEntityEnclosingRequestBase)methodToProxyRequest).setEntity( new InputStreamEntity(sReq.getInputStream()) );
			// 			} catch(IOException e) {
			// 				throw new RuntimeException(e);
			// 			}
			// 		}
			// 		
			// 	}
			// }
			// 
			// executeProxyRequest(rType, methodToProxyRequest, sReq, sRes, socketInputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// Execute the proxy request
		return true;
	}
	
	
	/// Executes the proxy request with the 
	
	
	/*
	
	protected void executeProxyRequest( 
		HttpRequestType reqType, 
		HttpUriRequest httpMethodProxyRequest, 
		HttpServletRequest httpServletRequest, 
		HttpServletResponse httpServletResponse,
		InputStream socketInputStream 
	) throws Exception {
		
		try {
			
			//System.out.println( "Request Type - "+reqType );
			
			// Create a default HttpClient with Disabled automated stuff
			HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().disableAuthCaching().build();
			
			/// Execute the proxy request
			HttpResponse response = httpClient.execute(httpMethodProxyRequest);
			
			/// Pass response headers back to the client
			Header[] headerArrayResponse = response.getAllHeaders();
			for(Header header : headerArrayResponse) {
				httpServletResponse.addHeader(header.getName(), header.getValue());
				//System.out.println( "Header res - "+header.getName()+" = "+header.getValue());
			}
			
			StatusLine statusLine = response.getStatusLine();
			int intProxyResponseCode = statusLine.getStatusCode();
			
			// Pass the response code back to the client
			httpServletResponse.setStatus(intProxyResponseCode);
			//httpServletResponse.setStatus(intProxyResponseCode);
			
			// Check if the proxy response is a redirect
			// The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
			// Hooray for open source software
			if (intProxyResponseCode >= HttpServletResponse.SC_MULTIPLE_CHOICES // 300 
			&& intProxyResponseCode < HttpServletResponse.SC_NOT_MODIFIED //304
			) {
				String stringStatusCode = Integer.toString(intProxyResponseCode);
				String stringLocation = response.getFirstHeader(LOCATION_HEADER).getValue();
				
				if(stringLocation == null) {
					throw new RuntimeException("Recieved status code: " + stringStatusCode 
					+ " but no " +  LOCATION_HEADER + " header was found in the response");
				}
				
				// Modify the redirect to go to this proxy servlet rather that the proxied host
				String stringMyHostName = httpServletRequest.getServerName();
				if(httpServletRequest.getServerPort() != 80) {
					stringMyHostName += ":" + httpServletRequest.getServerPort();
				}
				stringMyHostName += httpServletRequest.getContextPath();
				
				httpServletResponse.sendRedirect(stringLocation.replace(getProxyHostAndPort() + getProxyPath(), stringMyHostName));
				return;
			} else if(intProxyResponseCode == HttpServletResponse.SC_NOT_MODIFIED) {
				// 304 needs special handling.  See:
				// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
				// We get a 304 whenever passed an 'If-Modified-Since'
				// header and the data on disk has not changed; server
				// responds w/ a 304 saying I'm not going to send the
				// body because the file has not changed.
				httpServletResponse.setIntHeader(CONTENT_LENGTH_HEADER_NAME, 0);
				return;
			}
			
			// Send the content to the client
			InputStream inputStreamProxyResponse = response.getEntity().getContent();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			OutputStream outputStreamClientResponse = httpServletResponse.getOutputStream();
			int outputNextByte;
			//int bytesToRead = 0;
			
			/// Send via a second thread?
			// Thread outputStreamClientResponse_thread = new Thread(
			// 	new Runnable(){
			// 		public void run(){
			// 			int b;
			// 			try {
			// 				while ( ( b = bufferedInputStream.read() ) != -1 ) {
			// 					outputStreamClientResponse.write(b);
			// 					outputStreamClientResponse.flush();
			// 				}
			// 			} catch(Exception e) {
			// 				// INTENTIONALLY SILENCED, to handle socket close events
			// 				//throw new RuntimeException(e);
			// 			}
			// 		}
			// 	}
			// );
			// outputStreamClientResponse_thread.start();
			// 	
			// while( (outputStreamClientResponse_thread != null && outputStreamClientResponse_thread.isAlive()) || 
			//        (socketInputStream_thread != null && socketInputStream_thread.isAlive()) ) {
			// 	Thread.sleep(1);
			// }
			
			// uses blocking call instead?
			while ( ( outputNextByte = bufferedInputStream.read() ) != -1 ) {
				outputStreamClientResponse.write(outputNextByte);
				outputStreamClientResponse.flush();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	///////////////////////////////////////////////////////////
	// Core page overrides
	///////////////////////////////////////////////////////////

	/// Performs an output request, with special handling of POST / PUT
	@Override
	public boolean outputRequest(Map<String,Object> templateData, PrintWriter output) throws Exception {
		return proxyCorePageRequest(this);
	}
	
	///////////////////////////////////////////////////////////
	// Upload data handling
	///////////////////////////////////////////////////////////

	protected void handleStandardPost( HttpEntityEnclosingRequestBase postMethodProxyRequest, HttpServletRequest httpServletRequest) throws Exception {
		try {
			// Get the client POST data as a Map
			Map<String, String[]> mapPostParameters = httpServletRequest.getParameterMap();

			// Create a List to hold the NameValuePairs to be passed to the PostMethod
			List<NameValuePair> listNameValuePairs = RequestHttpUtils.parameterMapToList(mapPostParameters);

			// Set the proxy request POST data 
			postMethodProxyRequest.setEntity( new UrlEncodedFormEntity(listNameValuePairs) ); // listNameValuePairs.toArray(new NameValuePair[] { }) ??
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleMultipartPost( HttpEntityEnclosingRequestBase postMethodProxyRequest, HttpServletRequest httpServletRequest) throws Exception {
		// Create a factory for disk-based file items
		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		// Set factory constraints
		diskFileItemFactory.setSizeThreshold(getMaxFileUploadSize());
		diskFileItemFactory.setRepository(UPLOAD_TEMP_DIRECTORY);
		// Create a new file upload handler
		ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
		// Parse the request
		try {
			// Get the multipart items as a list
			List<FileItem> listFileItems = servletFileUpload.parseRequest(httpServletRequest);
			// Create a list to hold all of the parts
			MultipartEntityBuilder multipartRequestEntity = MultipartEntityBuilder.create();
			
			// Iterate the multipart items list
			for(FileItem fileItemCurrent : listFileItems) {
				
				// If the current item is a form field, then create a string part
				if (fileItemCurrent.isFormField()) {
					multipartRequestEntity.addTextBody(
						fileItemCurrent.getFieldName(), // The field name
						fileItemCurrent.getString()     // The field value
					);
				} else {
					// The item is a file upload, so we create a FilePart
					multipartRequestEntity.addBinaryBody(
						fileItemCurrent.getFieldName(),    // The field name
						fileItemCurrent.getInputStream()   //The file itself
					);
				}
			}
			
			HttpEntity multipartRequestEntity_final = multipartRequestEntity.build();
			
			postMethodProxyRequest.setEntity(multipartRequestEntity_final);
			
			// The current content-type header (received from the client) IS of
			// type "multipart/form-data", but the content-type header also
			// contains the chunk boundary string of the chunks. Currently, this
			// header is using the boundary of the client request, since we
			// blindly copied all headers from the client request to the proxy
			// request. However, we are creating a new request with a new chunk
			// boundary string, so it is necessary that we re-set the
			// content-type string to reflect the new chunk boundary string
			postMethodProxyRequest.setHeader(CONTENT_TYPE_HEADER_NAME, multipartRequestEntity_final.getContentType().getValue() );
			
		} catch (Exception fileUploadException) {
			throw new RuntimeException(fileUploadException);
		}
	}
	// */
}
