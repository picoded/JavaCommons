package picoded.struct;

import java.util.Map;

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
public class ProxyGenericConvertMap<K,V> extends
org.apache.commons.collections4.map.AbstractMapDecorator<K,V>
implements
GenericConvertMap<K,V>
{
	
}