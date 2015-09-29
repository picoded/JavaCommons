package picoded.struct;

import java.util.*;

/// Utiltiy function to create a KeyValuePair, useful for returning a pair of values
///
/// Also acts as a Map.Entry interface.
public class KeyValuePair<K extends Object, V extends Object> implements Map.Entry<K, V> {
	
	// Internal vars
	//----------------------------------------------
	
	protected K key = null;
	protected V val = null;
	
	// Constructor
	//----------------------------------------------
	
	/// Constructor with key and value
	public KeyValuePair(K inKey, V inVal) {
		key = inKey;
		val = inVal;
	}
	
	// Map.Entry operators
	//----------------------------------------------
	
	/// Returns the key corresponding to this entry.
	public K getKey() {
		return key;
	}
	
	/// Returns the value corresponding to this entry.
	public V getValue() {
		return val;
	}
	
	/// Compares the specified object with this entry for equality.
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o instanceof Map.Entry) {
			Map.Entry<K, V> e1 = this;
			Map.Entry<K, V> e2 = (Map.Entry<K, V>) o;
			
			return ((e1.getKey() == null ? e2.getKey() == null : e1.getKey().equals(e2.getKey())) && (e1.getValue() == null ? e2
				.getValue() == null : e1.getValue().equals(e2.getValue())));
		}
		return false;
	}
	
	/// Returns the hash code value for this map entry.
	///
	/// Note that you should not rely on hashCode =[
	/// See: http://stackoverflow.com/questions/785091/consistency-of-hashcode-on-a-java-string
	public int hashCode() {
		return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
	}
	
	/// Replaces the value corresponding to this entry with the specified value (optional operation).
	public V setValue(V value) {
		V old = val;
		val = value;
		return old;
	}
	
}
