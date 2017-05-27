package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

///
/// ApiBuilder is a utility class, in which facilitates the building of modern JSON pure API's.
/// This can be used in the project either via a public API, or even internally via a direct function call.
///
/// While faciltating the following....
///
/// + Packaging the entire web project core functionalities
/// + JS Api script generation
/// + Swagger compliant API documentation generation
/// + Internal function calls
///
/// # This favours JSON like API, which may or may not be REST strict
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
/// # Major, Minor sementic versioning support
///
/// See: http://semver.org/
///
/// Major and Minor versioning handling is supported. Patch is intentionally not supported, 
/// as it adds way too much development overhead, for what should by definition, be non breaking changes.
///
/// # Historic notes from JavaCommons.RESTBuilder
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
	
	//////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	//////////////////////////////////////////////////////////////////
	
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
	
	//////////////////////////////////////////////////////////////////
	//
	// Version handling
	//
	//////////////////////////////////////////////////////////////////
	
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
		return "v"+majorVersion+"."+minorVersion;
	}

	/// Set the version values
	///
	/// @param  Major version to configure
	/// @param  Minor version to configure
	///
	/// @return  Array of major, and minor int version
	public int[] version( int inMajor, int inMinor ) {
		majorVersion = inMajor;
		minorVersion = inMinor;
		return version();
	}

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

		// Normalize minor endpoint
		List< ApiVersionSet> minorVersionList = endpointVersionStore.get(majorVersion);
		if( minorVersionList == null ) {
			minorVersionList = new ArrayList<ApiVersionSet>();
			endpointVersionStore.set( inMajor, minorVersionList );
		}
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

	//////////////////////////////////////////////////////////////////
	//
	// API put / remove handling
	//
	//////////////////////////////////////////////////////////////////
	
	/// Registers an API function to a single endpoint
	///
	/// @paramater  Endpoint paths
	/// @parameter  Executor function
	///
	/// @return  returns null
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> put(String key, BiFunction<ApiRequest, ApiResponse, ApiResponse> value) {
		if( value == null ) {
			getVersionSet().endpointMap.put(key, NULLENDPOINT);
		} else {
			getVersionSet().endpointMap.put(key, new ApiEndpoint(value));
		}
		return null;
	}

	/// Removes an endpoint for the current version
	/// This intentionally remove if from the specified version onwards
	///
	/// @paramater  Endpoint paths
	///
	/// @return  returns null
	public BiFunction<ApiRequest, ApiResponse, ApiResponse> remove(String key) {
		return put(key, null);
	}
}