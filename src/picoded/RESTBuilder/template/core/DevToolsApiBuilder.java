package picoded.RESTBuilder.template.core;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;

public class DevToolsApiBuilder {
	
	/// The base builder to use
	protected RESTBuilder _builder = null;
	
	/// Constructor for the development api.
	/// @param The RESTBuilder object to do "development" on
	public DevToolsApiBuilder(RESTBuilder inBuilder) {
		_builder = inBuilder;
	}
	
	///
	/// # api.map (GET)
	///
	/// Gets the API keyspace mapping
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | filterPrefix    | String             | @TODO : The filter prefix to reduce down the result                           |
	/// | maxDepth        | Int                | @TODO : The maximum depth to reduce down the result                           |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | Object             | The object map that represents the API structure to return                    |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction api_tree_GET = (req, res) -> {
		if (_builder == null) {
			res.put("error", "RESTBuilder is null");
			return res;
		}
		
		res.put("data", _builder.namespaceTree());
		return res;
	};
	
	///
	/// Takes the restbuilder and implements its respective default API
	///
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb) {
		return setupRESTBuilder(rb, "dev.");
	}
	
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix) {
		return setupRESTBuilder(rb, new DevToolsApiBuilder(rb), setPrefix);
	}
	
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, DevToolsApiBuilder dev,
		String setPrefix) {
		// Get entire mapping
		rb.getNamespace(setPrefix + "api/tree").put(HttpRequestType.GET, dev.api_tree_GET);
		
		return rb;
	}
}
