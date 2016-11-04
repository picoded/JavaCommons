package picoded.servlet.api;

///
/// Version specific varient of ApiBuilder
///
class ApiBuilderVersion extends ApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// Version specific ApiBuilder
	///
	/// @param  Root node to use
	/// @param  Version string to use
	ApiBuilderVersion(ApiBuilder root, String inVer) {
		absRoot = root;
		verRoot = this;
		ver = inVer;
	}
	
}
