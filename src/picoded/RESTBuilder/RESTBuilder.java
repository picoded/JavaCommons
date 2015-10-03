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
	public static class RequestTypeSet extends picoded.enums.HttpRequestType.HttpRequestTypeSet {
	};
	
	/// Blank constructor
	public RESTBuilder() {
	}
	
	///----------------------------------------
	/// Namespace adding / handling
	///----------------------------------------
	
	/// Stores the various methods currently in place of the RESTBuilder
	protected Map<String, RESTNamespace> namespaceMap = new HashMap<String, RESTNamespace>();
	
	/// Namespace filter, amends common namespace errors
	protected String namespaceFilter(String namespace) {
		return namespace.replaceAll("\\.", "/").replaceAll("//", "/").replaceAll("//", "/").split("\\?")[0];
	}
	
	/// Filters and get the storage namespace
	protected String[] namespaceArray(String namespace) {
		return namespaceFilter(namespace).split("/");
	}
	
	/// Filters and get the storage namespace
	protected String namespaceString(String[] namespace) {
		String storeStr = StringUtils.join(namespace, "/").replaceAll("//", "/");
		while (storeStr.length() > 0 && storeStr.lastIndexOf("/") == (storeStr.length() - 1)) {
			storeStr = storeStr.substring(0, storeStr.length() - 1);
		}
		return storeStr;
	}
	
	/// Has api namespace check
	public boolean hasNamespace(String namespace) {
		return hasNamespace(namespaceArray(namespace));
	}
	
	/// Has api namespace check
	public boolean hasNamespace(String[] namespace) {
		return (namespaceMap.get(namespaceString(namespace)) != null);
	}
	
	/// Gets the api RESTNamespace, to add the respetive GET/PUT/POST/DELETE calls
	public RESTNamespace getNamespace(String namespace) {
		return getNamespace(namespaceArray(namespace));
	}
	
	/// Gets the api RESTNamespace, to add the respetive GET/PUT/POST/DELETE calls. Note this GENERATES one if it does not exists
	public RESTNamespace getNamespace(String[] namespace) {
		String storeStr = namespaceString(namespace);
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
	protected Map<String, Object> setupAndCall(String apiNamespace, HttpRequestType requestType, RESTRequest req,
		Map<String, Object> res) {
		String[] raw_ns = namespaceArray(namespaceFilter(apiNamespace));
		String[] ns = raw_ns;
		
		// Auto create result map if needed
		if (res == null) {
			res = new HashMap<String, Object>();
		}
		
		/// Set the request values, defaults
		req.builder = this; //links back to self
		req.rawRequestNamespace = raw_ns; //the raw requested namespace
		
		RESTNamespace nsObj = null;
		
		// The namespace has a non wildcard varient
		if (hasNamespace(ns)) {
			nsObj = getNamespace(ns);
			if (nsObj.get(requestType) == null) {
				nsObj = null;
			}
		}
		
		// Iterates the stored namespace, for the wildcard varient
		if (nsObj == null) {
			List<String> nsList = new ArrayList<String>(Arrays.asList(ns));
			nsList.add("*");
			
			while (nsList.size() >= 1) {
				ns = nsList.toArray(new String[nsList.size()]);
				
				if (hasNamespace(ns)) {
					nsObj = getNamespace(ns);
					if (nsObj.get(requestType) != null) {
						break; //found it
					} else {
						nsObj = null; //continue the loop
					}
				}
				
				//if( hasNamespace() )
				if (nsList.size() <= 1) {
					break;
				}
				nsList.remove(nsList.size() - 2); //remove the element before the wildcard
			}
		}
		
		if (nsObj == null) {
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
	public Map<String, Object> namespaceCall(String apiNamespace, HttpRequestType requestType,
		Map<String, Object> requestMap, Map<String, Object> resultMap) {
		return setupAndCall(apiNamespace, requestType, new RESTRequest(requestMap), resultMap);
	}
	
	/// Calls the API method, using the request parameters from the CorePage
	public Map<String, Object> namespaceCall(String apiNamespace, HttpRequestType requestType,
		picoded.servlet.CorePage page, Map<String, Object> resultMap) {
		@SuppressWarnings("unchecked")
		RESTRequest rRequest = new RESTRequest((Map<String, Object>) (Object) (page.requestParameters()));
		rRequest.requestPage = page;
		
		return setupAndCall(apiNamespace, requestType, rRequest, resultMap);
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	/// and automatic handling of missing API error
	///
	/// This is mainly intended to be call within CorePage doJSON
	public boolean servletCall(picoded.servlet.CorePage page, Map<String, Object> resultMap, String apiNamespace) {
		if (namespaceCall(apiNamespace, page.requestType(), page, resultMap) == null) {
			resultMap.put("requested-API", apiNamespace);
			resultMap.put("error", "REST API not found");
		}
		return true;
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	/// and automatic handling of missing API error
	///
	/// This is mainly intended to be call within CorePage doJSON
	public boolean servletCall(String apiPrefix, picoded.servlet.CorePage page, Map<String, Object> resultMap) {
		String apiNamespace = (apiPrefix + page.requestWildcardUri()); //namespaceFilter?
		return servletCall(page, resultMap, apiNamespace);
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	/// and automatic handling of missing API error
	///
	/// This is mainly intended to be call within CorePage doJSON
	public boolean servletCall(picoded.servlet.CorePage page, Map<String, Object> resultMap) {
		return servletCall("", page, resultMap);
	}
	
	///----------------------------------------
	/// Namespace tree generator / handling, used for generating the relevent javascript
	///----------------------------------------
	
	// protected class RESTNamespaceTree extends HashMap<String, RESTNamespaceTree> {
	// 	String registeredNamespace = null;
	// 	RESTNamespace namespaceObj = null;
	// }
	// 
	// protected RESTNamespaceTree cachedNamespaceTree = null;
	// 
	// //protected void addNamespaceToTree( String namespace, )
	// 
	// protected RESTNamespaceTree generateNamespaceTree() {
	// 	cachedNamespaceTree = new RESTNamespaceTree();
	// 	
	// 	return cachedNamespaceTree;
	// }
	
	///----------------------------------------
	/// Javascript generator code
	///----------------------------------------
	
	/// Creates a JS function source with the given name, defaults to "x". 
	/// With the following parameters.
	///
	/// @TODO: remove dependency on jQuery
	///
	/// + u : URL
	/// + t : URL request type "POST" / "GET"
	/// + p : The request parameters
	/// + c : The request call back, calls with ( return, argument, error )
	protected static String xmlHttpJS(String functionName) {
		return "" + //
			"function " + functionName + "(u,t,p,c) { " + // The function name
			"if(c === null) { " + // Ensures that even there is no callback, it is handled "gracefully"
			"c = function() {}; " + //
			"} " + //
			//----------------------------------------------------------------------------
			// JQuery varient
			//----------------------------------------------------------------------------
			"$.ajax({ " + //
			"url: u, " + //
			"type: t, " + //
			"data: p, " + //
			//
			"dataType: 'json', " + //
			"cache: false, " + //
			//
			"xhrFields: { withCredentials:true }, " +
			//
			"success: function(d){ c(d,p,null); }, " + //
			"error: function(j,s,t){ c(null,p, s+' - '+t); } " + //
			" }); " + //
			//
			//----------------------------------------------------------------------------
			// Experimental XMLHttpRequest varient (incomplete -> TODO), 
			// it needs a full wrappign implementation of all the variosu request type
			//
			// See: http://youmightnotneedjquery.com/
			//----------------------------------------------------------------------------
			// "var r=new XMLHttpRequest(); "+ // The browser XMLHttpRequest function
			// "r.onload = function(e) { "+ // Onload callback handling
			// 	"if(r.status >= 200 && r.status < 400) { "+ //
			// 		"c(JSON.parse(r.responseText), p, null); "+ //
			// 	"} else { "+ //
			// 		"c(null, p, e); "+
			// 	"} "+ //
			// "}; "+ //
			// "r.onerror = function(e) { "+ //
			// 	"s(null, p, e); "+ //
			// "}; "+ //
			// "r.open(t,u,true);"+ //
			// "r.send(p); "+ //
			"} "; //
	}
	
	/// Creates the API Node builder JS with the xmlHttpJS script embedded
	protected static String apiNodeBuilderJS(String builderName, String baseURL) {
		if (baseURL == null) {
			baseURL = "";
		}
		while (baseURL.length() > 0 && baseURL.lastIndexOf("/") == (baseURL.length() - 1)) {
			baseURL = baseURL.substring(0, baseURL.length() - 1);
		}
		
		String xHttpCall = "xHttpCall";
		
		return ""
			+ //
			xmlHttpJS(xHttpCall)
			+ //
			  //
			"var apiHost = '"
			+ baseURL
			+ "'; "
			+ //the base url hostname
			  //
			"function filterNamespace(n) { "
			+ //
			  //
			"if(n != null && n.length > 0) { "
			+ //Check if there is content
			"n=n.replace(/\\/\\*/g, '/').replace(/\\/\\//g, '/').replace(/\\/\\//g, '/'); "
			+ //
			  //
			"if(n.indexOf('/') !== 0) { "
			+ //Enforces starting / as it is needed
			"n='/'+n; "
			+ //
			"} "
			+ //
			  //
			"if(n[n.length-1] == '/') { "
			+ //
			"n=n.substring(0,n.length-1); "
			+ //
			"} "
			+ //
			"} else { "
			+ //
			"n = ''; "
			+ //
			"} "
			+ //
			  //
			"return n; "
			+ "} "
			+ //
			  //
			"function "
			+ builderName
			+ "(n) { "
			+ //
			"n = filterNamespace(n); "
			+ //
			"var api = function(namespace) { "
			+ //
			"return "
			+ builderName
			+ "(n+'/'+namespace); "
			+ //
			"}; "
			+ //
			  // The url that this node calls
			"var tar = apiHost+n; "
			+ //
			  // Setsup the various GET,PUT,POST,DELETE
			"api.GET=function(p,c){ return " + xHttpCall + "(tar, 'GET', p, c); }; " + "api.PUT=function(p,c){ return "
			+ xHttpCall + "(tar, 'PUT', p, c); }; " + "api.POST=function(p,c){ return " + xHttpCall
			+ "(tar, 'POST', p, c); }; " + "api.DELETE=function(p,c){ return " + xHttpCall + "(tar, 'DELETE', p, c); }; " +
			//
			"return api; " + //
			"} "; //
	}
	
	/// Craetes the api chain builder, which is called by the generateJS, 
	/// and links with apiNodeBuidlerJS
	protected static String apiChainBuilder(String chainBuilder, String nodeBuilderName) {
		return ""
			+ //
			"function " + chainBuilder + "(r,n){ " + "n = filterNamespace(n); " + "var nArr = n.split('/'); "
			+ "var t = r; " + "for( var i=1; i < nArr.length; ++i ) { " + "if( t[ nArr[i] ] == null ) { "
			+ "t[ nArr[i] ] = t( nArr[i] ); " + "} " + "t = t[ nArr[i] ]; " + "} " + "return r; " + "} ";
	}
	
	public String generateJS(String rootVarName, String baseURL) {
		StringBuilder ret = new StringBuilder();
		String builderName = "nodeBuidler";
		String chainBuilder = "chainBuilder";
		
		if (rootVarName == null || rootVarName.length() <= 0) {
			rootVarName = "REST";
		}
		if (baseURL == null) {
			baseURL = "";
		}
		
		ret.append("window.") //
			.append(rootVarName) //
			.append("=((function(){ ").append("\"use strict\"; ");
		
		ret.append(apiNodeBuilderJS(builderName, baseURL));
		ret.append(apiChainBuilder(chainBuilder, builderName));
		
		ret.append("var ret=") //
			.append(builderName) //
			.append("(); "); //
		
		for (Map.Entry<String, RESTNamespace> entry : namespaceMap.entrySet()) {
			ret.append(chainBuilder).append("(ret, '").append(entry.getKey()).append("'); ");
		}
		
		ret.append("return ret; ") //
			.append("})()); ");
		
		return ret.toString();
	}
}
