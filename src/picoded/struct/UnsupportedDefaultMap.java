package picoded.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

///
/// Simple interface pattern, that implements the default map functions, which throws an UnsupportedOperationException
///
/// @TODO Implment several default functions such as putAll using put / entrySet, using keySet, etc. 
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
	// -------------------------------------------------------------------
	
	// / throws an UnsupportedOperationException
	@Override
	default V get(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default V put(K key, V value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default V remove(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default Set<K> keySet() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	@Override
	default void clear() {
		for (K key : keySet()) {
			remove(key);
		}
	}
	
	// / Does an unoptimized check, using keySet9)
	@Override
	default boolean containsKey(Object key) {
		return keySet().contains(key);
	}
	
	// / Does an unoptimized check, using keySet9)
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
	
	// / throws an UnsupportedOperationException
	@Override
	default Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
		for (K key : keySet()) {
			ret.add(new DeferredMapEntry<K, V>(this, key));
		}
		return ret;
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default boolean isEmpty() {
		return keySet().isEmpty();
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	// / throws an UnsupportedOperationException
	@Override
	default int size() {
		return keySet().size();
	}
	
	// / throws an UnsupportedOperationException
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
