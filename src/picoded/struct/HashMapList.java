package picoded.struct;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import picoded.conv.MapValueConv;

/// Convinent class, for creating a Map containing list values, and appending them
public class HashMapList<K,V> extends HashMap<K, List<V>> implements GenericConvertMap<K, List<V>> {
	
	/// Appends the value to the inner list, creating a new ArrayList if needed
	///
	/// @Parameter   key     key to use
	/// @Parameter   value   values to append
	///
	/// @Returns   returns itself
	public HashMapList<K,V> append(K key, V value) {
		
		// Get or create list
		List<V> valArr = this.get(key);
		if(valArr == null) {
			valArr = new ArrayList<V>();
		}
		
		// Add and update list
		valArr.add( value );
		this.put( key, valArr );
		
		// Returns self
		return this;
	}
	
	/// Appends the value to the inner list, creating a new ArrayList if needed
	///
	/// @Parameter   key     key to use
	/// @Parameter   value   values emuneration to append
	///
	/// @Returns   returns itself
	public HashMapList<K,V> append(K key, Enumeration<V> values) {
		if( values == null ) {
			return this;
		}
		
		while(values.hasMoreElements()) {
			append(key, values.nextElement());
		}
		
		// Returns self
		return this;
	}
	
	/// Returns a new map, with all the internal List<V> objects cnverted to V[] Array
	///
	/// @Returns   the flatten map array
	public Map<K,V[]> toMapArray() {
		return MapValueConv.listToArray(this);
	}
}
