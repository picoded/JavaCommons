package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.set.HttpRequestType;

///
/// Represents the various different function / filter maps
/// specified specifically for a single version
///
public class ApiVersionSet {

	/// The endpoint map, for this api version
	Map<String, ApiEndpoint> endpointMap = new HashMap<String, ApiEndpoint>();

	/// The filter map for

	/// Takes in another endpoint map, and overwrite it onto itself!
	/// This is used internally by "collapsedVersionSet" functions
	///
	/// @param  External version set to import 
	protected void importVersionSet(ApiVersionSet importSet) {
		endpointMap.putAll( importSet.endpointMap );
	}
}