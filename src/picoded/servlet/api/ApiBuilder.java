package picoded.servlet.api;

import java.util.*;

///
/// ApiBuilder is a utility class, in which facilitates the building of modern JSON pure API's.
/// This can be used in the project either via a public API, or even internally via a direct function call.
///
/// While faciltating the following....
///
/// + Packaging the entire web project core functionalities
/// + JS Api script generation
/// + Swagger compliant API documentation generation
/// + Internal function calls
///
/// While, it replaces the original servlet framework role of creating JSON output page. The framework is 
/// also meant to facilitate intra project function calls. One of the examples learnt from the LMS project, 
/// in handling the encryption of export files (for example). Is that instead of rewriting the entire export 
/// module code to be "callable" by a function a rather indirect and "inefficent" method of calling its 
/// local page directly was used. Aka an encryption proxy. Due to the time constraints of the project.
///
/// Simply put, if a standardised REST API builder was built and used, several page API features can be 
/// called directly instead of being usued via a proxy
///
public class ApiBuilder {
	
	//////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	//////////////////////////////////////////////////////////////////
	
	/// Blank constructor, to build a root end point
	public ApiBuilder() {
		// Blank constructor
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Common exceptions string
	//
	//////////////////////////////////////////////////////////////////
	
	/// Exception for methods not supported in root node
	public static String UNSUPPORTED_IN_ROOT_NODE = "ApiBuilder root node : Does not support parent/version/path methods";
	
	/// Exception for methods ONLY supported in root node
	public static String SUPPORTED_ONLY_IN_ROOT_NODE = "ApiBuilder sub node : Version initialization is supported only in root node";
	
	//////////////////////////////////////////////////////////////////
	//
	// Core structure vars
	//
	//////////////////////////////////////////////////////////////////
	
	/// ApiBuilder absolute root
	protected ApiBuilder absRoot = this;
	
	/// ApiBuilder version roots
	protected ApiBuilder verRoot = null;
	
	/// ApiBuilder parent node
	protected ApiBuilder parent = null;
	
	/// @return boolean true if this is the root node
	public boolean isRoot() {
		return ( absRoot == this );
	}
	
	/// Getting the absolute root, of the API
	///
	/// @return  The root API node. Used to gurantee the root node used
	public ApiBuilder root() {
		return absRoot;
	}
	
	/// Version root, of the API
	/// 
	/// @return  Version root API node.
	public ApiBuilder versionRoot() {
		if( isRoot() ) {
			throw new RuntimeException(UNSUPPORTED_IN_ROOT_NODE);
		}
		return verRoot;
	}
	
	/// Parent node, of the API
	///
	/// @return  Parent node
	public ApiBuilder parent() {
		if( isRoot() ) {
			throw new RuntimeException(UNSUPPORTED_IN_ROOT_NODE);
		}
		return parent;
	}
	
	/// Current API version (taken for verRoot)
	///
	/// @return  version string (not inlcluding the v prefix)
	public String version() {
		if( verRoot == null ) {
			throw new RuntimeException(UNSUPPORTED_IN_ROOT_NODE);
		}
		return verRoot.version();
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Path namespace handling
	//
	//////////////////////////////////////////////////////////////////
	
	/// Fixed URI api path step map
	protected Map<String,ApiBuilder> fixedPath = new HashMap<String,ApiBuilder>();
	
	/// Dynamic named URI api path step map
	protected Map<String,ApiBuilder> dynamicPath = new HashMap<String,ApiBuilder>();
	
	/// Getting the version node, to start building the API =)
	///
	/// @param  The version string 
	///
	/// @return  version specific ApiBuilder node
	public ApiBuilder version(String reqVer) {
		if( isRoot() ) {
			String fixedKey = "v"+reqVer;
			ApiBuilder ret = fixedPath.get(fixedKey);
			if( ret == null ) {
				ret = new ApiBuilderVersion(this, reqVer);
				fixedPath.put(fixedKey, ret);
			}
			return ret;
		} else {
			throw new RuntimeException(SUPPORTED_ONLY_IN_ROOT_NODE);
		}
	}
	
	/// Path namespace handling, to actually build the API
	///
	/// @param  The path string
	///
	/// @return  The path aligned node
	public ApiBuilder path(String path) {
		if( isRoot() ) {
			throw new RuntimeException(UNSUPPORTED_IN_ROOT_NODE);
		}
		return multiplePathSteps(pathArray(path));
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Inner path utility management
	// ONLY applicable for extended classes (not root layer)
	//
	//////////////////////////////////////////////////////////////////
	
	/// Does an array of single steps to fetch the relevent ApiBuilder
	///
	/// @param   The path string array
	///
	/// @return  The path aligned ApiBuilder
	protected ApiBuilder multiplePathSteps(String[] steps) {
		ApiBuilder ret = this;
		for(String step : steps) {
			ret = this.singlePathStep(step);
		}
		return this;
	}
	
	/// Does a single step to fetch the target ApiBuilder
	///
	/// @param  The path string
	///
	/// @return  The path aligned ApiBuilder
	protected ApiBuilder singlePathStep(String pathStep) {
		// Minor santization step
		pathStep = pathStep.trim();
		
		// Map pathing to fetch from
		Map<String,ApiBuilder> pathMap = fixedPath;
		
		// Use the dynamic path, INSTEAD of fixed path
		if( pathStep.startsWith("{") && pathStep.endsWith("}") ) {
			pathMap = dynamicPath;
		}
		
		// ApiBuilder path and fetching
		ApiBuilder ret = pathMap.get(pathStep);
		if( ret == null ) {
			ret = new ApiBuilderNode(this);
			pathMap.put(pathStep, ret);
		}
		return ret;
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Path string manipulation
	//
	//////////////////////////////////////////////////////////////////
	
	/// Namespace filter, amends common namespace errors
	///
	/// @param  path to sanitize
	///
	/// @return Sanitize path return
	protected String pathFilter(String inPath) {
		// Basic sanitization
		inPath = inPath.split("\\?")[0].replaceAll("\\.", "/");
		
		// Sanitized repeated "//"
		while( inPath.indexOf("//") >= 0 ) {
			inPath = inPath.replaceAll("//", "/");
		}
		
		// Sanitize ending and starting "/"
		while( inPath.startsWith("/") ) {
			inPath = inPath.substring(1);
		}
		while( inPath.endsWith("/") ) {
			inPath = inPath.substring(0, inPath.length() - 1);
		}
		
		// Return the path
		return inPath;
	}
	
	/// Filters and get the storage namespace
	///
	/// @param  path to sanitize and split
	///
	/// @return Split sanitized path return
	protected String[] pathArray(String inPath) {
		return pathFilter(inPath).split("/");
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Function setup
	//
	//////////////////////////////////////////////////////////////////
	
}
