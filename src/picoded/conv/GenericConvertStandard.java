package picoded.conv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import picoded.struct.GenericConvertArrayList;
import picoded.struct.GenericConvertList;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

///
/// Contains conversions to Java standard objects types
///
/// Basically the various supported Objects under java.lang, java.math, java.util
///
/// @see GenericConvert 
///
class GenericConvertStandard extends GenericConvertPrimitive {
	
	/// Invalid constructor (throws exception)
	protected GenericConvertStandard() {
		throw new IllegalAccessError("Utility class");
	}
	
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
		
		return ConvertJSON.fromObject(input);
	}
	
	/// Default null fallback, To String conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted string, always possible unless null
	public static String toString(Object input) {
		return toString(input, null);
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
			return (Number) input;
		}
		
		if (input instanceof String && ((String) input).length() > 0) {
			//Numeric string conversion
			
			try {
				return new BigDecimal(input.toString());
			} catch (Exception e) {
				return fallbck;
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
	
	/// Default Null fallback, To GUID conversion of generic object
	///
	/// @param input     The input value to convert
	///
	/// @returns         The converted value
	public static String toGUID(Object input) {
		return toGUID(input, null);
	}
	
}
