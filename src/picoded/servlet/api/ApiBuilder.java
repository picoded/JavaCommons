package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

import picoded.servlet.*;

///
/// ApiBuilder is a utility class, in which facilitates the building of modern JSON pure API's.
/// This can be used in the project either via a public API, or even internally via a direct function call.
///
/// While faciltating the following....
///
/// + Packaging the entire web project core functionalities
/// + JS Api script generation
/// + Swagger compliant API documentation generation
/// + RAML compliant API documentation generation
/// + Internal function calls
///
/// # @TODO Items
///
/// [ ] Add in documentation / definition support API
/// [ ] Add in filter lambda API
/// [ ] Add in parametarised paths support
/// [ ] Add in actual servlet support (See RESTBuilder)
/// [ ] Add in JS API script generation
/// [ ] Patch up endpoint removal, and keyset to take into seperate account filters
/// [ ] Possibly seperate out filters?
/// [ ] Formally define how ambiguity of API endpoints is resolved
///
/// # Description rents
///
/// ## This favours JSON like API, which may or may not be REST strict
///
/// > While this is somewhat a programming 'religious' topic. And I respect your oppinion.
/// > Picoded as a company does deal with quite a number of enterprise companies who for "security reasons"
/// > block off HTTP commands such as PUT / DELETE, as such our API always somehow end up not being purely "RESTful"
/// > With GET commands modifying things, and POST commands deleting things.
/// 
/// So it does the REST blashpamy, it treats GET / PUT / POST / DELETE as all the same.
/// If you hate it, err. Dun use it =| use spark or apache, or something.
///
/// More rents on why REST on API's is problematic here : https://mmikowski.github.io/the_lie/
///
/// ## Major, Minor sementic versioning support
///
/// See: http://semver.org/
///
/// Major and Minor versioning handling is supported. Patch is intentionally not supported, 
/// as it adds way too much development overhead, for what should by definition, be non breaking changes.
///
/// # Really misc notes, I should probably delete
///
/// ## Historic notes from JavaCommons.RESTBuilder
///
/// While, it replaces the original servlet framework role of creating JSON API's. The framework is 
/// also meant to facilitate intra project function calls. One of the examples learnt from the LMS project, 
/// in handling the encryption of export files (for example). Is that instead of rewriting the entire export 
/// module code to be "callable" by a function a rather indirect and "inefficent" method of calling its 
/// local page directly was used. Aka an encryption proxy. Due to the time constraints of the project.
///
/// Simply put, if a standardised API builder was built and used, several page API features can be 
/// called directly instead of being usued via a proxy
///
public class ApiBuilder implements UnsupportedDefaultMap<String, BiFunction<ApiRequest, ApiResponse, ApiResponse>> {
	
	//-------------------------------------------------------------------
	//
	// Constructor
	//
	//-------------------------------------------------------------------
	
	/// Object token representing a "removed" endpoint
	protected static ApiEndpoint NULLENDPOINT = new ApiEndpoint();

	/// ROOT Api connection, refenced by sub-class implementation
	protected ApiBuilder root = null;

	/// Version specific namespace mapping
	/// Major,Minor,Patch,Path = Endpoint
	protected List< List< ApiVersionSet >> endpointVersionStore = null;

	/// Blank constructor, to build a root end point
	public ApiBuilder() {
		this.root = this;
		this.endpointVersionStore = new ArrayList< List< ApiVersionSet >>();
	}
	
	//-------------------------------------------------------------------
	//
	// Version string and configuration handling
	//
	//-------------------------------------------------------------------
	
	/// Major sementic version handling
	protected int majorVersion = 0;

	/// Minor sementic version handling
	protected int minorVersion = 0;

	/// Get the version values
	///
	/// @return  Array of major, and minor int version
	public int[] version() {
		return new int[] { majorVersion, minorVersion };
	}

	/// Get the version string
	///
	/// @return  v(major).(minor)
	public String versionStr() {
		return versionStr(majorVersion, minorVersion);
	}

	/// Get the version string
	///
	/// @return  v(major).(minor)
	protected String versionStr( int inMajor, int inMinor ) {
		return "v"+inMajor+"."+inMinor;
	}

	/// Set the version values
	///
	/// @param  Major version to configure
	/// @param  Minor version to configure
	///
	/// @return  Array of major, and minor int version
	public int[] setVersion( int inMajor, int inMinor ) {
		majorVersion = inMajor;
		minorVersion = inMinor;
		return version();
	}

	//-------------------------------------------------------------------
	//
	// (Raw, non collapsed) Version set handaling
	//
	//-------------------------------------------------------------------
	
	/// Get the current version set, after normalizing it
	///
	/// @return  the current version set
	protected ApiVersionSet getVersionSet() {
		return getVersionSet( majorVersion, minorVersion );
	}

