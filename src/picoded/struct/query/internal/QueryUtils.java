package picoded.struct.query.internal;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class QueryUtils {
	
	protected QueryUtils() {
		throw new IllegalAccessError("Utility class");
	}
	
	///
	/// Gets the field value to test
	///
	/// @param   object to extract out the field value 
	/// @param   field name of extraction
	///
	/// @TODO: Support FullyQualifiedDomainName extraction? with arrays even?
	///
	/// @returns  The extracted object
	///
	@SuppressWarnings("rawtypes")
	public static Object getFieldValue(Object t, String field) {
		if (field == null || field.toString().equalsIgnoreCase("this")) {
			return t;
		} else if (t instanceof Map) {
			return ((Map) t).get(field);
		}
		return null;
	}
	
	///
	/// Remove and returns field name "wrapers" for sql specific versions
	///
	/// @returns  Fieldnames after removal
	///
	public static String unwrapFieldName(String field) {
		if ( //
		(field.startsWith("\"") && field.endsWith("\""))
			|| (field.startsWith("'") && field.endsWith("'"))
			|| (field.startsWith("[") && field.endsWith("]"))) { //
			field = field.substring(1, field.length() - 1);
		}
		
		if (field.length() == 0) {
			throw new RuntimeException("Unexpected blank field");
		}
		
		return field;
	}
	
	// 
	//--------------------------------------------------------------------
	
	public static Double normalizeNumber(Object number) {
		Double val = null;
		if (number instanceof Integer) {
			val = (Integer) number * 1.0;
		} else if (number instanceof Float) {
			val = ((Float) number * 1.0);
		} else if (number instanceof Double) {
			val = (Double) number;
		}
		return val;
	}
	
	//returns String only, and ONLY if it should be compared as a string
	//if its a number, will return as a double
	public static Object normalizeObject(Object source) {
		if (source instanceof String) {
			if (((String) source).matches("[0-9]+") || ((String) source).contains(".")) { //extremely rudimentary check for a number, needs to be improved
				try {
					Number sourceAsNumber = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(
						(String) source);
					Double sourceAsDouble = sourceAsNumber.doubleValue();
					return sourceAsDouble;
				} catch (Exception ex) {
					throw new RuntimeException("exception in normalizeObject-> " + ex.getMessage());
				}
			} //else {
			return source;
			//}
		} //else {
		return normalizeNumber(source);
		//}
	}
}
