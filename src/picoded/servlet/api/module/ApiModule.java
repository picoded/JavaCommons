package picoded.servlet.api.module;

import picoded.servlet.api.*;

/**
* The ApiModule template
**/
public interface ApiModule {

	/**
	* [To-overwrite for actual implementation]
	* Does the actual setup for the API
	* Given the API Builder, and the namespace prefix
	*
	* @param  API builder to add the required functions
	* @param  Path to assume as prefix (should be able to accept "" blanks
	**/
	default void setupApiBuilder(ApiBuilder api, String path) {
		throw new UnsupportedOperationException("Missing the respective setup implementation");
	}

	/**
	* Convinence alterantive, where it assume the prefix is blank.
	**/
	default void setupApiBuilder(ApiBuilder api) {
		setupApiBuilder(api, "");
	}

}
