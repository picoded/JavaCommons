package picoded.conv;

public class RegexUtils {
	public static String removeAllWhiteSpace_regexString = "\\s";
	
	public static String removeAllWhiteSpace(String input) {
		return input.replaceAll(removeAllWhiteSpace_regexString, "");
	}
	
	public static String removeAllNonAlphaNumeric_regexString = "[^a-zA-Z0-9]";
	
	public static String removeAllNonAlphaNumeric(String input) {
		return input.replaceAll(removeAllNonAlphaNumeric_regexString, "");
	}
	
	public static String removeAllNonAlphaNumeric_allowUnderscoreAndDash_regexString = "[^a-zA-Z0-9-_.]";
	
	//regex is [^a-zA-Z0-9-_\\\\s]
	public static String removeAllNonAlphaNumeric_allowUnderscoreAndDash(String input) {
		return input.replaceAll(removeAllNonAlphaNumeric_allowUnderscoreAndDash_regexString, "");
	}
}
