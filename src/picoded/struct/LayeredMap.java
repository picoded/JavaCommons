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
	
	/// Internal var used to represent null value
	protected static Object nullObject = new Object();
	
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
	
	// Utility lamda iterators. 
	// Used to build more complex layer functions
	//----------------------------------------------
	
	/// Iterates all the layers, for non null layer starting from 0
	///
	/// @params  func  the function used
	///
	/// @returns the first non-null value the function returned. else return null
	public Object iterateLayersUntilReturn( Function<Map<K,V>, Object> func ) {
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
	///
	/// @returns the first non-null value the function returned. else return null
	public Object iterateLayersUntilReturn( Function<Map<K,V>, Object> func, boolean isReverse ) {
		/// Iterate in normal order
		if( !isReverse ) {
			return iterateLayersUntilReturn(func);
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
	
	/// Iterates all the layers, for non null layer starting from 0
	///
	/// @params  func  the function used
	/// @params  ret   the return parameter to pass forward
	///
	/// @returns the first non-null value the function returned. else return null
	public Object iterateLayersWithReturn( BiFunction<Map<K,V>, Object, Object> func, Object ret ) {
		for(int a=0; a<_layers.size(); ++a) {
			Map<K,V> subLayer = _layers.get(a);
			if( subLayer == null ) {
				continue;
			}
			
			ret = func.apply(subLayer, ret);
		}
		return ret;
	}
	
	/// Iterates all the layers, for non null layers
	///
	/// @params  func  the function used
	/// @params  ret   the return parameter to pass forward
	///
	/// @params  isReverse   runs from last node to first if in reverse order. Else runs in normal order
	///
	/// @returns the first non-null value the function returned. else return null
	public Object iterateLayersWithReturn( BiFunction<Map<K,V>, Object, Object> func, Object ret, boolean isReverse ) {
		/// Iterate in normal order
		if( !isReverse ) {
			return iterateLayersWithReturn(func, ret);
		}
		
		for(int a=_layers.size() - 1; a>0; --a) {
			Map<K,V> subLayer = _layers.get(a);
			if( subLayer == null ) {
				continue;
			}
			
			ret = func.apply(subLayer, ret);
		}
		return ret;
	}
	
	// Critical functions for map support
	//----------------------------------------------
	
	/// Get operators
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return (V)iterateLayersUntilReturn( (map) -> {
			if(map.containsKey(key)) {
				V val = map.get(key);
				if(val != null) {
					return val;
				}
			}
			return null;
		} );
	}
	
	/// Put the operation
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		V ret = null;
		
		if( writeToAllLayers ) {
			ret = (V)iterateLayersWithReturn( (map,r) -> {
				if( r  == null ) {
					r = map.put(key, value);
				} else {
					map.put(key, value);
				}
				return r;
			}, ret, reverseWriteLayerOrder );
		} else { //writes only to the first/last layer???
			ret = (V)iterateLayersUntilReturn( (map) -> {
				V val = map.put(key, value);
				
				if(val == null) {
					return nullObject;
				}
				
				return val;
			}, reverseWriteLayerOrder );
		}
		
		if(ret == nullObject) {
			return null;
		}
		return ret;
	}
	
	/// Removes all values from a key
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		V ret = null;
		
		ret = (V)iterateLayersWithReturn( (map,r) -> {
			if( r  == null ) {
				r = map.remove(key);
			} else {
				map.remove(key);
			}
			return r;
		}, ret, reverseWriteLayerOrder );
		
		return ret;
	}
	
	/// Iterate all layers and form a combined keySet
	@SuppressWarnings("unchecked")
	public Set<K> keySet() {
		Set<K> ret = new HashSet<K>();
		
		ret = (Set<K>)iterateLayersWithReturn( (map,r) -> {
			((Set<K>)r).addAll( map.keySet() );
			return r;
		}, ret );
		
		return ret;
	}
	
	// Optimization function for map
	//
	// @TODO optimzie sub functions
	// + containsValue
	// + entrySet
	// + isEmpty
	// + size
	// + values
	//----------------------------------------------
	
	/// Clears all layers
	/// 
	/// Please be careful of the implications
	public void clear() {
		iterateLayersUntilReturn( (map) -> {
			map.clear();
			return null;
		} );
	}
	
	/// Check if it contains key
	public boolean containsKey(Object key) {
		return ( iterateLayersUntilReturn( (map) -> {
			if( map.containsKey(key) ) {
				return new Boolean(true);
			}
			return null;
		} ) != null );
	}
	
	
}
