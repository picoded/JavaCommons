package picoded.dstack.core;

import picoded.dstack.CommonStructure;
import picoded.util.security.NxtCrypt;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;

/**
 * Common utility class of CommonStructure.
 *
 * Does not actually implement its required feature,
 * but helps provide a common base line for the configuration system.
 **/
public abstract class Core_DataStructure<K, V> implements CommonStructure {
	
	//--------------------------------------------------------------------------
	//
	// Configuration system used commonly
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Configuration map to use
	 **/
	protected GenericConvertMap<String, Object> configMap = new GenericConvertHashMap<String, Object>();
	
	/**
	 * Persistent config mapping implmentation.
	 *
	 * Currently these values are in use.
	 * + NonceLifespan
	 * + NonceKeyLength
	 **/
	@Override
	public GenericConvertMap<String, Object> configMap() {
		return configMap;
	}
	
	//--------------------------------------------------------------------------
	//
	// Utility functions used internally
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Gets the current system time in seconds
	 **/
	protected long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis()) / 1000L;
	}
	
}
