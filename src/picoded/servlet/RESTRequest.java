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

/// RESTRequest object handler, this is the first argument for all
/// functions registered to the RESTBuilder framework
public class RESTRequest extends HashMap<String, Object> {
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// The request arguments for the function, in its raw form
	public Object[] rawRequestArgs = null;
	
	/// The request arguments for the function, after applying default valeus
	public Object[] requestArgs = null;
	
	public RESTRequest(RESTSet baseSet, Map<String, Object> reqObj, Object[] reqArg) {
		rawRequestArgs = reqArg;
		requestSet = baseSet;
		
		// Setup the default set
		if (baseSet.defaultMap != null) {
			for (Map.Entry<String, Object> entry : baseSet.defaultMap.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
		
		// And default args
		requestArgs = new Object[baseSet.methodArgsLength];
		for (int a = 0; a < baseSet.methodArgsLength; ++a) {
			requestArgs[a] = baseSet.defaultArgs[a];
		}
		
		// Overwrite non default map
		if (reqObj != null) {
			for (Map.Entry<String, Object> entry : reqObj.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
		
		// And non default args
		if (reqArg != null) {
			for (int a = 0; a < reqArg.length; ++a) {
				requestArgs[a] = reqArg[a];
			}
		}
	}
	
	//-------------------------------------------------------------------
	// Internal function / package space
	//-------------------------------------------------------------------
	
	/// The RESTSet that contians the function call
	RESTSet requestSet = null;
	
	/// Does the actual request call, with the current request
	Object call() {
		try {
			// nothing to pass as arguments?
			if (requestSet.methodArgsClass == null || requestSet.methodArgsLength == 0) {
				if (requestSet.firstIsRESTRequest) {
					return requestSet.baseMethod.invoke(requestSet.baseObject, this);
				} else {
					return requestSet.baseMethod.invoke(requestSet.baseObject);
				}
			} else if (requestSet.firstIsRESTRequest) {
				return requestSet.baseMethod.invoke(requestSet.baseObject, this, requestArgs);
			} else {
				return requestSet.baseMethod.invoke(requestSet.baseObject, requestArgs);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return null;
	}
}
