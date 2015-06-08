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
	/// in event that more then 2 overloaded or 0 method exists with the name,
	/// a RuntimeException is thrown
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
	public Object callMethod(int methodType, Map<String, ?> reqObject, Object[] reqArgs) {
		return (methodSet[methodType] != null) ? methodSet[methodType].call(reqObject, reqArgs) : null;
	}
	
	/// Set the default for the method
	public RESTMethod setDefault(int methodType, Map<String, ?> defMap, Object... defArg) {
		if (methodSet[methodType] != null) {
			methodSet[methodType].setDefault(defMap, defArg);
		}
		return this;
	}
	
	//---------------------------------------
	// GET request handling
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
	
	/// Set the REST GET default value
	public RESTMethod setDefaultGET(Map<String, ?> defMap, Object... defArg) {
		return setDefault(TYPE_GET, defMap, defArg);
	}
	
	/// Calls a REST GET request with the map & array arguments
	public Object requestGET(Map<String, ?> reqObject, Object... reqArgs) {
		return callMethod(TYPE_GET, reqObject, reqArgs);
	}
	
	/// Calls a REST GET request with the map arguments
	public Object requestGET(Map<String, ?> reqObject) {
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
	
	//---------------------------------------
	// PUT request handling
	//---------------------------------------
	
	/// Sets a REST PUT request
	public RESTMethod setPUT(Object baseObject, Method baseMethod) {
		return setMethod(TYPE_PUT, baseObject, baseMethod);
	}
	
	/// Set a REST PUT with just the method name
	public RESTMethod setPUT(Object baseObject, String methodName) {
		return setMethod(TYPE_PUT, baseObject, methodName);
	}
	
	/// Set a REST PUT with the given class array
	public RESTMethod setPUT(Object baseObject, String methodName, Class<?> classArr) {
		return setMethod(TYPE_PUT, baseObject, methodName, classArr);
	}
	
	/// Set the REST PUT default value
	public RESTMethod setDefaultPUT(Map<String, ?> defMap, Object... defArg) {
		return setDefault(TYPE_PUT, defMap, defArg);
	}
	
	/// Calls a REST PUT request with the map & array arguments
	public Object requestPUT(Map<String, ?> reqObject, Object... reqArgs) {
		return callMethod(TYPE_PUT, reqObject, reqArgs);
	}
	
	/// Calls a REST PUT request with the map arguments
	public Object requestPUT(Map<String, ?> reqObject) {
		return callMethod(TYPE_PUT, reqObject, null);
	}
	
	/// Calls a REST PUT request with the array arguments
	public Object PUT(Object... reqArgs) {
		return callMethod(TYPE_PUT, null, reqArgs);
	}
	
	/// Calls a REST PUT request without any arguments
	public Object PUT() {
		return callMethod(TYPE_PUT, null, null);
	}
	
	//---------------------------------------
	// POST request handling
	//---------------------------------------
	
	/// Sets a REST POST request
	public RESTMethod setPOST(Object baseObject, Method baseMethod) {
		return setMethod(TYPE_POST, baseObject, baseMethod);
	}
	
	/// Set a REST POST with just the method name
	public RESTMethod setPOST(Object baseObject, String methodName) {
		return setMethod(TYPE_POST, baseObject, methodName);
	}
	
	/// Set a REST POST with the given class array
	public RESTMethod setPOST(Object baseObject, String methodName, Class<?> classArr) {
		return setMethod(TYPE_POST, baseObject, methodName, classArr);
	}
	
	/// Set the REST POST default value
	public RESTMethod setDefaultPOST(Map<String, ?> defMap, Object... defArg) {
		return setDefault(TYPE_POST, defMap, defArg);
	}
	
	/// Calls a REST POST request with the map & array arguments
	public Object requestPOST(Map<String, ?> reqObject, Object... reqArgs) {
		return callMethod(TYPE_POST, reqObject, reqArgs);
	}
	
	/// Calls a REST POST request with the map arguments
	public Object requestPOST(Map<String, ?> reqObject) {
		return callMethod(TYPE_POST, reqObject, null);
	}
	
	/// Calls a REST POST request with the array arguments
	public Object POST(Object... reqArgs) {
		return callMethod(TYPE_POST, null, reqArgs);
	}
	
	/// Calls a REST POST request without any arguments
	public Object POST() {
		return callMethod(TYPE_POST, null, null);
	}
	
	//---------------------------------------
	// DELETE request handling
	//---------------------------------------
	
	/// Sets a REST DELETE request
	public RESTMethod setDELETE(Object baseObject, Method baseMethod) {
		return setMethod(TYPE_DELETE, baseObject, baseMethod);
	}
	
	/// Set a REST DELETE with just the method name
	public RESTMethod setDELETE(Object baseObject, String methodName) {
		return setMethod(TYPE_DELETE, baseObject, methodName);
	}
	
	/// Set a REST DELETE with the given class array
	public RESTMethod setDELETE(Object baseObject, String methodName, Class<?> classArr) {
		return setMethod(TYPE_DELETE, baseObject, methodName, classArr);
	}
	
	/// Set the REST DELETE default value
	public RESTMethod setDefaultDELETE(Map<String, ?> defMap, Object... defArg) {
		return setDefault(TYPE_DELETE, defMap, defArg);
	}
	
	/// Calls a REST DELETE request with the map & array arguments
	public Object requestDELETE(Map<String, ?> reqObject, Object... reqArgs) {
		return callMethod(TYPE_DELETE, reqObject, reqArgs);
	}
	
	/// Calls a REST DELETE request with the map arguments
	public Object requestDELETE(Map<String, ?> reqObject) {
		return callMethod(TYPE_DELETE, reqObject, null);
	}
	
	/// Calls a REST DELETE request with the array arguments
	public Object DELETE(Object... reqArgs) {
		return callMethod(TYPE_DELETE, null, reqArgs);
	}
	
	/// Calls a REST DELETE request without any arguments
	public Object DELETE() {
		return callMethod(TYPE_DELETE, null, null);
	}
	
}
