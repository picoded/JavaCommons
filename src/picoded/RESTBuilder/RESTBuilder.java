package picoded.RESTBuilder;

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

import picoded.enums.HttpRequestType;
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
 * r.getNamespace("test.hello").setGET( this, "helloWorld" );
 *
 * // Calling the api method
 * r.getNamespace("test.hello").GET("world"); // "hello world"
 * r.getNamespace("test.hello").GET(); // "hello no one"
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 **/
public class RESTBuilder {
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// RESTBuilder HttpRequestType enum access
	public static class RequestTypeSet extends picoded.enums.HttpRequestType.HttpRequestTypeSet {};
	
	/// Blank constructor
	public RESTBuilder() {
	}
	
	///----------------------------------------
	/// Namespace adding / handling
	///----------------------------------------
	
	/// Stores the various methods currently in place of the RESTBuilder
	protected Map<String, RESTNamespace> namespaceMap = new HashMap<String, RESTNamespace>();
	
	/// Has api namespace check
	public boolean hasNamespace(String namespace) {
		return hasNamespace(namespace.replaceAll(".", "/").split("/"));
	}
	
	/// Has api namespace check
	public boolean hasNamespace(String[] namespace) {
		String storeStr = StringUtils.join(namespace, "/"); //Filters the trailing GET paramters if its given
		return (namespaceMap.get(storeStr) != null);
	}
	
	/// Gets the api RESTNamespace, to add the respetive GET/PUT/POST/DELETE calls
	public RESTNamespace getNamespace(String namespace) {
		return getNamespace(namespace.replaceAll(".", "/").split("/"));
	}
	
	/// Gets the api RESTNamespace, to add the respetive GET/PUT/POST/DELETE calls. Note this GENERATES one if it does not exists
	public RESTNamespace getNamespace(String[] namespace) {
		String storeStr = StringUtils.join(namespace, "/"); //Filters the trailing GET paramters if its given
		
		RESTNamespace m = namespaceMap.get(storeStr);
		if (m == null) {
			m = new RESTNamespace(storeStr);
			namespaceMap.put(storeStr, m);
		}
		return m;
	}
	
	///----------------------------------------
	/// Utility function
	///----------------------------------------
	
	/// Does a search for the relevent api, and additional setup for RESTRequest, relvent to the found api. And perfroms the call
	protected Map<String,Object> setupAndCall(String apiNamespace, HttpRequestType requestType, RESTRequest req, Map<String,Object> res) {
		String[] raw_ns = apiNamespace.split("?")[0].split("/");
		String[] ns = raw_ns;
		
		// Auto create result map if needed
		if( res == null ) {
			res = new HashMap<String,Object>();
		}
		
		/// Set the request values, defaults
		req.builder = this; //links back to self
		req.rawRequestNamespace = raw_ns; //the raw requested namespace
		
		RESTNamespace nsObj = null;
		
		// The namespace has a non wildcard varient
		if(hasNamespace(ns)) {
			nsObj = getNamespace(ns);
			if( nsObj.get(requestType) == null ) {
				nsObj = null;
			}
		}
		
		// Iterates the stored namespace, for the wildcard varient
		if( nsObj == null ) {
			List<String> nsList = new ArrayList<String>(Arrays.asList(ns));
			nsList.add("*");
			
			while( nsList.size() >= 1 ) {
				ns = nsList.toArray( new String[nsList.size()] );
				
				if( hasNamespace(ns) ) {
					nsObj = getNamespace(ns);
					if( nsObj.get(requestType) != null ) {
						break; //found it
					} else {
						nsObj = null; //continue the loop
					}
				}
				
				//if( hasNamespace() )
				if( nsList.size() <= 1 ) {
					break;
				}
				nsList.remove( nsList.size() - 2 ); //remove the element before the wildcard
			}
		}
		
		if( nsObj == null ) {
			return null;
		}
		
		/// Set the request values, respective to the found API node
		req.registeredNamespace = ns; //the registered namespace used
		
		return nsObj.call(requestType, req, res);
	}
	
	///----------------------------------------
	/// API calling function
	///----------------------------------------
	
	/// Calls the API method with a query string
	public Map<String,Object> namespaceCall(String apiNamespace, HttpRequestType requestType, Map<String,Object> requestMap, Map<String,Object> resultMap) {
		return setupAndCall(apiNamespace, requestType, new RESTRequest(requestMap), resultMap);
	}
	
	/// Calls the API method, using the request parameters from the CorePage
	public Map<String,Object> namespaceCall(String apiNamespace, HttpRequestType requestType, picoded.servlet.CorePage page, Map<String,Object> resultMap) {
		@SuppressWarnings("unchecked") 
		RESTRequest rRequest = new RESTRequest( (Map<String,Object>)(Object) (page.requestParameters()) );
		rRequest.requestPage = page;
		
		return setupAndCall(apiNamespace, requestType, rRequest, resultMap );
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	public Map<String,Object> servletCall(picoded.servlet.CorePage page, Map<String,Object> resultMap) {
		return namespaceCall(page.requestWildcardUri(), page.requestType(), page, resultMap);
	}
}
