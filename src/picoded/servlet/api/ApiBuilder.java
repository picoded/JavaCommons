package picoded.servlet.api;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import picoded.servlet.*;
import picoded.servlet.api.internal.HaltException;
import picoded.servlet.api.internal.ApiBuilderJS;
import picoded.core.struct.UnsupportedDefaultMap;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import picoded.core.conv.ConvertJSON;

import picoded.servlet.api.ApiVersionSet.ApiFunctionType;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

/**
 * ApiBuilder is a utility class, in which facilitates the building of modern JSON pure API's.
 * This can be used in the project either via a public API, or even internally via a direct function call.
 *
 * While faciltating the following....
 *
 * + Packaging the entire web project core functionalities
 * + JS Api script generation
 * + Swagger compliant API documentation generation
 * + RAML compliant API documentation generation
 * + Internal function calls
 *
 * # @TODO Items
 *
 * [ ] Add in documentation / definition support API
 * [+] Add in filter lambda API
 * [ ] Add in parametarised paths support
 * [ ] Add in actual servlet support (See RESTBuilder)
 * [+] Add in JS API script generation
 * [ ] Patch up endpoint removal, and keyset to take into seperate account filters
 * [+] Possibly seperate out filters? (before / after)
 * [ ] Formally define how ambiguity of API endpoints is resolved
 * [ ] Extend function for api endpoints / filters
 *
 * # Description rents
 *
 * ## This favours JSON like API, which may or may not be REST strict
 *
 * > While this is somewhat a programming 'religious' topic. And I respect your oppinion.
 * > Picoded as a company does deal with quite a number of enterprise companies who for "security reasons"
 * > block off HTTP commands such as PUT / DELETE, as such our API always somehow end up not being purely "RESTful"
 * > With GET commands modifying things, and POST commands deleting things.
 *
 * So it does the REST blashpamy, it treats GET / PUT / POST / DELETE as all the same.
 * If you hate it, err. Dun use it =| use spark or apache, or something.
 *
 * More rents on why REST on API's is problematic here : https://mmikowski.github.io/the_lie/
 *
 * ## Major, Minor sementic versioning support
 *
 * See: http://semver.org/
 *
 * Major and Minor versioning handling is supported. Patch is intentionally not supported,
 * as it adds way too much development overhead, for what should by definition, be non breaking changes.
 *
 * # Really misc notes, I should probably delete
 *
 * ## Historic notes from JavaCommons.RESTBuilder
 *
 * While, it replaces the original servlet framework role of creating JSON API's. The framework is
 * also meant to facilitate intra project function calls. One of the examples learnt from the LMS project,
 * in handling the encryption of export files (for example). Is that instead of rewriting the entire export
 * module code to be "callable" by a function a rather indirect and "inefficent" method of calling its
 * local page directly was used. Aka an encryption proxy. Due to the time constraints of the project.
 *
 * Simply put, if a standardised API builder was built and used, several page API features can be
 * called directly instead of being usued via a proxy
 **/
