package picoded.servlet.api;

import java.util.*;

///
/// Version specific varient of ApiBuilder
///
class ApiBuilderNode extends ApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// Version specific ApiBuilder
	///
	/// @param  in parent node
	ApiBuilderNode(ApiBuilder inParent) {
		// Parent node to setup
		parent = inParent;
		
		// AbsRoot and VerRoot
		absRoot = inParent.root();
		verRoot = inParent.versionRoot();
	}
	
}
