package picoded.conv;

import java.util.HashMap;
import java.math.BigInteger;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.codec.binary.Base64;

/// A class to convert various data types to BaseX. Where
/// Its primary usages is to convert large values sets (like UUID) into a format that can be safely
/// transmitted over the internet / is readable
///
/// Note that unlike general usage base64 this is not built for performance on large input values.
/// Or anything in ~20 bytes and above, as the input values are internally collected into a BigInteger.
/// However it allows the conversion of any abitaray base types, in a reliable manner.
///
/// @TODO: Performance optimization?: How??? : index maps?
///
public class BaseX {
	
	private final static Logger logger = Logger.getLogger(BaseX.class.getName());
	
	// The bitToString length (or vice visa) global Memoization cache
	protected static HashMap<Integer, HashMap<Integer, Integer>> sharedBitToStringLengthCache = new HashMap<Integer, HashMap<Integer, Integer>>();
	protected static HashMap<Integer, HashMap<Integer, Integer>> sharedStringToBitLengthCache = new HashMap<Integer, HashMap<Integer, Integer>>();
	
	// Reusable big integer of value 2
	protected static final BigInteger base2BigInteger = BigInteger.valueOf(2);
	protected static final BigInteger base0BigInteger = BigInteger.valueOf(0);
	
	//---------------------------------
	// Object instance functions
	//---------------------------------
	
	// The set char set for base object
	protected String inCharset = null;
	protected BigInteger inCharsetLength = null;
	
	// The Memoization cache for bit to string length (or vice visa) calculation
	protected HashMap<Integer, Integer> bitToStringCache = null;
	protected HashMap<Integer, Integer> stringToBitCache = null;
	
	/// Builds the object with the custom charspace
	public BaseX(String customCharset) {
		if (customCharset == null || customCharset.length() <= 1) {
			throw new IllegalArgumentException("Charset needs atleast 2 characters");
		}
		
		inCharset = customCharset;
		inCharsetLength = BigInteger.valueOf(customCharset.length());
		
		bitToStringCache = sharedBitToStringLengthCache.get(customCharset.length());
		stringToBitCache = sharedStringToBitLengthCache.get(customCharset.length());
		
		if (bitToStringCache == null) {
			bitToStringCache = new HashMap<Integer, Integer>();
			sharedBitToStringLengthCache.put(inCharset.length(), bitToStringCache);
			
			stringToBitCache = new HashMap<Integer, Integer>();
			sharedStringToBitLengthCache.put(inCharset.length(), stringToBitCache);
		}
	}
	
	/// Returns the current charspace
	public String charset() {
		return inCharset;
	}
	
	///
	/// Calculate the String length needed for the given bit count,
	///
	/// The following is the condition needed to be met with the lowest N
	/// value to the given bit length, to satisfy the condition.
	///
	/// (2^B) - (X^N) <= 0
	///
	/// B - the Bit length count
	/// X - the Base numeric length
	/// N - the Required string length (the return value)
	///
	public int bitToStringLength(int bitlength) {
		int n = 1;
		
		/// Load from Memoization cache
		if (bitToStringCache.containsKey(bitlength)) {
			return bitToStringCache.get(bitlength);
		}
		
		/// Derive the n value
		BigInteger base = base2BigInteger.pow(bitlength);
		BigInteger comp = inCharsetLength; //(BigInteger.valueOf(x));
		
		while (base.compareTo(comp) > 0) {
			++n;
			comp = comp.multiply(inCharsetLength);
			//divd = inCharsetLength.pow(n);
		}
		
		/// Store into the Memoization cache
		bitToStringCache.put(bitlength, n);
		
		return n;
	}
	
	///
	/// Calculate the maximum bit length possible for the given string length
	///
	public int stringToBitLength(int stringLength) {
		int n = 1;
		
		/// Load from Memoization cache
		if (stringToBitCache.containsKey(stringLength)) {
			return stringToBitCache.get(stringLength);
		}
		
		/// Derive the N value
		BigInteger base = inCharsetLength.pow(stringLength);
		BigInteger comp = base2BigInteger;
		
		while (base.compareTo(comp) > 0) {
			++n;
			comp = comp.multiply(base2BigInteger);
			//divd = inCharsetLength.pow(n);
		}
		--n; //get the last known valid value
		
		/// Store into the Memoization cache
		stringToBitCache.put(stringLength, n);
		return n;
	}
	
