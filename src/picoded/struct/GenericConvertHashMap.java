package picoded.struct;

import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.function.BiFunction;

import java.lang.reflect.TypeVariable;

import picoded.conv.GenericConvert;

///
/// HashMap implmentation of GenericConvertMap. 
///
/// NOTE: If your programing interfaces, use GenericConvertMap instead, it has WAY WAY more reuse.
///       In fact it is highly suggested to pass this object around as a GenericConvertMap (similar to HashMap vs Map)
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// map.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
/// map.put("nor", new String[] { "this", "is", "the", "end" });
///
/// assertEquals( new String[] { "is", "not", "the", "beginning" }, map.getStringArray("this") );
/// assertEquals( "[\"this\",\"is\",\"the\",\"end\"]", map.getString("nor") );
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class GenericConvertHashMap<K, V> extends HashMap<K, V> implements GenericConvertMap<K, V> {
}
