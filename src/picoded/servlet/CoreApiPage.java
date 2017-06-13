package picoded.servlet;

// Java Serlvet requirments
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

// java stuff
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.io.PrintWriter;

// javacommons stuff
import picoded.servlet.api.*;
import picoded.file.ConfigFileSet;

/**
 * Extends the core API page, to support API's
 */
public class CoreApiPage extends CorePage {
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal variables, can be overwritten. Else it is auto "filled" when needed
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String _webInfPath = null;
	protected String _classesPath = null;
	protected String _libraryPath = null;
	protected String _configsPath = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Path variables, according to standard WAR package convention
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/// @return WEB-INF folder path
	public String getWebInfPath() {
		return (_webInfPath != null) ? _webInfPath : (_webInfPath = getContextPath() + "WEB-INF/");
	}
	
	/// @return classes folder path
	public String getClassesPath() {
		return (_classesPath != null) ? _classesPath : (_classesPath = getWebInfPath() + "classes/");
	}
	
	/// @return library folder path
	public String getLibraryPath() {
		return (_libraryPath != null) ? _libraryPath : (_libraryPath = getWebInfPath() + "lib/");
	}
	
	/// @return config files path
	public String getConfigPath() {
		return (_configsPath != null) ? _configsPath : (_configsPath = getWebInfPath() + "config/");
	}
	
	//
	// TO MIGRATE TO NEXT LAYER
	//
	// protected String _pageTemplatePath = null;
	// protected String _pageOutputPath = null;
	// public String _jsmlTemplatePath = null;
	// /// @return  page templates path
	// public String getPageTemplatePath() {
	// 	return (_pageTemplatePath != null) ? _pageTemplatePath : (_pageTemplatePath = getWebInfPath() + "page/");
	// }
	// 
	// public String getPageOutputPath() {
	// 	return (_pageOutputPath != null) ? _pageOutputPath : (_pageOutputPath = getContextPath());
	// }
	//
	// public String getJsmlTemplatePath() {
	// 	return (_jsmlTemplatePath != null) ? _jsmlTemplatePath : (_jsmlTemplatePath = getWebInfPath() + "jsml/");
	// }
	
	/////////////////////////////////////////////
	//
	// Config handling
	//
	/////////////////////////////////////////////
	
	/// Cached memoizer
	protected ConfigFileSet _fileConfig = null;
	
	/// The configuration map
	public ConfigFileSet fileConfig() {
		if (_fileConfig == null) {
			_fileConfig = new ConfigFileSet(getConfigPath());
		}
		return _fileConfig;
	}
	
	/////////////////////////////////////////////
	//
	// ApiBuilder handling
	//
	/////////////////////////////////////////////
	
	/// Cached restbuilder object
	protected ApiBuilder _apiBuilderObj = null;
	
	/// REST API builder
	public ApiBuilder apiBuilder() {
		// Return the cached result (do not recreate)
		if (_apiBuilderObj != null) {
			return _apiBuilderObj;
		}
		
		// Create a new object, and set it up
		_apiBuilderObj = new ApiBuilder();
		apiBuilderSetup(_apiBuilderObj);
		
		// Return the result
		return _apiBuilderObj;
	}
	
	/// !To Override
	/// to configure the ApiBuilder steps
	///
	/// @param  The APIBuilder object used for setup
	public void apiBuilderSetup(ApiBuilder api) {
		
	}
	
	/////////////////////////////////////////////
	//
	// JSON integration
	//
	/////////////////////////////////////////////
	
	/// Set the request mode to JSON, for API page
	@Override
	public boolean isJsonRequest() {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Indicates its API for API page
		if (wildcardUri != null && wildcardUri.length >= 1 && wildcardUri[0].equalsIgnoreCase("api")) {
			return true;
		}
		
		// Default behaviour
		return super.isJsonRequest();
	}
	
	/// Does the actual final json object to json string output, with contentType "application/javascript"
	@Override
	public boolean outputJSON(Map<String, Object> outputData, Map<String, Object> templateData,
		PrintWriter output) throws Exception {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Does the API call
		if (wildcardUri.length >= 1 && (wildcardUri[0].equalsIgnoreCase("api"))) {
			
			// Prepare data
			String apiPath = String.join(".", Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length));

			// @TODO : Consider integrating template data (CorePage) with context data (ApiBuilder)

			// Does actual execution
			ApiResponse ret = apiBuilder().servletExecute(this, apiPath);

			// There is valid return data
			if( ret != null ) {
				outputData.putAll(ret);
				return super.outputJSON(outputData, templateData, output);
			}

			// Fails the return
			return false;
		}

		// Fails the return
		return false;
	}
	
}
