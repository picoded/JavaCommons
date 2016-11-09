package picoded.page.builder;

import java.io.IOException;
import java.util.Set;

import picoded.struct.GenericConvertMap;
// JMTE inner functions add-on
// Sub modules useds

///
/// Map interface wrapper for page component, for use within JMTE and PageBuilder
///
/// @TODO : Deprecate, as HTML based pageComponents supercede this
///
public class PageComponentMap implements GenericConvertMap<String, Object> {
	
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
		if (keyStr.equalsIgnoreCase("html")) {
			try {
				return getHtml();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//
		// Return sub map
		//
		return getSubMap(keyStr);
	}
	
	/// Get and return the raw HTML (without any frame, or JMTE runtime)
	///
	/// @return raw HTML string of page (without frame)
	public String getHtml() throws IOException {
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
		if (keySet().contains(keyStr)) {
			if (prefixPath.length() > 0) {
				return new PageComponentMap(core, prefixPath + "/" + keyStr);
			} else {
				return new PageComponentMap(core, keyStr);
			}
		}
		return null;
	}
	
	/// Returns the potential sub page in the current directory
	@Override
	public Set<String> keySet() {
		Set<String> ret = core.subPageList(prefixPath.toString());
		ret.add("html");
		return ret;
	}
	
	/// Returns the toString for html if exists, else returns only the namespace (for reference)
	@Override
	public String toString() {
		String ret = null;
		try {
			ret = getHtml();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ret == null || ret.trim().length() <= 0) {
			return ("(Page." + prefixPath + ")").replaceAll("/", ".");
		}
		return ret;
	}
}
