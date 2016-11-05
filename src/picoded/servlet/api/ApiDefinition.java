package picoded.servlet.api;

import java.util.*;

import picoded.enum.HttpRequestType;

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
	// HTTP request namespace
	//
	/////////////////////////////////////////////
	
	protected int supportedHttpRequests = HttpRequestType.GET | HttpRequestType.POST;
	
}
