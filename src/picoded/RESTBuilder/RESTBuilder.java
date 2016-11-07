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

import picoded.conv.*;
import picoded.set.HttpRequestType;

//import picoded.conv.CompileES6;

/**
 * picoded.servlet.RESTBuilder is a servlet utility class, in which facilitates the building of "RESTful API's"
 * that can be used in the project either via a public API, or even internally, via a direct function call.
 *
 * The main role of RESTBuilder, is to facilitate the packaging of all the web project core functionalities,
 * into a single API Framework. Where it can be called directly.
 *
 * While, it replaces the original servlet framework role of creating JSON output page. The framework is also meant to
 * facilitate intra project function calls. One of the examples learnt from the LMS project, in handling the encryption
 * of export files (for example). Is that instead of rewriting the entire export module code to be "callable" by a function
 * a rather indirect and "inefficent" method of calling its local page directly was used. Aka an encryption proxy.
 *
 * Simply put, if a standardised REST API builder was built and used, several page API features can be called directly
 * instead of being usued via a proxy
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
	
	// /// RESTBuilder HttpRequestType enum access
	// public static class RequestTypeSet extends picoded.set.HttpRequestType.HttpRequestTypeSet {
	// };
	
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
		return namespace.replaceAll("\\.", "/").replaceAll("//", "/").replaceAll("//", "/")
			.split("\\?")[0];
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
	protected Map<String, Object> setupAndCall(String apiNamespace, HttpRequestType requestType,
		RESTRequest req, Map<String, Object> res) {
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
		
		/// Missing name space handling
		if (nsObj == null) {
			res.put("requested-API", apiNamespace);
			res.put("error", "REST API not found");
			return res;
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
		RESTRequest rRequest = new RESTRequest(
			(Map<String, Object>) (Object) (page.requestParameters()));
		rRequest.requestPage = page;
		
		return setupAndCall(apiNamespace, requestType, rRequest, resultMap);
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	/// and automatic handling of missing API error
	///
	/// This is mainly intended to be call within CorePage doJSON
	public boolean servletCall(picoded.servlet.CorePage page, Map<String, Object> resultMap,
		String apiNamespace) {
		/// If null, the assumption is the request process flow is terminated. Hence default behaviour of JSON output is canceled
		if (namespaceCall(apiNamespace, page.requestType(), page, resultMap) == null) {
			return false;
		}
		
		/// Assume default behaviour (output JSON)
		return true;
	}
	
	/// Automatically calls the respective namespace, using the CorePage registered URI wildcard, as "API NameSpace"
	/// and automatic handling of missing API error
	///
	/// This is mainly intended to be call within CorePage doJSON
	public boolean servletCall(String apiPrefix, picoded.servlet.CorePage page,
		Map<String, Object> resultMap) {
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
	
	///
	/// Utility function that recursively setup the namespace for the object
	///
	/// @params  The tree object to populate
	/// @params  namespace as array
	/// @params  namespace object to add
	///
	protected void setupNamespaceInTree(Map<String, Object> tree, String[] names, RESTNamespace obj) {
		if (names == null || names.length == 0) {
			/// Add in a boolean flag for the respective method
			for (HttpRequestType type : HttpRequestType.values()) {
				if (obj.get(type) != null) {
					tree.put(type.toString().toUpperCase(), Boolean.TRUE);
				}
			}
		} else {
			String item = names[0];
			String[] subnames = ArrayConv.subarray(names, 1, names.length);
			
			if ( //
			item.equalsIgnoreCase("post") || item.equalsIgnoreCase("get")
				|| item.equalsIgnoreCase("put") || item.equalsIgnoreCase("delete")
				|| item.equalsIgnoreCase("update")) {
				throw new RuntimeException("Protected rest namespace key name used: " + item);
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> subtree = (Map<String, Object>) (tree.get(item));
			if (subtree == null) {
				subtree = new HashMap<String, Object>();
				tree.put(item, subtree);
			}
			
			setupNamespaceInTree(subtree, subnames, obj);
		}
	}
	
	///
	/// Generates the namespace tree using the current RESTBuilder
	///
	/// @returns  A json like structure of the API, where the respective POST/GET/etc methods, 
	///           are marked with boolean TRUE
	///
	public Map<String, Object> namespaceTree() {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (Map.Entry<String, RESTNamespace> entry : namespaceMap.entrySet()) {
			try {
				setupNamespaceInTree(ret, entry.getKey().toString().split("(\\.|/)"), entry.getValue());
			} catch (Exception e) {
				throw new RuntimeException("Failed to process namespace key :" + entry.getKey());
			}
		}
		return ret;
	}
	
	///----------------------------------------
	/// Javascript generator code
	///----------------------------------------
	
	/// Creates a JS function source with the given name, defaults to "x". 
	/// With the following parameters.
	/// @TODO: remove dependency on jQuery
	///
	/// Input params:
	/// + u : URL
	/// + t : URL request type "POST" / "GET"
	/// + p : The request parameters
	/// + c : The request call back, calls with ( return )
	protected static String xmlHttpJS(String functionName) {
		return "\n" //
			+ "function " + functionName + "(url,type,params,callbck) { \n" //
			//----------------------------------------------------------------------------
			// Default callback handling / logging
			//----------------------------------------------------------------------------
			+ "	if(callbck === undefined) { \n" //
			+ "		callbck = function(res) { console.log(url, type, params, res); }; \n" //
			+ "	} \n" //
			//----------------------------------------------------------------------------
			// JQuery varient : Promise returns
			//----------------------------------------------------------------------------
			+ "	var ret = new Promise(function(good,bad) { \n" //
			+ "		$.ajax({ \n" //
			+ "			url:url, type:type, data:params || {}, \n" //
			+ "			dataType:'json', cache:false, \n" //
			+ "			xhrFields: { withCredentials:true }, \n" //
			+ "			success: function(d) { \n" //
			+ "				if(d.error) { \n" //
			+ "					bad('200 - '+d.error); \n" //
			+ "					return; \n" //
			+ "				} \n" //
			+ "				good(d); \n" //
			+ "			}, \n" //
			+ "			error: function(jqxhr, status, error) { \n" //
			+ "				bad(status+' - '+error);\n" //
			+ "			} \n" //
			+ "		}); \n" //
			+ "	}); \n" //
			+ "	if(callbck) { ret.then(callbck); } \n" //
			+ "	return ret; \n" //
			//----------------------------------------------------------------------------
			// @TODO Experimental XMLHttpRequest varient (Tp Consider), 
			// it needs a full wrapping implementation of all the various request type
			//
			// See: http://youmightnotneedjquery.com/
			//----------------------------------------------------------------------------
			+ "} "; //
	}
	
	/// Creates the API Node builder JS with the xmlHttpJS script embedded
	protected static String apiNodeBuilderJS(String namespaceBuilder, String baseURL) {
		if (baseURL == null) {
			baseURL = "";
		}
		while (baseURL.length() > 0 && baseURL.endsWith("/")) {
			baseURL = baseURL.substring(0, baseURL.length() - 1);
		}
		
		String xHttpCall = "xHttpCall";
		
		return "\n" //
		//----------------------------------------------------------------------------
		// function xHttpCall(url,type,params,callbck) { .. }
		//----------------------------------------------------------------------------
			+ xmlHttpJS(xHttpCall)
			+ "\n" //
			//----------------------------------------------------------------------------
			// API base host name passing
			//----------------------------------------------------------------------------
			+ "function apiHost() {\n" //
			+ "		if(PageComponent == null || PageComponent.apiRootURI == null) {\n" //
			+ "			return '"
			+ baseURL
			+ "';\n" //
			+ "		}\n" //
			+ "		return PageComponent.apiRootURI;\n" //
			+ "}\n" //
			//----------------------------------------------------------------------------
			// Gets and filter the namespace string
			//
			// Input params:
			// + name : Namespace to do normalization on
			//
			// Return : The normalized namespace string
			//----------------------------------------------------------------------------
			+ "function normalizeNamespace(name) { \n" //
			+ "		if(name != null && name.length > 0) { \n" //
			+ "			name=name.replace(/\\/\\*/g, '/').replace(/\\/\\//g, '/').replace(/\\/\\//g, '/'); \n" //
			// 			// Enforces starting / as it is needed
			+ "			if(name.indexOf('/') !== 0) { \n" //
			+ "				name='/'+name; \n" //
			+ "			} \n" //
			+ "			if(name[name.length-1] == '/') { \n" //
			+ "				name=name.substring(0,name.length-1); \n" //
			+ "			} \n" //
			+ "		} else { \n" //
			+ "			name = ''; \n" //
			+ "		} \n" //
			+ "		return name; \n" //
			+ "} \n" //
			//----------------------------------------------------------------------------
			// Namespace builder / extender for a single node namespace
			//
			// Input params:
			// + name : Namespace to extend from
			//
			// Return : Namespace object with the .ajax function
			//----------------------------------------------------------------------------
			+ "var " + namespaceBuilder + " = (function " + namespaceBuilder + "(name) { \n" //
			+ "		name = normalizeNamespace(name); \n" //
			//      // Returns a function, allows recursive chaining
			+ "		var ret = function(namespace) { \n" //
			+ "			return " + namespaceBuilder + "(name+'/'+namespace); \n" //
			+ "		}; \n" //
			//		// The actual internal AJAX function which calls xHttpCall
			+ "		ret.ajax = function ajax(type, params, callbck) {\n" //
			+ "			var tar = apiHost()+name; \n" // Full URL for this node to request
			+ "			return " + xHttpCall + " (tar, type, params, callbck) \n" //
			+ "		} \n" //
			+ "		return ret; \n" //
			+ "})(); \n" //
			+ ""; //end
	}
	
	/// Craetes the api chain builder, which is called by the generateJS, 
	/// and links with apiNodeBuidlerJS
	protected static String apiChainBuilder(String chainBuilder, String nodeBuilderName) {
		return "\n" //
			+ "function " + chainBuilder + "(ret, name, typeArr) { \n" //
			+ "		name = normalizeNamespace(name); \n" //
			+ "		var nArr = name.split('/'); \n" //
			//		// Iterate and build each segment of the chain if needed
			+ "		for( var i=1; i < nArr.length; ++i ) { \n" //
			+ "			if( ret[ nArr[i] ] == null ) { \n" //
			+ "				ret[ nArr[i] ] = ret( nArr[i] ); \n" //
			+ "			} \n" //
			+ "			ret = ret[ nArr[i] ]; \n" //
			+ "		} \n" //
			//		// Setup in accordance to the type ARR 
			+ "		if( typeArr && Array.isArray(typeArr) ) { \n" //
			+ "			typeArr.forEach(function(type) { \n" //
			+ "				type = type.toUpperCase(); \n" // 
			+ "				ret[type] = function(params, callbck) { \n" //
			+ "					ret.ajax(type, params, callbck); \n" //
			+ "				} \n" //
			+ "			}); \n" //
			+ "		}\n" //
			//		// Return the chain 
			+ "		return ret; \n" //
			+ "} ";
	}
	
	public String generateJS(String rootVarName, String baseURL) {
		StringBuilder ret = new StringBuilder();
		String namespaceBuilder = "namespaceBuilder";
		String chainBuilder = "chainBuilder";
		
		if (rootVarName == null || rootVarName.length() <= 0) {
			rootVarName = "api";
		}
		if (baseURL == null) {
			baseURL = "";
		}
		
		// Setup global API namespace (isolated function)
		//---------------------------------------------------
		ret.append("window.") //
			.append(rootVarName) //
			.append("=((function(){ \n").append("\"use strict\"; \n");
		
		// Setup namespace and chain builders
		//---------------------------------------------------
		ret.append(apiNodeBuilderJS(namespaceBuilder, baseURL));
		ret.append(apiChainBuilder(chainBuilder, namespaceBuilder));
		
		// Iterate setup registered namespaces
		//---------------------------------------------------
		for (Map.Entry<String, RESTNamespace> entry : namespaceMap.entrySet()) {
			ret.append(chainBuilder + "(");
			ret.append(namespaceBuilder + ", ");
			ret.append("'" + entry.getKey() + "', [");
			boolean firstArrItem = true;
			for (HttpRequestType type : entry.getValue().keySet()) {
				if (firstArrItem == false) {
					ret.append(", ");
				}
				firstArrItem = false;
				ret.append("'" + type.toString() + "'");
			}
			ret.append("]); \n");
		}
		
		// Actual return and setup
		//---------------------------------------------------
		ret.append("return " + namespaceBuilder + "; \n") //
			.append("})()); ");
		
		// Closure compiler compression
		//---------------------------------------------------
		return ret.toString();
		//return CompileES6.compile(ret.toString());
	}
}
