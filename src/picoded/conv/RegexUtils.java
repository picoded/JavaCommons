package picoded.conv;

public class RegexUtils {
	private static String removeAllWhiteSpace_regexString = "\\s";
	
	public static String removeAllWhiteSpace(String input) {
		return input.replaceAll(removeAllWhiteSpace_regexString, "");
	}
	
	private static String removeAllNonAlphaNumeric_regexString = "[^a-zA-Z0-9]";
	
	public static String removeAllNonAlphaNumeric(String input) {
		return input.replaceAll(removeAllNonAlphaNumeric_regexString, "");
	}
	
	private static String removeAllNonAlphaNumeric_allowUnderscoreDashFullstop_regexString = "[^a-zA-Z0-9-_\\.]";
	
	public static String removeAllNonAlphaNumeric_allowUnderscoreDashFullstop(String input) {
		return input.replaceAll(removeAllNonAlphaNumeric_allowUnderscoreDashFullstop_regexString, "");
	}
	
	private static String removeAllNonAlphaNumeric_allowCommonSeparators_regexString = "[^a-zA-Z0-9-_\\.\\]\\[]";
	
	public static String removeAllNonAlphaNumeric_allowCommonSeparators(String input) {
		return input.replaceAll(removeAllNonAlphaNumeric_allowCommonSeparators_regexString, "");
	}
}
