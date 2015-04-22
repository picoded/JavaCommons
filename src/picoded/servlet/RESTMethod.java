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

///
/// Internal RESTMethod Sub class which handles each function namespace seperately
///
/// CRUD       HTTP
/// Create     POST
/// Read       GET
/// Update     PUT
/// Delete     DELETE
///
/// 
/// @TODO adding of GET/PUT/POST/DELETE
/// @TODO adding with default values being supported
///
public class RESTMethod {
	
	//---------------------------------------
	// Public static
	//---------------------------------------
	
	/// The GET method type
	public static final int TYPE_GET = 0;
	
	/// The PUT method type
	public static final int TYPE_PUT = 1;
	
	/// The POST method type
	public static final int TYPE_POST = 2;
	
	/// The DELETE method type
	public static final int TYPE_DELETE = 3;
	
	/// Maximium range of all method types
	private static final int methodSetMax = 4;
	
	//---------------------------------------
	// Protected vars
	//---------------------------------------
	
	/// The protected string namespace
	protected String namespace = null;
	
	/// Get the protected namespace
	public String namespace() {
		return namespace;
	}
	
	/// The request method set array, representing the various values
	protected RESTSet[] methodSet = new RESTSet[methodSetMax];
	
	//---------------------------------------
	// Constructor
	//---------------------------------------
	
	/// [internal use only] Setup the rest method with the given namespace
	public RESTMethod(String nme) {
		namespace = nme;
	}
	
	//-------------------------------------------------------------
	// Adding / Removing method functions, with type parameter
	//-------------------------------------------------------------
	
	/// Indicates if the method exists of the given type
	public boolean hasMethod(int methodType) {
		return (methodSet[methodType] != null);
	}
	
	/// Sets the method for the given type
	public RESTMethod setMethod(int methodType, Object baseObject, Method baseMethod) {
		RESTSet exist = methodSet[methodType];
		
		// ignore if already set
		if (exist != null && exist.baseObject == baseObject && exist.baseMethod == baseMethod) {
			return this;
		}
		
		// setup the method
		methodSet[methodType] = new RESTSet(baseObject, baseMethod);
		return this;
	}
	
	/// Sets the method for the given type via reflection
	public RESTMethod setMethod(int methodType, Object baseObject, String methodName, Class<?> classArr) {
		Method m = null;
		try {
			m = baseObject.getClass().getMethod(methodName, classArr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return setMethod(methodType, baseObject, m);
	}
	
	/// Sets the method for the given type via reflection with the method name only,
	/// in event that 2 overloaded method exists with the same name, a RuntimeException is thrown
	public RESTMethod setMethod(int methodType, Object baseObject, String methodName) {
		Method m = null;
		Method[] mArr = null;
		
		try {
			mArr = baseObject.getClass().getMethods();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for (int a = 0; a < mArr.length; ++a) {
			if (methodName.equals(mArr[a].getName())) {
				//found one,
				if (m != null) {
					throw new RuntimeException("Unable to set method, there is more then one overloaded method name : "
						+ methodName);
				}
				m = mArr[a];
			}
		}
		
		if (m == null) {
			throw new RuntimeException("Failed to find method with name : " + methodName);
		}
		
		return setMethod(methodType, baseObject, m);
	}
	
	/// Gets the method internal base object
	public Object getMethodObject(int methodType) {
		return (methodSet[methodType] != null) ? methodSet[methodType].baseObject : null;
	}
	
	/// Gets the method internal raw method
	public Method getMethodRaw(int methodType) {
		return (methodSet[methodType] != null) ? methodSet[methodType].baseMethod : null;
	}
	
	/// Calls the given method type
	public Object callMethod(int methodType, Map<String, Object> reqObject, Object[] reqArgs) {
		return (methodSet[methodType] != null) ? methodSet[methodType].call(reqObject, reqArgs) : null;
	}
	
	//---------------------------------------
	// Get request handling
	//---------------------------------------
	
	/// Sets a REST GET request
	public RESTMethod setGET(Object baseObject, Method baseMethod) {
		return setMethod(TYPE_GET, baseObject, baseMethod);
	}
	
	/// Set a REST GET with just the method name
	public RESTMethod setGET(Object baseObject, String methodName) {
		return setMethod(TYPE_GET, baseObject, methodName);
	}
	
	/// Set a REST GET with the given class array
	public RESTMethod setGET(Object baseObject, String methodName, Class<?> classArr) {
		return setMethod(TYPE_GET, baseObject, methodName, classArr);
	}
	
	/// Calls a REST GET request with the map & array arguments
	public Object GET(Map<String, Object> reqObject, Object... reqArgs) {
		return callMethod(TYPE_GET, reqObject, reqArgs);
	}
	
	/// Calls a REST GET request with the map arguments
	public Object GET(Map<String, Object> reqObject) {
		return callMethod(TYPE_GET, reqObject, null);
	}
	
	/// Calls a REST GET request with the array arguments
	public Object GET(Object... reqArgs) {
		return callMethod(TYPE_GET, null, reqArgs);
	}
	
	/// Calls a REST GET request without any arguments
	public Object GET() {
		return callMethod(TYPE_GET, null, null);
	}
	
}
