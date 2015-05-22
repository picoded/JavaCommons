package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Exceptions used
import javax.servlet.ServletException;
import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;

// Objects used
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

// Sub modules useds

/**
 * servletCommons.servlet pages base system, in which all additional pages are extended from.
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
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * initializePage ---\                                                     /--> doPostRequest --\
 *                   |                                                     |                    |
 * doPost -----------+--> processChain -+-> processRequest --> doRequest --+--> doGetRequest ---+--> doOutput
 *                   |                  |
 * doGet ------------+                  \-> processJson -----> doJson --------> doJsonOutput
 *                   |
 * doDelete ---------+
 *                   |
 * doPut ------------/
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##[TODO]
 *  + Accept a more valid range of "yes" indicator for JSON flag
 *  + Mockito tests
 *
 */
public class CorePage extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	// Common not so impt stuff
	//-------------------------------------------
	
	// Serialize version ID
	static final long serialVersionUID = 1L;
	
	// Static type variables declaration
	//-------------------------------------------
	
	/// GET type indicator
	static final byte TYPE_GET = 1;
	
	/// POST type indicator
	static final byte TYPE_POST = 2;
	
	/// PUT type indicator
	static final byte TYPE_PUT = 3;
	
	/// DELETE type indicator
	static final byte TYPE_DELETE = 4;
	
	// The loaded page configs
	//-------------------------------------------
	
	/// Request type indicator
	protected byte requestType = 0;
	
	// initialized config getters
	//-------------------------------------------
	
	/// Returns the request type
	public byte requestType() {
		return requestType;
	}
	
	/// Returns if the request is GET
	public boolean isGET() {
		return (requestType == TYPE_GET);
	}
	
	/// Returns if the request is GET
	public boolean isPOST() {
		return (requestType == TYPE_POST);
	}
	
	/// Returns if the request is GET
	public boolean isPUT() {
		return (requestType == TYPE_PUT);
	}
	
	/// Returns if the request is GET
	public boolean isDELETE() {
		return (requestType == TYPE_DELETE);
	}
	
	
	
	/*
	
	// initialized config getters
	//-------------------------------------------
	
	/// httpRequest used [modification of this value, is highly not suggested]
	protected HttpServletRequest httpRequest = null;
	
	/// httpResponse used [modification of this value, is highly not suggested]
	protected HttpServletResponse httpResponse = null;
	
	///
	protected Map<String,String> reqParam = null;
	
	///
	protected final CorePage spawnInstance(byte inRequestType, HttpServletRequest req, HttpServletResponse res) {
		httpRequest = req;
		httpResponse = res;
	}
	
	///
	protected final CorePage spawnInstance(byte inRequestType, Map<String,String> reqParam, OutputStream outStream ) {
		
	}
	*/
	
	/*
	
	////////////////////////////////
	// page initializing triggers //
	////////////////////////////////
	
	/// Spawns a copy of the current instance class. Not its corePage super class, but its current subclass
	private final corePage spawnInstance(HttpServletRequest req, HttpServletResponse res, boolean state) throws ServletException {
		try {
			corePage spawn = this.getClass().newInstance();
			spawn.initializePage(req, res, state);
			return spawn;
		} catch(InstantiationException e) {
			throw new ServletException(e);
		} catch(IllegalAccessException e) {
			throw new ServletException(e);
		}
	}
	
	boolean isInitialized = false;
	
	/// Initialize the servlet instance, note that repeated initializing will trigger an IllegalArgumentException
	/// This is automated via the private doGet / doPost if called directly as a servlet. However when initiated,
	/// normally as a class object. This function call is a requirment for majority of all the other function.
	///
	/// Note that this DOES NOT trigger the processChain() function, unlike doGet / doPost
	public void initializePage(HttpServletRequest request, HttpServletResponse response, boolean isPost) {
		//if(isInitialized) {
		doPurge();
		//throw new IllegalArgumentException("servlet page has already been initialized previously");
		//}
		
		if(request == null || response == null) {
			throw new IllegalArgumentException("provided HttpServletRequest/Response cannot be null");
		}
		httpRequest = request;
		httpResponse = response;
		isPostRequest = isPost;
		isInitialized = true;
	}
	
	/// Diverts the native doGet / doPost to the respective processing functions, initialize the page,
	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance(request, response, false).processChain();
	}
	
	/// Diverts the native doGet / doPost to the respective processing functions
	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		spawnInstance(request, response, true).processChain();
	}
	
	///return true if this instance has been initialized
	public boolean isInitialized() {
		return isInitialized;
	}
	
	/// Throws an RuntimeException if its not been initialized
	private final void throwIfNotInitialized() {
		if(!isInitialized) {
			throw new RuntimeException("servlet page has not been initialized. see initializePage()");
		}
	}
	
	////////////////////////////////////////
	// variables refences used internally //
	////////////////////////////////////////
	
	/// Boolean indicator for post request [modification of this value, is highly not suggested]
	public boolean isPostRequest = false;
	
	/// Boolean indicator for JSON request [modification of this value, is highly not suggested]
	public boolean isJsonRequest = false;
	
	/// The template data object wich is being passed around in each process stage
	public HashMap<String,Object> templateDataObj = new HashMap<String,Object>();
	
	/// The JSON output data object, if used in JSON processing mode
	public HashMap<String,Object> jsonDataObj = new HashMap<String,Object>();
	
	/////////////////////////////////////////////////////
	// Convienence functions : proxies acutal function //
	/////////////////////////////////////////////////////
	
	/// gets the OutputStream, from the httpResponse.getWriter() object and returns it
	/// also surpresses IOException, as RuntimeException
	public OutputStream getOutputStream() {
		throwIfNotInitialized();
		try {
			return httpResponse.getOutputStream();
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	/// gets the PrintWriter, from the httpResponse.getWriter() object and returns it
	/// also surpresses IOException, as RuntimeException
	public PrintWriter getWriter() {
		throwIfNotInitialized();
		return new PrintWriter( getOutputStream(), true );
	}
	
	/// gets a parameter value, from the httpRequest.getParameter
	public String getParameter(String paramName) {
		throwIfNotInitialized();
		return httpRequest.getParameter(paramName);
	}
	
	/// Returns the servlet contextual path : needed for base URI for page redirects / etc
	public String getContextPath() {
		//.throwIfNotInitialized();
		if(httpRequest == null) {
			try {
				return (URLDecoder.decode( this.getClass().getClassLoader().getResource("/").getPath(), "UTF-8" )).split("/WEB-INF/classes/")[0];
			} catch(UnsupportedEncodingException e) {
				return "../";
			} catch(NullPointerException e) {
				return "../";
			}
		}
		return httpRequest.getContextPath();
	}
	
	/// Proxies to httpResponse.sendRedirect
	public void sendRedirect(String uri) {
		throwIfNotInitialized();
		try {
			httpResponse.sendRedirect(uri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	////////////////////////////////////////
	// processChain managment
	////////////////////////////////////////
	
	/// Indicates true if this page processes json data
	public boolean hasJson = false;
	
	/// Indicate the parameter to check to "trigger json" processing,
	/// note that blank "" string or "*" wild card string, will process all request as JSON
	public String jsonFlag = "json";
	
	/// Does the logical switching between JSON mode processing, or REQUEST mode processing
	public boolean processChain() throws ServletException {
		boolean ret = false;
		if(this.hasJson) {
			// wild card mode
			if( this.jsonFlag == null || this.jsonFlag.length() <= 0 || this.jsonFlag.equals("*") ) {
				isJsonRequest = true;
				ret = processJson();
				doPurge();
				return ret;
			}
			
			String jValue = getParameter( this.jsonFlag );
			if(jValue != null && jValue.length() > 0) {
				isJsonRequest = true;
				// TODO: Accept a more valid range of chars (lower case), only required to check the first character,
				// for the 1/y/t. Regardless of the actual parameter length
				//if( jValue.equals("1") || jValue.equals("Y") || jValue.equals("T") ) {
				ret =  processJson();
				doPurge();
				return ret;
				//}
			}
		}
		
		//process as per normal
		ret = processRequest();
		doPurge();
		return ret;
	}
	
	/// Triggers the processRequest chain, this can be over-ridden (but not recommended)
	/// for highly customized process sequences of authentication, request and finally output.
	public boolean processRequest() throws ServletException {
		//does authentication step
		if( !doAuth(templateDataObj) ) {
			return false;
		}
		
		//does request / postRequest / getRequest chain
		if( !doRequest(templateDataObj) ) {
			return false;
		}
		
		if( isPostRequest ) {
			if( !doPostRequest(templateDataObj) ) {
				return false;
			}
		} else {
			if( !doGetRequest(templateDataObj) ) {
				return false;
			}
		}
		
		//does the output
		return doOutput(templateDataObj, getWriter());
	}
	
	public boolean processJson() throws ServletException {
		//does authentication step
		if( !doAuth(templateDataObj) ) {
			return false;
		}
		
		if( !doJson(jsonDataObj, templateDataObj) ) {
			return false;
		}
		
		return doJsonOutput(jsonDataObj, templateDataObj, getWriter());
	}
	
	
	////////////////////////////////////////
	// the various do "step" chain
	////////////////////////////////////////
	
	/// [To be extended by sub class, if needed]
	/// Called once when initialized, to purge all existing data.
	public void doPurge() {
		isInitialized = false;
		httpRequest = null;
		httpResponse = null;
		
		isPostRequest = false;
		isJsonRequest = false;
		
		templateDataObj = new HashMap<String,Object>();
		jsonDataObj = new HashMap<String,Object>();
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the needed page request authentication, page redirects (if needed), and so forth. Should not do any actual,
	/// output processing. Returns true to continue process chian (default) or false to terminate the process chain.
	public boolean doAuth(HashMap<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page request processing, this is used if both post / get behaviour is consistent
	public boolean doRequest(HashMap<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page POST processing, AFTER doRequest
	public boolean doPostRequest(HashMap<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the required page GET processing, AFTER doRequest
	public boolean doGetRequest(HashMap<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [To be extended by sub class, if needed]
	/// Does the output processing, this is after do(Post/Get)Request
	public boolean doOutput(HashMap<String,Object> templateData, PrintWriter output) throws ServletException {
		return true;
	}
	
	////////////////////////////////////////
	// the various JSON API related chain
	////////////////////////////////////////
	
	/// [To be extended by sub class, if needed]
	/// Does the JSON request processing, and outputs a JSON object
	public boolean doJson(HashMap<String,Object> outputData, HashMap<String,Object> templateData) throws ServletException {
		return true;
	}
	
	/// [Avoid Extending]
	/// Does the actual final json object to json string output, with contentType "application/json"
	public boolean doJsonOutput(HashMap<String,Object> outputData, HashMap<String,Object> templateData, PrintWriter output) throws ServletException {
		
		// Set content type to JSON
		httpResponse.setContentType("application/json");
		
		// Output the data
		output.println( json.toJson( outputData ) );
		
		return true;
	}
	*/
}