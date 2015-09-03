package picoded.RESTBuilder.templates;

import java.util.*;
import java.util.Map.Entry;

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
	
	private MetaTable _metaTableObj = null;
	
	// Constructor
	public MetaTableApiBuilder(MetaTable metaTableObj) {
		//
		//hold a table that is passed in
		//Map<String(_oid), Map<String,Object>>
		//_oid = 22 char base58 GUID which is like a "row" in a table
		//the object i get back is a column name (String key) to value stored (the object)
		_metaTableObj = metaTableObj;
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
	/// | Parameter Name  | Variable Type	    | Description                                                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	/// | length          | int (optional)     | Default 50: The number of records to return                                   |
	/// | orderBy         | String (optional)  | Default : order by _oid                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid"], the columns to return                                       |
	/// | query           | String (optional)  | Requested Query filter                                                        |
	/// | queryArgs       | String[] (optional)| Requested Query filter arguments                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | recordsTotal    | int (not critical) | Total amount of records. Before any search filter (But after base filters)    |
	/// | recordsFilterd  | int (not critical) | Total amount of records. After all search filter                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid"], the collumns to return                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | array              | Array of row records                                                          |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction list_GET_and_POST = (req, res) -> {
		int draw = req.getInt("draw");
		int start = req.getInt("start");
		int limit = req.getInt("length");
		
		String orderByStr = req.getString("orderBy");
		if(orderByStr == null || orderByStr.isEmpty()){
			orderByStr = "_oid";
		}
		
		String[] headers = req.getStringArray("headers");
		String query = req.getString("query");
		String[] queryArgs = req.getStringArray("queryArgs");
		
		//put back into response
		res.put("draw", draw);
		res.put("headers", headers);
		
		//add recordsTotal and recordsFiltered later
		
		List<List<String>> data = null;
		try{
			data = list_GET_and_POST_inner(draw, start, limit, headers, query, queryArgs, orderByStr);
			res.put("data",  data);
		}catch(Exception e){
			res.put("error",  e.getMessage());
		}
		
		return res;
	};
	
	public List<List<String>> list_GET_and_POST_inner(int draw, int start, int length, String[] headers, String query, String[] queryArgs, String orderBy) throws RuntimeException{
		if(_metaTableObj == null){
			return null;
		}
		
		List<List<String>> ret = new ArrayList<List<String>>();
		
		try{
			Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
			if(headers != null && headers.length > 0){
				for(String header : headers){
					MetaObject[] metaObjs = null;
					
					if(query.isEmpty() || queryArgs == null){
						metaObjs = _metaTableObj.getFromKeyName(header, orderBy, start, length);
					}else{
						metaObjs = _metaTableObj.query(query, queryArgs, orderBy, start, length);
					}
					
					for(MetaObject metaObj : metaObjs){
						String objOid = metaObj._oid();
						if(!tempMap.containsKey(objOid)){
							tempMap.put(objOid, new ArrayList<String>());
						}
						
						if(metaObj.containsKey(header)){
							tempMap.get(objOid).add((String)metaObj.get(header));
						}
					}
				}
			}
			
			
			for(Entry<String, List<String>> mapEntry : tempMap.entrySet()){
				List<String> mapValue = mapEntry.getValue();
				ret.add(mapValue);
			}
		}catch(Exception e){
			throw new RuntimeException("list_GET_and_POST_inner() -> " + e.getMessage());
		}
		
		return ret;
	}
	
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