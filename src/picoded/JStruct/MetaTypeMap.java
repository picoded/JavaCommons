package picoded.JStruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/// MetaTypeMap, used to hold the various meta type configs
public class MetaTypeMap extends ConcurrentHashMap<String, MetaType> {
	
	/// String put alternative
	///
	/// @param  Key, used to represent collumn name
	/// @param  Value, sting representation of metatype
	///
	/// @returns  previous MetaType if valid.
	///
	public MetaType put(String key, String value) {
		MetaType type = MetaType.fromName(value);
		if( type != null ) {
			return super.put(key, type);
		}
		throw new RuntimeException("Unknown meta type "+key+" = "+value);
	}
	
	/// Generic put function
	public MetaType put(String name, Object value) {
		if( value instanceof MetaType ) {
			return super.put( name, (MetaType)value );
		}
		return put( name, (value != null)? value.toString() : null );
	}
	
	/// Generic varient of put all
	public <K,V> void putAllGeneric(Map<K, V> m) {
		for(Map.Entry<K,V> entry : m.entrySet()) {
			put( entry.getKey().toString(), (Object)(entry.getValue()) );
		}
	}
}