public class ApiBuilder implements
	UnsupportedDefaultMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> {

	//-------------------------------------------------------------------
	//
	// Constructor
	//
	//-------------------------------------------------------------------

	/**
	 * Object token representing a "removed" endpoint / filter
	 **/
	protected static final ApiFunction NULLAPIFUNCTION = (req, res) -> {
		return res;
	};

	/**
	 * ROOT Api connection, refenced by sub-class implementation
	 **/
	protected ApiBuilder root = null;

	/**
	 * Version specific namespace mapping
	 * Major,Minor,Patch,Path = Endpoint
	 **/
	protected List<List<ApiVersionSet>> endpointVersionStore = null;

	/**
	 * Blank constructor, to build a root end point
	 **/
	public ApiBuilder() {
		this.root = this;
		this.endpointVersionStore = new ArrayList<List<ApiVersionSet>>();
	}

	//-------------------------------------------------------------------
	//
	// Version string and configuration handling
	//
	//-------------------------------------------------------------------

	/**
	 * Major sementic version handling
	 **/
	protected int majorVersion = 0;

	/**
	 * Minor sementic version handling
	 **/
	protected int minorVersion = 0;

	/**
	 * Get the version values
	 *
	 * @return  Array of major, and minor int version
	 **/
	public int[] version() {
		return new int[] { majorVersion, minorVersion };
	}

	/**
	 * Get the version string
	 *
	 * @return  v(major).(minor)
	 **/
	public String versionStr() {
		return versionStr(majorVersion, minorVersion);
	}

	/**
	 * Get the version string
	 *
	 * @return  v(major).(minor)
	 **/
	protected String versionStr(int inMajor, int inMinor) {
		return "v" + inMajor + "." + inMinor;
	}

	/**
	 * Set the version values
	 *
	 * @param  Major version to configure
	 * @param  Minor version to configure
	 *
	 * @return  Array of major, and minor int version
	 **/
	public int[] setVersion(int inMajor, int inMinor) {
		majorVersion = inMajor;
		minorVersion = inMinor;
		return version();
	}

	//-------------------------------------------------------------------
	//
	// (Raw, non collapsed) Version set handaling
	//
	//-------------------------------------------------------------------

	/**
	 * Get the current version set, after normalizing it
	 *
	 * @return  the current version set
	 **/
	protected ApiVersionSet getVersionSet() {
		return getVersionSet(majorVersion, minorVersion);
	}

	/**
	 * Get a specified version set, after normalizing it
	 *
	 * @return  the current version set
	 **/
	protected ApiVersionSet getVersionSet(int inMajor, int inMinor) {
		// Normalize major endpoint
		while (endpointVersionStore.size() <= inMajor) {
			endpointVersionStore.add(null);
		}
		List<ApiVersionSet> minorVersionList = endpointVersionStore.get(majorVersion);
		if (minorVersionList == null) {
			minorVersionList = new ArrayList<ApiVersionSet>();
			endpointVersionStore.set(inMajor, minorVersionList);
		}

		// Normalize minor endpoint
		while (minorVersionList.size() <= inMinor) {
			minorVersionList.add(null);
		}

		// Getting the stroage map
		ApiVersionSet vSet = minorVersionList.get(inMinor);
		if (vSet == null) {
			vSet = new ApiVersionSet();
			minorVersionList.set(inMinor, vSet);
		}

		// Return the version set
		return vSet;
	}

	//-------------------------------------------------------------------
	//
	// (Collapsed) Version set handaling
	//
	//-------------------------------------------------------------------

	/**
	 * Internal cache of collapsed version set
	 * Note that this is resetted, when any put operation is performed
	 **/
	protected Map<String, ApiVersionSet> cachedCollapsedVersionSet = new HashMap<String, ApiVersionSet>();

	/**
	 * Get the (possibly) cached collapsed version set
	 *
	 * This is used internally to sort out a definiative version set to scan
	 * for an execution request, results are cached until the respective put request overwrites it.
	 *
	 * @param  Major version to fetch
	 * @param  Minor version to fetch
	 *
	 * @return  The collapsed version set
	 **/
	protected ApiVersionSet collapsedVersionSet(int inMajor, int inMinor) {
		// Get the version string
		String verStr = versionStr(inMajor, inMinor);

		// Fetch the version data
		ApiVersionSet ret = cachedCollapsedVersionSet.get(verStr);

		// Make a new version set if null
		if (ret == null) {
			ret = generateCollapsedVersionSet(inMajor, inMinor);
			cachedCollapsedVersionSet.put(verStr, ret);
		}

		// return cached collapsed version set
		return ret;
	}

	/**
	 * Get the (possibly) cached collapsed version set, of the current version
	 *
	 * This is used internally to sort out a definiative version set to scan
	 * for an execution request, results are cached until the respective put request overwrites it.
	 **/
	protected ApiVersionSet collapsedVersionSet() {
		return collapsedVersionSet(majorVersion, minorVersion);
	}

	/**
	 * Generates an uncached collapsed version set
	 *
	 * This is used internally to sort out a definiative version set to scan
	 * for an execution request.
	 *
	 * @param  Major version to fetch
	 * @param  Minor version to fetch
	 *
	 * @return  The collapsed version set
	 **/
	protected ApiVersionSet generateCollapsedVersionSet(int inMajor, int inMinor) {
		ApiVersionSet ret = new ApiVersionSet();

		// Iterate major versions
		int maxMajor = endpointVersionStore.size();
		for (int major = 0; major < maxMajor; ++major) {

			// Get the list of minor versions
			List<ApiVersionSet> minorSet = endpointVersionStore.get(major);

			// Iterate the minor versions
			int maxMinor = minorSet.size();
			for (int minor = 0; minor < maxMinor; ++minor) {

				// Load the set to import
				ApiVersionSet setToImport = minorSet.get(minor);

				// If not null, import it
				if (setToImport != null) {
					ret.importVersionSet(setToImport);
				}

				// Terminates if the relevent major & minor version is met
				if (major >= inMajor && minor >= inMinor) {
					break;
				}
			}

			// Terminate at the required major version is met
			if (major >= inMajor) {
				break;
			}
		}

		// Time to remove the "null" endpoints
		HashSet<String> keySet = new HashSet<String>();
		keySet.addAll(ret.endpointMap.keySet());
		for (String key : keySet) {
			if (ret.endpointMap.get(key) == NULLAPIFUNCTION) {
				ret.endpointMap.remove(key);
			}
		}

		return ret;
	}

	//-------------------------------------------------------------------
	//
	// ApiRequest setup process
	//
	//-------------------------------------------------------------------

	/**
	 * The ApiRequest method setup handling, assumes an internal java call in this case
	 *
	 * @param   request query parameter to pass forward.
	 * @param   context data parameter to pass forward
	 *
	 * @return  new ApiRequest, configured to the current ApiBuilder, and any of its default settings (if relevent)
	 **/
	protected ApiRequest setupApiRequest(Map<String, Object> queryParams,
		Map<String, Object> contextParams) {
		ApiRequest ret = new ApiRequest(this, queryParams, contextParams);
		ret.requestMethod = "java";
		return ret;
	}

	/**
	 * The ApiRequest method setup handling, loading the request query paremeters from the servlet
	 *
	 * @param   The HTTP CorePage to extract data from
	 *
	 * @return  new ApiRequest, configured to the current ApiBuilder, and any of its default settings (if relevent)
	 **/
	protected ApiRequest setupApiRequest(CorePage core) {
		ApiRequest ret = new ApiRequest(this);
		ret.queryObj = core.requestParameters();
		return ret;
	}

	//-------------------------------------------------------------------
	//
	// API put / remove map handling, and its filters
	//
	//-------------------------------------------------------------------

	/**
	 * Register the API function with its respective type
	 *
	 * @param  ApiFunctionType  type mapping to use
	 * @param  String path to sanitize and use
	 * @param  Function to store as a "value"
	 */
	protected void registerApiFunction(ApiFunctionType type, String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		// Clears the collapsed version set cache
		cachedCollapsedVersionSet.clear();

		// Change / into .
		path = path.replaceAll("/", ".");

		// Get the current version, and write the respective endpoint to it
		Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> functionMap = getVersionSet().functionMap(type);
		if (value == null) {
			functionMap.put(path, NULLAPIFUNCTION);
		} else {
			functionMap.put(path, value);
		}
	}

	/**
	 * Registers an API function to a single endpoint.
	 *
	 * Note that due to the way ApiBuilder is designed to export the respective API libraries
	 * wildcard based endpoints will not be supported.
	 *
	 * @param  Endpoint paths
	 * @param  Executor function, use null to remove an existing function
	 *
	 * @return  returns null
	 **/
	public void endpoint(String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		registerApiFunction(ApiFunctionType.ENDPOINT, path, value);
	}

	/**
	 * Registers an API before filter function to a single endpoint.
	 *
	 * @param  Endpoint paths
	 * @param  Executor function, use null to remove an existing function
	 *
	 * @return  returns null
	 **/
	public void before(String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		registerApiFunction(ApiFunctionType.BEFORE, path, value);
	}

	/**
	 * Registers an API after filter function to a single endpoint.
	 *
	 * @param  Endpoint paths
	 * @param  Executor function, use null to remove an existing function
	 *
	 * @return  returns null
	 **/
	public void after(String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		registerApiFunction(ApiFunctionType.AFTER, path, value);
	}

	/**
	 * Execute a request
	 *
	 * @param   major version to use
	 * @param   minor version to use
	 * @param   path name to use
	 * @param   request query parameter to pass forward.
	 * @param   context data parameter to pass forward,
	 *
	 * @return  The result parameters
	 **/
	public ApiResponse execute(int inMajor, int inMinor, String path,
		Map<String, Object> queryParams, Map<String, Object> contextParams) {
		return execute(inMajor, inMinor, path, setupApiRequest(queryParams, contextParams), null);
	}

	//-------------------------------------------------------------------
	//
	// Extending an API function / filter
	//
	//-------------------------------------------------------------------

	/**
	 * Registers an API function to a single endpoint.
	 *
	 * Note that due to the way ApiBuilder is designed to export the respective API libraries
	 * wildcard based endpoints will not be supported.
	 *
	 * @param  Endpoint paths
	 * @param  Executor function, use null to remove an existing function
	 *
	 * @return  returns null
	 **/
	public void extendEndpoint(String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		for(String name : getVersionSet().functionMap(ApiFunctionType.ENDPOINT).keySet()){
			System.out.println(name +" the name of the endpoint");
		}
		// The original endpoint function to extend
		BiFunction<ApiRequest, ApiResponse, ApiResponse> originalEndpoint = fetchSpecificApiFunction(ApiFunctionType.ENDPOINT, majorVersion, minorVersion, path);
System.out.println(value==null);
System.out.println(path);
		// If originalEndpoint is null, just implement directly
		if( originalEndpoint == null ) {
			System.out.println("The originalEndpoint is null ");
			endpoint(path, value);
		}
System.out.println(value==null);
		// Registers the new endpoint function
		endpoint(path, (req,res) -> {
			// New request to use
			ApiRequest overwriteReq = new ApiRequest(req, originalEndpoint);
			System.out.println(value==null);
			System.out.println(ConvertJSON.fromObject(overwriteReq)+ "overwriteReq");
			// Call the BiFunction, with the extended request
			return value.apply(overwriteReq, res);
		});
	}



	//-------------------------------------------------------------------
	//
	// Fetch and get the relevent API Execution endpoints for the path
	//
	//-------------------------------------------------------------------

	/**
	 * Fetching a specific ApiFunction, that does an exact match for the path
	 *
	 * @param   type of api function to get
	 * @param   major version to use
	 * @param   minor version to use
	 * @param   path name to use
	 *
	 * @return  The requested API Function, if found
	 */
	protected BiFunction<ApiRequest, ApiResponse, ApiResponse> fetchSpecificApiFunction(
		ApiFunctionType type,
		int inMajor, int inMinor,
		String path
	) {
		// Gets the collapsed version set
		ApiVersionSet workingSet = collapsedVersionSet(inMajor, inMinor);

		// Get the current version, and write the respective endpoint to it
		Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> functionMap = workingSet.functionMap(type);

		// Find the exact match
		return functionMap.get(path);
	}

	/**
	 * Fetching ApiFunction that matches the given path, including functions with wildcard matches
	 *
	 * @param   type of api function to get
	 * @param   major version to use
	 * @param   minor version to use
	 * @param   path name to use
	 *
	 * @return  The requested API Function, if found
	 */
	protected List<BiFunction<ApiRequest, ApiResponse, ApiResponse>> fetchMultipleApiFunction(
		ApiFunctionType type,
		int inMajor, int inMinor,
		String path
	) {
		// Gets the collapsed version set
		ApiVersionSet workingSet = collapsedVersionSet(inMajor, inMinor);

		// Get the current version, and write the respective endpoint to it
		Map<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> functionMap = workingSet.functionMap(type);

		// List of API functions, with matching path
		List<BiFunction<ApiRequest, ApiResponse, ApiResponse>> filteredApiFunction = new ArrayList<BiFunction<ApiRequest, ApiResponse, ApiResponse>>();

		// Working pattern / match variables
		Pattern pattern = Pattern.compile("");
		Matcher match = null;

		// Iterate the function map, for relevent functions
		for (String currentPath : functionMap.keySet()) {
			// Working filterPath
			String filterPath = currentPath;

			// Converts path into a usable regex, where * is a wildcard
			filterPath = filterPath.replaceAll("\\.", "\\\\."); // Regex: escape all .
			filterPath = filterPath.replaceAll("\\*", ".*"); // Regex: change * into .*
			filterPath = filterPath.replaceAll("\\\\.\\.\\*$", "(\\\\..*)?"); // Regex: change last \..* into optional (\..*)?

			// Compile the regex
			pattern = Pattern.compile(filterPath);
			match = pattern.matcher(path);

			// Check if a match occur, if so the function is added to the list
			if (match.matches()) { // Find the exact match
				filteredApiFunction.add(functionMap.get(currentPath));
			}
		}

		// Return list of ApiFunctions : maybe blank
		return filteredApiFunction;
	}

	//-------------------------------------------------------------------
	//
	// API Execution handling
	//
	//-------------------------------------------------------------------

	/**
	 * Checks for valid path, returns success or failure
	 *
	 * @param   major version to use
	 * @param   minor version to use
	 * @param   path name to use
	 *
	 * @return  true if path is valid
	 **/
	public boolean isValidPath(int inMajor, int inMinor, String path) {
		return fetchSpecificApiFunction(ApiFunctionType.ENDPOINT, inMajor, inMinor, path) != null;
	}

	/**
	 * Checks for valid path, returns success or failure
	 *
	 * @param   path name to use
	 *
	 * @return  true if path is valid
	 **/
	public boolean isValidPath(String path) {
		return fetchSpecificApiFunction(ApiFunctionType.ENDPOINT, majorVersion, minorVersion, path) != null;
	}

	/**
	 * Utility function that iterate and execute the various ApiFunction
	 * and modify the resulting ApiResponse accordingly
	 *
	 * @param  functionList to execute
	 * @param  req request object
	 * @param  res result object
	 *
	 * @return The result object
	 */
	protected ApiResponse executeApiFunctionList(List<BiFunction<ApiRequest, ApiResponse, ApiResponse>> functionList, ApiRequest req, ApiResponse res) {
		// Iterate the functionList, and execute it
		for (BiFunction<ApiRequest, ApiResponse, ApiResponse> func : functionList) {
			ApiResponse funcResponse = func.apply(req, res);
			if (funcResponse != null) {
				res = funcResponse;

				// Automatically terminates on an error
				if( funcResponse.get(ERROR) != null ) {
					res.halt();
				}
			} else {
				// A null funcResponse, is considered a halt
				res.halt();
			}
		}
		return res;
	}

	/**
	 * Execute a request
	 *
	 * @param   major version to use
	 * @param   minor version to use
	 * @param   path name to use
	 * @param   request query parameter to pass forward.
	 * @param   result data to write and return into
	 *
	 * @return  The result parameters
	 **/
	public ApiResponse execute(int inMajor, int inMinor, String path, ApiRequest reqObj,
		ApiResponse resObj) {
		// Fetch the endpoint
		BiFunction<ApiRequest, ApiResponse, ApiResponse> endpoint = fetchSpecificApiFunction(ApiFunctionType.ENDPOINT, inMajor,
			inMinor, path);
		// Endpoitn does not exists
		if (endpoint == null) {
			throw new UnsupportedOperationException("Missing requested path : " + path);
		}

		// Fetch the list of filters
		List<BiFunction<ApiRequest, ApiResponse, ApiResponse>> beforeFilterList = fetchMultipleApiFunction(ApiFunctionType.BEFORE,
			inMajor, inMinor, path);
		List<BiFunction<ApiRequest, ApiResponse, ApiResponse>> afterFilterList = fetchMultipleApiFunction(ApiFunctionType.AFTER,
			inMajor, inMinor, path);

		// ApiResponse setup (if null)
		if (resObj == null) {
			resObj = new ApiResponse(this);
		}

		// Attempt to do the execution, any HaltException is caught and handled here
		try {
			// Before filter handling
			resObj = executeApiFunctionList(beforeFilterList, reqObj, resObj);

			// Endpoint execution
			resObj = endpoint.apply(reqObj, resObj);

			// After filter handling
			resObj = executeApiFunctionList(afterFilterList, reqObj, resObj);

			// Return result at the end
			return resObj;
		} catch (HaltException h) {
			// if ( resObj.get(ERROR) != null ) {
			// 	System.out.println(resObj.get(ERROR));
			// }
			return resObj;
		} catch (Exception e) {
			throw e;
		}

		// Return failure
		// return null;
	}

	/**
	 * Execute a request
	 *
	 * @param   path name to use
	 * @param   request query parameter to pass forward.
	 * @param   context data parameter to pass forward,
	 *
	 * @return  The result parameters
	 **/
	public ApiResponse execute(String path, Map<String, Object> queryParams,
		Map<String, Object> contextParams) {
		return execute(majorVersion, minorVersion, path, queryParams, contextParams);
	}

	/**
	 * Execute a request
	 *
	 * @param   path name to use
	 * @param   request query parameter to pass forward.
	 * @param   context data parameter to pass forward,
	 *
	 * @return  The result parameters
	 **/
	public ApiResponse execute(String path, ApiRequest reqObj, ApiResponse resObj) {
		return execute(majorVersion, minorVersion, path, reqObj, resObj);
	}

	/**
	 * Execute a request
	 *
	 * @param   path name to use
	 * @param   request query parameter to pass forward.
	 *
	 * @return  The result parameters
	 **/
	public ApiResponse execute(String path, Map<String, Object> queryParams) {
		return execute(majorVersion, minorVersion, path, queryParams, (Map<String, Object>) null);
	}

	//-------------------------------------------------------------------
	//
	// API map compliance
	//
	//-------------------------------------------------------------------

	/**
	 * Used mainly for debugging purposes, not optimize for general usage
	 *
	 * @return  The collapsed key set of the current configured version
	 **/
	public Set<String> keySet() {
		return collapsedVersionSet().endpointMap.keySet();
	}

	/**
	 * Used mainly for debugging purposes, not optimize for general usage
	 *
	 * @return  The registed function for the given key
	 **/
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> get(Object key) {
		return collapsedVersionSet().endpointMap.get(key);
	}

	/**
	 * Registers an API function to a single endpoint
	 *
	 * @param  Endpoint paths
	 * @param  Executor function
	 *
	 * @return  returns null
	 **/
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> put(String path,
		BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		endpoint(path, value);
		return null;
	}

	/**
	 * Removes an endpoint for the current version
	 * This intentionally remove if from the specified version onwards
	 *
	 * @param  Endpoint paths
	 *
	 * @return  returns null
	 **/
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> remove(String path) {
		return put(path, null);
	}

	//-------------------------------------------------------------------
	//
	// API JS Handling
	//
	//-------------------------------------------------------------------
	protected String getApiJS(String apiURL) {
		return ApiBuilderJS.generateApiJs(this, apiURL);
	}

	//-------------------------------------------------------------------
	//
	// Servlet processing
	//
	//-------------------------------------------------------------------

	/**
	 * The intenal base CorePage to refrence any needed servlet setup data
	 **/
	protected CorePage corePageServlet = null;

	/**
	 * Setup the servlet linkage, and does the respective API call
	 *
	 * @param  CorePage to based other request from
	 * @param  Path name to use
	 *
	 * @return ApiResponse, to propagate to the actual user
	 **/
	public ApiResponse servletExecute(CorePage inCore, String[] path, String apiURL) {
		// Setup
		ApiRequest req = setupApiRequest(inCore);
		ApiResponse res = new ApiResponse(this);
		int[] intV = new int[] { majorVersion, minorVersion };
		// Setup the ApiBuidler corePageServlet : for reuse if needed
		if (corePageServlet == null) {
			corePageServlet = inCore;
		}
		if (path.length > 1 && path[0].matches("^v\\d+\\.\\d+$")) {
			String[] versions = path[0].substring(1).split("\\.");
			intV = new int[versions.length];
			for (int idx = 0; idx < versions.length; idx++)
				intV[idx] = Integer.parseInt(versions[idx]);

			// Remove the versioning
			if (path.length > 1)
				path = Arrays.copyOfRange(path, 1, path.length);

		}
		// Check if it is calling API JS
		if (path.length >= 1 && path[0].equalsIgnoreCase("api.js")) {
			inCore.getHttpServletResponse().setContentType("text/javascript");
			PrintWriter output = inCore.getWriter();
			if (apiURL == null) {
				output.println(getApiJS(inCore.getServerName()));
			} else {
				output.println(getApiJS(apiURL));
			}
			return null;
		}

		// If it is invalid path with or without versioning
		if (!isValidPath(String.join(".", path))
			&& !isValidPath(intV[0], intV[1], String.join(".", path))) {
			//Invalid path, terminate
			res.put("ERROR", "Unknown API request endpoint");
			res.put("INFO", "Requested path : " + String.join(".", path));
			return res;
		}

		try {
			// The actual execution
			return execute(String.join(".", path), req, res);
		} catch (Exception e) {
			// Suppress and print out the error info
			String errorMsg = e.getMessage();
			if( errorMsg == null || errorMsg.trim().isEmpty() ) {
				errorMsg = "Fatal Error";
			}
			res.put("ERROR", errorMsg.trim() );
			res.put("INFO", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
		}
		return res;
	}

}
