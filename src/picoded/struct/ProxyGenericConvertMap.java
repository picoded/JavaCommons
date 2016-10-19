package picoded.struct;

import java.util.Map;

import org.apache.commons.collections4.map.AbstractMapDecorator;

import picoded.conv.GenericConvert;

public class ProxyGenericConvertMap<K, V> extends AbstractMapDecorator<K, V> implements GenericConvertMap<K, V> {
	
	// ------------------------------------------------------
	//
	// Constructors
	//
	// ------------------------------------------------------
	
	/// Consturctor
	public ProxyGenericConvertMap() {
		super();
	}
	
	/// Consturctor
	@SuppressWarnings("unchecked")
	public ProxyGenericConvertMap(Map<? extends K, ? extends V> m) {
		super((Map<K, V>) m);
	}
	
	/// The static builder for the map
	public static <A, B> GenericConvertMap<A, B> ensure(Map<A, B> inMap) {
		if (inMap instanceof GenericConvertMap) { // <A,B>
			return (GenericConvertMap<A, B>) inMap;
		}
		return new ProxyGenericConvertMap<A, B>(inMap);
	}
	
	// ------------------------------------------------------
	//
	// Overwrites
	//
	// ------------------------------------------------------
	
	/// Implments a JSON to string conversion
	@Override
	public String toString() {
		return GenericConvert.toString(this);
	}
}
