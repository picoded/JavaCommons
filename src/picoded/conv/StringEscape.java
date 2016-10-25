package picoded.conv;

//import java.net.URLDecoder;

/// Proxy to apache.commons.lang3.StringEscapeUtils. See its full documentation
/// [here](http://commons.apache.org/proper/commons-lang/javadocs/api-3.1/org/apache/
/// +commons/lang3/StringEscapeUtils.html)
///
/// ### Example
/// ...................................................................javaS
/// Notable static functions inherited (all with single string input)
/// + escapeCsv
/// + escapeEmacScript
/// + escapeHtml4
/// + escapeHtml3
/// + escapeJava
/// + escapeCsv
///
/// And its inverse function inherited (all with single string input)
/// + unescapedCsv
/// + unescapedEmacScript
/// + unescapedHtml4
/// + unescapedHtml3
/// + unescapedJava
/// + unescapedCsv
///
///
/// Technical notes: Jackson uses apache.commons internally.
///
public class StringEscape extends org.apache.commons.lang3.StringEscapeUtils {
	
	/// Invalid constructor (throws exception)
	protected StringEscape() {
		throw new IllegalAccessError("Utility class");
	}
	
	/// Direct proxy to escapeHtml4
	public static String escapeHtml(String input) {
		return org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(input);
	}
	
	/// Direct proxy to unescapeHtml4
	public static String unescapeHtml(String input) {
		return org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(input);
	}
	
	///
	/// simple uri append escape function, used for uriEncoding.
	/// @author Daniel Murphy
	/// @see http://web.archive.org/web/20130115153639/http://www.dmurph.com/2011/01/java-uri-encoder/
	///
	private static void appendEscaped(StringBuilder uri, char c) {
		if (c <= (char) 0xF) {
			uri.append("%");
			uri.append('0');
			uri.append(HEX[c]);
		} else if (c <= (char) 0xFF) {
			uri.append("%");
			uri.append(HEX[c >> 4]);
			uri.append(HEX[c & 0xF]);
		} else {
			/// unicode
			uri.append('\\');
			uri.append('u');
			uri.append(HEX[c >> 24]);
			uri.append(HEX[(c >> 16) & 0xF]);
			uri.append(HEX[(c >> 8) & 0xF]);
			uri.append(HEX[c & 0xF]);
		}
	}
	
	private static final String MARK = "-_.!~*'()\"";
	private static final char[] HEX = "0123456789ABCDEF".toCharArray();
	
	///
	/// simple uri encoder, made from the spec at http://www.ietf.org/rfc/rfc2396.txt
	/// Feel free to copy this. I'm not responsible for this code in any way, ever.
	///  Thanks to Marco and Thomas
	/// @author Daniel Murphy
	/// @see http://web.archive.org/web/20130115153639/http://www.dmurph.com/2011/01/java-uri-encoder/
	///
	public static String encodeURI(String argString) {
		StringBuilder uri = new StringBuilder();
		
		char[] chars = argString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || MARK.indexOf(c) != -1) {
				uri.append(c);
			} else {
				appendEscaped(uri, c);
			}
		}
		return uri.toString();
	}
	
	/// Reverse function for encodeURI. Basically it decodes into UTF-8
	/// @param argString as String
	/// @returns String
	public static String decodeURI(String argString) {
		try {
			return java.net.URLDecoder.decode(argString, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 not supported");
		}
	}
	
}
