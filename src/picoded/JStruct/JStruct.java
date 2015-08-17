package picoded.JStruct;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import picoded.JStruct.internal.*;

///
/// Base object, where the respective data structure
/// implmentation is loaded from.
///
public class JStruct {
	
	// KeyValueMap handling
	//----------------------------------------------

	protected ConcurrentHashMap<String,KeyValueMap> keyValueMapCache = new ConcurrentHashMap<String,KeyValueMap>();
	protected ReentrantReadWriteLock keyValueMapCache_lock = new ReentrantReadWriteLock();
	
	/// Setsup and return a KeyValueMap object,
	/// This is overriden for the various implmentation version
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	public KeyValueMap getKeyValueMap(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		KeyValueMap cacheCopy = keyValueMapCache.get(name);
		if(cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			keyValueMapCache_lock.writeLock().lock();
			
			cacheCopy = keyValueMapCache.get(name);
			if(cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = new JStruct_KeyValueMap();
			keyValueMapCache.put( name, cacheCopy );
			return cacheCopy;
			
		} finally {
			keyValueMapCache_lock.writeLock().unlock();
		}
	}
	
	// MetaTable handling
	//----------------------------------------------

	protected ConcurrentHashMap<String,KeyValueMap> keyValueMapCache = new ConcurrentHashMap<String,KeyValueMap>();
	protected ReentrantReadWriteLock keyValueMapCache_lock = new ReentrantReadWriteLock();
	
	/// Setsup and return a KeyValueMap object,
	/// This is overriden for the various implmentation version
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	public KeyValueMap getKeyValueMap(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		KeyValueMap cacheCopy = keyValueMapCache.get(name);
		if(cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			keyValueMapCache_lock.writeLock().lock();
			
			cacheCopy = keyValueMapCache.get(name);
			if(cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = new JStruct_KeyValueMap();
			keyValueMapCache.put( name, cacheCopy );
			return cacheCopy;
			
		} finally {
			keyValueMapCache_lock.writeLock().unlock();
		}
	}
}
