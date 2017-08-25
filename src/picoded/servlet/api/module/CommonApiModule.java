package picoded.servlet.api.module;

import picoded.servlet.api.*;
import picoded.core.conv.*;
import picoded.core.struct.*;
import picoded.core.common.SystemSetupInterface;

import java.util.*;

/**
 * CommonApiModule further extends the standard API module, by adding several common features
 *
 * Mainly as followed
 * + StringEscape
 * + AccessFilter config
 * 
 **/
abstract public class CommonApiModule extends AbstractApiModule {

	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	//  apiBuilderSetup functionality
	//
	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Does the setup of the StringEscape filter, and AccessFilter config.
	 *
	 * This functionality can be refined via the config object
	 */
	protected void apiBuilderSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {
		if( config.getBoolean("stringEscapeAfterFilter", true) ) {
			api.after(prefixPath+"/*", stringEscapeAfterFilter);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	//  StringEscape function filter
	//
	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * StringEscape filter module, that sanitizes the output for common "html injections"
	 *
	 * ## Request Parameters
	 *
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name | Variable Type      | Description                                                                   |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | stringEscape   | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	 * |                |                    | are returned as well.                                                         |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | stringEscape    | Boolean            | Indicate if string escape occured                                             |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public ApiFunction stringEscapeAfterFilter = (req, res) -> {
		// Get the desired chosen mode
		boolean stringEscape = req.getBoolean("stringEscape", true);

		// Apply string escape if needed
		if( stringEscape ) {
			StringEscape.commonHtmlEscapeCharacters(res);
		}

		// Output the chosen mode, and result
		res.put("stringEscape", stringEscape);
		return res;
	};
}