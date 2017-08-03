package picoded.servlet;

import java.util.List;
import java.io.File;

import picoded.conv.ConvertJSON;
//
// // Sub modules useds
import picoded.dstack.*;
// import picoded.JStruct.*;
// import picoded.JSql.*;

import picoded.dstack.jsql.connector.JSql;

/**
 * Extends the corePage functionality, and implements file directory listing, JStack usage, and config files
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##[TODO]
 *  + unit tests
 */
public class DStackPage extends CorePage {

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

	public String getPageTemplatePath() {
		return (_pageTemplatePath != null) ? _pageTemplatePath : (_pageTemplatePath = getWebInfPath() + "page/");
	}

	public String getPageOutputPath() {
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

	protected DConfig DConfigObj = null;

	/// @TODO, the actual DConfig integration with the DB. Currently its purely via the file system
	public DConfig DConfig() {
		if (DConfigObj != null) {
			return DConfigObj;
		}

		if ((new File(getConfigsPath())).exists()) {
			DConfigObj = new DConfig(getConfigsPath());
		} else {
			DConfigObj = new DConfig();
		}

		return DConfigObj;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// JStack auto load handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////

	protected DStack DStackObj = null;

	/// Generates the JSQL layer given the config namespace
	///
	/// @TODO : To Deprecate after full roll out of sys.JStack.stack config
	protected JSql JSqlLayerFromConfig(String profileNameSpace) {

		// Gets the configuration setup
		DConfig jc = DConfig();

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
		// } else if (engine.equalsIgnoreCase("mssql")) {
		// 	return JSql.mssql(path, database, username, password);
		} else if (engine.equalsIgnoreCase("mysql")) {
			return JSql.mysql(path, database, username, password);
		// } else if (engine.equalsIgnoreCase("oracle")) {
		// 	return JSql.oracle(path, username, password);
		} else {
			throw new RuntimeException("Unsupported " + profileNameSpace + ".engine: " + engine);
		}
	}

	// /// Loads the configurations from JStack, and setup the respective JStackLayers
	// ///
	// /// @TODO: Support JStackLayers jsons config
	protected List<CommonStack> loadConfiguredJStackLayers() {

		// Gets the configuration setup
		DConfig jc = DConfig();
		List<CommonStack> ret = null;

		// Gets the JStack configuration, and use it (if exists)
		//---------------------------------------------------------------
		List<Object> stackOptions = jc.getObjectList("sys.DStack.stack", null);
		if (stackOptions !=null)
			for(Object stackOption : stackOptions)
				System.out.println(ConvertJSON.fromObject(stackOption)+ " awjkehakjwehjkawe");
		if(stackOptions == null || stackOptions.size() <= 0){
			System.out.println("---JStackPage -> loadConfiguredJStackLayers -> stackOptions is null or empty---");
		}
		return null;
		// ret = JStackUtils.stackConfigLayersToJStackLayers(stackOptions, getWebInfPath());
		// if (ret != null) {
		// 	return ret;
		// } else {
		// 	throw new RuntimeException("Unable to configure DStack");
		// }

		// Else falls back to legacy support
		//---------------------------------------------------------------
	//
	// 	// Gets the profile name and type
	// 	String profileName = jc.getString("sys.dataStack.selected.profile.name", "JSqlOnly.sqlite");
	// 	String profileType = jc.getString("sys.dataStack.selected.profile.type", "JSql");
	//
	// 	if (profileType.equalsIgnoreCase("JSql")) {
	// 		return new JStackLayer[] { JSqlLayerFromConfig("sys.dataStack." + profileName) };
	// 	} else {
	// 		throw new RuntimeException("Unsupported sys.dataStack.selected.profile.type: " + profileType);
	// 	}
	}

	////////////////////////////////////////////////////
	// tableSetup calls for various jSql based modules
	////////////////////////////////////////////////////

	/// Returns the JStack object
	/// @TODO Actual JStack config loading, now it just loads a blank sqlite file =(
	public DStack DStack() {
		if (DStackObj != null) {
			return DStackObj;
		}

		// Load the JStack object
		loadConfiguredJStackLayers();
		// DStackObj = new DStack(loadConfiguredJStackLayers());

		// And preload the tables
		// JStackUtils.preloadJStruct(DStackObj, DConfig().getStringMap("sys.JStack.struct", null));

		return DStackObj;
	}

	/// Auto initialize page builder
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		System.out.println("cool shit happens");
		boolean skipSystemSetup = DConfig().getBoolean("sys.DStack.skipSystemSetup", false);
		if (!skipSystemSetup) {
			// DStack().systemSetup();
			DStack();
		} else {
			System.out.println("Skipping systemSetup in JStackPage");
		}
	}

	/// JStack.disposeStackLayers only if it was initialized
	public void DStack_disposeStackLayers() throws Exception {//JStackException {
		if (DStackObj != null) {
			DStackObj.systemDestroy();
			DStackObj = null;
		}
	}

	/// Does the disposal teardown of all layers (especially JSql in MySql mode)
	@Override
	public void doSharedTeardown() throws Exception {
		DStack_disposeStackLayers();
		super.doSharedTeardown();
	}

}
