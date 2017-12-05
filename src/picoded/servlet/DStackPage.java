package picoded.servlet;

import java.util.List;
import java.io.File;

import picoded.core.conv.ConvertJSON;
//
// // Sub modules useds
import picoded.dstack.*;
// import picoded.JStruct.*;
// import picoded.JSql.*;

import picoded.dstack.jsql.connector.JSql;
import picoded.dstack.DStack;
import picoded.dstack.stack.DStackConfigLoader;

/**
 * Extends the corePage functionality, and implements file directory listing, DStack usage, and config files
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
		if ((new File(getConfigPath())).exists()) {
			DConfigObj = new DConfig(getConfigPath());
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
	
	/**
	 * Auto initialize the DStack database setup
	 * on project load.
	 *
	 * Note that this step is skipped if DConfig : sys.Dstack.skipSystemSetup is false
	 */
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		boolean skipSystemSetup = DConfig().getBoolean("sys.DStack.skipSystemSetup", false);
		if (!skipSystemSetup) {
			DStack().systemSetup();
		} else {
			System.out.println("Skipping systemSetup in JStackPage");
		}
	}
	
	/**
	 * Performs the required DStack / JSql connection closure in "shared teardown"
	 **/
	public void doSharedTeardown() throws Exception {
		DStack().close();
		super.doSharedTeardown();
	}
	
}
