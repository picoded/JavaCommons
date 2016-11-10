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

import picoded.enums.HttpRequestType;

///
/// Internal RESTNamespace Sub class which handles each function namespace seperately
///
/// CRUD       HTTP
/// Create     POST
/// Read       GET
/// Update     PUT
/// Delete     DELETE
///
public class RESTNamespace extends HashMap<HttpRequestType, RESTFunction> {
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	//---------------------------------------
	// Protected vars
	//---------------------------------------
	
	/// The protected string namespace
	protected String namespace = null;
	
	/// Get the protected namespace
	public String namespace() {
		return namespace;
	}
	
	//---------------------------------------
	// Constructor
	//---------------------------------------
	
	/// [internal use only] Setup the rest method with the given namespace
	public RESTNamespace(String nme) {
		namespace = nme;
	}
	
	/// [FOR TESTING ONLY] Calls the method without any arguments, call via the RESTBuilder instead
	public Map<String, Object> call(HttpRequestType type) {
		return call(type, new RESTRequest(), new HashMap<String, Object>());
	}
	
	/// [FOR TESTING ONLY] Calls the method without any arguments, call via the RESTBuilder instead
	public Map<String, Object> call(HttpRequestType type, Map<String, Object> reqMap) {
		return call(type, new RESTRequest(reqMap), new HashMap<String, Object>());
	}
	
	/// Internal call method, with the full request, and result
	protected Map<String, Object> call(HttpRequestType type, RESTRequest req, Map<String, Object> res) {
		RESTFunction f = get(type);
		if (f == null) {
			throw new RuntimeException("Missing function for given namespace / type : " + namespace
				+ " / " + type);
		}
		return f.apply(req, res);
	}
	
	/// Internal call method, with the full request, and result
	// public Map<String,Object> call(HttpRequestType type, Map<String,Object> reqMap) {
	// 	return null;
	// }
}
