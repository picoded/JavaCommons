package picoded.JCache;

import java.util.*;
import java.util.concurrent.TimeUnit;

import picoded.struct.*;
import com.hazelcast.core.*;
import com.hazelcast.query.*;

///
/// JCacheMap access implementation
///
/// Used to provide implementation specific calls
///
/// @TODO Functional implmentation for Redission, IF POSSIBLE =?
/// 
/// @TODO Extend functionality as a ConcurrentHashMap 
/// (as all underlying map is one)
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class JCacheMap<K, V> extends ProxyGenericConvertMap<K, V> {
	
	///
	/// Constructor and internal vars
	///--------------------------------------------------------------------------
	
	/// Hazlecast map
	protected IMap<K,V> map_hazelcast = null;
	
	/// Protected constructor
	public JCacheMap(Map<K, V> inMap) {
		super(inMap);
		
		if( inMap instanceof IMap ) {
			map_hazelcast = (IMap<K,V>)inMap;
		}
	}
	
	///
	/// Utility functions used internally
	///--------------------------------------------------------------------------
	
	/// Gets the current system time in seconds
	protected long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis()) / 1000L;
	}
	
	///
	/// Extended funcitonality
	///--------------------------------------------------------------------------
	
	/// Sets the internal value with expire lifespan
	///
	/// @param key 
	/// @param value, null means removal
	/// @param expire timestamp (in seconds), 0 means not timestamp
	///
	/// @returns null
	public V put(K key, V value, long expire) {
		//
		// Standard operation for no expire 
		//
		if(expire <= 0) {
			return put(key,value);
		} 
		
		long ttl = expire - currentSystemTimeInSeconds();
		if(ttl < 1) {
			ttl = 1;
		}
		
		//
		// Implmentation specific operation
		//
		if(map_hazelcast != null) {
			map_hazelcast.set(key, value, ttl, TimeUnit.SECONDS);
			return null;
		} else {
			throw new RuntimeException("Put with expiry not supported yet");
		}
	}
	
	/// Gets the expiration time stamp of the entry
	///
	/// @param Key
	/// 
	/// @returns the expire timestamp in seconds
	public long getExpiry(K key) {
		if(map_hazelcast != null) {
			EntryView<K,V> ev = map_hazelcast.getEntryView(key);
			long time = ev.getExpirationTime(); //Do we need to post process this? (ie/1000s)
			return time;
		} else {
			throw new RuntimeException("Put with expiry not supported yet");
		}
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	@SuppressWarnings("unchecked") //supress unchecked of Comparable
	public Set<K> getKeys(V value) {
		if(map_hazelcast != null) {
			if(value == null) {
				return map_hazelcast.keySet();
			}
			
			if( !(value instanceof Comparable) ) {
				throw new RuntimeException("Provided value, must be 'java comparable' : "+value);
			}
			
			EntryObject eo = new PredicateBuilder().getEntryObject();
			Predicate<K,V> byValue = eo.equal( (Comparable<V>)value );
			
			return map_hazelcast.keySet(byValue);
		} else {
			throw new RuntimeException("Put with expiry not supported yet");
		}
	}
}
