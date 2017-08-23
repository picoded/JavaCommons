package picoded.core.struct;

import java.util.Map;

import picoded.core.conv.ConvertJSON;

/**
 * Case Insensitive HashMap, useful for various things.
 * Normalizes, stores, and retrives all keys in lowercase.
 *
 * As this class extends HashMap directly, several of its common functionalities are inherited.
 *
 * ### Example Usage
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
 *
 * CaseInsensitiveHashMap tObj = new CaseInsensitiveHashMap<String, String>();
 *
 * Case insensitive put
 * tObj.put("Hello", "WORLD");
 *
 * Outputs "WORLD"
 * String ret = tObj.get("HeLLO");
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 **/
@SuppressWarnings("all")
public class CaseInsensitiveHashMap<K extends String, V> extends GenericConvertHashMap<K, V> {
	
	/**
	 * Java serialversion uid: http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
	 **/
	private static final long serialVersionUID = 42L;
	
	public CaseInsensitiveHashMap() {
		super();
	}
	
	public CaseInsensitiveHashMap(Map<K, V> map) {
		super();
		this.putAll(map);
	}
	
	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for the key, the old value is replaced.
	 *
	 * @param   key     key string with which the specified value is to be associated
	 * @param   value   value to be associated with the specified key
	 *
	 * @return  the previous value associated with key, or null if there was no mapping for key.
	 *           (A null return can also indicate that the map previously associated null with key.)
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {
		return super.put((K) (key.toString().toLowerCase()), value);
	}
	
	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	 * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.toLowerCase().equals(k)),
	 * then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
	 * A return value of null does not necessarily indicate that the map contains no mapping for the key;
	 * it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
	 *
	 * @param    key     the key whose associated value is to be returned
	 *
	 * @return  the value to which the specified key is mapped, or null if this map contains no mapping for the key
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		return super.get((K) (key.toString().toLowerCase()));
	}
	
	/**
	 * Returns true if this map contains a mapping for the specified key.
	 *
	 * @param    key     The key whose presence in this map is to be tested
	 *
	 * @return  true if this map contains a mapping for the specified key.
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey((K) (key.toString().toLowerCase()));
	}
	
	/**
	 * Removes the mapping for the specified key from this map if present.
	 *
	 * @param    key     key whose mapping is to be removed from the map
	 *
	 * @return  the previous value associated with key, or null if there was no mapping for key.
	 *           (A null return can also indicate that the map previously associated null with key.)
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		return super.remove((K) (key.toString().toLowerCase()));
	}
	
	/**
	 * @TODO the actual class implementation
	 *
	 * Copies all of the mappings from the specified map to this map.
	 * These mappings will replace any mappings that this map had for any of the keys currently in the specified map.
	 *
	 * Note: Care should be taken when importing multiple case sensitive mappings, as the order which they are
	 * overwritten may not be predictable / in sequence.
	 *
	 * @param    m     Original mappings to be stored in this map
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (m == null) {
			throw new RuntimeException("putAll cannot be null");
		}
		for (Map.Entry<?, ?> entry : m.entrySet()) {
			super.put((K) (entry.getKey().toString().toLowerCase()), (V) (entry.getValue()));
		}
	}
	
	/**
	 * Implements a JSON to string conversion
	 **/
	@Override
	@SuppressWarnings("unchecked")
	public String toString() {
		return ConvertJSON.fromMap((Map<String, Object>) this);
	}
}
