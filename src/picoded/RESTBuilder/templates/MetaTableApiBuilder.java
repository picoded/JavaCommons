package picoded.RESTBuilder.templates;

import java.util.*;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;

import picoded.enums.HttpRequestType;

/// Account login template API
public class MetaTableApiBuilder {
	
	/////////////////////////////////////////////
	//
	// Common error messages
	//
	/////////////////////////////////////////////
	
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	public static final String MISSING_LOGIN_SESSION = "Authentication Error: Missing login session";
	public static final String MISSING_PERMISSION = "Permission Error: Missing permission for request (generic)";
	public static final String MISSING_PERMISSION_GROUP = "Permission Error: Missing group admin rights required for request";
	
	// Constructor
	public MetaTableApiBuilder(MetaTable metaTableObj) {
		//
	}
	
	/////////////////////////////////////////////
	//
	// Utility functions
	//
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	//
	// Authenticator overwirte
	//
	/////////////////////////////////////////////
	
	@FunctionalInterface
	public interface MetaTableAPI_authenticator {
		public abstract boolean auth(BasePage requestPage, MetaTable requestedTable, MetaObject requestedObject, String apiRequestType );
	}
	
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator list_authenticationFilter = null;
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator get_authenticationFilter = null;
	/// Authentication filter, if null there is no authentication check for GET request (default behaviour)
	public MetaTableAPI_authenticator post_authenticationFilter = null;
	
	/////////////////////////////////////////////
	//
	// List searching API
	//
	/////////////////////////////////////////////
	
	///
	/// # list (POST / GET) [Requires login]
	///
	/// Lists the MetaObjects according to the search criteria
	///
	/// This JSON api is compatible with the datatables.js server side API.
	/// See: https://web.archive.org/web/20140627100023/http://datatables.net/manual/server-side
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	/// | length          | int (optional)     | Default 50: The number of records to return                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | recordsTotal    | int                | Total amount of records. Before any search filter (But after base filters)    |
	/// | recordsFilterd  | int                | Total amount of records. After all search filter                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | array              | Array of row records                                                          |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction list_GET_and_POST = (req, res) -> {
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// Meta details operations
	//
	/////////////////////////////////////////////
	
	///
	/// # ${ObjectID} (GET) [Requires login]
	///
	/// Gets and return the meta object user info
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID used (or created)                                      |
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_GET = (req, res) -> {
		return res;
	};
	
	///
	/// # ${ObjectID} (POST) [Requires login]
	///
	/// Updates the accountID meta info, requires either the current user or SuperUser
	///
	/// Note: if ${ObjectID} is "new", it creates a new object
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID used (or created)                                      |
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | updateMode      | String             | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// | updateMeta      | {Object}           | The updated changes done                                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_POST = (req, res) -> {
		return res;
	};



	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) start
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) end
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	/////////////////////////////////////////////
	//
	// RestBuilder template builder
	//
	/////////////////////////////////////////////
	
	///
	/// Takes the restbuilder and the account table object and implements its respective default API
	///
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix ) {

		
		return rb;
	}

	/////////////////////////////////////////////
	//
	// Servlet constructor and setup
	//
	/////////////////////////////////////////////
//	
//	/// Default api set prefix
//	protected static String _apiSetPrefix_prefix = "account.";
//	
//	/// Internal prefix set for the API
//	protected String _apiSetPrefix = _apiSetPrefix_prefix;
//	
//	/// The prefix for the api
//	public String apiSetPrefix() {
//		return _apiSetPrefix;
//	}
//		
//	/// The prefix for the api
//	public void setApiSetPrefix(String api) {
//		_apiSetPrefix = api;
//	}
//	
//	/// Flags as JSON request
//	public boolean isJsonRequest() {
//		return true;
//	}
//	
//	/// Does the default setup
//	public void doSetup() throws Exception {
//		super.doSetup();
//		setupRESTBuilder( this.restBuilder(), this.accountAuthTable(), _apiSetPrefix );
//	}
//	
//	/// Process the request, not the authentication layer
//	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
//		return restBuilder().servletCall( _apiSetPrefix, this, outputData );
//	}

}