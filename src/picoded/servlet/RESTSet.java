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
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

// Ext lib used
import org.apache.commons.lang3.ArrayUtils;

/// Internal RESTSet Sub class which handles each function in a namespace
///
/// This object class helps hold all the various default values
/// and can represent a single GET/PUT/POST/DELETE request
///
class RESTSet {
	
	/// Base object for the method, represents "this"
	Object baseObject = null;
	
	/// The actual method to be called
	Method baseMethod = null;
	
	/// The default map values
	Map<String, ?> defaultMap = null;
	
	/// The default argument array
	Object[] defaultArgs = null;
	
	/// The array of the method classes, if the first argument is a RESTRequest, it is automatically removed
	Class<?>[] methodArgsClass = null;
	
	/// Indicates if the method requires a RESTRequest object as the first parameter
	boolean baseMethodUsesRestRequest = false;
	
	/// [For internal use] Setsup the actual request method, and check its various argument classes
	///
	/// @TODO : Preoperly handle auto default values (not just null?), based on the argument basic class types
	///
	/// @param bObject   baseObject representing the "this" for the method call
	/// @param bMethod   baseMethod is the function which is actually called
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	///
	private void commonSetup(Object bObject, Method bMethod, Map<String, ?> defMap, Object[] defArg) {
		baseObject = bObject;
		baseMethod = bMethod;
		
		methodArgsClass = bMethod.getParameterTypes();
		if (methodArgsClass == null || methodArgsClass.length == 0) {
			methodArgsClass = ArrayUtils.EMPTY_CLASS_ARRAY;
		}
		
		if (methodArgsClass.length > 0 && RESTRequest.class.isAssignableFrom(methodArgsClass[0])) {
			baseMethodUsesRestRequest = true;
			
			methodArgsClass = Arrays.asList(methodArgsClass) //
				.subList(1, methodArgsClass.length).toArray(new Class<?>[0]);
		}
		
		setDefault(defMap, defArg);
	}
	
	/// Setsup the default argument
	///
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	///
	public void setDefault(Map<String, ?> defMap, Object... defArg) {
		
		// Replace the default map
		if (defMap != null) {
			defaultMap = defMap;
		}
		if (defaultMap == null) {
			defaultMap = new HashMap<String, Object>();
		}
		
		// Replace the default arguments
		if (defArg != null) {
			// Derive the default arguments list
			defaultArgs = new Object[methodArgsClass.length];
			
			// The default arguments, apply if applicable
			for (int a = 0; a < defaultArgs.length; ++a) {
				defaultArgs[a] = null;
				if (defArg != null && a < defArg.length) {
					defaultArgs[a] = defArg[a];
				}
			}
		}
		if (defaultArgs == null) {
			defaultArgs = new Object[methodArgsClass.length];
			
			// The default arguments, apply if applicable
			for (int a = 0; a < defaultArgs.length; ++a) {
				defaultArgs[a] = null;
			}
		}
		
	}
	
	//---------------------------------------------------
	// Constructor function
	//---------------------------------------------------
	
	/// [For internal use] Setsup the RESTSet, and check its various argument classes
	///
	/// @param bObject      baseObject representing the "this" for the method call
	/// @param bMethod     baseMethod is the function which is actually called
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	public RESTSet(Object bObject, Method bMethod, Map<String, ?> defMap, Object[] defArg) {
		commonSetup(bObject, bMethod, defMap, defArg);
	}
	
	/// [For internal use] Setsup the RESTSet, and check its various argument classes
	///
	/// @param bObject     baseObject representing the "this" for the method call
	/// @param bMethod     baseMethod is the function which is actually called
	public RESTSet(Object bObject, Method bMethod) {
		commonSetup(bObject, bMethod, null, null);
	}
	
	//---------------------------------------------------
	// Function calling
	//---------------------------------------------------
	
	/// Generates a RESTRequest object with the default values
	protected RESTRequest generateRequest() {
		return new RESTRequest(baseObject, baseMethod, baseMethodUsesRestRequest, defaultMap, defaultArgs);
	}
	
	// Calls with provided values
	public Object call(Map<String, ?> reqObj, Object[] reqArg) {
		// new rest request with default values
		return generateRequest().setupRequestArgs(reqObj, reqArg).call();
	}
	
	// Calls using default values
	public Object call() {
		// new rest request with default values
		return generateRequest().call();
	}
	
}