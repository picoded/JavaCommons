package picoded.servlet.api.module;

import java.util.*;
import java.util.function.BiFunction;

import picoded.servlet.*;
import picoded.servlet.api.*;

///
/// Provides a basic API template to echo back ALL request parameter
///
public class EchoApi {
	
	/// Full simple echo, that clonse all request parmeters into result
	public static BiFunction<ApiRequest, ApiResponse, ApiResponse> SimpleEcho = (req, res) -> {
		res.putAll(req.query()); // Place all the request parameter back into result
		return res;
	};
	
}