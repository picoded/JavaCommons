package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Exceptions used
import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;

// Objects used
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.servlet.http.Cookie;

import picoded.conv.ConvertJSON;
import picoded.webUtils.HttpRequestType;

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
 *
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
	
	/// GET type indicator
//	static final byte TYPE_GET = 1;
//	
//	/// POST type indicator
//	static final byte TYPE_POST = 2;
//	
//	/// PUT type indicator
//	static final byte TYPE_PUT = 3;
//	
//	/// DELETE type indicator
//	static final byte TYPE_DELETE = 4;
//	
//	/// OPTION type indicator
//	static final byte TYPE_OPTION = 5;
	
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
	public RequestMap requestParameters = null;
	
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
	
	// Independent instance variables
	//-------------------------------------------
	
	/// The header map, this is ignored when httpResponse parameter is already set
	protected Map<String,String> responseHeaderMap = null;
	
	/// The cookie map, this is ignored when httpResponse parameter is already set
	protected Map<String,Cookie> responseCookieMap = null;
	
	/// The cookie map, this is ignored when httpResponse parameter is already set
	protected Map<String,String> requestHeaderMap = null;
	
	/// The cookie map, this is ignored when httpResponse parameter is already set
	protected Map<String,Cookie> requestCookieMap = null;
	
	/// local output stream, used for internal execution / testing
	protected ByteArrayOutputStream cachedResponseOutputStream = null;
	
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
	
	
	// Request type config getters
	//-------------------------------------------
	
	/// Returns the request type
	public byte requestType() {
		return requestType.value();
	}
	
	/// Returns if the request is GET
	public boolean isGET() {
		return (requestType == HttpRequestType.TYPE_GET);
	}
	
	/// Returns if the request is POST
	public boolean isPOST() {
		return (requestType == HttpRequestType.TYPE_POST);
	}
	
	/// Returns if the request is PUT
	public boolean isPUT() {
		return (requestType == HttpRequestType.TYPE_PUT);
	}
	
	/// Returns if the request is DELETE
	public boolean isDELETE() {
		return (requestType == HttpRequestType.TYPE_DELETE);
	}
	
	/// Returns if the request is OPTION
	public boolean isOPTION() {
		return (requestType == HttpRequestType.TYPE_OPTION);
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
	protected CorePage setupInstance(byte inRequestType, Map<String,String> reqParam) throws ServletException {
		requestType = HttpRequestType.getCorrectHttpRequestType(inRequestType);
		requestParameters = new RequestMap( reqParam );
		return this;
	}
	
	/// Setup the instance, with the request parameter, and cookie map
	protected CorePage setupInstance(byte inRequestType, Map<String,String> reqParam, Map<String,Cookie> reqCookieMap) throws ServletException {
		requestType = HttpRequestType.getCorrectHttpRequestType(inRequestType);
		requestParameters = new RequestMap( reqParam );
		requestCookieMap = reqCookieMap;
		return this;
	}
	
	/// Setup the instance, with http request & response
	protected CorePage setupInstance(byte inRequestType, HttpServletRequest req, HttpServletResponse res) throws ServletException {
		requestType = HttpRequestType.getCorrectHttpRequestType(inRequestType);
		httpRequest = req;
		httpResponse = res;
		
		requestParameters = RequestMap.fromStringArrayValueMap( httpRequest.getParameterMap() );
		
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
			CorePage ret = this.getClass().newInstance();
			ret.init( this.getServletConfig() );
			return ret;
		} catch(Exception e) {
			throw new ServletException(e);
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
		if(requestParameters != null) {
			return requestParameters.get(paramName);
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
		
		if( responseHeaderMap == null ) {
			responseHeaderMap = new HashMap<String, String>();
		}
		responseHeaderMap.put("location", uri);
	}
	
	///////////////////////////////////////////////////////
	//
	// Process Chain execution
	//
	///////////////////////////////////////////////////////
	
	/// Triggers the process chain with the current setup, and indicates failure / success
	public boolean processChain() throws ServletException {
		boolean ret = true;
		
		// Does setup
		doSetup();
		
		// Does authentication check
		if( ret = doAuth(templateDataObj) ) {
			// is JSON request?
			if( isJsonRequest() ) {
				ret = processChainJSON();
			} else { // or as per normal
				ret = processChainRequest();
			}
		}
		
		// Does teardwon
		doTeardown();
		
		// Returns success or failure
		return ret;
	}
	
	/// The process chain part specific to a normal request
	private boolean processChainRequest() throws ServletException {
		if( !doRequest(templateDataObj) ) {
			return false;
		}
		
		boolean ret = true;
		
		// Switch is used over if,else for slight compiler optimization
		// http://stackoverflow.com/questions/6705955/why-switch-is-faster-than-if
		//
		//HttpRequestType reqTypeAsEnum = HttpRequestType(requestType);
		
		switch (requestType) {
			case TYPE_GET:
				ret = doGetRequest(templateDataObj);
				break;
			case TYPE_POST:
				ret = doPostRequest(templateDataObj);
				break;
			case TYPE_PUT:
				ret = doPutRequest(templateDataObj);
				break;
			case TYPE_DELETE:
				ret = doDeleteRequest(templateDataObj);
				break;
		}
		
		if(ret) {
			outputRequest(templateDataObj, getWriter());
		}
		
		return ret;
	}
	
	/// The process chain part specific to JSON request
	private boolean processChainJSON() throws ServletException {
		if( !doJSON(jsonDataObj, templateDataObj) ) {
			return false;
		}
		
		boolean ret = true;
		
		// Switch is used over if,else for slight compiler optimization
		// http://stackoverflow.com/questions/6705955/why-switch-is-faster-than-if
		//
		switch (requestType) {
			case TYPE_GET:
				ret = doGetJSON(jsonDataObj, templateDataObj);
				break;
			case TYPE_POST:
				ret = doPostJSON(jsonDataObj, templateDataObj);
				break;
			case TYPE_PUT:
				ret = doPutJSON(jsonDataObj, templateDataObj);
				break;
			case TYPE_DELETE:
				ret = doDeleteJSON(jsonDataObj, templateDataObj);
				break;
		}
		
		if(ret) {
			outputJSON(jsonDataObj, templateDataObj, getWriter());
		}
		
		return ret;
	}
	
	///////////////////////////////////////////////////////
	//
	// Process chains overwrites
	//
	///////////////////////////////////////////////////////
	
	/// [To be extended by sub class, if needed]
	/// Called once when initialized per request
	public void doSetup() throws ServletException {
		
	}
	
	/// [To be extended by sub class, if needed]
	/// Called once when completed per request, this is called regardless of request status
	/// PS: This is rarely needed, just rely on java GC =)
	public void doTeardown() throws ServletException {
		
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the needed page request authentication, page redirects (if needed), and so forth. Should not do any actual,
	/// output processing. Returns true to continue process chian (default) or false to terminate the process chain.
	public boolean doAuth(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	// HTTP request handling
	//-------------------------------------------
	
	/// [To be extended by sub class, if needed]
	/// Does the required page request processing, this is used if both post / get behaviour is consistent
	public boolean doRequest(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page GET processing, AFTER doRequest
	public boolean doGetRequest(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page POST processing, AFTER doRequest
	public boolean doPostRequest(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page PUT processing, AFTER doRequest
	public boolean doPutRequest(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page DELETE processing, AFTER doRequest
	public boolean doDeleteRequest(Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the output processing, this is after do(Post/Get/Put/Delete)Request
	public boolean outputRequest(Map<String,Object> templateData, PrintWriter output) throws ServletException {
		return true;
	}
	
	// JSON request handling
	//-------------------------------------------
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doGetJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doPostJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doPutJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doDeleteJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [Avoid Extending, this handles all the various headers and JSONP / CORS]
	/// Does the actual final json object to json string output, with contentType "application/javascript"
	public boolean outputJSON(Map<String,Object> outputData, Map<String,Object> templateData, PrintWriter output) throws ServletException {
		// Set content type to JSON
		if(httpResponse != null) {
			httpResponse.setContentType("application/javascript");
		}
		
		// Output the data
		output.println( ConvertJSON.fromObject( outputData ) );
		return true;
	}
	
	///////////////////////////////////////////////////////
	//
	// Native Servlet do overwrites [Avoid overwriting]
	//
	///////////////////////////////////////////////////////
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.TYPE_GET.value(), request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.TYPE_POST.value(), request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.TYPE_PUT.value(), request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.TYPE_DELETE.value(), request, response).processChain();
	}
	
	/// [Do not extend] Diverts the native doX to spawnInstance().setupInstance(TYPE,Req,Res).processChain()
	@Override
	public final void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance().setupInstance(HttpRequestType.TYPE_OPTION.value(), request, response).processChain();
		try {
				super.doOptions(request,response);
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}
	
}
