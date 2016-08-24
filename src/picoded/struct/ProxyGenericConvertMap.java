package picoded.struct;

import java.util.Map;
import picoded.conv.*;
import org.apache.commons.collections4.map.AbstractMapDecorator;

///
/// This class provides a static constructor, that builds
/// the wrapper to ensure a GenericConvertMap is returned when needed
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class ProxyGenericConvertMap<K, V> extends AbstractMapDecorator<K, V> implements GenericConvertMap<K, V> {
	
	//------------------------------------------------------
	//
	// Constructors
	//
	//------------------------------------------------------

	/// Consturctor
	public ProxyGenericConvertMap() {
		super();
	}

	/// Consturctor
	@SuppressWarnings("unchecked")
	public ProxyGenericConvertMap(Map<? extends K,? extends V> m) {
		super((Map<K,V>)m);
	}
	
	//------------------------------------------------------
	//
	// ensure its type
	//
	//------------------------------------------------------

	/// The static builder for the map
	@Deprecated
	public static <A, B> GenericConvertMap<A, B> ensureGenericConvertMap(Map<A, B> inMap) {
		if (inMap instanceof GenericConvertMap) { // <A,B>
			return (GenericConvertMap<A, B>) inMap;
		}
		
		return (new ProxyGenericConvertMap<A, B>(inMap));
	}
	
	/// The static builder for the map
	public static <A, B> GenericConvertMap<A, B> ensure(Map<A, B> inMap) {
		if (inMap instanceof GenericConvertMap) { // <A,B>
			return (GenericConvertMap<A, B>) inMap;
		}
		
		return (new ProxyGenericConvertMap<A, B>(inMap));
	}
	
	//------------------------------------------------------
	//
	// Overwrites
	//
	//------------------------------------------------------

	/// Implments a JSON to string conversion
	@Override
	public String toString() {
		return GenericConvert.toString((Object)this);
	}
}
