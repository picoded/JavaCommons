package picoded.servlet.api;

import java.util.*;

import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

///
/// API Request map information
/// For the API function to process
///
public class ApiRequest /*implements GenericConvertMap<String, Object>*/ {

	//-----------------------------------------------------------------
	//
	//  Constructor vars
	//
	//-----------------------------------------------------------------

	/// The query and context object 
	protected GenericConvertMap<String,Object> queryObj = new GenericConvertHashMap<String,Object> ();

	/// The context object to use
	protected GenericConvertMap<String,Object> contextObj = new GenericConvertHashMap<String,Object> ();

	/// The base API builder
	protected ApiBuilder builder = null;

	//-----------------------------------------------------------------
	//
	//  Overwrites vars (that would have taken from builder instead)
	//
	//-----------------------------------------------------------------

	/// Overwrite the request type, to be a certain type, such as "JAVA"
	protected String requestMethod = null;

	//-----------------------------------------------------------------
	//
	//  Constructor
	//
	//-----------------------------------------------------------------

	/// Initialize the class with query and context object
	///
	/// @param   Parent ApiBuilder
	/// @param   Query map to assume
	/// @param   Context map to assume
	ApiRequest( ApiBuilder parent, Map<String,Object> query, Map<String,Object> context ) {
		// Setup parent API builder object
		builder = parent;
		// Query and context setup
		if( query != null ) {
			queryObj.putAll(query);
		}
		if( context != null ) {
			contextObj.putAll(context);
		}
	}

	/// Initialize the class
	///
	/// @param   Parent ApiBuilder
	ApiRequest( ApiBuilder parent ) {
		// Setup parent API builder object
		builder = parent;
	}

	//-----------------------------------------------------------------
	//
	//  Parameter getters
	//
	//-----------------------------------------------------------------

	/// @return  query parameters map
	public GenericConvertMap<String,Object> query() {
		return queryObj;
	}

	/// @return  context aprameter map
	public GenericConvertMap<String,Object> context() {
		return contextObj;
	}

	/*
	Request
	Request information and functionality is provided by the request parameter:
	request.attributes();             // the attributes list
	request.attribute("foo");         // value of foo attribute
	request.attribute("A", "V");      // sets value of attribute A to V
	request.body();                   // request body sent by the client
	request.bodyAsBytes();            // request body as bytes
	request.contentLength();          // length of request body
	request.contentType();            // content type of request.body
	request.contextPath();            // the context path, e.g. "/hello"
	request.cookies();                // request cookies sent by the client
	request.headers();                // the HTTP header list
	request.headers("BAR");           // value of BAR header
	request.host();                   // the host, e.g. "example.com"
	request.ip();                     // client IP address
	request.params("foo");            // value of foo path parameter
	request.params();                 // map with all parameters
	request.pathInfo();               // the path info
	request.port();                   // the server port
	request.protocol();               // the protocol, e.g. HTTP/1.1
	request.queryMap();               // the query map
	request.queryMap("foo");          // query map for a certain parameter
	request.queryParams();            // the query param list
	request.queryParams("FOO");       // value of FOO query param
	request.queryParamsValues("FOO")  // all values of FOO query param
	request.raw();                    // raw request handed in by Jetty
	request.requestMethod();          // The HTTP method (GET, ..etc)
	request.scheme();                 // "http"
	request.servletPath();            // the servlet path, e.g. /result.jsp
	request.session();                // session management
	request.splat();                  // splat (*) parameters
	request.uri();                    // the uri, e.g. "http://example.com/foo"
	request.userAgent();              // user agentCopy
	Response
	Response information and functionality is provided by the response parameter:
	response.body();               // get response content
	response.body("Hello");        // sets content to Hello
	response.header("FOO", "bar"); // sets header FOO with value bar
	response.raw();                // raw response handed in by Jetty
	response.redirect("/example"); // browser redirect to /example
	response.status();             // get the response status
	response.status(401);          // set status code to 401
	response.type();               // get the content type
	response.type("text/xml");     // set content type to text/xmlCopy
	Query Maps
	Query maps allows you to group parameters to a map by their prefix. This allows you to group two parameters like user[name] and user[age] to a user map.
	request.queryMap().get("user", "name").value();
	request.queryMap().get("user").get("name").value();
	request.queryMap("user").get("age").integerValue();
	request.queryMap("user").toMap();Copy
	Cookies
	request.cookies();                         // get map of all request cookies
	request.cookie("foo");                     // access request cookie by name
	response.cookie("foo", "bar");             // set cookie with a value
	response.cookie("foo", "bar", 3600);       // set cookie with a max-age
	response.cookie("foo", "bar", 3600, true); // secure cookie
	response.removeCookie("foo");              // remove cookieCopy
	*/
}
