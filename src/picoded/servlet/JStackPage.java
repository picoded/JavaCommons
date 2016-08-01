package picoded.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URL;
import java.lang.String;
import java.io.File;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset; //import java.nio.charset.StandardCharsets;
import java.util.*;

import java.lang.Number;
import java.lang.System;
import java.io.StringWriter;
import java.util.logging.*;

import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.Throwable;
import java.lang.Exception;

// Objects used
import java.util.HashMap;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.lang.Class;
import java.lang.RuntimeException;
import java.lang.Exception;

// Sub modules useds
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.JSql.*;

/**
 * Extends the corePage functionality, and implements file directory listing, JStack usage, and config files
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##[TODO]
 *  + unit tests
 */
public class JStackPage extends CorePage {
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Static variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal variables, can be overwritten. Else it is auto "filled" when needed
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public String _webInfPath = null;
	public String _classesPath = null;
	public String _libraryPath = null;
	public String _configsPath = null;
	public String _pageTemplatePath = null;
	public String _pageOutputPath = null;
	public String _jsmlTemplatePath = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Path variables, according to standard WAR package convention
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public String getWebInfPath() {
		return (_webInfPath != null) ? _webInfPath : (_webInfPath = getContextPath() + "WEB-INF/");
	}
	
	public String getClassesPath() {
		return (_classesPath != null) ? _classesPath : (_classesPath = getWebInfPath() + "classes/");
	}
	
	public String getLibraryPath() {
		return (_libraryPath != null) ? _libraryPath : (_libraryPath = getWebInfPath() + "lib/");
	}
	
	public String getConfigsPath() {
		return (_configsPath != null) ? _configsPath : (_configsPath = getWebInfPath() + "config/");
	}
	
	public String getPagesTemplatePath() {
		return (_pageTemplatePath != null) ? _pageTemplatePath : (_pageTemplatePath = getWebInfPath() + "page/");
	}
	
	public String getPagesOutputPath() {
		return (_pageOutputPath != null) ? _pageOutputPath : (_pageOutputPath = getContextPath());
	}
	
	public String getJsmlTemplatePath() {
		return (_jsmlTemplatePath != null) ? _jsmlTemplatePath : (_jsmlTemplatePath = getWebInfPath() + "jsml/");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Config file handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	protected JConfig JConfigObj = null;
	
	/// @TODO, the actual JConfig integration with the DB. Currently its purely via the file system
	public JConfig JConfig() {
		if (JConfigObj != null) {
			return JConfigObj;
		}
		
		if ((new File(getConfigsPath())).exists()) {
			JConfigObj = new JConfig(getConfigsPath());
		} else {
			JConfigObj = new JConfig();
		}
		
		return JConfigObj;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// JStack auto load handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	protected JStack JStackObj = null;
	
	/// Generates the JSQL layer given the config namespace
	/// 
	/// @TODO : To Deprecate after full roll out of sys.JStack.stack config
	protected JSql JSqlLayerFromConfig(String profileNameSpace) {
		
		// Gets the configuration setup
		JConfig jc = JConfig();
		
		// Gets the config vars
		String engine = jc.getString(profileNameSpace + ".engine", "");
		String path = jc.getString(profileNameSpace + ".path", "");
		String username = jc.getString(profileNameSpace + ".username", "");
		String password = jc.getString(profileNameSpace + ".password", "");
		String database = jc.getString(profileNameSpace + ".database", "");
		
		// Default fallback on sqlite engine, if the profileNameSpace is default
		// This is used to ensure existing test cases do not break
		if (profileNameSpace.equalsIgnoreCase("sys.dataStack.JSqlOnly.sqlite")) {
			if (engine.length() <= 0) {
				engine = "sqlite";
			}
			if (path.length() <= 0) {
				path = getWebInfPath() + "/sqlite.db";
			}
		}
		
		// SQLite implmentation
		if (engine.equalsIgnoreCase("sqlite")) {
			if (path.length() <= 0) {
				throw new RuntimeException("Unsupported " + profileNameSpace + ".path: " + path);
			}
			
			// Replaces WEB-INF path
			path = path.replace("./WEB-INF/", getWebInfPath());
			path = path.replace("${WEB-INF}", getWebInfPath());
			
			// Generates the sqlite connection with the path
			return JSql.sqlite(path);
		} else if (engine.equalsIgnoreCase("mssql")) {
			return JSql.mssql(path, database, username, password);
		} else if (engine.equalsIgnoreCase("mysql")) {
			return JSql.mysql(path, database, username, password);
		} else if (engine.equalsIgnoreCase("oracle")) {
			return JSql.oracle(path, username, password);
		} else {
			throw new RuntimeException("Unsupported " + profileNameSpace + ".engine: " + engine);
		}
	}
	
	/// Loads the configurations from JStack, and setup the respective JStackLayers
	///
	/// @TODO: Support JStackLayers jsons config
	protected JStackLayer[] loadConfiguredJStackLayers() {
		
		// Gets the configuration setup
		JConfig jc = JConfig();
		JStackLayer[] ret = null;
		
		// Gets the JStack configuration, and use it (if exists)
		//---------------------------------------------------------------
		ret = JStackUtils.stackConfigLayersToJStackLayers( jc.getObjectList("sys.JStack.stack", null), getWebInfPath() );
		if( ret != null ) {
			return ret;
		}
		
		// Else falls back to legacy support
		//---------------------------------------------------------------
		
		// Gets the profile name and type
		String profileName = jc.getString("sys.dataStack.selected.profile.name", "JSqlOnly.sqlite");
		String profileType = jc.getString("sys.dataStack.selected.profile.type", "JSql");
		
		if (profileType.equalsIgnoreCase("JSql")) {
			return new JStackLayer[] { JSqlLayerFromConfig("sys.dataStack." + profileName) };
		} else {
			throw new RuntimeException("Unsupported sys.dataStack.selected.profile.type: " + profileType);
		}
	}
	
	////////////////////////////////////////////////////
	// tableSetup calls for various jSql based modules
	////////////////////////////////////////////////////
	
	/// Returns the JStack object
	/// @TODO Actual JStack config loading, now it just loads a blank sqlite file =(
	public JStack JStack() {
		if (JStackObj != null) {
			return JStackObj;
		}
		
		// Load the JStack object
		JStackObj = new JStack(loadConfiguredJStackLayers());
		
		// And preload the tables
		JStackUtils.preloadJStruct(JStackObj, JConfig().getStringMap("sys.JStack.struct", null));
		
		return JStackObj;
	}
	
	/// JStack.disposeStackLayers only if it was initialized
	public void JStack_disposeStackLayers() throws JStackException {
		if (JStackObj != null) {
			JStackObj.disposeStackLayers();
			JStackObj = null;
		}
	}
	
	/// Does the disposal teardown of all layers (especially JSql in MySql mode)
	@Override
	public void doSharedTeardown() throws Exception {
		JStack_disposeStackLayers();
		super.doSharedTeardown();
	}
	
}
