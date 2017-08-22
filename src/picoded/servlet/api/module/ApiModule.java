package picoded.servlet.api.module;

import picoded.servlet.api.*;
import picoded.core.struct.GenericConvertMap;

/**
* The ApiModule interface template, without the 
**/
public interface ApiModule {

	//-------------------------------------------------------------
	// configuration setup
	//-------------------------------------------------------------

	/**
	* [To-overwrite for actual implementation]
	*
	* Does the configuration setup for the api module
	* Given the API Builder, and the namespace prefix
	*
	* @param  api         ApiBuilder to add the required functions
	* @param  prefixPath  prefix to assume as (should be able to accept "" blanks)
	* @param  config      configuration object, assumed to be a map. use GenericConvert.toStringMap to preprocess the data
	*/
	default void configSetup(ApiBuilder api, String path, Object configMap) {
		throw new UnsupportedOperationException("Missing the respective setup implementation");
	}

	/**
	* Convinence alterantive, where it assume the config is blank.
	**/
	default void configSetup(ApiBuilder api, String path) {
		configSetup(api,path,"{}");
	}

	/**
	* Convinence alterantive, where it assume the path, and config is blank.
	**/
	default void configSetup(ApiBuilder api) {
		configSetup(api, "");
	}

	//-------------------------------------------------------------
	// system setup
	//-------------------------------------------------------------
	
	// NOTE: Until a use case for this is found, config getter is commented out
	// /**
	// * Configuration getting, that was used in configSetup
	// *
	// * @return GenericConvertMap<String,Object> representing the current config
	// */
	// default GenericConvertMap<String,Object> config() {
	// 	throw new UnsupportedOperationException("Missing the respective config implementation");
	// }
}
