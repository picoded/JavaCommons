package picoded.core.struct.query;

import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparision utility functions
 *
 * Which can be used to create more complex Comperator, or used on its own
 **/
public class CompareUtils {
	
	protected CompareUtils() {
		throw new IllegalAccessError("Utility class");
	}
	
	//--------------------------------------------------------------------
	// Static comparators in use
	//-----------------------------------------------------------------
	
	/**
	 *
	 * String SQL 'LIKE' comparison
	 *
	 * @param o1 - the first object to be compared.
	 * @param o2 - the second object to be compared.
	 *
	 * @return -1, 0, or 1 as the first argument is less than, equal to, or greater than the second.
	 **/
	public static int stringMatches(String o1, String o2) {
		
		// Null handling
		if (o1 == null) {
			// Both equals
			if (o2 == null) {
				return 0;
			} //else { //o2 has value, therefor o1 is smaller
			return -1;
			//}
		} else if (o2 == null) { //o1 has value, therefor o1 is larger
			return 1;
		}
		
		return stringCompareCollator.compare(o1, o2);
	}
	
	/**
	 *
	 * String comparision
	 *
	 * @param o1 - the first object to be compared.
	 * @param o2 - the second object to be compared.
	 *
	 * @return -1, 0, or 1 as the first argument is less than, equal to, or greater than the second.
	 **/
	public static int stringCompare(String o1, String o2) {
		
		// Null handling
		if (o1 == null) {
			// Both equals
			if (o2 == null) {
				return 0;
			} //else { //o2 has value, therefor o1 is smaller
			return -1;
			//}
		} else if (o2 == null) { //o1 has value, therefor o1 is larger
			return 1;
		}
		
		return stringCompareCollator.compare(o1, o2);
	}
	
	/**
	 *
	 * Numeric comparision
	 *
	 * Note that this is currently based on doubleValue,
	 * if float values cause problems in the future, it maybe tweaked.
	 *
	 * @param o1 - the first object to be compared.
	 * @param o2 - the second object to be compared.
	 *
	 * @return -1, 0, or 1 as the first argument is less than, equal to, or greater than the second
	 **/
	public static int numericCompare(Number o1, Number o2) {
		
		// Null handling
		if (o1 == null) {
			// Both equals
			if (o2 == null) {
				return 0;
			} //else { //o2 has value, therefor o1 is smaller
			return -1;
			//}
		} else if (o2 == null) { //o1 has value, therefor o1 is larger
			return 1;
		}
		
		return Double.compare(o1.doubleValue(), o2.doubleValue());
	}
	
	/**
	 *
	 * Attempts numeric comparision first, else fallsback to string comparision
	 *
	 * @param o1 - the first object to be compared.
	 * @param o2 - the second object to be compared.
	 *
	 * @return -1, 0, or 1 as the first argument is less than, equal to, or greater than the second
	 **/
	public static int dynamicCompare(Object o1, Object o2) {
		
		// String type comparision
		if ( //
		(o1 instanceof String && o2 instanceof String) || //
			(o1 instanceof String && o2 == null) || //
			(o2 instanceof String && o1 == null) //
		) {
			return stringCompare( //
				(o1 != null) ? o1.toString() : null, //
				(o2 != null) ? o2.toString() : null //
			);
		}
		
		// Numeric comparision
		Number n1 = objectToNumberIfPossible(o1);
		Number n2 = objectToNumberIfPossible(o2);
		
		// Tries to numeric compare
		if (!(n1 == null && n2 == null)) { //
			return numericCompare(n1, n2);
		}
		
		// fallsback to string
		return stringCompare( //
			(o1 != null) ? o1.toString() : null, //
			(o2 != null) ? o2.toString() : null //
		);
	}
	
	//--------------------------------------------------------------------
	// Utility functions / objects
	//-----------------------------------------------------------------
	
	/**
	 * Shared Collator for string compare
	 **/
	protected static Collator stringCompareCollator = RuleBasedCollator.getInstance(Locale.ENGLISH);
	
	/**
	 * Number instance for string to numeric
	 **/
	protected static NumberFormat stringToNumberParser = NumberFormat
		.getNumberInstance(Locale.ENGLISH);
	
	/**
	 *
	 * Conversion to numeric format, if possible. Else its null
	 *
	 * @param  object to convert
	 *
	 * @return Number object. Else its null
	 **/
	protected static Number objectToNumberIfPossible(Object o) {
		if (o == null) {
			return null;
		}
		
		if (o instanceof Number) {
			return (Number) o;
		}
		
		try {
			return stringToNumberParser.parse(o.toString());
		} catch (ParseException e) {
			return null; //silence ParseException
		}
	}
	
	public static int stringLikeCompare(Object o1, Object o2) {
		// Null handling
		if (o1 == null) {
			// Both equals
			if (o2 == null) {
				return 0;
			} //else { //o2 has value, therefor o1 is smaller
			return -1;
			//}
		} else if (o2 == null) { //o1 has value, therefor o1 is larger
			return 1;
		}
		
		Pattern p = Pattern.compile(o2.toString().replaceAll("%", ".*"));
		Matcher m = p.matcher(o1.toString());
		
		if (m.matches()) {
			return 0;
		}
		
		return 1;
	}
}
