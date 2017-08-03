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
import picoded.dstack.DStack;
import picoded.dstack.stack.DStackConfigLoader;

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
	// DStack auto load handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////

	protected DStack DStackObj = null;

	////////////////////////////////////////////////////
	// tableSetup calls for various jSql based modules
	////////////////////////////////////////////////////

	/**
	 * Returns the DStack if exists, else generate and return
	 * 
	 * @return  the DStack object
	 */
	public DStack DStack() {
		if (DStackObj != null) {
			return DStackObj;
		}
		List<Object> stackConfig = DConfig().getObjectList("sys.DStack.stack", null);
		DStackObj = DStackConfigLoader.generateDStack(stackConfig);
		return DStackObj;
	}

	/// Auto initialize page builder
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		boolean skipSystemSetup = DConfig().getBoolean("sys.DStack.skipSystemSetup", false);
		if (!skipSystemSetup) {
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
