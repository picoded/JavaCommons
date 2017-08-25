package picoded.core.conv;

/**
 * Simple utility function, that does common regex search and replace.
 **/
public class RegexUtil {
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected RegexUtil() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * Remove all white spaces found in input
	 *
	 * Regex used to remove : \s
	 *
	 * @param  Input string to sanatize
	 *
	 * @return Sanatized string
	 **/
	public static String removeAllWhiteSpace(String input) {
		return input.replaceAll(removeAllWhiteSpaceRegexString, "");
	}
	
	private static String removeAllWhiteSpaceRegexString = "\\s";
	
	/**
	 * Remove all non-numeric values, dots are allowed. Used to sanatize numbers
	 *
	 * Regex used to remove : [^\d.]
	 *
	 * @param  Input string to sanatize
	 *
	 * @return Sanatized string
	 **/
	public static String removeAllNonNumeric(String input) {
		return input.replaceAll(removeAllNonNumericRegexString, "");
	}
	
	private static String removeAllNonNumericRegexString = "[^\\d.]";
	
	/**
	 * Remove all non-alpha-numeric values, dots are not allowed in this case
	 *
	 * Regex used to remove : [^a-zA-Z0-9]
	 *
	 * @param  Input string to sanatize
	 *
	 * @return Sanatized string
	 **/
	public static String removeAllNonAlphaNumeric(String input) {
		return input.replaceAll(removeAllNonAlphaNumericRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericRegexString = "[^a-zA-Z0-9]";
	
	/**
	 * Remove all non-alpha-numeric values, allows underscore, dash, fullstop
	 *
	 * Regex used to remove : [^a-zA-Z0-9-_\.]
	 *
	 * @param  Input string to sanatize
	 *
	 * @return Sanatized string
	 **/
	public static String removeAllNonAlphaNumeric_allowUnderscoreDashFullstop(String input) {
		return input.replaceAll(removeAllNonAlphaNumericAllowUnderscoreDashFullstopRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericAllowUnderscoreDashFullstopRegexString = "[^a-zA-Z0-9-_\\.]";
	
	/**
	 * Remove all non-alpha-numeric values, allows underscore, dash, fullstop, and brackets
	 *
	 * Regex used to remove : [^a-zA-Z0-9-_\.\]\[]
	 *
	 * @param  Input string to sanatize
	 *
	 * @return Sanatized string
	 **/
	public static String removeAllNonAlphaNumeric_allowCommonSeparators(String input) {
		return input.replaceAll(removeAllNonAlphaNumericAllowCommonSeparatorsRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericAllowCommonSeparatorsRegexString = "[^a-zA-Z0-9-_\\.\\]\\[]";
	
}
