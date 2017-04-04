package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

///
/// Version specific varient of ApiBuilder
///
class ApiBuilderNode extends ApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// Path step that represents itself
	protected String pathStep = null;
	
	/// Version specific ApiBuilder
	///
	/// @param  in parent node
	ApiBuilderNode(ApiBuilder inParent, String inStep) {
		// Parent node to setup
		parent = inParent;
		
		// AbsRoot and VerRoot
		absRoot = inParent.root();
		verRoot = inParent.versionRoot();
		
		// PathStepping
		pathStep = inStep;
	}
	
	/////////////////////////////////////////////
	//
	// Lamda registration
	//
	/////////////////////////////////////////////
	
	/// Does the setup with both the define lambda and function lambda,
	/// All other setup functions, is simply a variation / extension.
	///
	/// Note the following 
	/// + NULL values are ignored. 
	/// + No exception is thrown if both is NULL
	/// + Exception is thrown if existing value exists
	/// + Exception is thrown for root and version nodes
	///
	/// @param  Definition lamda to use
	/// @param  Function lamda to use
	///
	/// @return  Itself
	@Override
	public ApiBuilder setup(Consumer<ApiDefinition> defineLamda,
		BiFunction<ApiRequest, ApiResponse, ApiResponse> functionLamda) {
		ApiDefinition d = definition();
		d.setup(defineLamda);
		d.setup(functionLamda);
		
		return this;
	}
	
	/////////////////////////////////////////////
	//
	// Lamda management
	//
	/////////////////////////////////////////////
	
	/// Definition to use for the node
	protected ApiDefinition definition = null;
	
	/// Definition fetch, setup if needed
	protected ApiDefinition definition() {
		if (definition == null) {
			definition = new ApiDefinition(this);
		}
		return definition;
	}
	
	//////////////////////////////////////////////////////////////////
	//
	// Lamda Function direct invocation
	//
	//////////////////////////////////////////////////////////////////
	
	/// WIP
	public ApiResponse execute() {
		ApiRequest req = new ApiRequest();
		ApiResponse res = new ApiResponse();
		
		ApiDefinition def = definition();
		def.completeSetup();
		
		return def.functionLamda.apply(req, res);
	}
}
