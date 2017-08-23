package picoded.core.struct;

import java.util.HashMap;
import java.util.Map;

import picoded.core.conv.GenericConvert;

/**
 *
 * HashMap implmentation of GenericConvertMap.
 *
 * NOTE: If you are programing interfaces, use GenericConvertMap instead, it has a lot more reuses.
 *       It is highly suggested to pass this object around as a GenericConvertMap (similar to HashMap vs Map)
 *
 * ### Example Usage
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
 *
 * map.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
 * map.put("nor", new String[] { "this", "is", "the", "end" });
 *
 * assertEquals( new String[] { "is", "not", "the", "beginning" }, map.getStringArray("this") );
 * assertEquals( "[\"this\",\"is\",\"the\",\"end\"]", map.getString("nor") );
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 **/
public class GenericConvertHashMap<K, V> extends HashMap<K, V> implements GenericConvertMap<K, V> {
	
	/**
	 * Serial version UID
	 **/
	private static final long serialVersionUID = 1L;
	
	/**
	 * Implments a JSON to string conversion
	 **/
	@Override
	public String toString() {
		return GenericConvert.toString(this);
	}
	
	// ------------------------------------------------------
	//
	// Constructors
	//
	// ------------------------------------------------------
	
	/**
	 * Constructor
	 **/
	public GenericConvertHashMap() {
		super();
	}
	
	/**
	 * Constructor
	 **/
	public GenericConvertHashMap(Map<? extends K, ? extends V> m) {
		super(m);
	}
}
