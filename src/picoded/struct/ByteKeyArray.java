package picoded.struct;

/// Implements a dynamic 0~255 dynamic resizing array. Used in accordance to commonly used character syntaxes.
/// Standard english character set will range byte/char value 32 to 126. Resulting into an effective array size 
/// of 95 + 3 (for tabs and newlines) bytes.
///
/// Note: That CPU Line Size is assumed to be 64
///
/// Note: while a byte is being used for a data transfer, do take into account java byte are signed.
///
/// @todo Extend class to implement Iterable<V>, Collection<V>, List<V>, Serializable, Clonable
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
/// 
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// ### LineCache optimisation notes.
/// The following aims to keep its this class object (and its decendents), and its internal vars,
/// within CPU line cache size (Assumed 64 bytes)
///
/// + Java Object Overhead  = 12
/// + Key Array Pointer     = 8
/// + Storage Array Pointer = 8
/// + Optimization Byteflag = 1
///
/// *Total = 29 bytes*
///
public class ByteKeyArray<V> /* implements Map<K,V> */ {
	
	//---------------------------------------------
	// Static Key Sets
	//---------------------------------------------
	/*
	/// Represents a blank key set
	/// value: null
	private static final byte[] KEYSET_BLANK = null;
	
	/// Represents extended spacing values
	/// value: { 'tab' '\n' '\r' } 
	private static final byte[] KEYSET_EXTENDED_SPACING = new byte[]{ 9, 12, 15, 32 };
	
	/// Represents a basic symbols
	/// value: { 'space' - . / _ }
	private static final byte[] KEYSET_BASIC_SYMBOLS = new byte[]{ 32, 45, 46, 47, 95 };
	
	/// Represents a numeric key set
	/// value: { 0 1 2 3 4 5 6 7 8 9 }
	private static final byte[] KEYSET_NUMERIC = new byte[]{ 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 };
	
	/// Represents upper case character set
	/// value: { A B C D E F G H I J K L M N O P Q R S T U V W X Y Z }
	private static final byte[] KEYSET_UPPERCASE = new byte[]{ 
		65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,  //A-M
		78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90   //N-Z
	};
	
	/// Represents lower case character set
	/// value: { a b c d e f g h i j k l m n o p q r s t u v w x y z }
	private static final byte[] KEYSET_LOWERCASE = new byte[]{ 
		97,  98,  99,  100, 101, 102, 103, 104, 105, 106, 107, 108, 109,  //a-m
		110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122   //n-z
	};
	*/
	
	//--------------------------------------------------------------
	// Static Byte Flags, used internally to indicate system state
	//--------------------------------------------------------------
	
	// Byte flag, Optimized keysets, indicator
	// Made redundent by using keyArray = null
	// private static final byte BYTEFLAG_OPTIMIZED = (byte)0x80;
	
	/// Byte flag, lowercase keysets, indicator
	private static final byte BYTEFLAG_LOWERCASE = 0x40;
	
	/// Byte flag, uppercast keysets, indicator
	private static final byte BYTEFLAG_UPPERCASE = 0x20;
	
	/// Byte flag, numeric keysets, indicator
	private static final byte BYTEFLAG_NUMERIC = 0x10;
	
	/// Byte flag, basic symbols keysets, indicator
	private static final byte BYTEFLAG_BASIC_SYMBOLS = 0x08;
	
	/// Byte flag, extended spacing keysets, indicator
	private static final byte BYTEFLAG_EXTENDED_SPACING = 0x04;
	
	/// Byte flag, reserved character keysets, up to char 127, indicator
	private static final byte BYTEFLAG_RESERVED_127 = 0x02;
	
	/// Byte flag, reserved character keysets, up to char 255, indicator
	private static final byte BYTEFLAG_RESERVED_255 = 0x01;
	
	
	//---------------------------------------------
	// System variables
	//---------------------------------------------
	
	/// Boolean byte flag used to indicate the current status of the ByteKeyArray
	private byte statusByteFlag;
	
	private byte[] keyArray = null;
	
	private V[] storeArray = null;
	
	/// @returns Returns the type of internal storage array currently used.
	///
	/// return value        | map size | decimal range         | key map discription
	/// --------------------|----------|-----------------------+-----------------------
	/// blank               | 0        | 0                     | Empty CharKeyArray
	/// numeric             | 10       | {48-57}               | Numeric Keys only
	/// low-alpha-numeric   | 37       | {32}{48-57}{97-122}   | Space, Numeric, and lowercase alpha
	/// upp-alpha-symbolic  | 65       | {32-96}               | Space, Symbolic, Numeric, Symbolic, Uppercase alpha, Symbolic
	/// low-alpha-symbolic  |          | {}
	public String storageMode() {
		if(storeArray == null) {
			return "blank";
		}
		
		throw new RuntimeException("Unknown Storage Array Size found: "+storeArray.length);
	}
	
	
}