	/// Conversion from string to byte array to baseX string
	public String encode(byte[] bArr) {
		
		// Variables setup
		int remainder;
		int stringlength = bitToStringLength(bArr.length * 8); //Derived string length needed
		StringBuilder ret = new StringBuilder();
		BigInteger[] dSplit = new BigInteger[] { (new BigInteger(1, bArr)), null };
		
		// For every string character needed (to fit byte array), derive the value
		for (int a = 0; a < stringlength; ++a) {
			dSplit = dSplit[0].divideAndRemainder(inCharsetLength);
			
			remainder = dSplit[1].intValue();
			ret.append(inCharset.charAt((remainder < 0) ? 0 : remainder));
		}
		ret.reverse();
		return ret.toString();
	}
	
	/// Conversion from encoded string to the byte array, note that if the maximum value of the custom base,
	/// does not match standard byte spacing, the final byte array count may be higher then actually needed.
	///
	/// Use the byteLength varient, if the exact byte space is known before hand
	public byte[] decode(String encodedString) {
		return decode(encodedString, -1);
	}
	
	/// The byteLength varients outputs the data up to the given size. Note that prefix extra bits will be loss
	/// if encoded string value is larger then the byteLength can hold. Similarly, blank byte values will preced
	/// if byteLength is larger then the actual encoded value.
	///
	/// byteLength of -1, will use the automatically derived byte length base on the string length.
	public byte[] decode(String encodedString, int byteLength) {
		
		// Variable setup
		int stringlength = encodedString.length();
		int indx;
		
		// Derive max byte length : auto if -1
		if (byteLength < 0) {
			byteLength = stringToBitLength(stringlength) / 8;
			int mod = stringToBitLength(stringlength) % 8;
			if (mod != 0) {
				byteLength++;
			}
		}
		BigInteger encodedValue = base0BigInteger;
		
		// Reverse the encoded string, to process as BigInteger
		//encodedString = new StringBuilder(encodedString).reverse().toString();
		encodedString = new StringBuilder(encodedString).toString();
		
		// Iterate the characters and get the encoded value
		for (char character : encodedString.toCharArray()) {
			indx = inCharset.indexOf(character);
			if (indx < 0) {
				throw new IllegalArgumentException("Invalid character(s) in string: `" + character + "` for full string:"
					+ encodedString);
			}
			
			encodedValue = encodedValue.multiply(inCharsetLength).add(BigInteger.valueOf(indx));
		}
		byte[] fullEncodedValue = encodedValue.toByteArray();
		//ArrayUtils.reverse(fullEncodedValue);
		if (fullEncodedValue.length == byteLength) {
			return fullEncodedValue;
		}
		
		// The actual return array
		byte[] retValue = new byte[byteLength];
		
		// Initialize the array, is this needed???
		// @TODO: Check if this step is even needed in java, it is for C. If it isnt, optimize it out
		for (int a = 0; a < byteLength; ++a) {
			retValue[a] = 0;
		}
		
		// Actual value is larger, NOTE there will be bit value loss
		if (fullEncodedValue.length > byteLength) {
			int copyFrom = (fullEncodedValue.length - byteLength);
			
			for (int a = 0; a < copyFrom; ++a) {
				if (fullEncodedValue[a] != 0) {
					logger.warning("Encoded value loss for givent byteLength(" + byteLength + ") for input encodedString: "
						+ encodedString);
				}
			}
			
			System.arraycopy( //
				fullEncodedValue, copyFrom, //original value
				retValue, 0, //copy despite data loss?
				byteLength //all the data
				);
		} else { //is less, as equal is already checked above and is hence not possible
			System.arraycopy( //
				fullEncodedValue, 0, //original value
				retValue, retValue.length - fullEncodedValue.length, //copy despite data loss?
				fullEncodedValue.length //all the data
				);
		}
		
		return retValue;
	}
}