package picoded.conv;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

import picoded.struct.*;

/// Provides several autoamted generic conversion, from a given object format to another.
///
/// This is generally meant for most generic types, and is primarily used to handle
/// dynamic type conversion of input servlet parameters.
/// ----------------------------------------------------------------------------------------
///
/// @TODO: Unit test
///
public class GenericConvert {
	
	// to string conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To String conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Object to JSON string
	/// - Object.toString()
	/// - Fallback (only possible for non-null values)
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable, aka null)
	///
	/// @returns         The converted string, always possible unless null
	public static String toString(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toString(fallbck, null);
		}
		
		if (input instanceof String) {
			return input.toString();
		}
		
		try {
			return ConvertJSON.fromObject(input);
		} catch (Exception e) {
			// ignores
		}
		
		return input.toString();
	}
	
	/// Default null fallback, To String conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted string, always possible unless null
	public static String toString(Object input) {
		return toString(input, null);
	}
	
	// to boolean conversion
	//--------------------------------------------------------------------------------------------------
	
	/// To boolean conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric conversion
	/// - String conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public static boolean toBoolean(Object input, boolean fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		if (input instanceof Boolean) {
			return ((Boolean) input).booleanValue();
		}
		
		if (input instanceof Number) {
			return (((Number) input).floatValue() > 0.0f);
		}
		
		if (input instanceof String && ((String) input).length() > 0) {
			char tChar = ((String) input).charAt(0);
			
			//String conversion
			if (tChar == '+' || tChar == 't' || tChar == 'T' || tChar == 'y' || tChar == 'Y') {
				return true;
			} else if (tChar == '-' || tChar == 'f' || tChar == 'F' || tChar == 'n' || tChar == 'N') {
				return false;
			}
			
			//Numeric string conversion
			String s = ((String) input);
			
			if (s.length() > 2) {
				s = s.substring(0, 2);
			}
			try {
				Integer i = Integer.valueOf(s);
				return (i.intValue() > 0);
			} catch (Exception e) {
				//does nothing
			}
		}
		
		return fallbck;
	}
	
	/// Default false fallback, To boolean conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted boolean
	public static boolean toBoolean(Object input) {
		return toBoolean(input, false);
	}
	
	// to Number
	//--------------------------------------------------------------------------------------------------
	
	/// To Number conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted string, always possible unless null
	public static Number toNumber(Object input, Number fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		if (input instanceof Number) {
			return ((Number) input);
		}
		
		if (input instanceof String && ((String) input).length() > 0) {
			//Numeric string conversion
			
			try {
				BigDecimal bd = new BigDecimal(((String) input));
				return bd;
			} catch (Exception e) {
				
			}
		}
		
		return fallbck;
	}
	
	/// Default false fallback, To Number conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted boolean
	public static Number toNumber(Object input) {
		return toNumber(input, null);
	}
	
	// to int
	//--------------------------------------------------------------------------------------------------
	
	/// To int conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static int toInt(Object input, int fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).intValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static int toInt(Object input) {
		return toInt(input, 0);
	}
	
	// to long
	//--------------------------------------------------------------------------------------------------
	
	/// To long conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static long toLong(Object input, long fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).longValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static long toLong(Object input) {
		return toLong(input, 0);
	}
	
	// to float
	//--------------------------------------------------------------------------------------------------
	
	/// To float conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static float toFloat(Object input, float fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).floatValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static float toFloat(Object input) {
		return toFloat(input, 0);
	}
	
	// to double
	//--------------------------------------------------------------------------------------------------
	
	/// To double conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static double toDouble(Object input, double fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).doubleValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static double toDouble(Object input) {
		return toDouble(input, 0);
	}
	
	// to byte
	//--------------------------------------------------------------------------------------------------
	
	/// To byte conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static byte toByte(Object input, byte fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).byteValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static byte toByte(Object input) {
		return toByte(input, (byte) 0);
	}
	
	// to short
	//--------------------------------------------------------------------------------------------------
	
	/// To short conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static short toShort(Object input, short fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (toNumber(input, fallbck)).shortValue();
	}
	
	/// Default 0 fallback, To int conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static short toShort(Object input) {
		return toShort(input, (short) 0);
	}
	
	// to UUID aka GUID
	//--------------------------------------------------------------------------------------------------
	
	/// To UUID conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static UUID toUUID(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toUUID(fallbck, null);
		}
		if (input instanceof UUID) {
			return (UUID) input;
		}
		
		if (input instanceof String) {
			if (((String) input).length() == 22) {
				try {
					return GUID.fromBase58((String) input);
				} catch (Exception e) {
					
				}
			}
		}
		
		return toUUID(fallbck, null);
	}
	
	/// Default Null fallback, To UUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static UUID toUUID(Object input) {
		return toUUID(input, null);
	}
	
	/// To GUID conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Numeric string conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static String toGUID(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toGUID(fallbck, null);
		}
		
		if (input instanceof UUID) {
			try {
				return GUID.base58((UUID) input);
			} catch (Exception e) {
				
			}
		}
		
		if (input instanceof String) {
			if (((String) input).length() >= 22) {
				try {
					if (GUID.fromBase58((String) input) != null) {
						return (String) input;
					}
				} catch (Exception e) {
					
				}
			}
		}
		
		return toGUID(fallbck, null);
	}
	
	/// Default Null fallback, To GUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static String toGUID(Object input) {
		return toGUID(input, null);
	}
	
	// to list
	// @TODO generic list conversion
	//--------------------------------------------------------------------------------------------------
	
	// to string object map
	//--------------------------------------------------------------------------------------------------
	
	/// To String map conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion (if its a map)
	/// - JSON String to Map
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	@SuppressWarnings("unchecked")
	public static <K extends String, V> Map<K, V> toStringMap(Object input, Object fallbck) {
		
		// Null handling
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toStringMap(fallbck, null);
		}
		
		// If Map instance
		if (input instanceof Map) {
			return (Map<K, V>) input;
		}
		
		// If String instance, attampt JSON conversion
		if (input instanceof String) {
			try {
				return (Map<K, V>) ConvertJSON.toMap((String) input);
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		return toStringMap(fallbck, null);
	}
	
	/// Default Null fallback, To String Object map conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static <K extends String, V> Map<K, V> toStringMap(Object input) {
		return toStringMap(input, null);
	}
	
	///
	/// @Deprecated : Use {@link #toStringMap()} instead
	///
	/// To String Object map conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion (if its a map)
	/// - JSON String to Map
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	@Deprecated
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toStringObjectMap(Object input, Object fallbck) {
		return toStringMap(input, fallbck);
	}
	
	///
	/// @Deprecated : Use {@link #toStringMap()} instead
	///
	/// Default Null fallback, To String Object map conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	@Deprecated
	public static Map<String, Object> toStringObjectMap(Object input) {
		return toStringObjectMap(input, null);
	}
	
	// Generic string map
	//--------------------------------------------------------------------------------------------------
	
	/// To String GenericConvertMap conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion (if its a GenericConvertMap)
	/// - To GenericConvertMap (if its a Map)
	/// - toStringObjectMap -> GenericConvertMap conversion
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	@SuppressWarnings("unchecked")
	public static <K extends String, V> GenericConvertMap<K, V> toGenericConvertStringMap(Object input, Object fallbck) {
		
		// Null handling
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toGenericConvertStringMap(fallbck, null);
		}
		
		// If GenericConvertMap instance
		if (input instanceof GenericConvertMap) {
			return (GenericConvertMap<K, V>) input;
		}
		
		// If Map instance
		if (input instanceof Map) {
			return ProxyGenericConvertMap.ensureGenericConvertMap((Map<K, V>) input);
		}
		
		// If String instance, attampt JSON conversion
		if (input instanceof String) {
			try {
				Map<String, Object> strMap = ConvertJSON.toMap((String) input);
				if (strMap != null) {
					return ProxyGenericConvertMap.ensureGenericConvertMap((Map<K, V>) strMap);
				}
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		// Fallback
		return toGenericConvertStringMap(fallbck, null);
	}
	
	///
	/// Default Null fallback, To GenericConvert String map conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static <K extends String, V> GenericConvertMap<K, V> toGenericConvertStringMap(Object input) {
		return toGenericConvertStringMap(input, null);
	}
	
	// to array
	// @TODO generic array conversion
	//--------------------------------------------------------------------------------------------------
	
	// to string array
	//--------------------------------------------------------------------------------------------------
	
	/// To String array conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - String to List
	/// - List to array
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static String[] toStringArray(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toStringArray(fallbck, null);
		}
		
		if (input instanceof String[]) {
			return (String[]) input;
		} else if (input instanceof Object[]) {
			Object[] inArr = (Object[]) input;
			String[] ret = new String[inArr.length];
			for (int a = 0; a < inArr.length; ++a) {
				ret[a] = toString(inArr[a]);
			}
			return ret;
		}
		
		// From list conversion (if needed)
		List<?> list = null;
		
		// Conversion to List (if possible)
		if (input instanceof String) {
			try {
				Object o = ConvertJSON.toList((String) input);
				if (o instanceof List) {
					list = (List<?>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		} else if (input instanceof List) {
			list = (List<?>) input;
		} else { //Force the "toString", then to List conversion
			try {
				String inputStr = input.toString();
				Object o = ConvertJSON.toList(inputStr);
				if (o instanceof List) {
					list = (List<?>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		// List to string array conversion
		if (list != null) {
			// Try direct conversion?
			try {
				return list.toArray(new String[list.size()]);
			} catch (Exception e) {
				
			}
			
			// Try value by value conversion
			String[] ret = new String[list.size()];
			for (int a = 0; a < ret.length; ++a) {
				ret[a] = toString(list.get(a));
			}
			return ret;
		}
		
		return toStringArray(fallbck, null);
	}
	
	/// Default Null fallback, To String array conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static String[] toStringArray(Object input) {
		return toStringArray(input, null);
	}
	
	// to object list
	//--------------------------------------------------------------------------------------------------
	
	/// To object list conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - Array to List
	/// - String to List
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	@SuppressWarnings("unchecked")
	public static List<Object> toObjectList(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toObjectList(fallbck, null);
		}
		
		if (input instanceof List) {
			return (List<Object>) input;
		}
		
		if (input instanceof Object[]) {
			return (Arrays.asList(((Object[]) input)));
		}
		
		List<Object> ret = null;
		
		// Conversion to List (if possible)
		if (input instanceof String) {
			try {
				Object o = ConvertJSON.toList((String) input);
				if (o instanceof List) {
					ret = (List<Object>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		} else { //Force the "toString", then to List conversion
			try {
				String inputStr = input.toString();
				Object o = ConvertJSON.toList(inputStr);
				if (o instanceof List) {
					ret = (List<Object>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		// List to string array conversion
		if (ret != null) {
			return ret;
		}
		
		return toObjectList(fallbck, null);
	}
	
	/// Default Null fallback, To object list conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static List<Object> toObjectList(Object input) {
		return toObjectList(input, null);
	}
	
	// to object array
	//--------------------------------------------------------------------------------------------------
	
	/// To object array conversion of generic object
	///
	/// Performs the following strategies in the following order
	///
	/// - No conversion
	/// - String to List
	/// - List to array
	/// - Fallback
	///
	/// @param input     The input value to convert
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The converted value
	public static Object[] toObjectArray(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toObjectArray(fallbck, null);
		}
		
		if (input instanceof Object[]) {
			return (Object[]) input;
		}
		
		// From list conversion (if needed)
		List<?> list = null;
		
		// Conversion to List (if possible)
		if (input instanceof String) {
			try {
				Object o = ConvertJSON.toList((String) input);
				if (o instanceof List) {
					list = (List<?>) o;
				}
			} catch (Exception e) {
				
			}
		} else if (input instanceof List) {
			list = (List<?>) input;
		} else { //Force the "toString", then to List conversion
			try {
				String inputStr = input.toString();
				Object o = ConvertJSON.toList(inputStr);
				if (o instanceof List) {
					list = (List<?>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		// List to string array conversion
		if (list != null) {
			// Try direct conversion? (almost always works for object list)
			try {
				return list.toArray(new Object[list.size()]);
			} catch (Exception e) {
				
			}
		}
		
		return toObjectArray(fallbck, null);
	}
	
	/// Default Null fallback, To object array conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static Object[] toObjectArray(Object input) {
		return toObjectArray(input, null);
	}
	
	//--------------------------------------------------------------------------------------------------
	//
	// NESTED object fetch (related to fully qualified keys handling)
	//
	//--------------------------------------------------------------------------------------------------
	
	///
	/// Fetch an Object from either a Map, or a List. By attempting to use the provided key.
	///
	/// This attempts to use the key AS IT IS. Only converting it to an int for List if needed.
	/// It does not do recursive fetch, if that is needed see `fetchNestedObject`
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	///
	public static Object fetchObject(Object base, String key, Object fallback) {
		
		// Base to map / list conversion
		Map<String, Object> baseMap = null;
		List<Object> baseList = null;
		
		// Base to map / list conversion
		if (base instanceof Map) {
			baseMap = toStringMap(base);
		} else if (base instanceof List) {
			baseList = toObjectList(base);
		}
		
		// Fail on getting base item
		if (baseMap == null && baseList == null) {
			return fallback;
		}
		
		// Reuse vars?
		Object ret = null;
		int idxPos = 0;
		
		// Full key fetch
		if (baseMap != null) {
			ret = baseMap.get(key);
		} else { // if( baseList != null ) {
			idxPos = toInt(key, -1);
			if (idxPos > 0 && idxPos < baseList.size()) {
				ret = baseList.get(idxPos);
			}
		}
		
		// Full key found
		if (ret != null) {
			return ret;
		}
		
		// Fallback
		return fallback;
	}
	
	///
	/// Gets an object from the map,
	/// That could very well be, a map inside a list, inside a map, inside a .....
	///
	/// Note that at each iteration step, it attempts to do a FULL key match first, 
	/// before the next iteration depth
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	public static Object fetchNestedObject(Object base, String key, Object fallback) {
		
		// Invalid base -> null, or not ( map OR list ) -> fallback
		if (base == null || !((base instanceof Map) || (base instanceof List))) {
			return fallback;
		}
		
		// Reuse ret object
		Object ret = null;
		
		// Full key fetching found -> if found it is returned =)
		ret = fetchObject(base, key, null);
		if (ret != null) {
			return ret;
		}
		
		// Fallsback if key is ALREADY EMPTY !
		// cause nothing is found, and nothing else can be done
		if (key == null || key.length() <= 0) {
			return fallback;
		}
		
		// Trim off useless spaces, and try again (if applicable)
		int keyLen = key.length();
		key = key.trim();
		if (key.length() != keyLen) {
			return fetchNestedObject(base, key, fallback);
		}
		
		// Trim off useless starting ".dots" and try again
		if (key.startsWith(".")) {
			return fetchNestedObject(base, key.substring(1), fallback);
		}
		
		// Array bracket fetching
		// This is most likely an array fetching,
		// but could also be a case of map fetching with string
		if (key.startsWith("[")) {
			int rightBracketIndex = key.indexOf("]", 1);
			
			String bracketKey = key.substring(1, rightBracketIndex).trim();
			String rightKey = key.substring(rightBracketIndex + 1).trim();
			
			// Fetch [sub object]
			Object subObject = fetchObject(base, bracketKey, null);
			
			// No sub object, cant go a step down, fallback
			//
			// Meaning this can neither be the final object (ending key)
			// nor could it be a recusive fetch.
			if (subObject == null) {
				return fallback;
			}
			
			// subObject is THE object, as its the ending key
			if (rightKey.length() <= 0) {
				return subObject;
			}
			
			// ELSE : Time to continue, recusively fetching
			return fetchNestedObject(subObject, rightKey, fallback);
		}
		
		// Fetch one nested level =(
		int dotIndex = key.indexOf(".");
		int leftBracketIndex = key.indexOf("[");
		
		if (dotIndex >= 0 && (leftBracketIndex < 0 || dotIndex < leftBracketIndex)) {
			// Dot Index exists, and is before left bracket index OR there is no left bracket index
			//
			// This is most likely a nested object fetch -> so recursion is done
			
			// Gets the left.right key
			String leftKey = key.substring(0, dotIndex); //left
			String rightKey = key.substring(dotIndex + 1); //right
			
			// Fetch left
			Object left = fetchObject(base, leftKey, null);
			
			// Time to continue, recusively fetching
			return fetchNestedObject(left, rightKey, fallback);
			//
		} else if (leftBracketIndex > 0) {
			// Left bracket index exists, and there is no dot before it
			//
			// This is most likely a nested object fetch -> so recursion is done
			
			// Gets the left[right] key
			String leftKey = key.substring(0, leftBracketIndex); //left
			String rightKey = key.substring(leftBracketIndex); //[right]
			
			// Fetch left
			Object left = fetchObject(base, leftKey, null);
			
			// Time to continue, recusively fetching [right]
			return fetchNestedObject(left, rightKey, fallback);
		}
		
		// All else failed, including full key fetch -> fallback
		return fallback;
	}
	
	// to custom class
	//--------------------------------------------------------------------------------------------------
	/*
	/// Converts an input object to the desired Class, note that this is on a "best effort" basis
	///
	/// @param input   The input value to convert
	/// @param cClass  The target conversion class if possible
	///
	/// @returns       The converted value (if converted) null if failed
	///
	/// @SuppressWarnings("unchecked")
	public static Object toCustomClass(Class<?> cClass, Object input, Object fallback) {
		
		/// Does not need conversion
		if( cClass.isInstance(input) ) {
			return input;
		}
		
		//if( cClass.isPrimitive() ) {
		//	if( cClass == String.class ) {
		//		return (Object)toString(input, (String)fallback );
		//	}
		//}
		
		return null;
	}
	 */
	
	// to BiFunction Map, used to automated put conversion handling
	//--------------------------------------------------------------------------------------------------
	public static BiFunction<Object, Object, String> toString_BiFunction = (i, f) -> GenericConvert.toString(i, f);
	public static BiFunction<Object, Object, String[]> toStringArray_BiFunction = (i, f) -> GenericConvert
		.toStringArray(i, f);
	
	protected static Map<Class<?>, BiFunction<Object, Object, ?>> biFunctionMap = null;
	
	public static Map<Class<?>, BiFunction<Object, Object, ?>> biFunctionMap() {
		if (biFunctionMap != null) {
			return biFunctionMap;
		}
		
		Map<Class<?>, BiFunction<Object, Object, ?>> ret = new HashMap<Class<?>, BiFunction<Object, Object, ?>>();
		
		ret.put(String.class, toString_BiFunction);
		ret.put(String[].class, toStringArray_BiFunction);
		
		biFunctionMap = ret;
		return biFunctionMap;
	}
	
	/// Gets and return the relevent BiFunction for the given class
	public static BiFunction<Object, Object, ?> getBiFunction(Class<?> resultClassObj) {
		return biFunctionMap().get(resultClassObj);
	}
	
	/// Gets and return the relevent BiFunction for the given class, throws an error if not found
	public static BiFunction<Object, Object, ?> getBiFunction_noisy(Class<?> resultClassObj) {
		BiFunction<Object, Object, ?> ret = getBiFunction(resultClassObj);
		if (ret == null) {
			throw new RuntimeException("Unable to find specified class object: " + resultClassObj);
		}
		return ret;
	}
}
