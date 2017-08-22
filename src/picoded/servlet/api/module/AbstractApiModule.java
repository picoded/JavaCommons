package picoded.servlet.api.module;

import picoded.servlet.api.*;
import picoded.core.conv.*;
import picoded.core.struct.*;

/**
* The Abstract ApiModule that does some basic implementation for ApiModule.
* This makes it easier to complete the implementation of the ApiModule interface
**/
abstract public class AbstractApiModule implements ApiModule {

	//-------------------------------------------------------------
	// Stored objects
	//-------------------------------------------------------------
	protected ApiBuilder api = null;
	protected String prefixPath = null;
	protected GenericConvertMap<String,Object> config = null;

	//-------------------------------------------------------------
	// API setup, load and config functions
	//-------------------------------------------------------------

	/**
	* Does the actual setup for the API
	* Given the API Builder, and the namespace prefix
	*
	* @param  api         ApiBuilder to add the required functions
	* @param  prefixPath  prefix to assume as (should be able to accept "" blanks)
	* @param  config      configuration object, assumed to be a map. use GenericConvert.toStringMap to preprocess the data
	*/
	public void configSetup(ApiBuilder inApi, String inPrefixPath, Object inConfigMap) {
		// Set the api and its path prefix
		api = inApi;
		prefixPath = inPrefixPath;

		// Set the config
		config = GenericConvert.toGenericConvertStringMap(inConfigMap);
	}

	// NOTE: Until a use case for this is found, config getter is commented out
	// /**
	// * Configuration getter
	// * 
	// * @return GenericConvertMap<String,Object> representing the updated config
	// */
	// public GenericConvertMap<String,Object> config() {
	// 	return config;
	// }

}