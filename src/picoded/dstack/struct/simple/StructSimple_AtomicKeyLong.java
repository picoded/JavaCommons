package picoded.dstack.struct.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.*;
import picoded.dstack.core.*;

/// Refence implementation of AtomicKeyLong data structure
///
/// This is intended to be an optimized key value map data storage
/// Used mainly in caching or performance critical scenerios.
///
/// As such its sacrifices much utility for performance
public class StructSimple_AtomicKeyLong extends Core_AtomicKeyLong {
	
	///
	/// Constructor vars
	///--------------------------------------------------------------------------
	
	/// Stores the key to value map
	protected ConcurrentMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
	
	/// Read write lock
	public static final ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Constructor
	public StructSimple_AtomicKeyLong() {
		// does nothing =X
	}
	
	///
	/// Backend system setup / maintenance teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		// does nothing
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	@Override
	public void systemDestroy() {
		valueMap.clear();
	}

	///
	/// Removes all data, without tearing down setup
	///
	/// Handles re-entrant lock where applicable
	///
	@Override
	public void clear() {
		try {
			accessLock.writeLock().lock();
			valueMap.clear();
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	//
	// Core put / get
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Long
	///
	/// @returns null
	@Override
	public Long put(Object key, Long value) {
		try {
			accessLock.writeLock().lock();
			
			if (value == null) {
				valueMap.remove(key);
			} else {
				valueMap.put(key.toString(), value);
			}
			return null;
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  value of the given key
	@Override
	public Long get(Object key) {
		try {
			accessLock.readLock().lock();
			
			Long val = valueMap.get(key);
			if (val == null) {
				return null;
			}
			return val;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key
	@Override
	public Long getAndAdd(Object key, Object delta) {
		try {
			accessLock.readLock().lock();
			
			Long oldVal = valueMap.get(key);
			
			if (oldVal == null) {
				return null;
			}
			
			Long newVal = oldVal + (Long) delta;
			valueMap.put(key.toString(), newVal);
			
			return oldVal;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns true if successful
	@Override
	public boolean weakCompareAndSet(String key, Long expect, Long update) {
		
		try {
			accessLock.writeLock().lock();
			
			Long curVal = valueMap.get(key);
			
			//if current value is equal to expected value, set to new value
			if (curVal.equals(expect)) {
				valueMap.put(key, update);
				
				return true;
			} else {
				return false;
			}
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
}
