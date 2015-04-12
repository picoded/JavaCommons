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

/// Internal RESTSet Sub class which handles each function in a namespace
///
/// This handles the actual function calling and can represent either GET/PUT/POST/DELETE
class RESTSet {
	// Core functionalities
	Object baseObject = null;
	Method baseMethod = null;
	
	// Optional defaults
	Map<String, Object> defaultMap = null;
	Object[] defaultArgs = null;
	Class<?>[] methodArgsClass = null;
	
	int methodArgsLength = 0;
	boolean firstIsRESTRequest = false;
	
	// Commons setup function
	//
	// @TODO : Preoperly handle default values (not just null?)
	//
	private void commonSetup(Object bObject, Method bMethod, Object[] dArgs, Map<String, Object> dMap) {
		baseObject = bObject;
		baseMethod = bMethod;
		
		methodArgsClass = bMethod.getParameterTypes();
		if (methodArgsLength > 0 && RESTRequest.class.isAssignableFrom(methodArgsClass[0])) {
			firstIsRESTRequest = true;
			
			methodArgsClass = Arrays.asList(methodArgsClass) //
				.subList(1, methodArgsClass.length).toArray(new Class<?>[0]);
		}
		
		methodArgsLength = methodArgsClass.length;
		
		// Derive the default arguments list
		defaultArgs = new Object[methodArgsLength];
		
		for (int a = 0; a < methodArgsLength; ++a) {
			defaultArgs[a] = null;
			
			if (a < dArgs.length) {
				defaultArgs[a] = dArgs[a];
			}
		}
		
		defaultMap = dMap;
		if (defaultMap == null) {
			defaultMap = new HashMap<String, Object>();
		}
	}
	
	public RESTSet() {
	}
	
	public RESTSet(Object bObject, Method bMethod) {
		commonSetup(bObject, bMethod, null, null);
	}
	
	// Calls using default values
	public Object call() {
		// new rest request with default values
		return (new RESTRequest(this, null, null)).call();
	}
}