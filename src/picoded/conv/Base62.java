package picoded.conv;

/// A class to convert various data types to Base62. Static functions default to Default charset.
/// Its primary usages is to convert large values sets (like UUID) into a format that can be safely
/// transmitted over the internet / is readable
///
/// Default charset: A-Za-z0-9
///
/// Alternate character sets can be specified when constructing the object.
public class Base62 extends BaseX {
	
	/// Default charset value
	public final static String defaultCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	// ---------------------------------
	// Object instance functions
	// ---------------------------------
	
	/// Defaultconstructor, use default charset
	public Base62() {
		super(defaultCharSet);
	}
	
	public Base62(String customCharset) {
		super(customCharset);
		if (customCharset.length() != 62) {
			throw new IllegalArgumentException("Charset string length, must be 62. This is base62 after all my friend.");
		}
	}
	
	/// Self refencing static copy
	public final static Base62 obj = new Base62();
	
}