package picoded.servlet.api;

import java.util.*;

import picoded.set.HttpRequestType;

///
/// API Request map information
/// For the API function to process
///
public class ApiDefinition {
	
	/////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////
	
	/// ApiBuilder parent path
	protected ApiBuilder parent = null;
	
	/// Version specific ApiBuilder
	///
	/// @param  in parent node
	ApiDefinition(ApiBuilder path) {
		// Parent node to setup
		parent = path;
	}
	
	/////////////////////////////////////////////
	//
	// HTTP request path handling
	//
	/////////////////////////////////////////////
	
	/// HTTP Request types supported
	/// The default supported is both GET & POST
	protected EnumSet<HttpRequestType> supportedHttpRequests = EnumSet.of(HttpRequestType.GET, HttpRequestType.POST);
	
	
}
