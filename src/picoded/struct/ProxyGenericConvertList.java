package picoded.struct;

import java.util.List;
import picoded.conv.*;
import org.apache.commons.collections4.list.AbstractListDecorator;

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
public class ProxyGenericConvertList<V> extends AbstractListDecorator<V> implements GenericConvertList<V> {
	/// Protected constructor
	public ProxyGenericConvertList(List<V> inList) {
		super(inList);
	}
	
	/// Protected constructor
	public ProxyGenericConvertList() {
		super();
	}
	
	/// The static builder for the map
	public static <V> GenericConvertList<V> ensure(List<V> inList) {
		if (inList instanceof GenericConvertList) { // <V>
			return (GenericConvertList<V>) inList;
		}
		return (new ProxyGenericConvertList<V>(inList));
	}
	
	/// Implments a JSON to string conversion
	@Override
	public String toString() {
		return GenericConvert.toString((Object)this);
	}
}
