package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

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
	// Lamda registration, and setup
	//
	/////////////////////////////////////////////
	
	/// definition setup lamda (if given)
	protected Consumer<ApiDefinition> defineLamda = null;
	
	/// actual function call lamda 
	protected BiFunction<ApiRequest, ApiResponse, ApiResponse> functionLamda = null;
	
	/// Boolean flag to indicate if defineLamda has been called
	protected boolean completeSetupFlag = false;
	
	/// calls the lamda definition for setup if present
	protected void completeSetup() {
		// Skip if previously completed
		if(completeSetupFlag) {
			return;
		}
		
		// Execute lamda if it is given
		if(defineLamda != null) {
			defineLamda.accept(this);
		}
		
		// Set completedSetup flag
		completeSetupFlag = true;
	}
	
	/// @param  Setup the function lamda
	protected void setup(Consumer<ApiDefinition> inDefine) {
		// Ignore null calls
		if(inDefine == null) {
			return;
		}
		
		// Potential conflict, to check / resolve
		if(defineLamda != inDefine) {
			// Existing setup already registered
			if(defineLamda != null) {
				throw new IllegalArgumentException( ApiBuilder.DEFINE_SETUP_CONFLICT );
			}
			// null setup was previously called
			if(completeSetupFlag) {
				throw new IllegalArgumentException( ApiBuilder.DEFINE_SETUP_CALLED );
			}
		}
		
		// Actual setup
		defineLamda = inDefine;
	}
	
	/// @param  Setup the definition lamda
	protected void setup(BiFunction<ApiRequest, ApiResponse, ApiResponse> inFunction) {
		// Ignore null calls
		if(inFunction == null) {
			return;
		}
		
		// Potential conflict, to check / resolve
		if(functionLamda != inFunction) {
			// Existing setup already registered
			if(functionLamda != null) {
				throw new IllegalArgumentException( ApiBuilder.FUNCTION_SETUP_CONFLICT );
			}
		}
		
		// Actual setup
		functionLamda = inFunction;
	}
	
	/////////////////////////////////////////////
	//
	// HTTP Request type handling
	//
	/////////////////////////////////////////////
	
	/// HTTP Request types supported
	/// The default supported is both GET & POST
	///
	/// @TODO implmentation support
	protected EnumSet<HttpRequestType> supportedHttpRequests = EnumSet.of(HttpRequestType.GET, HttpRequestType.POST);
	 
}
