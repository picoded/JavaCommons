package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

// Exceptions used
import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.util.*;
import java.lang.reflect.*;

// Objects used
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

/**
 * picoded.servlet.RESTBuilder is a servlet utility class, in which facilitates the building of "RESTful API's"
 * that can be used in the project either via a public API, or even internally, via a direct function call.
 *
 * The main role of RESTBuilder, is to facilitate the packaging of all the web project core functionalities,
 * into a single API Framework. Where it can be called directly.
 *
 * While, it replaces the original servlet framework role of creating JSON output pages. The framework is also meant to
 * facilitate intra project function calls. One of the examples learnt from the LMS project, in handling the encryption
 * of export files (for example). Is that instead of rewriting the entire export module code to be "callable" by a function
 * a rather indirect and "inefficent" method of calling its local page directly was used. Aka an encryption proxy.
 *
 * Simply put, if a standardised REST API builder was built and used, several pages API features can be called directly
 * instead of being usued via a proxy
 *
 * STATUS: PROOF OF CONCEPT, for method adding, and direct calling
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
 * // Function to add to API
 * static public String helloWorld(String v) {
 *		return "hello "+(v != null)? v : "no one";
 * }
 * 
 * ....
 *
 * // Setting up a method in the RESTBuilder
 * RESTBuilder r = new RESTBuilder()
 * r.apiMethod("test.hello").setGET( this, "helloWorld" );
 *
 * // Calling the api method
 * r.apiMethod("test.hello").GET("world"); // "hello world"
 * r.apiMethod("test.hello").GET(); // "hello no one"
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 **/
public class RESTBuilder extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// Blank constructor
	public RESTBuilder() {
	}
	
	///----------------------------------------
	/// Method adding / handling
	///----------------------------------------
	
	/// Stores the various methods currently in place of the RESTBuilder
	protected HashMap<String, RESTMethod> methodMap = new HashMap<String, RESTMethod>();
	
	/// Gets the api RESTMethod, to add the respetive GET/PUT/POST/DELETE calls
	public RESTMethod apiMethod(String namespace) {
		return apiMethod(namespace.replaceAll(".", "/").split("/"));
	}
	
	/// Gets the api RESTMethod, to add the respetive GET/PUT/POST/DELETE calls
	public RESTMethod apiMethod(String[] namespace) {
		String storeStr = StringUtils.join(namespace, "/");
		
		RESTMethod m = methodMap.get(storeStr);
		if (m == null) {
			m = new RESTMethod(storeStr);
			methodMap.put(storeStr, m);
		}
		return m;
	}
	
}