package picoded.core.conv;

/**
 * A class to convert various data types to Base62. Static functions default to Default charset.
 *
 * Its primary usages is to convert large values sets (like UUID) into a format that can be
 * safely transmitted over the internet / is readable.
 *
 * Default charset: A-Za-z0-9
 *
 * Alternate character sets can be specified when constructing the object.
 **/
public class Base62 extends BaseX {
	
	/**
	 * Default charset value
	 **/
	public static final String DEFAULT_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	// ---------------------------------
	// Object instance functions
	// ---------------------------------
	
	/**
	 * Default constructor, use default charset
	 **/
	public Base62() {
		super(DEFAULT_CHARSET);
	}
	
	/**
	 * Constructor with alternative charset
	 **/
	public Base62(String customCharset) {
		super(customCharset);
		if (customCharset.length() != 62) {
			throw new IllegalArgumentException(
				"Charset string length, must be 62. This is base62 after all my friend.");
		}
	}
	
	// ---------------------------------
	// Singleton
	// ---------------------------------
	
	/**
	 * Singleton cache
	 **/
	private static volatile Base62 instance = null;
	
	/**
	 * Singleton copy
	 **/
	public static Base62 getInstance() {
		if (instance != null) {
			return instance;
		}
		return instance = new Base62();
	}
	
}
