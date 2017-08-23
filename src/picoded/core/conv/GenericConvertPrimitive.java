package picoded.core.conv;

/**
 * A reduced java primitives only version of GenericConvert.
 *
 * Which is the basic types found in java, in accordence to
 * https://en.wikibooks.org/wiki/Java_Programming/Primitive_Types
 *
 * Or as listed below (in order of most common usage @ picoded)
 *
 * + boolean
 * + int
 * + long
 * + float
 * + double
 * + byte
 * + short
 * + char
 *
 * @see GenericConvert
 **/
class GenericConvertPrimitive {
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected GenericConvertPrimitive() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * to boolean conversion
	 *--------------------------------------------------------------------------------------------------
	 *
	 * Takes in a char, and convert it to true/false statements, if a match occurs
	 *
	 * @param  tChar   The character to deduce true/false statements
	 *
	 * @return  "true", "false", or ""
	 **/
	protected static Boolean charToBoolean(char tChar) {
		if (tChar == '+' || tChar == 't' || tChar == 'T' || tChar == 'y' || tChar == 'Y') {
			return Boolean.TRUE;
		} else if (tChar == '-' || tChar == 'f' || tChar == 'F' || tChar == 'n' || tChar == 'N') {
			return Boolean.FALSE;
		}
		return null;
	}
	
	/**
	 * To boolean conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - Numeric conversion
	 * - String conversion
	 * - Numeric string conversion
	 * - Fallback
	 *
	 * @param  input     The input value to convert
	 * @param  fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted string, always possible unless null
	 **/
	public static boolean toBoolean(Object input, boolean fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		if (input instanceof Boolean) {
			return ((Boolean) input).booleanValue();
		}
		
		if (input instanceof Number) {
			return ((Number) input).floatValue() > 0.0F;
		}
		
		if (input instanceof String && ((String) input).length() > 0) {
			char tChar = ((String) input).charAt(0);
			/**
			 * String conversion
			 **/
			Boolean returnValue = charToBoolean(tChar);
			
			if (returnValue != null) {
				return returnValue.booleanValue();
			}
			/**
			 * Numeric string conversion
			 **/
			String s = (String) input;
			
			if (s.length() > 2) {
				s = s.substring(0, 2);
			}
			try {
				return Integer.parseInt(s) > 0;
			} catch (Exception e) {
				//return fallbck; //commented due to Code Smell
				// Silence the exception
			}
		}
		
		return fallbck;
	}
	
	/**
	 * Default false fallback, To boolean conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted boolean
	 **/
	public static boolean toBoolean(Object input) {
		return toBoolean(input, false);
	}
	
	/**
	 * to int
	 *
	 * --------------------------------------------------------------------------------------------------
	 *
	 * To int conversion of generic object.
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
	public static int toInt(Object input, int fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).intValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static int toInt(Object input) {
		return toInt(input, 0);
	}
	
	/**
	 * to long
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To long conversion of generic object
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
	public static long toLong(Object input, long fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).longValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static long toLong(Object input) {
		return toLong(input, 0);
	}
	
	/**
	 * to float
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To float conversion of generic object
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
	public static float toFloat(Object input, float fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).floatValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static float toFloat(Object input) {
		return toFloat(input, 0);
	}
	
	/**
	 * to double
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To double conversion of generic object
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
	public static double toDouble(Object input, double fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).doubleValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static double toDouble(Object input) {
		return toDouble(input, 0);
	}
	
	/**
	 * to byte
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To byte conversion of generic object
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
	public static byte toByte(Object input, byte fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).byteValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static byte toByte(Object input) {
		return toByte(input, (byte) 0);
	}
	
	/**
	 * to short
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To short conversion of generic object
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
	public static short toShort(Object input, short fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		return (GenericConvert.toNumber(input, fallbck)).shortValue();
	}
	
	/**
	 * Default 0 fallback, To int conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static short toShort(Object input) {
		return toShort(input, (short) 0);
	}
	
	/**
	 * to short
	 *--------------------------------------------------------------------------------------------------
	 *
	 * To char conversion of generic object
	 *
	 * Performs the following strategies in the following order
	 *
	 * - No conversion
	 * - String extraction
	 * - Numeric string conversion
	 * - Fallback
	 *
	 * @param input     The input value to convert
	 * @param fallbck   The fallback default (if not convertable)
	 *
	 * @return         The converted value
	 **/
	public static char toChar(Object input, char fallbck) {
		if (input == null) {
			return fallbck;
		}
		
		if (input instanceof Character) {
			return ((Character) input).charValue();
		}
		
		if (input instanceof String) {
			return ((String) input).charAt(0);
		}
		
		return (char) (GenericConvert.toNumber(input, (short) fallbck)).shortValue();
	}
	
	/**
	 * Default 0 fallback, To char conversion of generic object
	 *
	 * @param input     The input value to convert
	 *
	 * @return         The converted value
	 **/
	public static char toChar(Object input) {
		return toChar(input, (char) 0);
	}
	
}
