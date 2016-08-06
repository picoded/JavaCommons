package picoded.JStruct;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JStruct.internal.*;
import picoded.JStack.JStackLayer;

///
/// Base object, where the respective data structure
/// implmentation is loaded from.
///
public class JStruct implements JStackLayer {
	
	// KeyValueMap handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, KeyValueMap> keyValueMapCache = new ConcurrentHashMap<String, KeyValueMap>();
	protected ReentrantReadWriteLock keyValueMapCache_lock = new ReentrantReadWriteLock();
	
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
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		KeyValueMap cacheCopy = keyValueMapCache.get(name);
		if (cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			keyValueMapCache_lock.writeLock().lock();
			
			cacheCopy = keyValueMapCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupKeyValueMap(name);
			keyValueMapCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			keyValueMapCache_lock.writeLock().unlock();
		}
	}
	
	// AtomicLongMap handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, AtomicLongMap> atomicLongMapCache = new ConcurrentHashMap<String, AtomicLongMap>();
	protected ReentrantReadWriteLock atomicLongMapCache_lock = new ReentrantReadWriteLock();
	
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
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		AtomicLongMap cacheCopy = atomicLongMapCache.get(name);
		if (cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			atomicLongMapCache_lock.writeLock().lock();
			
			cacheCopy = atomicLongMapCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupAtomicLongMap(name);
			atomicLongMapCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			atomicLongMapCache_lock.writeLock().unlock();
		}
	}
	
	// MetaTable handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, MetaTable> metaTableCache = new ConcurrentHashMap<String, MetaTable>();
	protected ReentrantReadWriteLock metaTableCache_lock = new ReentrantReadWriteLock();
	
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
		name = name.toUpperCase();
		
		// Tries to get 1 time, without locking
		MetaTable cacheCopy = metaTableCache.get(name);
		if (cacheCopy != null) {
			return cacheCopy;
		}
		
		// Tries to get again with lock, creates and put if not exists
		try {
			metaTableCache_lock.writeLock().lock();
			
			cacheCopy = metaTableCache.get(name);
			if (cacheCopy != null) {
				return cacheCopy;
			}
			
			cacheCopy = setupMetaTable(name);
			metaTableCache.put(name, cacheCopy);
			return cacheCopy;
			
		} finally {
			metaTableCache_lock.writeLock().unlock();
		}
	}
	
	// AccountTable handling
	//----------------------------------------------
	
	protected ConcurrentHashMap<String, AccountTable> accountTableCache = new ConcurrentHashMap<String, AccountTable>();
	protected ReentrantReadWriteLock accountTableCache_lock = new ReentrantReadWriteLock();
	
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
	
	///
	/// Preload a single JStruct type object. This is use to prime the JStruct object for systemSetup calls.
	///
	public void preloadJStructType(String type, String name) {
		if (type.equalsIgnoreCase("AccountTable")) {
			this.getAccountTable(name);
		} else if (type.equalsIgnoreCase("MetaTable")) {
			this.getMetaTable(name);
		} else if (type.equalsIgnoreCase("KeyValueMap")) {
			this.getKeyValueMap(name);
		} else if (type.equalsIgnoreCase("AtomicLongMap")) {
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
			keyValueMapCache_lock.readLock().lock();
			metaTableCache_lock.readLock().lock();
			atomicLongMapCache_lock.readLock().lock();
			
			keyValueMapCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
			metaTableCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
			atomicLongMapCache.entrySet().stream().forEach(e -> e.getValue().systemSetup());
		} finally {
			keyValueMapCache_lock.readLock().unlock();
			metaTableCache_lock.readLock().unlock();
			atomicLongMapCache_lock.readLock().unlock();
		}
	}
	
	/// This does the teardown called on all the cached tables, created via get calls
	public void systemTeardown() {
		try {
			keyValueMapCache_lock.readLock().lock();
			metaTableCache_lock.readLock().lock();
			atomicLongMapCache_lock.readLock().lock();
			
			keyValueMapCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
			metaTableCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
			atomicLongMapCache.entrySet().stream().forEach(e -> e.getValue().systemTeardown());
		} finally {
			keyValueMapCache_lock.readLock().unlock();
			metaTableCache_lock.readLock().unlock();
			atomicLongMapCache_lock.readLock().unlock();
		}
	}
	
}