	/// Get a specified version set, after normalizing it
	///
	/// @return  the current version set
	protected ApiVersionSet getVersionSet( int inMajor, int inMinor ) {
		// Normalize major endpoint
		while( endpointVersionStore.size() <= inMajor ) {
			endpointVersionStore.add(null);
		}
		List<ApiVersionSet> minorVersionList = endpointVersionStore.get(majorVersion);
		if( minorVersionList == null ) {
			minorVersionList = new ArrayList<ApiVersionSet>();
			endpointVersionStore.set( inMajor, minorVersionList );
		}

		// Normalize minor endpoint
		while( minorVersionList.size() <= inMinor ) {
			minorVersionList.add(null);
		}

		// Getting the stroage map
		ApiVersionSet vSet = minorVersionList.get(inMinor);
		if( vSet == null ) {
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
	
	/// Internal cache of collapsed version set
	/// Note that this is resetted, when any put operation is performed
	protected Map<String, ApiVersionSet> cachedCollapsedVersionSet = new HashMap<String, ApiVersionSet>();

	/// Get the (possibly) cached collapsed version set
	///
	/// This is used internally to sort out a definiative version set to scan
	/// for an execution request, results are cached until the respective put request overwrites it.
	///
	/// @param  Major version to fetch
	/// @param  Minor version to fetch
	///
	/// @return  The collapsed version set
	protected ApiVersionSet collapsedVersionSet( int inMajor, int inMinor ) {
		// Get the version string
		String verStr = versionStr( inMajor, inMinor );

		// Fetch the version data
		ApiVersionSet ret = cachedCollapsedVersionSet.get(verStr);

		// Make a new version set if null
		if( ret == null ) {
			ret = generateCollapsedVersionSet( inMajor, inMinor ) ;
			cachedCollapsedVersionSet.put(verStr, ret);
		}

		// return cached collapsed version set
		return ret;
	}

	/// Get the (possibly) cached collapsed version set, of the current version
	///
	/// This is used internally to sort out a definiative version set to scan
	/// for an execution request, results are cached until the respective put request overwrites it.
	protected ApiVersionSet collapsedVersionSet() {
		return collapsedVersionSet(majorVersion, minorVersion);
	}

	/// Generates an uncached collapsed version set
	///
	/// This is used internally to sort out a definiative version set to scan
	/// for an execution request.
	///
	/// @param  Major version to fetch
	/// @param  Minor version to fetch
	///
	/// @return  The collapsed version set
	protected ApiVersionSet generateCollapsedVersionSet( int inMajor, int inMinor ) {
		ApiVersionSet ret = new ApiVersionSet();

		// Iterate major versions
		int maxMajor = endpointVersionStore.size();
		for( int major=0; major < maxMajor; ++major ) {

			// Get the list of minor versions
			List<ApiVersionSet> minorSet = endpointVersionStore.get(major);

			// Iterate the minor versions
			int maxMinor = minorSet.size();
			for( int minor = 0; minor < maxMinor; ++minor ) {

				// Load the set to import
				ApiVersionSet setToImport = minorSet.get(minor);

				// If not null, import it
				if( setToImport != null ) {
					ret.importVersionSet(setToImport);
				}

				// Terminates if the relevent major & minor version is met
				if( major >= inMajor && minor >= inMinor ) {
					break;
				}
			}

			// Terminate at the required major version is met
			if( major >= inMajor ) {
				break;
			}
		}

		// Time to remove the "null" endpoints
		HashSet<String> keySet = new HashSet<String>();
		keySet.addAll( ret.endpointMap.keySet() );
		for(String key : keySet) {
			if( ret.endpointMap.get(key) == NULLENDPOINT ) {
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
	
	/// The ApiRequest method setup handling, assumes an internal java call in this case
	///
	/// @param   request query parameter to pass forward. 
	/// @param   context data parameter to pass forward
	///
	/// @return  new ApiRequest, configured to the current ApiBuilder, and any of its default settings (if relevent)
	protected ApiRequest setupApiRequest( Map<String,Object> queryParams, Map<String,Object>contextParams ) {
		ApiRequest ret = new ApiRequest(this, queryParams, contextParams);
		ret.requestMethod = "java";
		return ret;
	}

	/// The ApiRequest method setup handling, loading the request query paremeters from the servlet
	///
	/// @param   The HTTP CorePage to extract data from
	///
	/// @return  new ApiRequest, configured to the current ApiBuilder, and any of its default settings (if relevent)
	protected ApiRequest setupApiRequest( CorePage core ) {
		ApiRequest ret = new ApiRequest(this);
		ret.queryObj = core.requestParameters();
		return ret;
	}

	//-------------------------------------------------------------------
	//
	// API put / remove handling
	//
	//-------------------------------------------------------------------
	
	/// Registers an API function to a single endpoint
	///
	/// @param  Endpoint paths
	/// @param  Executor function
	///
	/// @return  returns null
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> put(String path, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		// Clears the collapsed version set cache
		cachedCollapsedVersionSet.clear();

		// Get the current version, and write the respective endpoint to it
		if( value == null ) {
			getVersionSet().endpointMap.put(path, NULLENDPOINT);
		} else {
			getVersionSet().endpointMap.put(path, new ApiEndpoint(path, value));
		}
		return null;
	}

	/// Removes an endpoint for the current version
	/// This intentionally remove if from the specified version onwards
	///
	/// @paramater  Endpoint paths
	///
	/// @return  returns null
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> remove(String path) {
		return put(path, null);
	}

	/// Execute a request
	///
	/// @param   major version to use
	/// @param   minor version to use
	/// @param   path name to use
	/// @param   request query parameter to pass forward. 
	/// @param   context data parameter to pass forward,
	///
	/// @return  The result parameters
	public ApiResponse execute(int inMajor, int inMinor, String path, Map<String,Object> queryParams, Map<String,Object>contextParams ) {
		return execute(inMajor, inMinor, path, setupApiRequest(queryParams, contextParams), null);
	}

	/// Execute a request
	///
	/// @param   major version to use
	/// @param   minor version to use
	/// @param   path name to use
	/// @param   request query parameter to pass forward. 
	/// @param   context data parameter to pass forward,
	///
	/// @return  The result parameters
	public ApiResponse execute(int inMajor, int inMinor, String path, ApiRequest reqObj, ApiResponse resObj ) {

		// Normalize response object
		if(resObj == null) {
			resObj = new ApiResponse(this);
		}

		// Gets the collapsed version set
		ApiVersionSet workingSet = collapsedVersionSet(inMajor, inMinor);

		// @TODO : ITERATE FILTERS

		// Find the exact match
		ApiEndpoint endpoint = workingSet.endpointMap.get(path);
		if( endpoint != null ) {
			// Exact match found
			resObj = endpoint.execute(reqObj, resObj);
		} else {
			// @TODO : ITERATE FOR DYNAMIC MATCH
		}


		// Return result
		return resObj;
	}

	/// Execute a request
	///
	/// @param   path name to use
	/// @param   request query parameter to pass forward. 
	/// @param   context data parameter to pass forward,
	///
	/// @return  The result parameters
	public ApiResponse execute(String path, Map<String,Object> queryParams, Map<String,Object>contextParams ) {
		return execute(majorVersion, minorVersion, path, queryParams, contextParams);
	}

	/// Execute a request
	///
	/// @param   path name to use
	/// @param   request query parameter to pass forward. 
	/// @param   context data parameter to pass forward,
	///
	/// @return  The result parameters
	public ApiResponse execute(String path, ApiRequest reqObj, ApiResponse resObj ) {
		return execute(majorVersion, minorVersion, path, reqObj, resObj);
	}

	/// Execute a request
	///
	/// @param   path name to use
	/// @param   request query parameter to pass forward. 
	///
	/// @return  The result parameters
	public ApiResponse execute(String path, Map<String,Object> queryParams) {
		return execute(majorVersion, minorVersion, path, queryParams, (Map<String,Object>)null);
	}

	//-------------------------------------------------------------------
	//
	// Useful API's for debugging, via map functions
	//
	//-------------------------------------------------------------------
	
	/// Used mainly for debugging purposes, not optimize for general usage
	///
	/// @return  The collapsed key set of the current configured version
	public Set<String> keySet() {
		return collapsedVersionSet().endpointMap.keySet();
	}

	/// Used mainly for debugging purposes, not optimize for general usage
	///
	/// @return  The registed function for the given key
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> get(Object key) {
		ApiEndpoint endpoint = collapsedVersionSet().endpointMap.get(key);
		if( endpoint != null ) {
			return endpoint.functionLambda;
		}
		return null;
	}
	
	//-------------------------------------------------------------------
	//
	// Servlet processing
	//
	//-------------------------------------------------------------------
	
	/// The intenal base CorePage to refrence any needed servlet setup data
	protected CorePage corePageServlet = null;
	
	/// Setup the servlet linkage, and does the respective API call
	///
	/// @param  CorePage to based other request from
	/// @param  Path name to use
	public ApiResponse servletExecute(CorePage inCore, String path) {
		corePageServlet = inCore;
		ApiRequest req = setupApiRequest(inCore);
		ApiResponse res = new ApiResponse(this);
		return execute(path, req, res);
	}

}