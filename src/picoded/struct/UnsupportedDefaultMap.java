package picoded.struct;

import java.util.*;

///
/// Simple interface pattern, that implements the default map functions, which throws an UnsupportedOperationException
///
/// @TODO Implment several default functions such as putAll using put / entrySet, using keySet, etc. Including the test case
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public interface UnsupportedDefaultMap<K, V> extends Map<K, V> {
	
	// Critical functions that need to over-ride, to support Map
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	public default V get(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default V put(K key, V value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default V remove(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default Set<K> keySet() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// Optional function which collides with native object
	// and hence cannot be default interface function
	//-------------------------------------------------------------------
	
	// (optional) throws an UnsupportedOperationException
	// public boolean equals(Object o) {
	// 	return (o instanceof Map) &&
	// 	entrySet.equals( ((Map)o).entrySet() );
	// }
	
	// (optional) throws an UnsupportedOperationException
	// public int hashCode() {
	// 	int ret = 0;
	// 	for(K key : keySet()) {
	// 		ret += (new DeferredMapEntry<K,V>(this, key)).hashCode();
	// 	}
	// 	return ret;
	// }
	
	// Optional functions to support with unoptimized solutions,
	// based on critical functions
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	public default void clear() {
		for (K key : keySet()) {
			remove(key);
		}
	}
	
	/// Does an unoptimized check, using keySet9)
	public default boolean containsKey(Object key) {
		return keySet().contains(key);
	}
	
	/// Does an unoptimized check, using keySet9)
	public default boolean containsValue(Object value) {
		for (K key : keySet()) {
			V val = get(key);
			
			if (value == null) {
				if (val == null) {
					return true;
				}
			} else {
				if (value.equals(val)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/// throws an UnsupportedOperationException
	public default Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
		for (K key : keySet()) {
			ret.add(new DeferredMapEntry<K, V>(this, key));
		}
		return ret;
	}
	
	/// throws an UnsupportedOperationException
	public default boolean isEmpty() {
		return (keySet().size() == 0);
	}
	
	/// throws an UnsupportedOperationException
	public default void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	/// throws an UnsupportedOperationException
	public default int size() {
		return keySet().size();
	}
	
	/// throws an UnsupportedOperationException
	public default Collection<V> values() {
		List<V> ret = new ArrayList<V>();
		for (K key : keySet()) {
			ret.add(get(key));
		}
		return ret;
	}
	
}
