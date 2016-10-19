package picoded.struct;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import picoded.conv.GenericConvert;

/// Common map class, used to implement all the generic convert convinence functions in a map interface
public interface GenericConvertMap<K, V> extends UnsupportedDefaultMap<K, V> {
	
	// Static proxy build
	// --------------------------------------------------------------------------------------------------
	
	// / Ensures the returned map is a GenericConvertMap, doing the conversion
	// if needed.
	static <A, B> GenericConvertMap<A, B> build(Map<A, B> inMap) {
		return ProxyGenericConvertMap.ensure(inMap);
	}
	
	// to string conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable, aka null)
	// /
	// / @returns The converted string, always possible unless null
	default String getString(K key, String fallbck) {
		return GenericConvert.toString(get(key), fallbck);
	}
	
	// / Default null fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default String getString(K key) {
		return GenericConvert.toString(get(key));
	}
	
	// to boolean conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To boolean conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default boolean getBoolean(K key, boolean fallbck) {
		return GenericConvert.toBoolean(get(key), fallbck);
	}
	
	// / Default boolean fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default boolean getBoolean(K key) {
		return GenericConvert.toBoolean(get(key));
	}
	
	// to Number conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To Number conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default Number getNumber(K key, Number fallbck) {
		return GenericConvert.toNumber(get(key), fallbck);
	}
	
	// / Default Number fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default Number getNumber(K key) {
		return GenericConvert.toNumber(get(key));
	}
	
	// to int conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To int conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default int getInt(K key, int fallbck) {
		return GenericConvert.toInt(get(key), fallbck);
	}
	
	// / Default int fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default int getInt(K key) {
		return GenericConvert.toInt(get(key));
	}
	
	// to long conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To long conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default long getLong(K key, long fallbck) {
		return GenericConvert.toLong(get(key), fallbck);
	}
	
	// / Default long fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default long getLong(K key) {
		return GenericConvert.toLong(get(key));
	}
	
	// to float conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To float conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default float getFloat(K key, float fallbck) {
		return GenericConvert.toFloat(get(key), fallbck);
	}
	
	// / Default float fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default float getFloat(K key) {
		return GenericConvert.toFloat(get(key));
	}
	
	// to double conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To double conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default double getDouble(K key, double fallbck) {
		return GenericConvert.toDouble(get(key), fallbck);
	}
	
	// / Default float fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default double getDouble(K key) {
		return GenericConvert.toDouble(get(key));
	}
	
	// to byte conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To byte conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default byte getByte(K key, byte fallbck) {
		return GenericConvert.toByte(get(key), fallbck);
	}
	
	// / Default float fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default byte getByte(K key) {
		return GenericConvert.toByte(get(key));
	}
	
	// to short conversion
	// --------------------------------------------------------------------------------------------------
	
	// / To short conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted string, always possible unless null
	default short getShort(K key, short fallbck) {
		return GenericConvert.toShort(get(key), fallbck);
	}
	
	// / Default short fallback, To String conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted string, always possible unless null
	default short getShort(K key) {
		return GenericConvert.toShort(get(key));
	}
	
	// to UUID / GUID
	// --------------------------------------------------------------------------------------------------
	
	// / To UUID conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted UUID, always possible unless null
	default UUID getUUID(K key, Object fallbck) {
		return GenericConvert.toUUID(get(key), fallbck);
	}
	
	// / Default Null fallback, To UUID conversion of generic object
	// /
	// / @param input The input value to convert
	// /
	// / @returns The converted value
	default UUID getUUID(K key) {
		return GenericConvert.toUUID(get(key));
	}
	
	// / To GUID conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted UUID, always possible unless null
	default String getGUID(K key, Object fallbck) {
		return GenericConvert.toGUID(get(key), fallbck);
	}
	
	// / Default Null fallback, To GUID conversion of generic object
	// /
	// / @param input The input value to convert
	// /
	// / @returns The converted value
	default String getGUID(K key) {
		return GenericConvert.toGUID(get(key));
	}
	
	// / To List<Object> conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted Object[], always possible unless null
	default List<Object> getObjectList(K key, Object fallbck) {
		return GenericConvert.toObjectList(get(key), fallbck);
	}
	
	// / Default Null fallback, To List<Object> conversion of generic object
	// /
	// / @param input The input value to convert
	// /
	// / @default The converted value
	default List<Object> getObjectList(K key) {
		return GenericConvert.toObjectList(get(key));
	}
	
	// to map
	// --------------------------------------------------------------------------------------------------
	// / To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted Map if possible, else null
	default <K extends String, V> Map<K, V> getStringMap(K key, Object fallbck) {
		return GenericConvert.toStringMap(get(key), fallbck);
	}
	
	// /
	// / Default Null fallback, To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted Map if possible, else null
	default <K extends String, V> Map<K, V> getStringMap(K key) {
		return GenericConvert.toStringMap(get(key));
	}
	
	// / To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted Map if possible, else null
	default <K extends String, V> GenericConvertMap<K, V> getGenericConvertStringMap(K key, Object fallbck) {
		return GenericConvert.toGenericConvertStringMap(get(key), fallbck);
	}
	
	// /
	// / Default Null fallback, To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted Map if possible, else null
	default <K extends String, V> GenericConvertMap<K, V> getGenericConvertStringMap(K key) {
		return GenericConvert.toGenericConvertStringMap(get(key));
	}
	
	// to array
	// --------------------------------------------------------------------------------------------------
	
	// / To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted Map if possible, else null
	default GenericConvertList<V> getGenericConvertList(K key, Object fallbck) {
		return GenericConvert.toGenericConvertList(get(key), fallbck);
	}
	
	// /
	// / Default Null fallback, To String Map conversion of generic object
	// /
	// / @param key The input value key to convert
	// /
	// / @returns The converted Map if possible, else null
	default GenericConvertList<V> getGenericConvertList(K key) {
		return GenericConvert.toGenericConvertList(get(key));
	}
	
	// to string array
	// --------------------------------------------------------------------------------------------------
	
	// / To String[] conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted String[], always possible unless null
	default String[] getStringArray(K key, Object fallbck) {
		return GenericConvert.toStringArray(get(key), fallbck);
	}
	
	// / Default Null fallback, To String[] conversion of generic object
	// /
	// / @param input The input value to convert
	// /
	// / @returns The converted value
	default String[] getStringArray(K key) {
		return GenericConvert.toStringArray(get(key));
	}
	
	// to object array
	// --------------------------------------------------------------------------------------------------
	
	// / To Object[] conversion of generic object
	// /
	// / @param key The input value key to convert
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The converted Object[], always possible unless null
	default Object[] getObjectArray(K key, Object fallbck) {
		return GenericConvert.toObjectArray(get(key), fallbck);
	}
	
	// / Default Null fallback, To Object[] conversion of generic object
	// /
	// / @param input The input value to convert
	// /
	// / @default The converted value
	default Object[] getObjectArray(K key) {
		return GenericConvert.toObjectArray(get(key));
	}
	
	// NESTED object fetch (related to fully qualified keys handling)
	// --------------------------------------------------------------------------------------------------
	
	// /
	// / Gets an object from the map,
	// / That could very well be, a map inside a list, inside a map, inside a
	// .....
	// /
	// / Note that at each iteration step, it attempts to do a FULL key match
	// first,
	// / before the next iteration depth
	// /
	// / @param base Map / List to manipulate from
	// / @param key The input key to fetch, possibly nested
	// / @param fallbck The fallback default (if not convertable)
	// /
	// / @returns The fetched object, always possible unless fallbck null
	default Object getNestedObject(String key, Object fallbck) {
		return GenericConvert.fetchNestedObject(this, key, fallbck);
	}
	
	// /
	// / Default Null fallback, for `getNestedObject(key,fallback)`
	// /
	// / @param base Map / List to manipulate from
	// / @param key The input key to fetch, possibly nested
	// /
	// / @returns The fetched object, always possible unless fallbck null
	default Object getNestedObject(String key) {
		//return getNestedObject(key, null);
		return GenericConvert.fetchNestedObject(this, key);
	}
	
	// Does a simple typecast and put conversion
	// --------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	default V typecastPut(K key, Object value) {
		return put(key, (V) value);
	}
	
	// Attempts to convert against known V value types, and insert into the map.
	// If no conversion is required, please use typecastPut
	// --------------------------------------------------------------------------------------------------
	default V convertPut(K key, Object value, Class<V> valueClass) {
		@SuppressWarnings("unchecked")
		BiFunction<Object, Object, V> bf = (BiFunction<Object, Object, V>) GenericConvert.getBiFunction_noisy(valueClass);
		V val = bf.apply(key, value);
		return put(key, val);
	}
	
	default V convertPut(K key, Object value) {
		throw new UnsupportedOperationException(
			"Sadly convertPut without class parameter needs to be manually extended. "
				+ "Eg: 'return convertPut(key, value, V.class)', where V is not a generic");
	}
}
