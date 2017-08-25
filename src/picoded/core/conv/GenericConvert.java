package picoded.core.conv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import picoded.core.struct.GenericConvertArrayList;
import picoded.core.struct.GenericConvertList;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.ProxyGenericConvertMap;

/**
 * Core class for all the common variable / primitive type conversions.
 *
 * If there was only one feature I could choose to use in the whole JavaCommons package,
 * it will be the GenericConvert class series.
 *
 * As its pattern is inherited into all the
 * GenericConvert struct classes, it allows for easy, quality of (programmers) life access to variable converters.
 *
 * This has since been split into 2 additional sub-classes, however as this main class inherits them
 * all anyway. It is highly recommended to simply use this class instead.
 *
 * This split is mainly done to simplify code maintenance of this Giant package.
 **/
public class GenericConvert extends GenericConvertStandard {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected GenericConvert() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * To GenericConvertMap
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To GenericConvertMap conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion (if its a GenericConvertMap)
	 * - To GenericConvertMap (if its a Map)
	 * - toStringObjectMap -> GenericConvertMap conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	@SuppressWarnings("unchecked")
	public static <K extends String, V> GenericConvertMap<K, V> toGenericConvertStringMap(
		Object input, Object fallbck) {
		
		/**
		 * Null handling
		 **/
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toGenericConvertStringMap(fallbck, null);
		}
		/**
		 * If GenericConvertMap instance
		 **/
		if (input instanceof GenericConvertMap) {
			return (GenericConvertMap<K, V>) input;
		}
		/**
		 * If Map instance
		 **/
		if (input instanceof Map) {
			return ProxyGenericConvertMap.ensure((Map<K, V>) input);
		}
		/**
		 * If String instance, attampt JSON conversion
		 **/
		if (input instanceof String) {
			try {
				Map<String, Object> strMap = ConvertJSON.toMap((String) input);
				if (strMap != null) {
					return ProxyGenericConvertMap.ensure((Map<K, V>) strMap);
				}
			} catch (Exception e) {
				/**
				 * Silence the exception
				 **/
			}
		}
		/**
		 * Fallback
		 **/
		return toGenericConvertStringMap(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To GenericConvert String map conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static <K extends String, V> GenericConvertMap<K, V> toGenericConvertStringMap(
		Object input) {
		return toGenericConvertStringMap(input, null);
	}
	
	/**
	 * To GenericConvertList
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To GenericConvertList conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion (if its a GenericConvertList)
	 * - To GenericConvertList (if its a List)
	 * - toList -> GenericConvertList conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	@SuppressWarnings("unchecked")
	public static <V> GenericConvertList<V> toGenericConvertList(Object input, Object fallbck) {
		/**
		 * Null handling
		 **/
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toGenericConvertList(fallbck, null);
		}
		/**
		 * If GenericConvertMap instance
		 **/
		if (input instanceof GenericConvertList) {
			return (GenericConvertList<V>) input;
		}
		/**
		 * If List instance
		 **/
		if (input instanceof List) {
			return new GenericConvertArrayList<V>((List<V>) input);
			//return ProxyGenericConvertMap.ensure((Map<K, V>) input);
		}
		/**
		 * If String instance, attampt JSON conversion
		 **/
		if (input instanceof String) {
			try {
				List<Object> jsonList = ConvertJSON.toList((String) input);
				if (jsonList != null) {
					return new GenericConvertArrayList<V>((List<V>) jsonList);
				}
			} catch (Exception e) {
				/**
				 * Silence the exception
				 **/
			}
		}
		/**
		 * Fallback
		 **/
		return toGenericConvertList(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To GenericConvert String map conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static <V> GenericConvertList<V> toGenericConvertList(Object input) {
		return toGenericConvertList(input, null);
	}
	
	//--------------------------------------------------------------------------------------------------
	//
	// NESTED object fetch (related to fully qualified keys handling)
	//
	//--------------------------------------------------------------------------------------------------
	
	/**
	 * Fetch an Object from either a Map, or a List. By attempting to use the provided key.
	 *
	 * This attempts to use the key AS IT IS. Only converting it to an int for List if needed.
	 * It does not do recursive fetch, if that is needed see `fetchNestedObject`
	 *
	 * @param base      Map / List to manipulate from
	 * @param key       The input key to fetch, possibly nested
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The fetched object, always possible unless fallbck null
	 **/
	public static Object fetchObject(Object base, String key, Object fallback) {
		return NestedObject.fetchObject(base, key, fallback);
	}
	
	/**
	 * Default Null fallback, for `fetchObject(base, key, fallback)`
	 *
	 * @param base      Map / List to manipulate from
	 * @param key       The input key to fetch, possibly nested
	 *
	 * @return         The fetched object, always possible unless fallbck null
	 **/
	public static Object fetchObject(Object base, String key) {
		return NestedObject.fetchObject(base, key);
	}
	
	/**
	 * Split the key path into their respective component
	 *
	 * @param key       The input key to fetch, possibly nested
	 *
	 * @return         The fetched object, possibly empty array if key is invalid?
	 **/
	public static String[] splitObjectPath(String key) {
		return NestedObject.splitObjectPath(key);
	}
	
	/**
	 * Split the key path into their respective component
	 *
	 * @param key       The input key to fetch, possibly nested
	 *
	 * @return         The fetched object, possibly empty array if key is invalid?
	 **/
	public static List<String> splitObjectPath(String key, List<String> ret) {
		return NestedObject.splitObjectPath(key, ret);
	}
	
	/**
	 * Gets an object from the map,
	 * That could very well be, a map inside a list, inside a map, inside a .....
	 *
	 * Note that at each iteration step, it attempts to do a FULL key match first,
	 * before the next iteration depth
	 *
	 * @param base      Map / List to manipulate from
	 * @param key       The input key to fetch, possibly nested
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The fetched object, always possible unless fallbck null
	 **/
	public static Object fetchNestedObject(Object base, String key, Object fallback) {
		return NestedObject.fetchNestedObject(base, key, fallback);
	}
	
	/**
	 * Default Null fallback, for `fetchNestedObject(base, key, fallback)`
	 *
	 * @param base      Map / List to manipulate from
	 * @param key       The input key to fetch, possibly nested
	 *
	 * @return         The fetched object, always possible unless fallbck null
	 **/
	public static Object fetchNestedObject(Object base, String key) {
		return NestedObject.fetchNestedObject(base, key);
	}
	
	/**
	 * Takes a possibly case insensitive key, and normalize it to the actual key path (if found) for the selected object
	 *
	 * @param base      Map / List to manipulate from
	 * @param objPath   The input key to fetch, possibly nested
	 *
	 * @return         The normalized key
	 **/
	public static String normalizeObjectPath(Object base, String key) {
		return NestedObject.normalizeObjectPath(base, key);
	}
	
	/**
	 * Takes a possibly case insensitive key, and normalize it to the actual key path (if found) for the selected object
	 *
	 * @param base           Map / List to manipulate from
	 * @param splitKeyPath   Key path in a list format
	 * @param res            StringBuilder results
	 *
	 * @return         The normalized key
	 **/
	public static StringBuilder normalizeObjectPath(Object base, List<String> splitKeyPath,
		StringBuilder res) {
		return NestedObject.normalizeObjectPath(base, splitKeyPath, res);
	}
	
	// //--------------------------------------------------------------------------------------------------
	// //
	// // to BiFunction Map, used to automated put conversion handling
	// // NOTE: Removed due to lack of use
	// //
	// //--------------------------------------------------------------------------------------------------
	//
	// protected static BiFunction<Object, Object, String> toString_BiFunction = (i, f) -> GenericConvert.toString(i, f);
	// protected static BiFunction<Object, Object, String[]> toStringArray_BiFunction = (i, f) -> GenericConvert
	// 	.toStringArray(i, f);
	//
	// protected static Map<Class<?>, BiFunction<Object, Object, ?>> biFunctionMap = null;
	//
	// public static Map<Class<?>, BiFunction<Object, Object, ?>> biFunctionMap() {
	// 	if (biFunctionMap != null) {
	// 		return biFunctionMap;
	// 	}
	//
	// 	Map<Class<?>, BiFunction<Object, Object, ?>> ret = new HashMap<Class<?>, BiFunction<Object, Object, ?>>();
	//
	// 	ret.put(String.class, toString_BiFunction);
	// 	ret.put(String[].class, toStringArray_BiFunction);
	//
	// 	biFunctionMap = ret;
	// 	return biFunctionMap;
	// }
	//
	// /// Gets and return the relevent BiFunction for the given class
	// public static BiFunction<Object, Object, ?> getBiFunction(Class<?> resultClassObj) {
	// 	return biFunctionMap().get(resultClassObj);
	// }
	//
	// /// Gets and return the relevent BiFunction for the given class, throws an error if not found
	// public static BiFunction<Object, Object, ?> getBiFunction_noisy(Class<?> resultClassObj) {
	// 	BiFunction<Object, Object, ?> ret = getBiFunction(resultClassObj);
	// 	if (ret == null) {
	// 		throw new RuntimeException("Unable to find specified class object: " + resultClassObj);
	// 	}
	// 	return ret;
	// }
	
}
