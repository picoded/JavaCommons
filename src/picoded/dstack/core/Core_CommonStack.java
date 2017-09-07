package picoded.dstack.core;

import picoded.dstack.*;
import picoded.dstack.CommonStructure;
import picoded.util.security.NxtCrypt;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import picoded.dstack.module.account.AccountTable;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Common utility class of CommonStructure.
 *
 * Does not actually implement its required feature,
 * but helps provide a common base line for the configuration system.
 **/
public abstract class Core_CommonStack implements CommonStack {
	
	//----------------------------------------------------------------
	//
	//  Get the various DStack structure
	//
	//----------------------------------------------------------------
	
	/**
	 * Holds the cache collection of KeyValueMaps currently initiated
	 **/
	protected ConcurrentHashMap<String, CommonStructure> structureCache = new ConcurrentHashMap<String, CommonStructure>();
	
	/**
	 * Holds the lock for (possible) write access to the keyValueMap cache
	 **/
	protected ReentrantReadWriteLock structureCacheLock = new ReentrantReadWriteLock();
	
	// [TO OVERWRITE]
	/**
	 * Common structure initialization interface, to be overwritten by actual implementation.
	 * Note that if a specified structure type is not yet supported, return null;
	 *
	 * @param   Type of structure to setup
	 * @param   Name used to initialize the structure
	 *
	 * @return  The CommonStructure that was initialized
	 **/
	public abstract CommonStructure initializeStructure(String type, String name);
	
	/**
	 * Validate the CommonStructure, to its respective type
	 *
	 * @param   Type of structure to validate
	 * @param   CommonStructure to validate
	 *
	 * @return  True if the type is valid
	 **/
	protected boolean validateStructureType(String type, CommonStructure inObj) {
		if ("DataTable".equalsIgnoreCase(type)) {
			return (inObj instanceof DataTable);
		} else if ("KeyValueMap".equalsIgnoreCase(type)) {
			return (inObj instanceof KeyValueMap);
		} else if ("AtomicLongMap".equalsIgnoreCase(type)) {
			return (inObj instanceof AtomicLongMap);
		}
		throw new RuntimeException("Unknown struct type : " + type);
	}
	
	/**
	 * Validate the CommonStructure, to its respective type.
	 * Throws an exception if invalid
	 *
	 * @param   Type of structure to validate
	 * @param   Structure name to use in exception
	 * @param   CommonStructure to validate
	 *
	 * @return  True if the type is valid
	 **/
	protected boolean enforceStructureType(String type, String structName, CommonStructure inObj) {
		if (!validateStructureType(type, inObj)) {
			throw new RuntimeException("Invalid structure type found for " + structName
				+ " - expected " + type);
		}
		return true;
	}
	
	/**
	 * Get the respective structure required,
	 * If its missing the respective structure, a setup call is performed.
	 * If a conflicting structure of a different type was made, an exception occurs
	 *
	 * @param   Type of structure to setup
	 * @param   Name used to initialize the structur
	 *
	 * @return  The respective structure requested
	 **/
	public CommonStructure getStructure(String type, String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase(Locale.ENGLISH);
		
		// Tries to get 1 time, without locking
		// If NULL, assume that a setup call maybe
		// be required (or is occuring concurrently)
		CommonStructure cacheCopy = structureCache.get(name);
		if (cacheCopy != null) {
			enforceStructureType(type, name, cacheCopy);
			return cacheCopy;
		}
		
		// Tries to get again with lock,
		// creates and put if not exists
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			
			// It exists, after write lock,
			// probably a race condition just occured
			cacheCopy = structureCache.get(name);
			if (cacheCopy != null) {
				enforceStructureType(type, name, cacheCopy);
				return cacheCopy;
			}
			
			// Setup and store the newly created
			// structure into the local object cache
			cacheCopy = initializeStructure(type, name);
			if (cacheCopy == null) {
				throw new RuntimeException("Failed to generate structure for name / type : " + name
					+ " / " + type);
			}
			
			structureCache.put(name, cacheCopy);
			
			// Return the newly setup strcture
			// Unlock occurs in "finally"
			return cacheCopy;
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
	//----------------------------------------------------------------
	//
	// Setting up Account Table
	//
	//----------------------------------------------------------------
	
	protected ConcurrentHashMap<String, AccountTable> accountTableCache = new ConcurrentHashMap<String, AccountTable>();
	protected ReentrantReadWriteLock accountTableCache_lock = new ReentrantReadWriteLock();
	
	protected AccountTable setupAccountTable(String name) {
		return new AccountTable(this, name);
	}
	
	public AccountTable getAccountTable(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		AccountTable cacheCopy = accountTableCache.get(name);
		if (cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			accountTableCache_lock.writeLock().lock();
			
			cacheCopy = accountTableCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupAccountTable(name);
			accountTableCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			accountTableCache_lock.writeLock().unlock();
		}
	}
	
	//----------------------------------------------------------------
	//
	//  Preloading of DStack structures, systemSetup/Teardown
	//  or the respective maintenance/incrementalMaintenance call
	//
	//----------------------------------------------------------------
	
	/**
	 * This does the setup called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void systemSetup() {
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			// Iterate and does the systemSetup
			structureCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
	/**
	 * This does the system destruction (no undo)
	 * called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void systemDestroy() {
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			// Iterate and does the systemTeardown
			structureCache.entrySet().stream().forEach(e -> e.getValue().systemDestroy());
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
	/**
	 * This clears out the data (no undo)
	 * called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void clear() {
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			// Iterate and does the systemTeardown
			structureCache.entrySet().stream().forEach(e -> e.getValue().clear());
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
	/**
	 * This does a maintenance across all DStack structures
	 * called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void maintenance() {
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			// Iterate and does the systemTeardown
			structureCache.entrySet().stream().forEach(e -> e.getValue().maintenance());
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
	/**
	 * This does an incrementalMaintenance across all DStack structures
	 * called on all the preloaded DStack structures, created via preload/get calls
	 **/
	public void incrementalMaintenance() {
		try {
			// Acquire write lock, to prevent concurrent initializations
			structureCacheLock.writeLock().lock();
			// Iterate and does the systemTeardown
			structureCache.entrySet().stream().forEach(e -> e.getValue().incrementalMaintenance());
		} finally {
			// Ensure unlock occurs after process completion
			// (Regardless of error handling)
			structureCacheLock.writeLock().unlock();
		}
	}
	
}
