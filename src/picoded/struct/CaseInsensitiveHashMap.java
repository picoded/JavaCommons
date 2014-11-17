package picoded.struct;

import java.util.HashMap;

/// Case Insensitive HashMap, useful for various things (like Oracle/mysql compatibility)
public abstract class CaseInsensitiveHashMap<K extends String,V>  extends HashMap<K,V> {
	
	@Override @SuppressWarnings("unchecked")
	public V put(K key, V value) {
		return super.put( (K)(key.toString().toLowerCase()), value);
	}
	
	@Override @SuppressWarnings("unchecked")
	public V get(Object key) {
		return super.get( (K)(key.toString().toLowerCase()) );
	}
	
	@Override @SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		return super.containsKey( (K)(key.toString().toLowerCase()) );
	}
	
	@Override @SuppressWarnings("unchecked")
	public V remove(Object key) {
		return super.remove( (K)(key.toString().toLowerCase()) );
	}
	
	/// TODO: putAll
	// putAll(Map<? extends K,? extends V> m)
	
}