package picoded.webTemplateEngines.PagesBuilder;

import java.io.*;
import java.util.*;

import picoded.conv.*;
import picoded.servlet.*;
import picoded.fileUtils.FileUtils;

///
/// Utilty class, handles the basic HTML only pages building
///
/// @TODO
/// + Minify the pagename/index.html
///
public class PagesHTML {
	
	////////////////////////////////////////////////////////////
	//
	// Local variables
	//
	////////////////////////////////////////////////////////////
	
	/// The folder to get the various page definition from
	protected File pagesFolder = null;
	
	/// The local JMTE reference
	protected JMTE jmteObj = null;
	
	/// The URI root context for built files
	protected String uriRootPrefix = "";
	
	// Variables caches
	//----------------------------------------------------------
	
	/// HTML Prefix cache
	protected String _prefixHTML = null;
	
	/// HTML Suffix cache
	protected String _suffixHTML = null;
	
	////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesHTML(File inPagesFolder) {
		pagesFolder = inPagesFolder;
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesHTML(String inPagesFolder) {
		this(new File(inPagesFolder));
	}
	
	////////////////////////////////////////////////////////////
	//
	// Protected vars access
	//
	////////////////////////////////////////////////////////////
	
	/// @returns Gets the protected JMTE object, used internally. 
	///          This is autocreated if not set
	public JMTE getJMTE() {
		if (jmteObj == null) {
			jmteObj = new JMTE();
		}
		return jmteObj;
	}
	
	/// Overides the default (if loaded) JMTE object. 
	public void setJMTE(JMTE set) {
		jmteObj = set;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Basic HTML setup
	//
	////////////////////////////////////////////////////////////
	
	/// Generates the needed map string template for the respective page
	protected Map<String, Object> pageJMTEvars(String pageName) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		String pageURI = uriRootPrefix + "/" + pageName + "/";
		
		ret.put("PagesRootURI", uriRootPrefix);
		ret.put("PageURI", pageURI);
		ret.put("PageName", pageName);
		
		return ret;
	}
	
	/// Gets the prefix
	public String prefixHTML(String pageName) {
		String buffer = FileUtils.readFileToString_withFallback(new File(pagesFolder, "index/prefix.html"), "UTF-8", "");
		return getJMTE().parseTemplate(buffer, pageJMTEvars(pageName));
	}
	
	/// Gets the prefix
	public String prefixHTML() {
		// Cached copy
		if (_prefixHTML != null) {
			return _prefixHTML;
		}
		// Return it
		return _prefixHTML = prefixHTML("index");
	}
	
	/// Gets the prefix
	public String suffixHTML(String pageName) {
		String buffer = FileUtils.readFileToString_withFallback(new File(pagesFolder, "index/suffix.html"), "UTF-8", "");
		return getJMTE().parseTemplate(buffer, pageJMTEvars(pageName));
	}
	
	/// Gets the suffix
	public String suffixHTML() {
		// Cached copy
		if (_suffixHTML != null) {
			return _suffixHTML;
		}
		// Return it
		return _suffixHTML = suffixHTML("index");
	}
	
	/// Get pageFrame
	public String buildPageFrame(String pageName) {
		return buildPageFrame(pageName, false);
	}
	
	public String buildPageFrame(String pageName, boolean isHidden) {
		StringBuilder frame = new StringBuilder();
		frame.append("<div class='pageFrame pageFrame_" + pageName + "'");
		if (isHidden) {
			frame.append(" style='display:none;'");
		}
		frame.append(">\n");
		frame.append(FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName + "/" + pageName + ".html"),
			"UTF-8", ""));
		frame.append("\n</div>\n");
		return getJMTE().parseTemplate(frame.toString(), pageJMTEvars(pageName));
	}
	
	/// HTML builder 
	public StringBuilder buildFullPageFrame(String pageName) {
		StringBuilder ret = new StringBuilder();
		ret.append(prefixHTML(pageName));
		ret.append(buildPageFrame(pageName));
		ret.append(suffixHTML(pageName));
		return ret;
	}
}
