package picoded.core.conv;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contains conversions to Java standard objects types.
 *
 * The various supported Objects under java.lang, java.util,
 *
 * + String
 * + String[]
 * + Map<String,?>
 * + List<?>
 * + Object[]
 * + Number
 * + UUID
 * + base-58 GUID
 *
 * @see GenericConvert
 **/
class GenericConvertStandard extends GenericConvertPrimitive {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected GenericConvertStandard() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * Internal utils function
	 *--------------------------------------------------------------------------------------------------
	 *
	 * Converts anything to a list if possible
	 *
	 * @return List<?> or null
	 **/
	protected static List<?> toArrayHelper(Object input) {
		List<?> list = null;
		
		/**
		 * Conversion to List (if possible)
		 **/
		if (input instanceof String) {
			try {
				//Object o = ConvertJSON.toList((String) input);
				list = ConvertJSON.toList((String) input);
				//if (o instanceof List) {
				//list = (List<?>) o;
				//}
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
		return list;
	}
	
	/**
	 * Converts anything to a list or map
	 *
	 * @return List<?> or Map<String,?> or null
	 **/
	protected static Object resolvedListOrMap(Object base) {
		Map<String, Object> baseMap = null;
		List<Object> baseList = null;
		/**
		 * Base to map / list conversion
		 **/
		if (base instanceof Map) {
			baseMap = toStringMap(base);
		} else if (base instanceof List) {
			baseList = toList(base);
		}
		
		/**
		 * Fail on getting base item : attempts conversion
		 **/
		if (baseMap == null && baseList == null) {
			baseMap = toStringMap(base);
			if (baseMap == null) {
				baseList = toList(base);
			}
		}
		if (baseMap == null) {
			return baseList;
		}
		return baseMap;
	}
	
	/**
	 * to string conversion
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To String conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Object to JSON string
	 * - Object.toString()
	 * - Fallback (only possible for non-null values)
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable, aka null)
	 *
	 * @return         The converted string, always possible unless null
	 **/
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
		
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Default null fallback, To String conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted string, always possible unless null
	 **/
	public static String toString(Object input) {
		return toString(input, null);
	}
	
	/**
	 * to string array
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To String array conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - String to List
	 * - List to array
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	public static String[] toStringArray(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toStringArray(fallbck, null);
		}
		String[] ret = null;
		if (input instanceof String[]) {
			ret = (String[]) input;
		} else if (input instanceof Object[]) {
			Object[] inArr = (Object[]) input;
			ret = new String[inArr.length];
			for (int a = 0; a < inArr.length; ++a) {
				ret[a] = toString(inArr[a]);
			}
		}
		if (ret != null) {
			return ret;
		}
		/**
		 * From list conversion (if needed)
		 **/
		List<?> list = toArrayHelper(input);
		
		/**
		 * List to string array conversion
		 **/
		if (list != null) {
			// Try direct conversion?
			try {
				return list.toArray(new String[list.size()]);
			} catch (Exception e) {
				
			}
			
			/**
			 * Try value by value conversion
			 **/
			ret = new String[list.size()];
			for (int a = 0; a < ret.length; ++a) {
				ret[a] = toString(list.get(a));
			}
			return ret;
		}
		
		return toStringArray(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To String array conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static String[] toStringArray(Object input) {
		return toStringArray(input, null);
	}
	
	/**
	 * to string object map
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To String map conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion (if its a map)
	 * - JSON String to Map
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	@SuppressWarnings("unchecked")
	public static <K extends String, V> Map<K, V> toStringMap(Object input, Object fallbck) {
		
		/**
		 * Null handling
		 **/
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toStringMap(fallbck, null);
		}
		
		/**
		 * If Map instance
		 **/
		if (input instanceof Map) {
			return (Map<K, V>) input;
		}
		
		/**
		 * If String instance, attampt JSON conversion
		 **/
		if (input instanceof String) {
			try {
				return (Map<K, V>) ConvertJSON.toMap((String) input);
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		return toStringMap(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To String Object map conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static <K extends String, V> Map<K, V> toStringMap(Object input) {
		return toStringMap(input, null);
	}
	
	/**
	 * to object list
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To object list conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Array to List
	 * - String to List
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	@SuppressWarnings("unchecked")
	public static <V> List<V> toList(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toList(fallbck, null);
		}
		
		if (input instanceof List) {
			return (List<V>) input;
		}
		
		if (input instanceof Object[]) {
			return (List<V>) Arrays.asList((Object[]) input);
		}
		
		List<V> ret = null;
		
		/**
		 * Conversion to List (if possible)
		 **/
		if (input instanceof String) {
			try {
				ret = (List<V>) ConvertJSON.toList((String) input);
			} catch (Exception e) {
				// Silence the exception
			}
		} else { //Force the "toString", then to List conversion
			try {
				String inputStr = input.toString();
				Object o = ConvertJSON.toList(inputStr);
				if (o instanceof List) {
					ret = (List<V>) o;
				}
			} catch (Exception e) {
				// Silence the exception
			}
		}
		
		/**
		 * List to string array conversion
		 **/
		if (ret != null) {
			return ret;
		}
		
		return toList(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To object list conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static <V> List<V> toList(Object input) {
		return toList(input, null);
	}
	
	/**
	 * to object array
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To object array conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - String to List
	 * - List to array
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
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
		
		/**
		 * From list conversion (if needed)
		 **/
		List<?> list = toArrayHelper(input);
		
		/**
		 * List to string array conversion
		 **/
		if (list != null) {
			// Try direct conversion? (almost always works for object list)
			try {
				return list.toArray(new Object[list.size()]);
			} catch (Exception e) {
				
			}
		}
		
		return toObjectArray(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To object array conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static Object[] toObjectArray(Object input) {
		return toObjectArray(input, null);
	}
	
	/**
	 * to Number
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To Number conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Numeric string conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted string, always possible unless null
	 **/
	public static Number toNumber(Object input, Number fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		if (input instanceof Number) {
			return (Number) input;
		}
		
		if (input instanceof String && ((String) input).length() > 0) {
			/**
			 * Numeric string conversion
			 **/
			
			try {
				return new BigDecimal(input.toString());
			} catch (Exception e) {
				return fallbck;
			}
		}
		
		return fallbck;
	}
	
	/**
	 * Default false fallback, To Number conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted boolean
	 **/
	public static Number toNumber(Object input) {
		return toNumber(input, null);
	}
	
	/**
	 * to UUID aka GUID
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To UUID conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Numeric string conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
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
		
		if (input instanceof String && ((String) input).length() == 22) {
			//if (((String) input).length() == 22) {
			try {
				return GUID.fromBase58((String) input);
			} catch (Exception e) {
				// Silence the exception
			}
			//}
		}
		
		return toUUID(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To UUID conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static UUID toUUID(Object input) {
		return toUUID(input, null);
	}
	
	/**
	 * To GUID conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Numeric string conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	public static String toGUID(Object input, Object fallbck) {
		if (input == null) {
			if (fallbck == null) {
				return null;
			}
			return toGUID(fallbck, null);
		}
		
		if (input instanceof UUID) {
			return GUID.base58((UUID) input);
		}
		
		if (input instanceof String && ((String) input).length() >= 22) {
			//if (((String) input).length() >= 22) {
			try {
				if (GUID.fromBase58((String) input) != null) {
					return (String) input;
				}
			} catch (Exception e) {
				// Silence the exception
			}
			//}
		}
		
		return toGUID(fallbck, null);
	}
	
	/**
	 * Default Null fallback, To GUID conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static String toGUID(Object input) {
		return toGUID(input, null);
	}
	
}
