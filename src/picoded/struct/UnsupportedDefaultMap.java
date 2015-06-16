package picoded.struct;

import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

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
public interface UnsupportedDefaultMap<K, V> extends Map<K,V> {
	
	/// throws an UnsupportedOperationException
	public default void clear() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default boolean containsKey(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default boolean containsValue(Object value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default Set<Map.Entry<K,V>> entrySet() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// (optional) throws an UnsupportedOperationException
	//	public boolean equals(Object o) {
	//		throw new UnsupportedOperationException("function not supported");
	//	}
	
	/// throws an UnsupportedOperationException
	public default V get(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	// (optional) throws an UnsupportedOperationException
	//	public int hashCode() {
	//		throw new UnsupportedOperationException("function not supported");
	//	}
	
	/// throws an UnsupportedOperationException
	public default boolean isEmpty() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default Set<K> keySet() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default V put(K key, V value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default V remove(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default int size() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	public default Collection<V> values() {
		throw new UnsupportedOperationException("function not supported");
	}
	
}