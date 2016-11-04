package picoded.servlet.api;

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
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// Blank constructor, to build a root end point
	public ApiBuilder() {
		// Blank constructor
	}
	
	/////////////////////////////////////////////
	//
	// Core structure vars
	//
	/////////////////////////////////////////////
	
	/// ApiBuilder absolute root
	protected ApiBuilder absRoot = this;
	
	/// ApiBuilder version roots
	protected ApiBuilder verRoot = null;
	
	/// Version, of the api (assuming ver root)
	protected String ver = null;
	
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
	public ApiBuilder verRoot() {
		if( verRoot == null ) {
			throw new RuntimeException("Root node, does not have a version root");
		}
		return verRoot;
	}
	
	/// Current API version (taken for verRoot)
	///
	/// @return  version string (not inlcluding the v prefix)
	public String version() {
		if( verRoot == null ) {
			throw new RuntimeException("Root node, does not have a version root");
		}
		return verRoot.version();
	}
	
	/////////////////////////////////////////////
	//
	// Path namespace handling
	//
	/////////////////////////////////////////////
	
	/// Fixed URI api path step map
	protected Map<String,ApiBuilder> fixedPath = new HashMap<String,ApiBuilder>();
	
	/// Dynamic named URI api path step map
	protected Map<String,ApiBuilder> dynamicPath = new HashMap<String,ApiBuilder>();
	
	/// Getting the version node, to start building the API =)
	///
	/// @param  The version string 
	///
	/// @return  version specific ApiBuilder node
	public ApiBuidler version(String reqVer) {
		if( isRoot() ) {
			String fixedKey = "v"+reqVer;
			ApiBuidler ret = fixedPath.get(fixedKey);
			if( ret == null ) {
				ret = new ApiBuilderVersion(this, reqVer);
				fixedPath.put(fixedKey, ret);
			}
			return ret;
		} else {
			throw new RuntimeException("Extending version, is only allowed in Root node");
		}
	}
}
