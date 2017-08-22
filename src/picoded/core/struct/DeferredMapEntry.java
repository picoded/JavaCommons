package picoded.core.struct;

import java.util.Map;

/**
* Utiltiy function to create a Map.Entry, which gets the value only when requested
*
* AKA. put / get is only called when needed.
* Allowing skipping value checks by key name to be optimized out.
*
* This is used by default in UnssuportedDefaultMap
**/
public class DeferredMapEntry<K extends Object, V extends Object> implements Map.Entry<K, V> {
	// Internal vars
	// ----------------------------------------------

	protected K key = null;
	protected Map<K, V> sourceMap = null;

	// Constructor
	// ----------------------------------------------

	/**
	* Constructor with key and value.
	**/
	public DeferredMapEntry(Map<K, V> map, K inKey) {
		sourceMap = map;
		key = inKey;
	}

	// Map.Entry operators
	// ----------------------------------------------

	/**
	* Returns the key corresponding to this entry.
	**/
	@Override
	public K getKey() {
		return key;
	}

	/**
	* Returns the value corresponding to this entry.
	**/
	@Override
	public V getValue() {
		return sourceMap.get(key);
	}

	/**
	* Compares the specified object with this entry for equality.
	**/
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		boolean flag = false;
		if (this.getClass() == o.getClass()) {
			Map.Entry<K, V> e1 = this;
			Map.Entry<K, V> e2 = (Map.Entry<K, V>) o;
			flag = e1.getKey() == null ? (e2.getKey() == null) : e1.getKey().equals(e2.getKey());
			flag = flag
				&& (e1.getValue() == null ? (e2.getValue() == null) : e1.getValue().equals(
					e2.getValue()));
		}
		return flag;
	}

	/**
	* Returns the hash code value for this map entry.
	*
	* Note that you should not rely on hashCode.
	* See: http://stackoverflow.com/questions/785091/consistency-of-hashcode-on-a-java-string
	**/
	@Override
	public int hashCode() {
		return (getKey() == null ? 0 : getKey().hashCode())
			^ (getValue() == null ? 0 : getValue().hashCode());
	}

	/**
	* Replaces the value corresponding to this entry with the specified value
	**/
	@Override
	public V setValue(V value) {
		return sourceMap.put(key, value);
	}

	@Override
	public String toString() {
		return getKey() + "=" + getValue();
	}
}
