package picoded.dstack.struct.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.*;
import picoded.dstack.core.*;
import picoded.core.conv.GenericConvert;

/**
 * Refence implementation of AtomicLongMap data structure
 *
 * This is intended to be an optimized key value map data storage
 * Used mainly in caching or performance critical scenerios.
 *
 * As such its sacrifices much utility for performance
 **/
public class StructSimple_AtomicLongMap extends Core_AtomicLongMap {
	
	//--------------------------------------------------------------------------
	//
	// Constructor vars
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Stores the key to value map
	 **/
	protected ConcurrentMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
	
	/**
	 * Read write lock
	 **/
	public static final ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
	
	//--------------------------------------------------------------------------
	//
	// Constructor setup
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Constructor
	 **/
	public StructSimple_AtomicLongMap() {
		// does nothing =X
	}
	
	//--------------------------------------------------------------------------
	//
	// Backend system setup / maintenance teardown
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Sets up the backend storage table, etc. If needed
	 **/
	@Override
	public void systemSetup() {
		// does nothing
	}
	
	/**
	 * Teardown and delete the backend storage table, etc. If needed
	 **/
	@Override
	public void systemDestroy() {
		valueMap.clear();
	}
	
	/**
	 *
	 * Removes all data, without tearing down setup
	 *
	 * Handles re-entrant lock where applicable
	 **/
	@Override
	public void clear() {
		try {
			accessLock.writeLock().lock();
			valueMap.clear();
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	//--------------------------------------------------------------------------
	//
	// Core put / get
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Stores (and overwrites if needed) key, value pair
	 *
	 * Important note: It does not return the previously stored value
	 *
	 * @param key as String
	 * @param value as Long
	 *
	 * @return null
	 **/
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
	
	/**
	 * Returns the value, given the key
	 * @param key param find the thae meta key
	 *
	 * @return  value of the given key
	 **/
	@Override
	public Long get(Object key) {
		try {
			accessLock.readLock().lock();
			
			Long val = valueMap.get(key);
			if (val == null) {
				return 0l;
			}
			return val;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/**
	 * Returns the value, given the key. Then apply the delta change
	 *
	 * @param key param find the meta key
	 * @param delta value to add
	 *
	 * @return  value of the given key, note that it returns 0 if there wasnt a previous value set
	 **/
	@Override
	public Long getAndAdd(Object key, Object delta) {
		try {
			accessLock.readLock().lock();
			
			Long oldVal = valueMap.get(key);
			
			// Assume 0, if old value does not exists
			if (oldVal == null) {
				oldVal = (Long) 0l;
			}
			
			Long newVal = oldVal.longValue() + GenericConvert.toNumber(delta).longValue();
			valueMap.put(key.toString(), newVal);
			
			return oldVal;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/**
	 * Returns 1 if deleted, given the key.
	 *
	 * @param key param find the meta key
	 *
	 * @returns  the result of deletion, it returns 0 if nothing is deleted.
	 **/
	@Override
	public Long remove(Object key) {
		try {
			accessLock.readLock().lock();
			if (valueMap.get(key) != null) {
				valueMap.remove(key);
				return (Long) 1l;
			}
			return (Long) 0l;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/**
	 * Stores (and overwrites if needed) key, value pair
	 *
	 * Important note: It does not return the previously stored value
	 *
	 * @param key as String
	 * @param value as long
	 *
	 * @return true if successful
	 **/
	@Override
	public boolean weakCompareAndSet(String key, Long expect, Long update) {
		
		try {
			accessLock.writeLock().lock();
			Long curVal = valueMap.get(key);
			
			//if current value is equal to expected value, set to new value
			if (curVal != null && curVal.equals(expect)) {
				valueMap.put(key, update);
				return true;
			} else if (curVal == null || curVal == 0l) {
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
