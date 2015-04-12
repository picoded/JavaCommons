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

/// Internal RESTMethod Sub class which handles each function namespace seperately
///
/// CRUD       HTTP
/// Create      POST
/// Read         GET
/// Update     PUT
/// Delete      DELETE
///
/// 
/// @TODO adding of GET/PUT/POST/DELETE
/// @TODO adding with default values being supported
///
public class RESTMethod {
	
	protected String namespace = null;
	
	protected RESTSet getMethod = null;
	protected RESTSet putMethod = null;
	protected RESTSet postMethod = null;
	protected RESTSet deleteMethod = null;
	
	public RESTMethod(String nme) {
		namespace = nme;
	}
	
	//---------------------------------------
	// Adding of the various sub functions
	//---------------------------------------
	
	/// Sets a REST GET request
	public RESTMethod setGet(Object baseObject, Method baseMethod) {
		
		// Already created, ignores
		if (getMethod != null && getMethod.baseObject == baseObject && getMethod.baseMethod == baseMethod) {
			return this;
		}
		
		// Create and add to the list
		getMethod = new RESTSet(baseObject, baseMethod);
		return this;
	}
	
	/// Calls a REST GET request without any arguments
	public Object get() {
		try {
			if (getMethod != null) {
				return getMethod.call();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/*
	
	/// Calls a REST GET request without any arguments
	public Object get(Map<String,Object> reqObject) {
		try {
			// Already created, ignores
			for(RESTSet rs : getMethod) {
				if(rs.methodArgs == null || rs.methodArgs.length == 0) {
					return rs.baseMethod.invoke(rs.baseObject, reqObject);
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Calls a REST GET request without any arguments
	public Object get(Map<String,Object> reqObject, Object[] reqArgs) {
		try {
			// Already created, ignores
			for(RESTSet rs : getMethod) {
				if(rs.methodArgs == null || rs.methodArgs.length == 0) {
					return rs.baseMethod.invoke(rs.baseObject, reqObject, reqArgs);
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	//  */
}
