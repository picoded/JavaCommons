package picoded.JStruct.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JStruct.AtomicLongMap;

/// Refence implementation of AtomicLongMap data structure
///
/// This is intended to be an optimized key value map data storage
/// Used mainly in caching or performance critical scenerios.
///
/// As such its sacrifices much utility for performance
///
/// Note that expire timestamps are measured in seconds,
/// anything smaller then that is pointless over a network
///
public class JStruct_AtomicLongMap implements AtomicLongMap {
	
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
	public JStruct_AtomicLongMap() {
		// does nothing =X
	}
	
	///
	/// Utility functions used internally
	///--------------------------------------------------------------------------
	
	/// Gets the current system time in seconds
	public long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis()) / 1000L;
	}
	
	//
	// put, get, etc (public)
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Number
	///
	/// @returns null
	@Override
	public Long put(String key, Number value) {
		try {
			accessLock.writeLock().lock();
			
			if (value == null) {
				valueMap.remove(key);
			} else {
				valueMap.put(key, value.longValue());
			}
			
			return null;
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns null
	@Override
	public Long put(String key, long value) {
		
		try {
			accessLock.writeLock().lock();
			
			//convert from long to Long
			Long newVal = Long.valueOf(value);
			
			if (newVal.longValue() <= 0) {
				valueMap.remove(key);
			} else {
				valueMap.put(key, newVal);
			}
			return null;
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Long
	///
	/// @returns null
	@Override
	public Long put(String key, Long value) {
		
		try {
			accessLock.writeLock().lock();
			
			if (value == null) {
				valueMap.remove(key);
			} else {
				valueMap.put(key, value);
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
		
		// return valueMap.get(key);
	}
	
	/// Returns the value, given the key
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
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key
	@Override
	public Long getAndIncrement(Object key) {
		
		try {
			accessLock.readLock().lock();
			
			Long oldVal = valueMap.get(key);
			if (oldVal == null) {
				return null;
			}
			
			Long newVal = oldVal + 1;
			valueMap.put(key.toString(), newVal);
			
			return oldVal;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after adding
	@Override
	public Long incrementAndGet(Object key) {
		try {
			accessLock.readLock().lock();
			
			Long oldVal = valueMap.get(key);
			if (oldVal == null) {
				return null;
			}
			
			Long newVal = oldVal + 1;
			valueMap.put(key.toString(), newVal);
			
			return valueMap.get(key);
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
			// System.out.println("CurVal:" + curVal + " ExpectVal:" + expect);
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
