package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.core.common.HttpRequestType;

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
	Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> beforeFilterMap = new HashMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>>();
	Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> afterFilterMap = new HashMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>>();
	
	/**
	 * Takes in another endpoint map, and overwrite it onto itself!
	 * This is used internally by "collapsedVersionSet" functions
	 *
	 * @param  External version set to import
	 **/
	protected void importVersionSet(ApiVersionSet importSet) {
		// Endpoints
		endpointMap.putAll(importSet.endpointMap);

		// Filters
		beforeFilterMap.putAll(importSet.beforeFilterMap);
		afterFilterMap.putAll(importSet.afterFilterMap);
	}

	/**
	 * Enum set for helper function access for before / endpoint / after function maps
	 */
	static enum ApiFunctionType {
		BEFORE,
		ENDPOINT,
		AFTER
	};
	
	/**
	 * Get the function map of the specified type
	 *
	 * @param  ApiFunctionType type of map to fetch
	 * 
	 * @return The function map to manipulation
	 */
	Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> functionMap( ApiFunctionType type ) {
		if( type == ApiFunctionType.BEFORE ) {
			return beforeFilterMap;
		} else if( type == ApiFunctionType.ENDPOINT ) {
			return endpointMap;
		} else if( type == ApiFunctionType.AFTER ) {
			return afterFilterMap;
		}
		throw new IllegalArgumentException("Unknown ApiFunctionType : "+type);
	}
}
