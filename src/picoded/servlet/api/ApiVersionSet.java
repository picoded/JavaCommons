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
}