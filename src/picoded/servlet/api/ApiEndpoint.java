package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.set.HttpRequestType;

///
/// This represents a single API Endpoint,
/// Its definition, and its respective lambda function
///
/// Note that this classs is meant to be used internally
/// by ApiBuilder, and not to be created directly
///
public class ApiEndpoint {

	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// The internal raw path name
	protected String apiPath = null;

	/// actual function call lamda 
	protected BiFunction<ApiRequest, ApiResponse, ApiResponse> functionLambda = null;

	/// Null Endpoint builder, does nothing
	ApiEndpoint() {
	}
	
	/// Setup an ApiEndpoint with a lamdba function
	///
	/// @param  The defined api path
	/// @param  Root ApiBuilder node
	ApiEndpoint(String inPath, BiFunction<ApiRequest, ApiResponse, ApiResponse> inFunc) {
		// The api path to use
		apiPath = inPath;
		// Parent node to setup
		functionLambda = inFunc;
	}
	
	/////////////////////////////////////////////
	//
	// Execution !
	//
	/////////////////////////////////////////////
	
	/// Execute a request
	///
	/// @param   request object to do execution on
	/// @param   response object to "respond to"
	///
	/// @return  The result parameters
	public ApiResponse execute( ApiRequest req, ApiResponse res ) {
		return functionLambda.apply(req, res);
	}
}