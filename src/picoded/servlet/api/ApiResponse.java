package picoded.servlet.api;

import java.util.*;

import picoded.conv.*;
import picoded.struct.*;

import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

///
/// API Request map information
/// For the API function to process
///
public class ApiResponse extends GenericConvertHashMap<String, Object> {
	
	
	//-----------------------------------------------------------------
	//
	//  Constructor vars
	//
	//-----------------------------------------------------------------

	/// The base API builder
	protected ApiBuilder builder = null;

	//-----------------------------------------------------------------
	//
	//  Constructor
	//
	//-----------------------------------------------------------------

	/// Initialize the class
	///
	/// @param   Parent ApiBuilder
	ApiResponse( ApiBuilder parent ) {
		// Setup parent API builder object
		builder = parent;
	}
}
