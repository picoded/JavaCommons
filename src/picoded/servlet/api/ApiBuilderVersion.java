package picoded.servlet.api;

import java.util.*;

///
/// Version specific varient of ApiBuilder
///
class ApiBuilderVersion extends ApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// Version, of the api
	protected String ver = null;
	
	/// Version specific ApiBuilder
	///
	/// @param  Root node to use
	/// @param  Version string to use
	ApiBuilderVersion(ApiBuilder root, String inVer) {
		absRoot = root;
		verRoot = this;
		parent = root;
		ver = inVer;
	}
	
	/// Current API version
	///
	/// @return  version string (not inlcluding the v prefix)
	public String version() {
		return ver;
	}
}
