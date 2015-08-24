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

	protected ConcurrentHashMap<String,MetaTable> metaTableCache = new ConcurrentHashMap<String,MetaTable>();
	protected ReentrantReadWriteLock metaTableCache_lock = new ReentrantReadWriteLock();
	
	/// Setsup and return a MetaTable object,
	/// This is overriden for the various implmentation version
	///
	/// @param name - name of MetaTable in backend
	///
	/// @returns MetaTable
	public MetaTable getMetaTable(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		MetaTable cacheCopy = metaTableCache.get(name);
		if(cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			metaTableCache_lock.writeLock().lock();
			
			cacheCopy = metaTableCache.get(name);
			if(cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = new JStruct_MetaTable();
			metaTableCache.put( name, cacheCopy );
			return cacheCopy;
			
		} finally {
			metaTableCache_lock.writeLock().unlock();
		}
	}
	
	// AccountTable handling
	//----------------------------------------------

	protected ConcurrentHashMap<String,AccountTable> accountTableCache = new ConcurrentHashMap<String,AccountTable>();
	protected ReentrantReadWriteLock accountTableCache_lock = new ReentrantReadWriteLock();
	
	/// Setsup and return a MetaTable object,
	/// This is overriden for the various implmentation version
	///
	/// @param name - name of AccountTable in backend
	///
	/// @returns AccountTable
	public AccountTable getAccountTable(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		AccountTable cacheCopy = accountTableCache.get(name);
		if(cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			accountTableCache_lock.writeLock().lock();
			
			cacheCopy = accountTableCache.get(name);
			if(cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = new AccountTable(this, name);
			accountTableCache.put( name, cacheCopy );
			return cacheCopy;
			
		} finally {
			accountTableCache_lock.writeLock().unlock();
		}
	}
	
	
	
}
