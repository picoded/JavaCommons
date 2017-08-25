package picoded.core.common;

import java.util.EnumSet;

/**
 * HttpRequestType enum, used in webUtils, servletUtils, servlet, and RESTBuidler
 **/
public enum HttpRequestType {
	
	//--------------------------------------------------------
	//
	//  Constructor setup
	//
	//--------------------------------------------------------
	
	/**
	 * List of request types (that is supported)
	 **/
	GET(1 << 1), POST(1 << 2), PUT(1 << 3), DELETE(1 << 4), HEAD(1 << 5), OPTION(1 << 6);
	/**
	 * The internal value used by the Enum constructor
	 **/
	private final int value;
	
	/**
	 * Constructor
	 *
	 * @param  Value used as Enum Constructor
	 **/
	HttpRequestType(int inValue) {
		value = inValue;
	}
	
	/**
	 * Value fetching
	 *
	 * @return  The value of the Enum object
	 **/
	public int value() {
		return value;
	}
	
	//--------------------------------------------------------
	//
	//  EnumSet handling
	//
	//--------------------------------------------------------
	
	/**
	 * EnumSet for iterating
	 **/
	public static final EnumSet<HttpRequestType> set = EnumSet.allOf(HttpRequestType.class);
	
	//--------------------------------------------------------
	//
	// Name conversion
	//
	//--------------------------------------------------------
	
	/**
	 * Get name and toString alias to name() varient
	 *
	 * @return  Enum name string
	 **/
	public String toString() {
		return super.name().toUpperCase();
	}
	
	/**
	 * name to enum serialization
	 *
	 * @param   String name to lookup
	 *
	 * @return the Enum object, if string match (not case sensitive)
	 **/
	public static HttpRequestType fromName(String val) {
		for (HttpRequestType candidate : set) {
			if (candidate.name().equalsIgnoreCase(val)) {
				return candidate;
			}
		}
		return null;
	}
	
	//--------------------------------------------------------
	//
	//  Numeric value conversion
	//
	//--------------------------------------------------------
	
	/**
	 * Byte to enum serialization
	 *
	 * @param  Request type represented as an int
	 *
	 * @return  The exact enum object, only if exact match found
	 **/
	public static HttpRequestType toEnum(int val) {
		for (HttpRequestType candidate : set) {
			if (candidate.value() == val) {
				return candidate;
			}
		}
		return null;
	}
	
	/**
	 * Enum to byte serialization
	 *
	 * @param  Request type to convert to int
	 *
	 * @return  int value, -1 if null
	 **/
	public static int toInt(HttpRequestType val) {
		if (val != null) {
			return val.value();
		}
		return -1;
	}
	
	//--------------------------------------------------------
	//
	//  Flag lookup
	//
	//--------------------------------------------------------
	
	/**
	 * Check if the given Enum, matches another flag
	 *
	 * @param  Single or multiple flag values
	 *
	 * @return  true if current Enum flag is found
	 **/
	public boolean hasFlag(int flagSet) {
		if (flagSet != 0 && ((flagSet & value) == flagSet)) {
			return true;
		}
		return false;
	}
}
