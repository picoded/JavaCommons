package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.*;

// Exceptions used
import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;

// Objects used
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import picoded.conv.ConvertJSON;
import picoded.enums.HttpRequestType;
import picoded.struct.HashMapList;

// Sub modules useds

/**
 * servletCommons.servlet pages core system, in which all additional pages are extended from.
 * In addition, this is intentionally structured to be "usable" even without the understanding / importing of
 * the various HttpServlet functionalities. Though doing so is still highly recommended.
 *
 * corePages, and its sub pages is designed to facilitate rapid servlet pages creation, and extension across
 * 3 distinct processing layers/roles : Authentication, Data, and output.
 *
 * in addition, it has built in mechanism to facilitate the handling of JSON data request
 *
 * Note that internally, doPost, doGet creates a new class instance for each call/request it recieves.
 * As such, all subclass built can consider all servlet instances are fresh instances on process request.
 * If however, the usage is countrary. doPurge is called on each additonal initilizing step. This may
 * occur due to servlet class used for other "purposes" beyond the intended get/post handling.
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##Process flow
 * <pre>
 * {@code
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * spawnInstance ----\
 *                   |
 * doOption ---------+
 *                   |
 * doPost -----------+--> processChain --> doAuth -+-> doRequest --> do_X_Request --> doOutput
 *                   |         |                   |
 * doGet ------------+         V                   \-> doJson -----> do_X_Json -----> doJsonOutput
 *                   |      doSetup
 * doDelete ---------+         |
 *                   |         V
 * doPut ------------/     doTeardown
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * }
 * </pre>
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##[TODO]
 * + Websocket support?
 */
