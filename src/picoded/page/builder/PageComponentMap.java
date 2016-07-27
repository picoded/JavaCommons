package picoded.page.builder;

import java.io.*;
import java.util.*;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Map interface wrapper for page components, for use within JMTE and PageBuilder
///
public class PageComponentMap implements GenericConvertMap<String,Object> {
	
	////////////////////////////////////////////////////////////
	//
	// Local vars & Constructor
	//
	////////////////////////////////////////////////////////////
	
	/// The core pagebuilder object, to pull the data from
	protected PageBuilderCore core = null;
	
	/// The prefix pathing to assume
	protected String prefixPath = null;
	
	///
	/// Constructor : avoid using this. Please call from PageBuilderCore instad
	///
	/// @param  Actual implmentation to fetch from
	/// @param  Prefix to use, for nested fetching
	///
	public PageComponentMap(PageBuilderCore inCore, String inPrefix) {
		core = inCore;
		prefixPath = inPrefix;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Map implmentation
	//
	////////////////////////////////////////////////////////////
	
	/// Get and return the relevent item
	///
	/// @param  Keyname to use
	///
	/// @return  Result according to keyname
	@Override
	public Object get(Object key) {
		String keyStr = key.toString();
		
		//
		// Get the raw HTML 
		//
		if( keyStr.equalsIgnoreCase("html") ) {
			return getHtml();
		}
		
		//
		// Return sub map
		//
		return getSubMap(keyStr);
	}
	
	/// Get and return the raw HTML (without any frame, or JMTE runtime)
	///
	/// @return raw HTML string of page (without frame)
	public String getHtml() {
		return core.buildPageInnerRawHTML(prefixPath);
	}
	
	/// Get the children submap (if it exsits)
	///
	/// @param  Keyname to use
	///
	/// @return  Children mapping only if it exists
	public PageComponentMap getSubMap(Object key) {
		String keyStr = key.toString();
		
		//
		// Return sub map
		//
		if( keySet().contains(keyStr) ) {
			if(prefixPath.length() > 0) {
				return new PageComponentMap(core, prefixPath+"/"+keyStr);
			} else {
				return new PageComponentMap(core, keyStr);
			}
		}
		return null;
	}
	
	/// Returns the potential sub pages in the current directory
	@Override
	public Set<String> keySet() {
		Set<String> ret = core.subPagesList(prefixPath.toString());
		ret.add("html");
		return ret;
	}
	
	/// Returns the toString for html if exists, else returns only the namespace (for reference)
	@Override
	public String toString() {
		String ret = getHtml();
		if( ret == null || ret.trim().length() <= 0 ) {
			return ("(Page."+prefixPath+")").replaceAll("/",".");
		}
		return ret;
	}
}
