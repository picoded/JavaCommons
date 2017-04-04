package picoded.conv;

public class RegexUtil {
	
	/// Invalid constructor (throws exception)
	protected RegexUtil() {
		throw new IllegalAccessError("Utility class");
	}
	
	private static String removeAllWhiteSpaceRegexString = "\\s";
	
	public static String removeAllWhiteSpace(String input) {
		return input.replaceAll(removeAllWhiteSpaceRegexString, "");
	}
	
	private static String removeAllNonNumericRegexString = "[^\\d.]";
	
	public static String removeAllNonNumeric(String input) {
		return input.replaceAll(removeAllNonNumericRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericRegexString = "[^a-zA-Z0-9]";
	
	public static String removeAllNonAlphaNumeric(String input) {
		return input.replaceAll(removeAllNonAlphaNumericRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericAllowUnderscoreDashFullstopRegexString = "[^a-zA-Z0-9-_\\.]";
	
	public static String removeAllNonAlphaNumeric_allowUnderscoreDashFullstop(String input) {
		return input.replaceAll(removeAllNonAlphaNumericAllowUnderscoreDashFullstopRegexString, "");
	}
	
	private static String removeAllNonAlphaNumericAllowCommonSeparatorsRegexString = "[^a-zA-Z0-9-_\\.\\]\\[]";
	
	public static String removeAllNonAlphaNumeric_allowCommonSeparators(String input) {
		return input.replaceAll(removeAllNonAlphaNumericAllowCommonSeparatorsRegexString, "");
	}
	
	public static String sanitiseCommonEscapeCharactersIntoAscii(String input) {
		String ret = input;
		ret = ret.replaceAll("\\<", "&#60;");
		ret = ret.replaceAll("\\>", "&#62;");
		
		//ret = ret.replaceAll("\\`", "&#96;");
		//ret = ret.replaceAll("\\'", "&#8216;");
		//ret = ret.replaceAll("\\\"", "&#34;"); //Removing quote sanitisation as SQL security happens on another layer
		
		ret = ret.replaceAll("\\\\", "&#92;");
		return ret;
	}
}
