package picoded.struct;

import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import picoded.conv.GenericConvert;

///
/// Mirrors default implementation varients of GenericConvert class, for in map convienence.
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
public interface GenericConvertMap<K,V> /* implements Map<K,V> */ {
	
	// map interface implementation required
	//-----------------------------------------
	V get(Object key);
	V put(K key, V value);
	
	// to string conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To String conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable, aka null)
	///
	/// @returns         The converted string, always possible unless null
	public default String getString(K key, String fallbck) {
		return GenericConvert.toString( get(key), fallbck );
	}
	
	/// Default null fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default String getString(K key) {
		return GenericConvert.toString( get(key) );
	}
	
	// to boolean conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To boolean conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default boolean getBoolean(K key, boolean fallbck) {
		return GenericConvert.toBoolean( get(key), fallbck );
	}
	
	/// Default boolean fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default boolean getBoolean(K key) {
		return GenericConvert.toBoolean( get(key) );
	}
	
	// to Number conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To Number conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default Number getNumber(K key, Number fallbck) {
		return GenericConvert.toNumber( get(key), fallbck );
	}
	
	/// Default Number fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default Number getNumber(K key) {
		return GenericConvert.toNumber( get(key) );
	}
	
	// to int conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To int conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default int getInt(K key, int fallbck) {
		return GenericConvert.toInt( get(key), fallbck );
	}
	
	/// Default int fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default int getInt(K key) {
		return GenericConvert.toInt( get(key) );
	}
	
	// to long conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To long conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default long getLong(K key, long fallbck) {
		return GenericConvert.toLong( get(key), fallbck );
	}
	
	/// Default long fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default long getLong(K key) {
		return GenericConvert.toLong( get(key) );
	}
	
	// to float conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To float conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default float getFloat(K key, float fallbck) {
		return GenericConvert.toFloat( get(key), fallbck );
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default float getFloat(K key) {
		return GenericConvert.toFloat( get(key) );
	}
	
	// to double conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To double conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default double getDouble(K key, double fallbck) {
		return GenericConvert.toDouble( get(key), fallbck );
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default double getDouble(K key) {
		return GenericConvert.toDouble( get(key) );
	}
	
	// to byte conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To byte conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default byte getByte(K key, byte fallbck) {
		return GenericConvert.toByte( get(key), fallbck );
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default byte getByte(K key) {
		return GenericConvert.toByte( get(key) );
	}
	
	// to short conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To short conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public default short getShort(K key, short fallbck) {
		return GenericConvert.toShort( get(key), fallbck );
	}
	
	/// Default short fallback, To String conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted string, always possible unless null
	public default short getShort(K key) {
		return GenericConvert.toShort( get(key) );
	}
	
	// to UUID / GUID
	//--------------------------------------------------------------------------------------------------
	
	/// To UUID conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	public default UUID getUUID(K key, Object fallbck) {
		return GenericConvert.toUUID( get(key), fallbck );
	}
	
	/// Default Null fallback, To UUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default UUID getUUID(K key) {
		return GenericConvert.toUUID( get(key) );
	}
	
	/// To GUID conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	public default String getGUID(K key, Object fallbck) {
		return GenericConvert.toGUID( get(key), fallbck );
	}
	
	/// Default Null fallback, To GUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default String getGUID(K key) {
		return GenericConvert.toGUID( get(key) );
	}
	
	// to list
	// @TODO generic list conversion
	//--------------------------------------------------------------------------------------------------
	
	// to map
	// @TODO generic map conversion
	//--------------------------------------------------------------------------------------------------
	
	// to array
	// @TODO generic array conversion
	//--------------------------------------------------------------------------------------------------
	
	// to string array
	//--------------------------------------------------------------------------------------------------
	
	/// To String[] conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted String[], always possible unless null
	public default String[] getStringArray(K key, Object fallbck) {
		return GenericConvert.toStringArray( get(key), fallbck );
	}
	
	/// Default Null fallback, To String[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public default String[] getStringArray(K key) {
		return GenericConvert.toStringArray( get(key) );
	}
	
	// to object array
	//--------------------------------------------------------------------------------------------------
	
	/// To Object[] conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Object[], always possible unless null
	public default Object[] getObjectArray(K key, Object fallbck) {
		return GenericConvert.toObjectArray(key, fallbck);
	}
	
	/// Default Null fallback, To Object[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @default         The converted value
	public default Object[] getObjectArray(K key) {
		return GenericConvert.toObjectArray(key);
	}
	
}