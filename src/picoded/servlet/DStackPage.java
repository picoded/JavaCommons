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
public class DStackPage extends CoreApiPage {

	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Static variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////////

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
