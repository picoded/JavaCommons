package picoded.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* Interface pattern, that implements most of the default map functions,
* This builds ontop of core functions, in which implmentors of this interface will need to support.
*
* These core functions are as followed
* + get
* + put
* + remove
* + keyset
*
* All other functions are then built ontop of these core function,
* with suboptimal usage patterns. For example .isEmpty(), calls up the keyset(),
* and check its length. When there are probably much more efficent implmentation.
*
* However more importantly is, it works =)
*
* The idea is that this interface allows a programmer, to rapidly implement
* a Map object from any class, with just 4 function, instead of 12
*
* ### Example Usage
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
*
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
**/
public interface UnsupportedDefaultMap<K, V> extends Map<K, V> {

	//-------------------------------------------------------------------
	//
	// Critical functions that need to over-ride, to support Map
	//
	//-------------------------------------------------------------------

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default V get(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default V put(K key, V value) {
		throw new UnsupportedOperationException("function not supported");
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default V remove(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default Set<K> keySet() {
		throw new UnsupportedOperationException("function not supported");
	}

	//-------------------------------------------------------------------
	//
	// Map polyfill related functions
	//
	//-------------------------------------------------------------------

	@Override
	default void clear() {
		// This is a intentional converted to a new HashSet, to avoid
		// ConcurrentModificationException
		Set<K> clearSet = new HashSet<K>(keySet());
		for (K key : clearSet) {
			remove(key);
		}
	}

	/**
	* Does an unoptimized check, using keySet
	**/
	@Override
	default boolean containsKey(Object key) {
		return keySet().contains(key);
	}

	/**
	* Does an unoptimized check, using keySet
	**/
	@Override
	default boolean containsValue(Object value) {
		for (Map.Entry<K, V> entry : entrySet()) {
			V val = entry.getValue();
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

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
		for (K key : keySet()) {
			ret.add(new DeferredMapEntry<K, V>(this, key));
		}
		return ret;
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default boolean isEmpty() {
		return keySet().isEmpty();
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default int size() {
		return keySet().size();
	}

	/**
	* Throws an UnsupportedOperationException
	**/
	@Override
	default Collection<V> values() {
		List<V> ret = new ArrayList<V>();
		for (Map.Entry<K, V> entry : entrySet()) {
			K key = entry.getKey();
			ret.add(get(key));
		}
		return ret;
	}
}
