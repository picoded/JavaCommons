package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.set.HttpRequestType;

/**
* Represents the various different function / filter maps
* specified specifically for a single version
**/
public class ApiVersionSet {

	/**
	* The endpoint map, for this api version
	**/
	Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> endpointMap = new HashMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>>();

	/**
	* The filter map for
	**/
	Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> filterMap = new HashMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>>();

	/**
	* Takes in another endpoint map, and overwrite it onto itself!
	* This is used internally by "collapsedVersionSet" functions
	*
	* @param  External version set to import
	**/
	protected void importVersionSet(ApiVersionSet importSet) {
		filterMap.putAll( importSet.filterMap );
		endpointMap.putAll( importSet.endpointMap );
	}
}
