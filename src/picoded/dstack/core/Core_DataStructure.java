package picoded.dstack.core;

import picoded.dstack.DStackCommon;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

///
/// Common utility class of DStackCommon.
///
/// Does not actually implement its required feature,
/// but helps provide a common base line for the configuration system.
///
public abstract class Core_DataStructure<K, V> implements DStackCommon<K, V> {
	
	//--------------------------------------------------------------------------
	//
	// Configuration system used commonly
	//
	//--------------------------------------------------------------------------
	
	/// Configuration map to use
	protected GenericConvertMap<String, Object> configMap = new GenericConvertHashMap<String, Object>();
	
	///
	/// Persistent config mapping implmentation. 
	/// 
	/// Currently these values are in use.
	/// + NonceLifespan
	/// + NonceKeyLength
	///
	@Override
	public GenericConvertMap<String, Object> configMap() {
		return configMap;
	}
	
	//--------------------------------------------------------------------------
	//
	// Utility functions used internally
	//
	//--------------------------------------------------------------------------
	
	/// Gets the current system time in seconds
	protected long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis()) / 1000L;
	}
	
}
