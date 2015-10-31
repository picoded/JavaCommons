package picoded.struct;

import java.util.*;

///
/// Provides a HashMap<K,A ArrayList<V>>,
/// with utility functions to edit records
///
public class ArrayListMap<K,V> extends HashMap<K,ArrayList<V>> {
	
	/// Blank constructor
	public ArrayListMap() {
		super();
	}
	
	/// Gets the sublist stored for a key.
	/// If it does not exists, it is initiated
	///
	/// @params  the key used
	///
	/// @returns Sublist used for the key
	protected ArrayList<V> getSubList(K key) {
		ArrayList<V> ret = get(key);
		
		if( ret == null ) {
			ret = new ArrayList<V>();
			put(key, ret);
		}
		
		return ret;
	}
	
	/// Adds to the sublist associated to the key value
	///
	/// @params  the key used
	/// @params  the value to store
	///
	public void addToList(K key, V val) {
		getSubList(key).add(val);
	}
	
	/// Adds to the sublist associated to the key value,
	/// only if it does not exists (no duplicates)
	///
	/// @params  the key used
	/// @params  the value to store
	///
	public void addToListIfNotExists(K key, V val) {
		ArrayList<V> subList = getSubList(key);
		if( !(subList.contains(val)) ) {
			subList.add(val);
		}
	}
	
	/// Returns the map object, type casted
	@SuppressWarnings("unchecked")
	public Map<K,List<V>> genericMap() {
		return (Map<K,List<V>>)(Object)this;
	}
}
