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
	
	/// actual function call lamda 
	protected BiFunction<ApiRequest, ApiResponse, ApiResponse> functionLamda = null;
	
	/// Null Endpoint builder, does nothing
	ApiEndpoint() {
	}
	
	/// Setup an ApiEndpoint with a lamdba function
	///
	/// @param  Root ApiBuilder node
	ApiEndpoint(BiFunction<ApiRequest, ApiResponse, ApiResponse> inFunc) {
		// Parent node to setup
		functionLamda = inFunc;
	}
	
	/////////////////////////////////////////////
	//
	// Lamda registration, and setup
	//
	/////////////////////////////////////////////
	

}