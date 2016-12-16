package picoded.JStruct;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JStruct.internal.*;
//import picoded.JStack.JStackLayer;

///
/// Base object, where the respective data structure
/// implmentation is loaded from.
///
public class JStruct /*implements JStackLayer*/ {
	
	// KeyValueMap handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, KeyValueMap> keyValueMapCache = new ConcurrentHashMap<String, KeyValueMap>();
	protected ReentrantReadWriteLock keyValueMapCacheLock = new ReentrantReadWriteLock();
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	protected KeyValueMap setupKeyValueMap(String name) {
		return new JStruct_KeyValueMap();
	}
	
	/// Setsup and return a KeyValueMap object,
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	public KeyValueMap getKeyValueMap(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase(Locale.ENGLISH);
		
		// Tries to get 1 time, without locking
		KeyValueMap cacheCopy = keyValueMapCache.get(name);
		//		if (cacheCopy != null) {
		//			return cacheCopy;
		//		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			keyValueMapCacheLock.writeLock().lock();
			
			cacheCopy = keyValueMapCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupKeyValueMap(name);
			keyValueMapCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			keyValueMapCacheLock.writeLock().unlock();
		}
	}
	
	// AtomicLongMap handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, AtomicLongMap> atomicLongMapCache = new ConcurrentHashMap<String, AtomicLongMap>();
	protected ReentrantReadWriteLock atomicLongMapCacheLock = new ReentrantReadWriteLock();
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	protected AtomicLongMap setupAtomicLongMap(String name) {
		return new JStruct_AtomicLongMap();
	}
	
	/// Setsup and return a KeyValueMap object,
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	public AtomicLongMap getAtomicLongMap(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase(Locale.ENGLISH);
		
		// Tries to get 1 time, without locking
		AtomicLongMap cacheCopy = atomicLongMapCache.get(name);
		//		if (cacheCopy != null) {
		//			return cacheCopy;
		//		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			atomicLongMapCacheLock.writeLock().lock();
			
			cacheCopy = atomicLongMapCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupAtomicLongMap(name);
			atomicLongMapCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			atomicLongMapCacheLock.writeLock().unlock();
		}
	}
	
	// MetaTable handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, MetaTable> metaTableCache = new ConcurrentHashMap<String, MetaTable>();
	protected ReentrantReadWriteLock metaTableCacheLock = new ReentrantReadWriteLock();
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	protected MetaTable setupMetaTable(String name) {
		return new JStruct_MetaTable();
	}
	
	/// Setsup and return a MetaTable object,
	///
	/// @param name - name of MetaTable in backend
	///
	/// @returns MetaTable
	public MetaTable getMetaTable(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase(Locale.ENGLISH);
		
		// Tries to get 1 time, without locking
		MetaTable cacheCopy = metaTableCache.get(name);
		//		if (cacheCopy != null) {
		//			return cacheCopy;
		//		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			metaTableCacheLock.writeLock().lock();
			
			cacheCopy = metaTableCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupMetaTable(name);
			metaTableCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			metaTableCacheLock.writeLock().unlock();
		}
	}
	
	// AccountTable handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, AccountTable> accountTableCache = new ConcurrentHashMap<String, AccountTable>();
	protected ReentrantReadWriteLock accountTableCacheLock = new ReentrantReadWriteLock();
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns AccountTable
	protected AccountTable setupAccountTable(String name) {
		return new AccountTable(this, name);
	}
	
	/// Setsup and return a MetaTable object,
	///
	/// @param name - name of AccountTable in backend
	///
	/// @returns AccountTable
	public AccountTable getAccountTable(String name) {
		
		// Structure backend name is case insensitive
		name = name.toUpperCase(Locale.ENGLISH);
		
		// Tries to get 1 time, without locking
		AccountTable cacheCopy = accountTableCache.get(name);
		//		if (cacheCopy != null) {
		//			return cacheCopy;
		//		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			accountTableCacheLock.writeLock().lock();
			
			cacheCopy = accountTableCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupAccountTable(name);
			accountTableCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			accountTableCacheLock.writeLock().unlock();
		}
	}
	
	///
	/// Preload a single JStruct type object. This is use to prime the JStruct object for systemSetup calls.
	///
	public void preloadJStructType(String type, String name) {
		if ("AccountTable".equalsIgnoreCase(type)) {
			this.getAccountTable(name);
		} else if ("MetaTable".equalsIgnoreCase(type)) {
			this.getMetaTable(name);
		} else if ("KeyValueMap".equalsIgnoreCase(type)) {
			this.getKeyValueMap(name);
		} else if ("AtomicLongMap".equalsIgnoreCase(type)) {
			this.getAtomicLongMap(name);
		} else {
			throw new RuntimeException("Unknown struct type : " + type);
		}
	}
	
	//----------------------------------------------
	// automated setup of cached tables
	//----------------------------------------------
	
	/// This does the setup called on all the cached tables, created via get calls
	public void systemSetup() {
		try {
			keyValueMapCacheLock.readLock().lock();
			metaTableCacheLock.readLock().lock();
			atomicLongMapCacheLock.readLock().lock();
			
			keyValueMapCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
			metaTableCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
			atomicLongMapCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
		} finally {
			keyValueMapCacheLock.readLock().unlock();
			metaTableCacheLock.readLock().unlock();
			atomicLongMapCacheLock.readLock().unlock();
		}
	}
	
	/// This does the teardown called on all the cached tables, created via get calls
	public void systemTeardown() {
		try {
			keyValueMapCacheLock.readLock().lock();
			metaTableCacheLock.readLock().lock();
			atomicLongMapCacheLock.readLock().lock();
			
			keyValueMapCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
			metaTableCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
			atomicLongMapCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
		} finally {
			keyValueMapCacheLock.readLock().unlock();
			metaTableCacheLock.readLock().unlock();
			atomicLongMapCacheLock.readLock().unlock();
		}
	}
	
}
