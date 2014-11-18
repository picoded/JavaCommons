package picoded.struct;

import java.util.HashMap;

/// Case Insensitive HashMap, useful for various things (like Oracle/mysql compatibility)
/// Normalizes, stores, and retrives all keys in lowercase.
///
/// As this class extends HashMap directly, several of its common functionalities are inherited
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// CaseInsensitiveHashMap tObj = new CaseInsensitiveHashMap<String, String>();
///
/// // Case insensitive put
/// tObj.put("Hello", "WORLD");
///
/// // Outputs "WORLD"
/// String ret = tObj.get("HeLLO");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class CaseInsensitiveHashMap<K extends String,V>  extends HashMap<K,V> {
	
	/// Java serialversion uid: http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
	private static final long serialVersionUID = 42L;
	
	
	///
	/// Associates the specified value with the specified key in this map.
	/// If the map previously contained a mapping for the key, the old value is replaced.
	///
	/// @param   key     key with which the specified value is to be associated
	/// @param   value   value to be associated with the specified key
	///
	public V put(K key, V value) {
		@Override @SuppressWarnings("unchecked")
		return super.put( (K)(key.toString().toLowerCase()), value);
	}
	
	public V get(Object key) {
		@Override @SuppressWarnings("unchecked")
		return super.get( (K)(key.toString().toLowerCase()) );
	}
	
	public boolean containsKey(Object key) {
		@Override @SuppressWarnings("unchecked")
		return super.containsKey( (K)(key.toString().toLowerCase()) );
	}
	
	public V remove(Object key) {
		@Override @SuppressWarnings("unchecked")
		return super.remove( (K)(key.toString().toLowerCase()) );
	}
	
	/// TODO: putAll
	// putAll(Map<? extends K,? extends V> m)
	
}