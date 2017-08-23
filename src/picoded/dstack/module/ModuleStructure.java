package picoded.dstack.module;

import java.util.*;

import picoded.dstack.*;
import picoded.core.conv.*;
import picoded.core.struct.*;

/**
 * The core module structure in which, every module class has to comply with.
 * This helps provide consistent setup across implementations.
 **/
public abstract class ModuleStructure {
	
	//----------------------------------------------------------------
	//
	//  Constructor setup
	//
	//----------------------------------------------------------------
	
	/**
	 * Common stack being used
	 **/
	protected CommonStack stack = null;
	
	/**
	 * The name prefix to use for the module
	 **/
	protected String name = null;
	
	/**
	 * The internal structure list, sued by setup/destroy/maintenance
	 **/
	protected List<CommonStructure> internalStructureList = null;
	
	/**
	 * Setup a module structure given a stack, and its name
	 *
	 * @param  CommonStack / DStack system to use
	 * @param  Name used to setup the prefix of the complex structure
	 **/
	public ModuleStructure(CommonStack inStack, String inName) {
		stack = inStack;
		name = inName;
		internalStructureList = setupInternalStructureList();
	}
	
	//----------------------------------------------------------------
	//
	//  Internal CommonStructure management
	//
	//----------------------------------------------------------------
	
	/**
	 * Setup the list of local CommonStructure's
	 * this is used internally by setup/destroy/maintenance
	 *
	 * THIS MUST BE OVERWRITTEN, by actual implementation
	 * so that the various setup/destroy/maintenance calls works
	 **/
	protected abstract List<CommonStructure> setupInternalStructureList();
	
	//----------------------------------------------------------------
	//
	//  Preloading of DStack structures, systemSetup/Teardown
	//
	//----------------------------------------------------------------
	
	/**
	 * This does the setup called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void systemSetup() {
		internalStructureList.forEach(item -> item.systemSetup());
	}
	
	/**
	 * This does the teardown called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void systemDestroy() {
		internalStructureList.forEach(item -> item.systemDestroy());
	}
	
	/**
	 * Perform maintenance, this is meant for large maintenance jobs.
	 * Such as weekly or monthly compaction. It may or may not be a long
	 * running task, where its use case is backend specific
	 **/
	public void maintenance() {
		internalStructureList.forEach(item -> item.maintenance());
	}
	
	/**
	 * Perform increment maintenance, meant for minor changes between requests.
	 *
	 * By default this randomly triggers a maintenance call with 2% probability.
	 * The main reason for doing so, is that for many implmentations there may not be
	 * a concept of incremental maintenance, and in many cases its implementor may forget
	 * to actually call a maintenance call. For years.
	 *
	 * Unless the maintenance call is too expensive, (eg more then 2 seconds), having
	 * it randomly trigger and slow down one transaction randomly. Helps ensure everyone,
	 * systems is more performant in overall.
	 *
	 * It is a very controversal decision, however as awsome as your programming or
	 * devops team is. Your client and their actual infrastructure may be "not as awesome"
	 **/
	public void incrementalMaintenance() {
		internalStructureList.forEach(item -> item.incrementalMaintenance());
	}
	
	/**
	 * Removes all data, without tearing down setup
	 *
	 * This is equivalent of "TRUNCATE TABLE {TABLENAME}"
	 **/
	public void clear() {
		internalStructureList.forEach(item -> item.clear());
	}
	
}
