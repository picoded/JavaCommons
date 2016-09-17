package picoded.conv;

/// A class to convert various data types to Base58. Static functions default to Default charset.
/// Its primary usages is to convert large values sets (like UUID) into a format that can be safely
/// transmitted over the internet / is readable
///
/// Default charset: 123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz
///
/// The default base58 charset is based on bitcoin base58 charset. According to wikipedia: http://en.wikipedia.org/wiki/Base58
///
/// Main advantage of using a base 58 charset, is that for a GUID its length space is the same compared to Base64, while avoiding common typos
///
/// Alternate character sets can be specified when constructing the object.
public class Base58 extends BaseX {
	
	/// Default charset value
	public final static String defaultCharSet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	
	//---------------------------------
	// Object instance functions
	//---------------------------------
	
	/// Defaultconstructor, use default charset
	public Base58() {
		super(defaultCharSet);
	}
	
	/// Constructor with alternative charset
	public Base58(String customCharset) {
		super(customCharset);
		if (customCharset.length() != 58) {
			throw new IllegalArgumentException("Charset string length, must be 58. This is base58 after all my friend.");
		}
	}
	
	// ---------------------------------
	// Singleton
	// ---------------------------------
	
	/// Singleton cache
	private static Base58 instance = null;
	
	/// Singleton copy 
	public Base58 getInstance() {
		if (instance != null) {
			return instance;
		}
		return instance = new Base58();
	}
	
	/// Self refencing static copy
	@Deprecated
	public final static Base58 obj = new Base58();
	
}