public class CorePage extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	///////////////////////////////////////////////////////
	//
	// Static variables
	//
	///////////////////////////////////////////////////////
	
	// Common not so impt stuff
	//-------------------------------------------
	
	// Serialize version ID
	static final long serialVersionUID = 1L;
	
	// Static type variables declaration
	//-------------------------------------------
	
	/// RESTBuilder HttpRequestType enum access
	public static class RequestTypeSet extends picoded.enums.HttpRequestType.HttpRequestTypeSet {};
	
	///////////////////////////////////////////////////////
	//
	// Instance variables
	//
	///////////////////////////////////////////////////////
	
	// Instance variables
	//-------------------------------------------
	
	/// Request type indicator
	//protected byte requestType = 0;
	protected HttpRequestType requestType = null;
	
	/// The actual output stream used
	protected OutputStream responseOutputStream = null;
	
	/// parameter map, either initialized from httpRequest, or directly
	protected RequestMap requestParameters = null;
	
	/// The template data object wich is being passed around in each process stage
	protected Map<String,Object> templateDataObj = new HashMap<String,Object>();
	
	/// The JSON output data object, if used in JSON processing mode
	protected Map<String,Object> jsonDataObj = new HashMap<String,Object>();
	
	// Servlet specific variables
	//-------------------------------------------
	
	/// httpRequest used [modification of this value, is highly discouraged]
	protected HttpServletRequest httpRequest = null;
	
	/// httpResponse used [modification of this value, is highly discouraged]
	protected HttpServletResponse httpResponse = null;
	
	public HttpServletRequest getHttpServletRequest() {
		return httpRequest;
	}
	
	public HttpServletResponse getHttpServletResponse() {
		return httpResponse;
	}
	
	// Independent instance variables
	//-------------------------------------------
	
	// /// The header map, this is ignored when httpResponse parameter is already set
	// protected Map<String,String> responseHeaderMap = null;
	// 
	// /// The cookie map, this is ignored when httpResponse parameter is already set
	// protected Map<String,Cookie> responseCookieMap = null;
	// 
	// /// local output stream, used for internal execution / testing
	// protected ByteArrayOutputStream cachedResponseOutputStream = null;
	
	/// The requested headers map, either set at startup or extracted from httpRequest
	protected Map<String,String[]> _requestHeaderMap = null;
	
	/// Gets and returns the requestHeaderMap
	public Map<String,String[]> requestHeaderMap() {
		// gets the constructor set cookies / cached cookies
		if( _requestHeaderMap != null ) {
			return _requestHeaderMap;
		}
		
		// if the cached copy not previously set, and request is null, nothing can be done
		if( httpRequest == null ) {
			return null;
		}
		
		// Creates the _requestHeaderMap from httpRequest 
		HashMapList<String, String> mapList = new HashMapList<String, String>();
		
		// Get an Enumeration of all of the header names sent by the client
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			
			// As per the Java Servlet API 2.5 documentation:
			//        Some headers, such as Accept-Language can be sent by clients
			//        as several headers each with a different value rather than
			//        sending the header as a comma separated list.
			// Thus, we get an Enumeration of the header values sent by the client
			mapList.append( name, httpRequest.getHeaders(name) );
		}
		
		return ( _requestHeaderMap = mapList.toMapArray(new String[0]) );
	}
	
	/// The requested cookie map, either set at startup or extracted from httpRequest
	protected Map<String,String[]> _requestCookieMap = null;
	
	/// Gets and returns the requestCookieMap
	public Map<String,String[]> requestCookieMap() {
		// gets the constructor set cookies / cached cookies
		if( _requestCookieMap != null ) {
			return _requestCookieMap;
		}
		
		// if the cached copy not previously set, and request is null, nothing can be done
		if( httpRequest == null ) {
			return null;
		}
		
		// Creates the _requestCookieMap from httpRequest 
		HashMapList<String, String> mapList = new HashMapList<String, String>();
		for( Cookie oneCookie : httpRequest.getCookies() ) {
			mapList.append( oneCookie.getName(), oneCookie.getValue() );
		}
		
		// Cache and return
		return ( _requestCookieMap = mapList.toMapArray(new String[0]) );
	}
	
	///////////////////////////////////////////////////////
	//
	// Instance config
	//
	///////////////////////////////////////////////////////
	
	// JSON request config handling
	//-------------------------------------------
	
	/// Sets the JSON detection flag
	protected String jsonRequestFlag = null;
	
	/// Sets the JSON request flag, used to handle JSON requests.
	/// Note that NULL, means disabled. While "*" means anything
	public CorePage setJsonRequestFlag( String in ) {
		jsonRequestFlag = in;
		return this;
	}
	
	/// Gets the current JSON request flag
	public String getJsonRequestFlag() {
		return jsonRequestFlag;
	}
	
	/// Returns true / false if current request qualifies as JSON
	/// Note this is used internally by the process chain
	public boolean isJsonRequest() {
		if( jsonRequestFlag != null ) {
			if( jsonRequestFlag.equals("*") ) {
				return true;
			}
			
			String rStr = getParameter(jsonRequestFlag);
			if( rStr != null && rStr.length() > 0 ) {
				return true;
			}
		}
		return false;
	}
	
	// CORS config handling
	// @TODO CORS OPTION implementation
	//-------------------------------------------
	
	// HTTP Servlet convinence functions
	//-------------------------------------------
	
	/// Gets the server requestURI
	public String requestURI() {
		return httpRequest.getRequestURI();
	}
	
	/// Gets the request servlet path
	public String requestServletPath() {
		return httpRequest.getServletPath();
	}
	
	/// Gets the serer wildcard segment of the URI
	public String requestWildcardUri() {
		String servletPath = requestServletPath();
		String reqURI = requestURI();
		
		while(servletPath.endsWith("*") || servletPath.endsWith("/")) {
			servletPath = servletPath.substring(0, servletPath.length() - 1);
		}
		
		if( reqURI.startsWith(servletPath) ) {
			return reqURI.substring(servletPath.length());
		}
		return "";
	}
	
	// Request type config getters
	//-------------------------------------------
	
	/// Returns the request type
	public HttpRequestType requestType() {
		return requestType;
	}
	
	/// Returns the request parameters
	public RequestMap requestParameters() {
		if( requestParameters != null ) {
			return requestParameters;
		}
		
		requestParameters = RequestMap.fromStringArrayValueMap( httpRequest.getParameterMap() );
		
		return requestParameters;
	}
	
	/// Returns if the request is GET
	public boolean isGET() {
		return (requestType == HttpRequestType.GET);
	}
	
	/// Returns if the request is POST
	public boolean isPOST() {
		return (requestType == HttpRequestType.POST);
	}
	
	/// Returns if the request is PUT
	public boolean isPUT() {
		return (requestType == HttpRequestType.PUT);
	}
	
	/// Returns if the request is DELETE
	public boolean isDELETE() {
		return (requestType == HttpRequestType.DELETE);
	}
	
	/// Returns if the request is OPTION
	public boolean isOPTION() {
		return (requestType == HttpRequestType.OPTION);
	}
	
	///////////////////////////////////////////////////////
	//
	// Constructor, setup and instance spawn
	//
	///////////////////////////////////////////////////////
	
	/// Blank constructor, used for template building, unit testing, etc
	public CorePage() {
		super();
	}
	
	/// Setup the instance, with the request parameter, and
	protected CorePage setupInstance(HttpRequestType inRequestType, Map<String,String[]> reqParam) throws ServletException {
		requestType = inRequestType;
		//requestParameters = new RequestMap( reqParam );
		return this;
	}
	
	/// Setup the instance, with the request parameter, and cookie map
	protected CorePage setupInstance(HttpRequestType inRequestType, Map<String,String[]> reqParam, Map<String,Cookie[]> reqCookieMap) throws ServletException {
		requestType = inRequestType;
		//requestParameters = new RequestMap( reqParam );
		//requestCookieMap = reqCookieMap;
		return this;
	}
	
	/// Setup the instance, with http request & response
	protected CorePage setupInstance(HttpRequestType inRequestType, HttpServletRequest req, HttpServletResponse res) throws ServletException {
		requestType = inRequestType;
		httpRequest = req;
		httpResponse = res;
		
		// @TODO: To use IOUtils.buffer for inputstream of httpRequest / parameterMap
		// THIS IS CRITICAL, for the POST request in proxyServlet to work
		//requestParameters = RequestMap.fromStringArrayValueMap( httpRequest.getParameterMap() );
		
		try {
			responseOutputStream = httpResponse.getOutputStream();
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		return this;
	}
	
	/// Spawn and instance of the current class
	public final CorePage spawnInstance() throws ServletException { //, OutputStream outStream
		try {
			Class<? extends CorePage> pageClass = this.getClass();
			CorePage ret = pageClass.newInstance();
			pageClass.cast(ret).initSetup( this, this.getServletConfig() );
			
			return ret;
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}
	
	/// To be over-ridden
	public void initSetup( CorePage original, ServletConfig servletConfig ) {
		try {
			if( servletConfig != null ) {
				init( servletConfig );
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	///////////////////////////////////////////////////////
	//
	// Convinence functions
	//
	///////////////////////////////////////////////////////
	
	/// gets the PrintWriter, from the getOutputStream() object and returns it
	public PrintWriter getWriter() {
		return new PrintWriter( getOutputStream(), true );
	}
	
	/// gets the OutputStream, from the httpResponse.getOutputStream() object and returns it
	/// also surpresses IOException, as RuntimeException
	public OutputStream getOutputStream() {
		return responseOutputStream;
	}
	
	/// Returns the servlet contextual path : needed for base URI for page redirects / etc
	public String getContextPath() {
		if(httpRequest != null) {
			return httpRequest.getContextPath();
		}
		try {
			return (URLDecoder.decode( this.getClass().getClassLoader().getResource("/").getPath(), "UTF-8" )).split("/WEB-INF/classes/")[0];
		} catch(UnsupportedEncodingException e) {
			return "../";
		} catch(NullPointerException e) {
			return "../";
		}
	}
	
	/// gets a parameter value, from the httpRequest.getParameter
	public String getParameter(String paramName) {
		if(requestParameters() != null) {
			return requestParameters().get(paramName);
		}
		return null;
	}
	
	/// Proxies to httpResponse.sendRedirect,
	/// Fallsback to responseHeaderMap.location, if httpResponse is null
	public void sendRedirect(String uri) {
		if( httpResponse != null ) {
			try {
				httpResponse.sendRedirect(uri);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		
		// if( responseHeaderMap == null ) {
		// 	responseHeaderMap = new HashMap<String, String>();
		// }
		// responseHeaderMap.put("location", uri);
	}
	
	///////////////////////////////////////////////////////
	//
	// Process Chain execution
	//
	///////////////////////////////////////////////////////
	
	/// Triggers the process chain with the current setup, and indicates failure / success
	public boolean processChain() throws ServletException {
		try {
			try {
				boolean ret = true;
				
				// Does setup
				doSetup();
				
				// is JSON request?
				if( isJsonRequest() ) {
					ret = processChainJSON();
				} else { // or as per normal
					ret = processChainRequest();
				}
				
				// Flush any data if exists
				getWriter().flush();
				
				// Does teardwon
				doTeardown();
				
				// Returns success or failure
				return ret;
			} catch(Exception e) {
				doException(e);
				return false;
			}
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}
	
	/// The process chain part specific to a normal request
	private boolean processChainRequest() throws Exception {
		try {
			// Does authentication check
			if(!doAuth(templateDataObj) ) {
				return false;
			}
			
			// Does for all requests
			if( !doRequest(templateDataObj) ) {
				return false;
			}
			boolean ret = true;
			
			// Switch is used over if,else for slight compiler optimization
			// http://stackoverflow.com/questions/6705955/why-switch-is-faster-than-if
			//
			// HttpRequestType reqTypeAsEnum = HttpRequestType(requestType);
			switch (requestType) {
				case GET:
					ret = doGetRequest(templateDataObj);
					break;
				case POST:
					ret = doPostRequest(templateDataObj);
					break;
				case PUT:
					ret = doPutRequest(templateDataObj);
					break;
				case DELETE:
					ret = doDeleteRequest(templateDataObj);
					break;
			}
			
			if(ret) {
				outputRequest(templateDataObj, getWriter());
			}
			
			return ret;
		} catch(Exception e) {
			return outputRequestException(templateDataObj, getWriter(), e);
		}
	}
	
	/// The process chain part specific to JSON request
	private boolean processChainJSON() throws Exception {
		try {
			// Does authentication check
			if(!doAuth(templateDataObj) ) {
				return false;
			}
			
			// Does for all JSON
			if( !doJSON(jsonDataObj, templateDataObj) ) {
				return false;
			}
			
			boolean ret = true;
			
			// Switch is used over if,else for slight compiler optimization
			// http://stackoverflow.com/questions/6705955/why-switch-is-faster-than-if
			//
			switch (requestType) {
				case GET:
					ret = doGetJSON(jsonDataObj, templateDataObj);
					break;
				case POST:
					ret = doPostJSON(jsonDataObj, templateDataObj);
					break;
				case PUT:
					ret = doPutJSON(jsonDataObj, templateDataObj);
					break;
				case DELETE:
					ret = doDeleteJSON(jsonDataObj, templateDataObj);
					break;
			}
			
			if(ret) {
				outputJSON(jsonDataObj, templateDataObj, getWriter());
			}
			
			return ret;
		} catch(Exception e) {
			return outputJSONException(jsonDataObj, templateDataObj, getWriter(), e);
		}
	}
	
	///////////////////////////////////////////////////////
	//
	// Process chains overwrites
	//
	///////////////////////////////////////////////////////
	
	/// [To be extended by sub class, if needed]
	/// Called once when initialized per request
	public void doSetup() throws Exception {
		
	}
	
	/// [To be extended by sub class, if needed]
	/// Called once when completed per request, this is called regardless of request status
	/// PS: This is rarely needed, just rely on java GC =)
	public void doTeardown() throws Exception {
		
	}
	
	/// Handles setup and teardown exception
	public void doException(Exception e) throws Exception {
		throw e;
	}
	
	// HTTP request handling
	//-------------------------------------------
	
	/// [To be extended by sub class, if needed]
	/// Does the needed page request authentication, page redirects (if needed), and so forth. Should not do any actual,
	/// output processing. Returns true to continue process chian (default) or false to terminate the process chain.
	public boolean doAuth(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page request processing, this is used if both post / get behaviour is consistent
	public boolean doRequest(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page GET processing, AFTER doRequest
	public boolean doGetRequest(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page POST processing, AFTER doRequest
	public boolean doPostRequest(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page PUT processing, AFTER doRequest
	public boolean doPutRequest(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page DELETE processing, AFTER doRequest
	public boolean doDeleteRequest(Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the output processing, this is after do(Post/Get/Put/Delete)Request
	public boolean outputRequest(Map<String,Object> templateData, PrintWriter output) throws Exception {
		return true;
	}
	
	/// Exception handler for the request stack
	///
	/// note that this should return false, or throw a ServletException, UNLESS the exception was gracefully handled.
	/// which in most cases SHOULD NOT be handled here.
	public boolean outputRequestException(Map<String,Object> templateData, PrintWriter output, Exception e) throws Exception {
		// Throws a runtime Exception, let the servlet manager handle the rest
		throw e;
		//return false;
	}
	
	// JSON request handling
	//-------------------------------------------
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doGetJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doPostJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doPutJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doDeleteJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return true;
	}
	
	/// [Avoid Extending, this handles all the various headers and JSONP / CORS]
	/// Does the actual final json object to json string output, with contentType "application/javascript"
	public boolean outputJSON(Map<String,Object> outputData, Map<String,Object> templateData, PrintWriter output) throws Exception {
		// Set content type to JSON
		if(httpResponse != null) {
			httpResponse.setContentType("application/javascript");
		}
		
		// Output the data
		output.println( ConvertJSON.fromObject( outputData ) );
		return true;
	}
	
	/// Exception handler for the request stack
	///
	/// note that this should return false, UNLESS the exception was gracefully handled.
	/// which in most cases SHOULD NOT be handled here.
	public boolean outputJSONException(Map<String,Object> outputData, Map<String,Object> templateData, PrintWriter output, Exception e) throws Exception  {
		// Converts the stack trace to a string
		String stackTrace = org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
		
		// Performs a stack trace, and returns it in a JSON object
		Map<String,String> ret = new HashMap<String,String>();
		ret.put("ERROR", stackTrace);
		
		// Set content type to JSON
		if(httpResponse != null) {
			httpResponse.setContentType("application/javascript");
		}
		
		// Output the data
		output.println( ConvertJSON.fromObject( ret ) );
		return false;
	}
	
	///////////////////////////////////////////////////////
	//
	// Native Servlet do overwrites [Avoid overwriting]
	//
	///////////////////////////////////////////////////////
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.GET, request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.POST, request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.PUT, request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.DELETE, request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.OPTION, request, response).processChain();
		try {
				super.doOptions(request,response);
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}
	
}
