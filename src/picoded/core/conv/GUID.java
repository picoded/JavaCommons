package picoded.core.conv;

/**
 * Java includes
 **/
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Apache includes
 **/
import org.apache.commons.codec.binary.Base64;

/**
 * Provides several core GUID functionalities.
 **/
public class GUID {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected GUID() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * Proxies UUID.randomUUID();
	 *
	 * @return UUID representing the GUID
	 **/
	public static UUID randomUUID() {
		return UUID.randomUUID();
	}
	
	//---------------------------------------------------------------------------------------------
	// Generates various formats of a GUID value, either randomly or via a valid UUID
	//---------------------------------------------------------------------------------------------
	
	/**
	 * RANDOMLY Returns a long[2] array representing a guid
	 *
	 * @return long[2], representing the most, and least significant bits
	 **/
	public static long[] longPair() {
		return longPair(randomUUID());
	}
	
	/**
	 * Returns a long[2] array representing a guid
	 *
	 * @param   uuid unique guid value
	 * @return long[2], representing the most, and least significant bits
	 **/
	public static long[] longPair(UUID uuid) {
		return new long[] { uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() };
	}
	
	/**
	 * RANDOMLY Returns a byte[16] array representing a guid
	 *
	 * @return byte[16] array, representing the most, and least significant bits
	 **/
	public static byte[] byteArray() {
		return byteArray(randomUUID());
	}
	
	/**
	 * Returns a byte[16] array representing a guid
	 *
	 * @param  uuid unique guid value
	 * @return byte[16] array, representing the most, and least significant bits
	 **/
	public static byte[] byteArray(UUID uuid) {
		/**
		 * Converts the uuid into the byteArray format
		 * http://stackoverflow.com/questions/2983065/guid-to-bytearray
		 **/
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	/**
	 * RANDOMLY Returns a 22 character base64 GUID string
	 *
	 * @param  uuid unique guid value
	 * @return long array of length 2, representing the most, and least significant bits
	 **/
	public static String base64() {
		return base64(randomUUID());
	}
	
	/**
	 * Returns a 22 character base64 GUID string
	 *
	 * @param  uuid unique guid value
	 * @return string of 22 characters representing the GUID
	 **/
	public static String base64(UUID uuid) {
		byte[] uuidArr = byteArray(uuid);
		
		/**
		 * Convert a byte array to base64 string : for safe storing of GUID in JSql
		 **/
		String s = new Base64().encodeAsString(uuidArr).replaceAll("=", "");
		
		// Conversion to base64, with blank is impossible (as 0 still produces a valid char)
		// Hence a length less then 22 check is pointless (will not happen)
		//
		// if (s.length() < 22) {
		// 	throw new RuntimeException("GUID generation exception, invalid length of " + s.length() + " (" + s + ")");
		// }
		
		return s.substring(0, 22); //remove uneeded
	}
	
	/**
	 * RANDOMLY Returns a 22 character base58 GUID string
	 *
	 * @param   uuid unique guid value
	 * @return string of 22 characters representing the GUID
	 **/
	public static String base58() {
		return base58(randomUUID());
	}
	
	/**
	 * Returns a 22 character base58 GUID string
	 *
	 * @param  uuid unique guid value
	 * @return string of 22 characters representing the GUID
	 **/
	public static String base58(UUID uuid) {
		byte[] uuidArr = byteArray(uuid);
		return Base58.getInstance().encode(uuidArr);
	}
	
	//---------------------------------------------------------------------------------------------
	// Converts the various fromat into a UUID object
	//---------------------------------------------------------------------------------------------
	
	/**
	 * Returns a UUID using a long[2] most and least signficant bits pair
	 *
	 * @param   most significant bits pair
	 * @param   least significant bits pair
	 * @return A UUID object
	 **/
	public static UUID fromLongPair(long mostSignificant, long leastSignificant) {
		return new UUID(mostSignificant, leastSignificant);
	}
	
	/**
	 * Returns a UUID using a long[2] most and least signficant bits pair
	 *
	 * @param   long[2] pair with the most and least significant bits pair
	 * @return A UUID object
	 **/
	public static UUID fromLongPair(long[] longPair) {
		return new UUID(longPair[0], longPair[1]);
	}
	
	/**
	 * Returns a UUID using a byte[16]
	 *
	 * @param   byte[16] value of the UUID
	 * @return A UUID object
	 **/
	public static UUID fromByteArray(byte[] byteArray) {
		ByteBuffer bb = ByteBuffer.wrap(byteArray);
		return fromLongPair(bb.getLong(0), bb.getLong(8));
	}
	
	/**
	 * Returns a UUID using a base64 GUID string
	 *
	 * @param   base64 string to convert from
	 * @return A UUID object
	 **/
	public static UUID fromBase64(String base64str) {
		return fromByteArray(Base64.decodeBase64(base64str));
	}
	
	/**
	 * Returns a UUID using a base58 GUID string
	 *
	 * @param   base64 string to convert from
	 * @return A UUID object
	 **/
	public static UUID fromBase58(String base58str) {
		return fromByteArray(Base58.getInstance().decode(base58str));
	}
	
}
