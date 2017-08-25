package picoded.core.struct;

import java.util.List;

import org.apache.commons.collections4.list.AbstractListDecorator;

import picoded.core.conv.GenericConvert;

/**
 * This class provides a static constructor, that builds
 * the wrapper to ensure a GenericConvertMap is returned when needed
 *
 * ### Example Usage
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 **/
public class ProxyGenericConvertList<V> extends AbstractListDecorator<V> implements
	GenericConvertList<V> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Protected constructor
	 **/
	public ProxyGenericConvertList(List<V> inList) {
		super(inList);
	}
	
	/**
	 * The static builder for the map
	 **/
	public static <V> GenericConvertList<V> ensure(List<V> inList) {
		if (inList instanceof GenericConvertList) { // <V>
			return (GenericConvertList<V>) inList;
		}
		return new ProxyGenericConvertList<V>(inList);
	}
	
	/**
	 * Implments a JSON to string conversion
	 **/
	@Override
	public String toString() {
		return GenericConvert.toString(this);
	}
}
