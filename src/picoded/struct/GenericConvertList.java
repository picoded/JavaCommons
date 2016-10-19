package picoded.struct;

import java.util.List;
import java.util.UUID;

import picoded.conv.GenericConvert;

/// Common list class, used to implement all the generic convert convinence functions in a map interface
public interface GenericConvertList<E> extends UnsupportedDefaultList<E> {
	
	// Static proxy build
	//--------------------------------------------------------------------------------------------------
	
	/// Ensures the returned map is a GenericConvertMap, doing the conversion if needed.
	static <E> GenericConvertList<E> build(List<E> inList) {
		return ProxyGenericConvertList.ensure(inList);
	}
	
	// Silent varient of get without OUT OF BOUND exception
	//--------------------------------------------------------------------------------------------------
	
	default E getSilent(int index) {
		if (index < this.size() && index >= 0) {
			return get(index);
		}
		return null;
	}
	
	// to string conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To String conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable, aka null)
	///
	/// @returns         The converted string, always possible unless null
	default String getString(int index, String fallbck) {
		return GenericConvert.toString(getSilent(index), fallbck);
	}
	
	/// Default null fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default String getString(int index) {
		return GenericConvert.toString(getSilent(index));
	}
	
	// to boolean conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To boolean conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default boolean getBoolean(int index, boolean fallbck) {
		return GenericConvert.toBoolean(getSilent(index), fallbck);
	}
	
	/// Default boolean fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string to boolean, always possible 
	default boolean getBoolean(int index) {
		return GenericConvert.toBoolean(getSilent(index));
	}
	
	// to Number conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To Number conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default Number getNumber(int index, Number fallbck) {
		return GenericConvert.toNumber(getSilent(index), fallbck);
	}
	
	/// Default Number fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default Number getNumber(int index) {
		return GenericConvert.toNumber(getSilent(index));
	}
	
	// to int conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To int conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default int getInt(int index, int fallbck) {
		return GenericConvert.toInt(getSilent(index), fallbck);
	}
	
	/// Default int fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default int getInt(int index) {
		return GenericConvert.toInt(getSilent(index));
	}
	
	// to long conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To long conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default long getLong(int index, long fallbck) {
		return GenericConvert.toLong(getSilent(index), fallbck);
	}
	
	/// Default long fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default long getLong(int index) {
		return GenericConvert.toLong(getSilent(index));
	}
	
	// to float conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To float conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default float getFloat(int index, float fallbck) {
		return GenericConvert.toFloat(getSilent(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default float getFloat(int index) {
		return GenericConvert.toFloat(getSilent(index));
	}
	
	// to double conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To double conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default double getDouble(int index, double fallbck) {
		return GenericConvert.toDouble(getSilent(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default double getDouble(int index) {
		return GenericConvert.toDouble(getSilent(index));
	}
	
	// to byte conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To byte conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default byte getByte(int index, byte fallbck) {
		return GenericConvert.toByte(getSilent(index), fallbck);
	}
	
	/// Default float fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default byte getByte(int index) {
		return GenericConvert.toByte(getSilent(index));
	}
	
	// to short conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To short conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	default short getShort(int index, short fallbck) {
		return GenericConvert.toShort(getSilent(index), fallbck);
	}
	
	/// Default short fallback, To String conversion of generic object
	///
	/// @param index       The input value index to convert
	///
	/// @returns         The converted string, always possible unless null
	default short getShort(int index) {
		return GenericConvert.toShort(getSilent(index));
	}
	
	// to UUID / GUID
	//--------------------------------------------------------------------------------------------------
	
	/// To UUID conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	default UUID getUUID(int index, Object fallbck) {
		return GenericConvert.toUUID(getSilent(index), fallbck);
	}
	
	/// Default Null fallback, To UUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	default UUID getUUID(int index) {
		return GenericConvert.toUUID(getSilent(index));
	}
	
	/// To GUID conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted UUID, always possible unless null
	default String getGUID(int index, Object fallbck) {
		return GenericConvert.toGUID(getSilent(index), fallbck);
	}
	
	/// Default Null fallback, To GUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	default String getGUID(int index) {
		return GenericConvert.toGUID(getSilent(index));
	}
	
	/// To List<Object> conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Object[], always possible unless null
	default List<Object> getObjectList(int index, Object fallbck) {
		return GenericConvert.toObjectList(getSilent(index), fallbck);
	}
	
	/// Default Null fallback, To List<Object> conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @default         The converted value
	default List<Object> getObjectList(int index) {
		return GenericConvert.toObjectList(getSilent(index));
	}
	
	// to string array
	//--------------------------------------------------------------------------------------------------
	
	/// To String[] conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted String[], always possible unless null
	default String[] getStringArray(int index, Object fallbck) {
		return GenericConvert.toStringArray(getSilent(index), fallbck);
	}
	
	/// Default Null fallback, To String[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	default String[] getStringArray(int index) {
		return GenericConvert.toStringArray(getSilent(index));
	}
	
	// to object array
	//--------------------------------------------------------------------------------------------------
	
	/// To Object[] conversion of generic object
	///
	/// @param index       The input value index to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Object[], always possible unless null
	default Object[] getObjectArray(int index, Object fallbck) {
		return GenericConvert.toObjectArray(index, fallbck);
	}
	
	/// Default Null fallback, To Object[] conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @default         The converted value
	default Object[] getObjectArray(int index) {
		return GenericConvert.toObjectArray(index);
	}
	
	// NESTED object fetch (related to fully qualified indexs handling)
	//--------------------------------------------------------------------------------------------------
	
	///
	/// Gets an object from the List,
	/// That could very well be, a list inside a list, inside a map, inside a .....
	///
	/// Note that at each iteration step, it attempts to do a FULL index match first, 
	/// before the next iteration depth
	///
	/// @param index       The input index to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	default Object getNestedObject(String index, Object fallbck) {
		return GenericConvert.fetchNestedObject(this, index, fallbck);
	}
	
	///
	/// Default Null fallback, for `getNestedObject(index,fallback)`
	///
	/// @param index       The input index to fetch, possibly nested
	///
	/// @returns         The fetched object, always possible unless fallbck null
	default Object getNestedObject(String index) {
		return getNestedObject(index, null);
	}
	
	// Generic string map
	//--------------------------------------------------------------------------------------------------
	
	/// To String Map conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Map if possible, else null
	default <K extends String, V> GenericConvertMap<K, V> getGenericConvertStringMap(int index, Object fallbck) {
		return GenericConvert.toGenericConvertStringMap(getSilent(index), fallbck);
	}
	
	///
	/// Default Null fallback, To String Map conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted Map if possible, else null
	default <K extends String, V> GenericConvertMap<K, V> getGenericConvertStringMap(int index) {
		return GenericConvert.toGenericConvertStringMap(getSilent(index));
	}
	
	// to array
	//--------------------------------------------------------------------------------------------------
	
	/// To String Map conversion of generic object
	///
	/// @param key       The input value key to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted Map if possible, else null
	default <V> GenericConvertList<V> getGenericConvertList(int index, Object fallbck) {
		return GenericConvert.toGenericConvertList(getSilent(index), fallbck);
	}
	
	///
	/// Default Null fallback, To String Map conversion of generic object
	///
	/// @param key       The input value key to convert
	///
	/// @returns         The converted Map if possible, else null
	default <V> GenericConvertList<V> getGenericConvertList(int index) {
		return GenericConvert.toGenericConvertList(getSilent(index));
	}
}
