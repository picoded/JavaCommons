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

import org.apache.commons.lang3.StringUtils;

/**
 * picoded.servlet.RESTBuilder is a utility class, in which facilitates the building of "RESTful API's"
 * that can be used in the project either via a public API, or even internally, via a direct function call.
 */
public class RESTBuilder extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// Blank constructor
	public RESTBuilder() {
	}
	
	///----------------------------------------
	/// Method adding / handling
	///----------------------------------------
	
	/// Stores the various methods currently in place of the RESTBuilder
	protected HashMap<String, RESTMethod> methodMap = new HashMap<String, RESTMethod>();
	
	/// Gets the api RESTMethod, to add the respetive GET/PUT/POST/DELETE calls
	public RESTMethod apiMethod(String namespace) {
		return apiMethod(namespace.replaceAll(".", "/").split("/"));
	}
	
	/// Gets the api RESTMethod, to add the respetive GET/PUT/POST/DELETE calls
	public RESTMethod apiMethod(String[] namespace) {
		String storeStr = StringUtils.join(namespace, "/");
		
		RESTMethod m = methodMap.get(storeStr);
		if (m == null) {
			m = new RESTMethod(storeStr);
			methodMap.put(storeStr, m);
		}
		return m;
	}
	
}