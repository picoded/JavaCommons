package picoded.struct;

import java.util.Map;
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
	/// Protected constructor
	protected ProxyGenericConvertMap(Map<K, V> inMap) {
		super(inMap);
	}
	
	/// The static builder for the map
	public static <A, B> GenericConvertMap<A, B> ensureGenericConvertMap(Map<A, B> inMap) {
		if (inMap instanceof GenericConvertMap) { // <A,B>
			return (GenericConvertMap<A, B>) inMap;
		}
		
		return (new ProxyGenericConvertMap<A, B>(inMap));
	}
}
