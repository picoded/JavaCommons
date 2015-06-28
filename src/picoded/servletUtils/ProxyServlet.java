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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.servlet.*;
import picoded.webUtils.*;

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
	
	///////////////////////////////////////////////////////////
	// Proxy host params PUT/GET
	///////////////////////////////////////////////////////////

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
	// Utility functions for Request / Response
	///////////////////////////////////////////////////////////

	/// Gets and returns the target proxy URL given the httpServletReqeust
	protected String getProxyURL(HttpServletRequest httpServletRequest) {
		// Set the protocol to HTTP
		String stringProxyURL = "http://" + getProxyHostAndPort();
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
	
	///////////////////////////////////////////////////////////
	// Loading of proxy host config values from web.xml (if applicable)
	///////////////////////////////////////////////////////////

	/// Initialize the <code>ProxyServlet</code>
	/// @param servletConfig The Servlet configuration passed in by the servlet conatiner
	@Override
	public void initSetup( CorePage original, ServletConfig servletConfig) throws ServletException {
		super.initSetup(original, servletConfig);
		
		ProxyServlet ori = (ProxyServlet)original;
		
		// Load original values?
		proxyHost = ori.proxyHost;
		proxyPort = ori.proxyPort;
		proxyPath = ori.proxyPath;
		maxFileUploadSize = ori.maxFileUploadSize;
		
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
	
	/// Takes a servelt request, and populate the proxy request parameters / headers
	@SuppressWarnings("unchecked")
	protected void setProxyRequestHeaders(HttpServletRequest httpServletRequest, HttpUriRequest httpMethodProxyRequest) {
		// Get an Enumeration of all of the header names sent by the client
		Enumeration<String> enumerationOfHeaderNames = httpServletRequest.getHeaderNames();
		while(enumerationOfHeaderNames.hasMoreElements()) {
			
			String stringHeaderName = enumerationOfHeaderNames.nextElement();
			if(stringHeaderName.equalsIgnoreCase(CONTENT_LENGTH_HEADER_NAME)) {
				continue;
			}
				
			// As per the Java Servlet API 2.5 documentation:
			//        Some headers, such as Accept-Language can be sent by clients
			//        as several headers each with a different value rather than
			//        sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			Enumeration<String> enumerationOfHeaderValues = httpServletRequest.getHeaders(stringHeaderName);
			while(enumerationOfHeaderValues.hasMoreElements()) {
				String stringHeaderValue = enumerationOfHeaderValues.nextElement();
				//	In case the proxy host is running multiple virtual servers,
				// rewrite the Host header to ensure that we get content from
				// the correct virtual server
				if(stringHeaderName.equalsIgnoreCase(HOST_HEADER_NAME)){
  					stringHeaderValue = getProxyHostAndPort();
				}
				
				// Set the same header on the proxy request
				httpMethodProxyRequest.setHeader(stringHeaderName, stringHeaderValue);
			}
		}
	}
	
	protected void executeProxyRequest( HttpUriRequest httpMethodProxyRequest, 
		HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
		
		try {
			
			// Create a default HttpClient with Disabled automated stuff
			HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().disableAuthCaching().build();
			
			HttpResponse response = httpClient.execute(httpMethodProxyRequest);
			
			StatusLine statusLine = response.getStatusLine();
			int intProxyResponseCode = statusLine.getStatusCode();
			
			// Check if the proxy response is a redirect
			// The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
			// Hooray for open source software
			if (intProxyResponseCode >= HttpServletResponse.SC_MULTIPLE_CHOICES // 300 
			&& intProxyResponseCode < HttpServletResponse.SC_NOT_MODIFIED //304
			) {
				String stringStatusCode = Integer.toString(intProxyResponseCode);
				String stringLocation = response.getFirstHeader(LOCATION_HEADER).getValue();
				
				if(stringLocation == null) {
					throw new ServletException("Recieved status code: " + stringStatusCode 
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
				httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			// Pass the response code back to the client
			httpServletResponse.setStatus(intProxyResponseCode);

			// Pass response headers back to the client
			Header[] headerArrayResponse = response.getAllHeaders();
			for(Header header : headerArrayResponse) {
				httpServletResponse.setHeader(header.getName(), header.getValue());
			}
			
			// Send the content to the client
			InputStream inputStreamProxyResponse = response.getEntity().getContent();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
			OutputStream outputStreamClientResponse = httpServletResponse.getOutputStream();
			
			int intNextByte;
			while ( ( intNextByte = bufferedInputStream.read() ) != -1 ) {
				outputStreamClientResponse.write(intNextByte);
			}
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
	}
	
	///////////////////////////////////////////////////////////
	// Core page overrides
	///////////////////////////////////////////////////////////

	/// Performs an output request, with special handling of POST / PUT
	@Override
	public boolean outputRequest(Map<String,Object> templateData, PrintWriter output) throws ServletException {
		return proxyCorePageRequest(this);
	}
	
	/// Performs a proxy redirect using the given CorePage instance
	public boolean proxyCorePageRequest(CorePage page) throws ServletException {
		
		HttpRequestType rType = page.getHttpRequestType();
		HttpServletRequest sReq = page.getHttpServletRequest();
		HttpServletResponse sRes = page.getHttpServletResponse();
		
		// Create the respective request URL based on requestType and URL
		HttpUriRequest methodToProxyRequest = RequestHttpUtils.apache_HttpUriRequest_fromRequestType(rType, getProxyURL(sReq));
		
		// Forward the request headers
		setProxyRequestHeaders(sReq, methodToProxyRequest);
		
		// Handles post or put
		if( rType == HttpRequestType.TYPE_POST || rType == HttpRequestType.TYPE_PUT ) {
			//if(ServletFileUpload.isMultipartContent( sReq )) {
			//	handleMultipartPost( (HttpEntityEnclosingRequestBase)methodToProxyRequest, sReq);
			//} else {
			//	handleStandardPost( (HttpEntityEnclosingRequestBase)methodToProxyRequest, sReq);
			//}
			
			String reqLength = sReq.getHeader(CONTENT_LENGTH_HEADER_NAME);
			
			if( reqLength != null && !(reqLength.equals("0")) ) {
				// Gets as raw input stream, and passes it
				try {
					((HttpEntityEnclosingRequestBase)methodToProxyRequest).setEntity( new InputStreamEntity(sReq.getInputStream()) );
				} catch(IOException e) {
					throw new ServletException(e);
				}
			}
		}
		
		// Execute the proxy request
		executeProxyRequest(methodToProxyRequest, sReq, sRes);
		return true;
	}
	
	///////////////////////////////////////////////////////////
	// Upload data handling
	///////////////////////////////////////////////////////////

	protected void handleStandardPost( HttpEntityEnclosingRequestBase postMethodProxyRequest, HttpServletRequest httpServletRequest) throws ServletException {
		try {
			// Get the client POST data as a Map
			Map<String, String[]> mapPostParameters = httpServletRequest.getParameterMap();

			// Create a List to hold the NameValuePairs to be passed to the PostMethod
			List<NameValuePair> listNameValuePairs = RequestHttpUtils.parameterMapToList(mapPostParameters);

			// Set the proxy request POST data 
			postMethodProxyRequest.setEntity( new UrlEncodedFormEntity(listNameValuePairs) ); // listNameValuePairs.toArray(new NameValuePair[] { }) ??
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleMultipartPost( HttpEntityEnclosingRequestBase postMethodProxyRequest, HttpServletRequest httpServletRequest) throws ServletException {
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
			throw new ServletException(fileUploadException);
		}
	}
	
}
