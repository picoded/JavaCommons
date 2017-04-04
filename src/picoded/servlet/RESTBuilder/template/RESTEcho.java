package picoded.RESTBuilder.template;

import picoded.RESTBuilder.*;

///
/// Utility function for doing just echo's of REST requests
/// Useful for debugging purposes of the API connection
///
public class RESTEcho {
	
	/// Takes everything in request, and outputs to result
	public static RESTFunction echoFunction = (req, res) -> {
		res.putAll(req);
		return res;
	};
	
}
