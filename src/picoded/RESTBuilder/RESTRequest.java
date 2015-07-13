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

import picoded.struct.GenericConvertMap;

// Ext lib used
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.collections4.map.AbstractMapDecorator;

///
/// RESTRequest object handler, this is usually the first argument for all
/// functions registered to the RESTBuilder framework
///
/// It represents the request object, and include utility functions 
///
public class RESTRequest extends AbstractMapDecorator<String, Object> implements GenericConvertMap<String, Object>  {
	
	//--------------------------------------------------------------------------------
	// Protected vars
	//--------------------------------------------------------------------------------
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// [internal] avoid use this directly
	public RESTRequest(Map<String, Object> map) {
		super(map);
	}
	
	/// [internal] avoid use this directly
	public RESTRequest() {
		super(new HashMap<String,Object>());
	}
	
	/// The requesting CorePage, if given
	protected picoded.servlet.CorePage requestPage = null;
	
}
