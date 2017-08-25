package picoded.servlet.api.module;

import picoded.servlet.api.*;
import picoded.core.conv.*;
import picoded.core.struct.*;
import picoded.core.common.SystemSetupInterface;

import java.util.*;

/**
 * The Abstract ApiModule that does some basic implementation for ApiModule.
 * This makes it easier to complete the implementation of the ApiModule interface
 **/
abstract public class AbstractApiModule implements ApiModule {
	
	//-------------------------------------------------------------
	// Stored objects
	//-------------------------------------------------------------
	protected ApiBuilder api = null;
	protected String prefixPath = null;
	protected GenericConvertMap<String, Object> config = null;
	protected List<SystemSetupInterface> subsystemList = null;
	
	//-------------------------------------------------------------
	// api setup
	//-------------------------------------------------------------
	
	/**
	 * Setup the API module, with the given parameters. And register the relevant end points
	 *
	 * @param  api         ApiBuilder to add the required functions
	 * @param  prefixPath  prefix to assume as (should be able to accept "" blanks)
	 * @param  config      configuration object, assumed to be a map. use GenericConvert.toStringMap to preprocess the data
	 */
	public void apiSetup(ApiBuilder inApi, String inPrefixPath, Object inConfigMap) {
		// Set the api and its path prefix
		api = inApi;
		prefixPath = inPrefixPath;
		
		// Set the config
		config = defaultConfig();
		config.putAll( GenericConvert.toStringMap(inConfigMap, "{}") );

		subsystemList = internalSubsystemList();

		apiBuilderSetup(api, prefixPath, config);
	}

	/**
	 * [To Overwrite]
	 * Defines the default config object, this is useful in extending modules, and adjusting default behaviours
	 */
	protected GenericConvertHashMap<String,Object> defaultConfig() {
		return new GenericConvertHashMap<String,Object>();
	}

	/**
	 * [To Overwrite]
	 * Does the actual API backend setup logic, this is after standardised apiSetup initialization
	 */
	abstract protected void apiBuilderSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config);

	//-------------------------------------------------------------
	// SystemSetup / Teardown chain helpers
	//-------------------------------------------------------------
	
	/**
	 * [To Overwrite]
	 * List of internal subsystem modules, which is chained systemSetup / Teardown / clear.
	 *
	 * @return List of SystemSetupInterface
	 */
	abstract protected List<SystemSetupInterface> internalSubsystemList();

	//----------------------------------------------------------------
	//
	//  Preloading of DStack structures, systemSetup/Teardown
	//
	//----------------------------------------------------------------
	
	/**
	 * This does the systemSetup call on all the subsystems by default
	 **/
	public void systemSetup() {
		subsystemList.forEach(item -> item.systemSetup());
	}
	
	/**
	 * This does the systemDestroy call on all the subsystems by default
	 **/
	public void systemDestroy() {
		subsystemList.forEach(item -> item.systemDestroy());
	}
	
	/**
	 * This does the maintenance call on all the subsystems by default
	 *
	 * This is meant for large maintenance jobs.
	 * Such as weekly or monthly compaction. It may or may not be a long
	 * running task, where its use case is backend specific
	 **/
	public void maintenance() {
		subsystemList.forEach(item -> item.maintenance());
	}
	
	/**
	 * This does the increment maintenance call on all the subsystems by default,
	 * meant for minor changes between requests.
	 **/
	public void incrementalMaintenance() {
		subsystemList.forEach(item -> item.incrementalMaintenance());
	}
	
	/**
	 * This does the clear call on all the subsystems by default
	 **/
	public void clear() {
		subsystemList.forEach(item -> item.clear());
	}
	
}