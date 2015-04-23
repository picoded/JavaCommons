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

// Ext lib used
import org.apache.commons.lang3.ArrayUtils;

///
/// RESTRequest object handler, this is usually the first argument for all
/// functions registered to the RESTBuilder framework
///
/// It represents the request object, and include utility functions 
///
public class RESTRequest extends HashMap<String, Object> {
	
	//--------------------------------------------------------------------------------
	// Protected vars
	//--------------------------------------------------------------------------------
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// Base object which represents the "this" argument during the function call
	protected Object baseObject = null;
	
	/// Base method that is actually executed
	protected Method baseMethod = null;
	
	/// Indicates if the base method starts with RESTRequest
	protected boolean baseMethodUsesRestRequest = false;
	
	//--------------------------------------------------------------------------------
	// Public request vars
	//--------------------------------------------------------------------------------
	
	/// The request arguments for the function, in its raw form
	public Object[] rawRequestArgs = null;
	
	/// The request arguments for the function, after applying default valeus,
	/// and limiting it to the accepted argument length
	public Object[] requestArgs = null;
	
	//--------------------------------------------------------------------------------
	// Constructor and setup
	//--------------------------------------------------------------------------------
	
	/// [For internal use] Builds the rest request with the default values only
	///
	/// @param bObj      baseObject representing the "this" for the method call
	/// @param bMeth     baseMethod is the function which is actually called
	/// @param bRest     boolean used to indicate if first argument is RESTRequest
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	public RESTRequest(Object bObj, Method bMeth, boolean bRest, //
		Map<String, ?> defMap, Object[] defArg //
	) {
		commonSetup(bObj, bMeth, bRest, defMap, defArg);
	}
	
	/// [For internal use] Builds the full rest request with the given base, default, and request values
	/// Note that this constructor is meant for use internally, and not meant to be called directly
	///
	/// @param bObj      baseObject representing the "this" for the method call
	/// @param bMeth     baseMethod is the function which is actually called
	/// @param bRest     boolean used to indicate if first argument is RESTRequest
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	/// @param reqMap    request argument map, which overrides the default
	/// @param reqArg    request argument list, which overrides the default
	public RESTRequest(Object bObj, Method bMeth, boolean bRest, //
		Map<String, ?> defMap, Object[] defArg, //
		Map<String, ?> reqMap, Object[] reqArg //
	) {
		commonSetup(bObj, bMeth, bRest, defMap, defArg);
		setupRequestArgs(reqMap, reqArg);
	}
	
	/// [For internal use] Common setup function
	///
	/// @param bObj      baseObject representing the "this" for the method call
	/// @param bMeth     baseMethod is the function which is actually called
	/// @param bRest     boolean used to indicate if first argument is RESTRequest
	/// @param defMap    default arguments map
	/// @param defArg    default request arguments list
	protected void commonSetup(Object bObj, Method bMeth, boolean bRest, //
		Map<String, ?> defMap, Object[] defArg //
	) {
		
		baseObject = bObj;
		baseMethod = bMeth;
		baseMethodUsesRestRequest = bRest;
		
		// Setup the default set
		if (defMap != null) {
			for (Map.Entry<String, ?> entry : defMap.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
		
		// Setup the default args
		if (defArg != null) {
			requestArgs = new Object[defArg.length];
			for (int a = 0; a < defArg.length; ++a) {
				requestArgs[a] = defArg[a];
			}
		}
		
	}
	
	/// Sets up the request arguments, this is useful if the constructor occurs
	/// with only the default values first
	///
	/// @param reqMap    request argument map, which overrides the default
	/// @param reqArg    request argument list, which overrides the default
	///
	/// @returns RESTRequest self object, for function chaining
	public RESTRequest setupRequestArgs(Map<String, ?> reqMap, Object[] reqArg) {
		
		// Overwrite non default map
		if (reqMap != null) {
			for (Map.Entry<String, ?> entry : reqMap.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
		
		// And non default args
		if (reqArg != null) {
			// Make the raw request args avaliable
			rawRequestArgs = reqArg;
			
			int aLen = (requestArgs == null) ? 0 : Math.min(reqArg.length, requestArgs.length);
			for (int a = 0; a < aLen; ++a) {
				requestArgs[a] = reqArg[a];
			}
		}
		
		return this;
	}
	
	//-------------------------------------------------------------------
	// Internal function / package space
	//-------------------------------------------------------------------
	
	/// Does the actual request call, with the current request
	///
	/// Note that care should be done to make when doing this call by the internal method itself
	/// which can result into a never ending recursive loop.
	///
	/// @returns  the value returned by the method
	public Object call() {
		if (requestArgs == null) {
			requestArgs = ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		if (rawRequestArgs == null) {
			rawRequestArgs = ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		
		try {
			if (baseMethodUsesRestRequest) {
				// The function requires RESTRequest as first argument
				if (requestArgs.length == 0) {
					return baseMethod.invoke(baseObject, this);
				} else {
					return baseMethod.invoke(baseObject, this, requestArgs);
				}
			} else {
				// The function does not accepts RESTRequest as first argument
				if (requestArgs.length == 0) {
					return baseMethod.invoke(baseObject);
				} else {
					return baseMethod.invoke(baseObject, requestArgs);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return null;
	}
}
