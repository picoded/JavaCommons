package picoded.JStruct;

import picoded.struct.*;

import java.util.*;
import java.util.function.*;

/// 
/// A map that pulls and writes its data from multiple sub maps.
///
/// This is used to faciltate complex  multiple layers of data, such as a caching layer
/// for systems such as JStack. This can however be used with other similar structures
/// 
/// Note that NULL values, are considered invalid values. In which the layered map will proceed
/// to search the next layer. As such it is recommended not to use the NULL values, 
/// without careful consideration of its implication
///
public class LayeredMap<K, V> implements GenericConvertMap<K, V> {
	
	// Internal vars
	//----------------------------------------------
	
	/// Internal layers of map to read / write values from
	protected List<Map<K,V>> _layers = null;
	
	// Constructor
	//----------------------------------------------
	
	public LayeredMap() {
		_layers = new ArrayList<Map<K, V>>();
	}
	
	/// Setsup the tiered map, with the various layers
	public LayeredMap( List<Map<K, V>> list ) {
		_layers = list;
	}
	
	// Layer list handling
	//----------------------------------------------
	
	/// Gets and return the layers
	public List<Map<K,V>> layers() {
		return _layers;
	}
	
	// Configuration vars to adjust logic
	//----------------------------------------------
	
	/// Writes to all layers, and not the first layer only
	protected boolean writeToAllLayers = true;
	
	/// Reverse write order, from last to first.
	protected boolean reverseWriteLayerOrder = true;
	
	// Utility operator
	//----------------------------------------------
	
	/// Iterates all the layers, for non null layer starting from 0
	///
	/// @params  func  the function used
	protected Object iterateLayers( Function<Map<K,V>, Object> func ) {
		Object ret = null;
		for(int a=0; a<_layers.size(); ++a) {
			Map<K,V> subLayer = _layers.get(a);
			if( subLayer == null ) {
				continue;
			}
			
			ret = func.apply(subLayer);
			if( ret != null ) {
				return ret;
			}
		}
		return null;
	}
	
	/// Iterates all the layers, for non null layers
	///
	/// @params  func  the function used
	/// @params  isReverse   runs from last node to first if in reverse order. Else runs in normal order
	protected Object iterateLayers( Function<Map<K,V>, Object> func, boolean isReverse ) {
		
		/// Iterate in normal order
		if( !isReverse ) {
			return iterateLayers(func);
		}
		
		Object ret = null;
		for(int a=_layers.size() - 1; a>0; --a) {
			Map<K,V> subLayer = _layers.get(a);
			if( subLayer == null ) {
				continue;
			}
			
			ret = func.apply(subLayer);
			if( ret != null ) {
				return ret;
			}
		}
		return null;
	}
	
	// Standard Map handling operations
	//----------------------------------------------
	
	/// Clears all layers
	/// 
	/// Please be careful of the implications
	public void clear() {
		iterateLayers( (map) -> {
			map.clear();
			return null;
		} );
	}
	
	/// Check if it contains key
	public boolean containsKey(Object key) {
		return ( iterateLayers( (map) -> {
			if( map.containsKey(key) ) {
				return new Boolean(true);
			}
			return null;
		} ) != null );
	}
	
	/// Get operators
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return (V)iterateLayers( (map) -> {
			if(map.containsKey(key)) {
				V val = map.get(key);
				if(val != null) {
					return val;
				}
			}
			return null;
		} );
	}
	
}
