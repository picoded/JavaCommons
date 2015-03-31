package picoded.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

/// Provides several core GUID functionalities
///
/// ------------------------------------------------
///
/// @TODO Unit Testing
///
public class GUID {
	
	/// Proxies UUID.randomUUID();
	public static UUID randomUUID() {
		return UUID.randomUUID();
	}
	
	/// Returns a long[2] array representing a guid
	public static long[] longPair() {
		UUID uuid = UUID.randomUUID();
		return new long[] { uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() };
	}
	
	// Returns a byte[16] array representing a guid
	public static byte[] byteArray() {
		UUID uuid = UUID.randomUUID();
		
		// Converts the uuid into the byteArray format
		// http://stackoverflow.com/questions/2983065/guid-to-bytearray
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	/// Returns a 22 character base64 GUID string
	/// Refer to: http://stackoverflow.com/questions/607651/how-many-characters-are-there-in-a-guid
	/// & http://blog.codinghorror.com/equipping-our-ascii-armor/
	public static String base64() {
		byte[] uuidArr = byteArray();
		
		// Convert a byte array to base64 string : for safe storing of GUID in jSql
		String s = new Base64().encodeAsString(uuidArr).replaceAll("=", "");
		if (s.length() < 22) {
			throw new RuntimeException("GUID generation exception, invalid length of " + s.length() + " (" + s + ")");
		}
		return s.substring(0, 22); //remove uneeded
	}
	
}
