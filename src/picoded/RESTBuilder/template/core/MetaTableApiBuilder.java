package picoded.RESTBuilder.template.core;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.conv.ConvertJSON;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.enums.HttpRequestType;
import picoded.servlet.BasePage;
import picoded.servlet.CorePage;

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
		public abstract boolean auth(BasePage requestPage, MetaTable requestedTable, MetaObject requestedObject,
			String apiRequestType);
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
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
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
	/// | sanitiseOutput  | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	/// |                 |                    | are returned as well.                                                         |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | wildcardMode    | String (optional)  | Default SUFFIX. Determines SQL query wildcard position. Either prefix,        |
	/// |                 |                    | suffix, or both. Defaults to suffix                                           |
	/// | searchValue     | String (optional)  | Search string passed                                                          |
	/// | search[value]   | String (optional)  | Same as above, this is for datatables API. Uses above if both is given.       |
	/// | queryColumns    | String[] (optional)| Query columns used for searching, defaults to headers                         |
	/// | caseSensitive   | boolean (Optional) | Use case sensitive check. Default true?                                       | @TODO
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | recordsTotal    | int (not critical) | Total amount of records. Before any search filter (But after base filters)    |
	/// | recordsFiltered | int (not critical) | Total amount of records. After all search filter                              |
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
		boolean caseSensitive = req.getBoolean("caseSensitive", true);
		
		String[] headers = req.getStringArray("headers");
		if (headers == null || headers.length < 1) {
			headers = new String[] { "_oid" };
		}
		
		String[] queryColumns = req.getStringArray("queryColumns", headers);
		
		String searchParams = req.getString("searchValue", "").trim(); //datatables specific key
		
		if (searchParams.isEmpty()) {
			searchParams = req.getString("search[value]", "").trim();
		}
		
		if (searchParams.length() >= 2 && searchParams.charAt(0) == '"'
			&& searchParams.charAt(searchParams.length() - 1) == '"') {
			searchParams = searchParams.substring(1, searchParams.length() - 1);
		}
		
		String wildcardMode = req.getString("wildcardMode", "suffix");
		
		String orderByStr = req.getString("orderBy");
		if (orderByStr == null || orderByStr.isEmpty()) {
			//try and get datatables order data
			int orderByColumn = req.getInt("order[0][column]", -1);
			if (orderByColumn <= -1) {
				orderByStr = "\"oID\"";
			} else {
				boolean isColumnOrderable = req.getBoolean("columns[" + orderByColumn + "][orderable]", false);
				if (!isColumnOrderable || headers.length < orderByColumn) {
					orderByStr = "\"oID\"";
				} else {
					orderByStr = headers[orderByColumn];
				}
			}
		}
		
		//failsafe
		if (orderByStr == null || orderByStr == "") {
			orderByStr = "\"oID\"";
		}
		
		//ordering direction
		String orderDirection = req.getString("order[0][dir]", "asc"); //datatables specific key
		if (orderByStr != "") {
			orderByStr += (" " + orderDirection);
		}
		
		// The query to use
		String query = req.getString("query", "");
		String[] queryArgs = req.getStringArray("queryArgs");
		if (queryArgs == null) {
			queryArgs = new String[0];
		}
		
		// The query that was given (needed for recordsTotal counting)
		String queryOriginal = query;
		String[] queryOriginalArgs = req.getStringArray("queryArgs");
		
		// Indicates if the additional searchParams reduction
		boolean dataTableSearchFilter = false;
		
		// Data tables search refinement
		if (!searchParams.isEmpty() && queryColumns != null && queryColumns.length > 0) {
			List<String> queryArgsList = new ArrayList<String>(); //rebuild query arguments
			if (queryArgs != null) {
				for (String queryArg : queryArgs) {
					queryArgsList.add(queryArg);
				}
			}
			
			query = "" + query + " AND " + generateQueryStringForSearchValue(searchParams, queryColumns, wildcardMode)
				+ ""; //rebuilding query string
			generateQueryStringArgsForSearchValue_andAddToList(searchParams, queryColumns, wildcardMode, queryArgsList);
			queryArgs = queryArgsList.toArray(new String[queryArgsList.size()]);
			
			dataTableSearchFilter = true;
		}
		
		// Used to sanatize the output result, to protect against HTML injections (by default)
		boolean sanitiseOutput = req.getBoolean("sanitiseOutput", true);
		
		// put back into response, draws and headers
		res.put("draw", draw);
		res.put("headers", headers);
		
		// Records total handling
		long recordsTotal = 0;
		if (queryOriginal != null && queryOriginal.length() > 0) {
			recordsTotal = _metaTableObj.queryCount(queryOriginal, queryOriginalArgs); //base reduce count
		} else {
			recordsTotal = _metaTableObj.size(); //count all
		}
		res.put("recordsTotal", recordsTotal);
		
		// Records filtered handling
		long recordsFiltered = recordsTotal;
		if (dataTableSearchFilter) {
			recordsFiltered = _metaTableObj.queryCount(query, queryArgs);
		}
		res.put("recordsFiltered", recordsFiltered);
		
		// Actual fetching of data
		List<List<Object>> data = null;
		try {
			data = list_GET_and_POST_inner(draw, start, limit, headers, query, queryArgs, orderByStr, sanitiseOutput);
			res.put("data", data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return res;
	};
	
	private static List<String> generateQueryStringArgsForSearchValue_andAddToList(String inSearchString,
		String[] queryColumns, String wildcardMode, List<String> ret) {
		if (inSearchString != null && queryColumns != null) {
			String[] searchStringSplit = inSearchString.trim().split("\\s+");
			StringBuilder querySB = new StringBuilder();
			
			String wildMode = wildcardMode;
			for (String searchString : searchStringSplit) {
				for (String queryColumn : queryColumns) {
					ret.add(getStringWithWildcardMode(searchString, wildMode));
				}
				wildMode = "both"; //Second string onwards is both side wildcard
			}
		}
		
		return ret;
	}
	
	private static String generateQueryStringForSearchValue(String inSearchString, String[] queryColumns,
		String wildcardMode) {
		if (inSearchString != null && queryColumns != null) {
			String[] searchStringSplit = inSearchString.trim().split("\\s+");
			StringBuilder querySB = new StringBuilder();
			
			for (int i = 0; i < searchStringSplit.length; ++i) {
				querySB.append("(");
				for (int queryCol = 0; queryCol < queryColumns.length; ++queryCol) {
					querySB.append(queryColumns[queryCol] + " LIKE ?");
					
					if (queryCol < queryColumns.length - 1) {
						querySB.append(" OR ");
					}
				}
				querySB.append(")");
				
				if (i < searchStringSplit.length - 1) {
					querySB.append(" AND ");
				}
			}
			return querySB.toString();
		}
		
		return "";
	}
	
	private static String getStringWithWildcardMode(String searchString, String wildcardMode) {
		if (wildcardMode.equalsIgnoreCase("prefix")) {
			return "%" + searchString;
		} else if (wildcardMode.equalsIgnoreCase("suffix")) {
			return searchString + "%";
		} else {
			return "%" + searchString + "%";
		}
	}
	
	public List<List<Object>> list_GET_and_POST_inner(int draw, int start, int length, String[] headers, String query,
		String[] queryArgs, String orderBy, boolean sanitiseOutput) throws RuntimeException {
		if (_metaTableObj == null) {
			return null;
		}
		
		List<List<Object>> ret = new ArrayList<List<Object>>();
		try {
			if (headers != null && headers.length > 0) {
				MetaObject[] metaObjs = null;
				if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
					metaObjs = _metaTableObj.query(null, null, orderBy, start, length);
				} else {
					metaObjs = _metaTableObj.query(query, queryArgs, orderBy, start, length);
				}
				
				for (MetaObject metaObj : metaObjs) {
					List<Object> row = new ArrayList<Object>();
					for (String header : headers) {
						Object rawVal = metaObj.get(header);
						
						if (sanitiseOutput && rawVal != null && rawVal instanceof String) {
							String stringVal = GenericConvert.toString(rawVal);
							if (stringVal != null) {
								stringVal = RegexUtils.sanitiseCommonEscapeCharactersIntoAscii(stringVal);
							}
							row.add(stringVal);
						} else {
							row.add(rawVal);
						}
					}
					ret.add(row);
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException("list_GET_and_POST_inner() ", e);
		}
		
		return ret;
	}
	
	public List<String> csv_list(int draw, int start, int length, String[] headers, String query, String[] queryArgs,
		String orderBy) throws RuntimeException {
		
		List<String> ret = new ArrayList<String>();
		
		try {
			MetaObject[] metaObjs = null;
			
			if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
				metaObjs = _metaTableObj.query(null, null, orderBy, start, length);
			} else {
				metaObjs = _metaTableObj.query(query, queryArgs, orderBy, start, length);
			}
			
			StringBuilder headerRow = new StringBuilder();
			for (int i = 0; i < headers.length; ++i) {
				String header = headers[i];
				if (header == null) {
					header = "";
				}
				header = header.replace("\"", ""); //might not be best solution
				header = header.replace("\n", "");
				
				header = "\"" + header + "\"";
				headerRow.append(header);
				if (i < headers.length - 1) {
					headerRow.append(",");
				} else {
					headerRow.append("\n");
				}
			}
			
			ret.add(headerRow.toString());
			
			for (MetaObject metaObj : metaObjs) {
				List<Object> row = new ArrayList<Object>();
				for (String header : headers) {
					row.add(metaObj.get(header));
				}
				
				//for each element in row, stringbuilder and add to ret
				StringBuilder singleRowCSV = new StringBuilder();
				int rowSize = row.size();
				for (int i = 0; i < rowSize; ++i) {
					String valStr = GenericConvert.toString(row.get(i));
					if (valStr == null) {
						valStr = "";
					}
					valStr = valStr.replace("\"", ""); //might not be best solution
					valStr = valStr.replace("\n", "");
					
					valStr = "\"" + valStr + "\"";
					singleRowCSV.append(valStr);
					
					if (i < rowSize - 1) {
						singleRowCSV.append(",");
					} else {
						singleRowCSV.append("\n");
					}
				}
				
				ret.add(singleRowCSV.toString());
			}
			
		} catch (Exception e) {
			throw new RuntimeException("csv_list() ", e);
		}
		
		return ret;
	}
	
	//csv export function
	public RESTFunction csv_export = (req, res) -> {
		
		int draw = req.getInt("draw");
		int start = req.getInt("start");
		int limit = req.getInt("length");
		
		String orderByStr = req.getString("orderBy");
		if (orderByStr == null || orderByStr.isEmpty()) {
			orderByStr = "oID";
		}
		
		String[] headers = req.getStringArray("headers");
		
		if (headers == null || headers.length < 1) {
			headers = new String[] { "_oid" };
		}
		
		String query = req.getString("query");
		String[] queryArgs = req.getStringArray("queryArgs");
		
		//put back into response
		res.put("draw", draw);
		res.put("headers", headers);
		
		res.put("recordsTotal", _metaTableObj.size());
		
		if (query != null && !query.isEmpty() && queryArgs != null && queryArgs.length > 0) {
			res.put("recordsFiltered", _metaTableObj.queryCount(query, queryArgs));
		}
		
		List<String> data = null;
		int count = start;
		
		CorePage page = req.requestPage();
		PrintWriter pWriter = page.getWriter();
		page.getHttpServletResponse().setContentType("text/csv");
		page.getHttpServletResponse().setHeader("Content-Disposition", "attachment;filename=reports.csv");
		
		try {
			
			// VIEW VIA CONSOLE.LOG()
			// data = csv_list (draw, count, limit, headers, query, queryArgs, orderByStr);
			// String d = "";
			// for(String str : data){
			// d += str;
			// }
			// res.put("data", d);
			
			if ((data = csv_list(draw, count, limit, headers, query, queryArgs, orderByStr)).size() >= limit) {
				count += data.size();
				for (String str : data) {
					pWriter.write(str);
				}
				pWriter.flush();
			}
			
			// final batch write
			for (String str : data) {
				pWriter.write(str);
			}
			pWriter.flush();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// res.put("MetaTableApiBuilder", "csv_export");
		return null;
	};
	
	/////////////////////////////////////////////
	//
	// Meta details operations
	//
	/////////////////////////////////////////////
	
	///
	/// # meta/${ObjectID} (GET) [Requires login]
	///
	/// Gets and return the meta object user info
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | object ID used to retrieve the meta object. If no oid is given, return null.  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | sanitiseOutput  | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	/// |                 |                    | are returned as well.                                                         |
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
		String oid = req.getString("_oid");
		if (oid == null) {
			oid = (req.rawRequestNamespace() != null) ? req.rawRequestNamespace()[req.rawRequestNamespace().length - 1]
				: null;
		}
		
		//put data back into response
		res.put("_oid", oid);
		
		boolean sanitiseOutput = req.getBoolean("sanitiseOutput", true);
		
		try {
			MetaObject mObj = meta_GET_inner(oid, sanitiseOutput);
			res.put("meta", mObj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return res;
	};
	
	public MetaObject meta_GET_inner(String oid, boolean sanitiseOutput) throws RuntimeException {
		if (_metaTableObj == null) {
			return null;
		}
		
		if (oid == null || oid.isEmpty()) {
			return null;
		}
		
		MetaObject[] metaObjs = null;
		metaObjs = _metaTableObj.query("_oid=?", new String[] { oid });
		
		if (metaObjs.length > 1) {
			throw new RuntimeException("meta_GET_inner() -> More than 1 meta object was returned for _oid : " + oid);
		} else if (metaObjs.length < 1) {
			return null;
		} else {
			MetaObject ret = metaObjs[0];
			if (sanitiseOutput) {
				Set<String> keySet = ret.keySet();
				for (String key : keySet) {
					Object rawVal = ret.get(key);
					if (rawVal instanceof String) {
						String stringVal = GenericConvert.toString(rawVal);
						stringVal = RegexUtils.sanitiseCommonEscapeCharactersIntoAscii(stringVal);
						ret.put(key, stringVal);
					} else {
						ret.put(key, rawVal);
					}
				}
			}
			
			return ret;
		}
	}
	
	///
	/// # meta/${ObjectID} (POST) [Requires login]
	///
	/// Updates the oid meta info, requires either the current user or SuperUser
	///
	/// Note: if ${ObjectID} is "new", it creates a new object
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID used (or created)                                      |
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | updateMode      | String             | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// | updateMeta      | {Object}           | The updated changes done                                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_POST = (req, res) -> {
		String oid = req.getString("_oid");
		if (oid == null) {
			oid = (req.rawRequestNamespace() != null) ? req.rawRequestNamespace()[req.rawRequestNamespace().length - 1]
				: null;
		}
		
		//put data back into response
		res.put("_oid", oid);
		
		String updateMode = req.getString("updateMode", "delta");
		res.put("updateMode", updateMode);
		res.put("updateMeta", null);
		
		Map<String, Object> givenMObj = null;
		if (req.get("meta") instanceof String) {
			String jsonMetaString = req.getString("meta");
			if (jsonMetaString != null && !jsonMetaString.isEmpty()) {
				givenMObj = ConvertJSON.toMap(jsonMetaString);
			}
		}
		
		if (givenMObj != null) {
			try {
				MetaObject updatedMObj = meta_POST_inner(oid, givenMObj, updateMode);
				res.put("updateMeta", updatedMObj);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			res.put("error", "No meta object was found in the request");
		}
		
		return res;
	};
	
	public MetaObject meta_POST_inner(String oid, Map<String, Object> mObj, String updateMode) throws RuntimeException {
		boolean updateModeFull = false;
		if (updateMode != null && updateMode.equalsIgnoreCase("full")) {
			updateModeFull = true;
		}
		
		MetaObject ret = null;
		
		try {
			if (oid.equalsIgnoreCase("new")) {
				ret = _metaTableObj.append(null, mObj);
				ret.saveAll();
				return ret;
			} else {
				//
				// Get the meta object from id
				//
				ret = _metaTableObj.get(oid);
				
				//
				// Terminate if null (no object)
				//
				if (ret == null) {
					return null;
				}
				
				//
				// Else update the update withdata
				//
				if (updateModeFull) {
					//full overwrite
					ret.clear();
					ret.putAll(mObj);
					ret.saveAll();
					return ret;
				} else {
					//delta update
					ret.putAll(mObj);
					ret.saveDelta();
					return ret;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("meta_POST_inner()", e);
		}
	}
	
	///
	/// # delete/${ObjectID} (DELETE) [Requires login]
	///
	/// Deletes the current oid object from the table
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID to delete                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | Returns oid of metaObject to delete                                           |
	/// | deleted         | boolean            | Returns true ONLY if the element was removed from the table                   |
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction meta_DELETE = (req, res) -> {
		String oid = req.getString("_oid");
		if (oid == null) {
			oid = (req.rawRequestNamespace() != null) ? req.rawRequestNamespace()[req.rawRequestNamespace().length - 1]
				: null;
		}
		
		if (oid == null || oid.isEmpty()) {
			return res;
		}
		
		res.put("_oid", oid);
		
		boolean foundKeyToDelete = meta_DELETE_inner(oid);
		res.put("foundKeyToDelete", foundKeyToDelete);
		
		return res;
	};
	
	public boolean meta_DELETE_inner(String oid) {
		if (_metaTableObj.containsKey(oid)) {
			_metaTableObj.remove(oid);
			return true;
		} else {
			return false;
		}
	}
	
	/////////////////////////////////////////////
	//
	// KeyNames look up (for adminstration)
	//
	/////////////////////////////////////////////
	
	///
	/// # keyNames (GET) [Requires login]
	///
	/// Gets and return the various object keys in meta objects
	///
	/// ## HTTP Request Parameters
	///
	/// +------+
	/// | None |
	/// +------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | keyNames        | String[]           | Key names found in the meta object                                            |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction keyNames_GET = (req, res) -> {
		try {
			Set<String> resSet = _metaTableObj.getKeyNames();
			res.put("keyNames", new ArrayList<String>(resSet));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	public RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix) {
		rb.getNamespace(setPrefix + "list").put(HttpRequestType.GET, list_GET_and_POST);
		rb.getNamespace(setPrefix + "list").put(HttpRequestType.POST, list_GET_and_POST);
		
		rb.getNamespace(setPrefix + "meta.*").put(HttpRequestType.GET, meta_GET);
		rb.getNamespace(setPrefix + "meta.*").put(HttpRequestType.POST, meta_POST);
		
		rb.getNamespace(setPrefix + "meta").put(HttpRequestType.GET, meta_GET);
		rb.getNamespace(setPrefix + "meta").put(HttpRequestType.POST, meta_POST);
		
		rb.getNamespace(setPrefix + "meta").put(HttpRequestType.DELETE, meta_DELETE);
		rb.getNamespace(setPrefix + "meta.*").put(HttpRequestType.DELETE, meta_DELETE);
		
		rb.getNamespace(setPrefix + "keyNames").put(HttpRequestType.GET, keyNames_GET);
		rb.getNamespace(setPrefix + "keyNames").put(HttpRequestType.POST, keyNames_GET);
		
		rb.getNamespace(setPrefix + "csv").put(HttpRequestType.GET, csv_export);
		rb.getNamespace(setPrefix + "csv").put(HttpRequestType.POST, csv_export);
		
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
