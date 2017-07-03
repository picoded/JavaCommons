package picoded.conv;

import java.math.BigInteger;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

/**
* A class to convert various data types to BaseX. Where
* Its primary usages is to convert values sets (like UUID) into a format that can be safely
* transmitted over the internet / is readable.
*
* Note that unlike general usage Base64, this is not built for performance on large input streams,
* or anything in ~20 bytes and above, as the input values are internally collected into a BigInteger.
* And hence process everything in memory in one go.
*
* However it allows the conversion of any abitaray base types, in a reliable manner.
**/
public class BaseX {

	//----------------------------------------
	// Static reuse vars
	//----------------------------------------

	/**
	* Reusable big integer of value 2
	**/
	protected static final BigInteger value2_BigInteger = BigInteger.valueOf(2);
	/**
	* Reusable big integer of value 0
	**/
	protected static final BigInteger value0_BigInteger = BigInteger.valueOf(0);

	//----------------------------------------
	// Object instance / variables / memoizers
	//----------------------------------------

	/**
	* The set char set for base object
	**/
	protected String inCharset = null;
	/**
	* The char set length as big int
	**/
	protected BigInteger inCharsetLength = null;
	/**
	* The Memoization cache for bit to string length
	**/
	protected HashMap<Integer, Integer> bitToStringCache = null;
	/**
	* The Memoization cache for string to bit length
	**/
	protected HashMap<Integer, Integer> stringToBitCache = null;

	/**
	* Builds the object with the custom charspace
	*
	* @param  The custom charset to use for bit to string conversion
	**/
	public BaseX(String customCharset) {
		if (customCharset == null || customCharset.length() <= 1) {
			throw new IllegalArgumentException("Charset needs atleast 2 characters");
		}

		inCharset = customCharset;
		inCharsetLength = BigInteger.valueOf(customCharset.length());
		bitToStringCache = new HashMap<Integer, Integer>();
		stringToBitCache = new HashMap<Integer, Integer>();
	}

	/**
	* Returns the current charspace
	**/
	public String charset() {
		return inCharset;
	}

	//--------------------------------------------
	// Bit-to-string length conversion handling
	//--------------------------------------------

	/**
	* Calculate the String length needed for the given bit count.
	*
	* The following is the condition needed to be met with the lowest N
	* value to the given bit length, for a valid return result.
	*
	* (2^B) - (X^N) <= 0
	*
	* B - the Bit length count (input parameter)
	* X - the Base numeric length
	* N - the Required string length (the return value)
	*
	* @param  Bit length to use
	*
	* @return Lowest string length where valid
	**/
	public int bitToStringLength(int bitlength) {
		/**
		* Load from Memoization cache
		**/
		if (bitToStringCache.containsKey(bitlength)) {
			return bitToStringCache.get(bitlength);
		}

		/**
		* Derive the n value
		**/
		BigInteger base = value2_BigInteger.pow(bitlength);
		BigInteger comp = inCharsetLength; // (BigInteger.valueOf(x));
		int n = 1;

		while (base.compareTo(comp) > 0) {
			++n;
			comp = comp.multiply(inCharsetLength);
			// divd = inCharsetLength.pow(n);
		}

		/**
		* Store into the Memoization cache
		**/
		bitToStringCache.put(bitlength, n);
		return n;
	}

	/**
	*
	* Calculate the maximum bit length possible for the given string length.
	*
	* Note due to the encoding differences, this value can be higher then its,
	* reverse function `bitToStringLength`.
	*
	* @param The string length
	*
	* @return Highest bit length possible
	**/
	public int stringToBitLength(int stringLength) {
		/**
		* Load from Memoization cache
		**/
		if (stringToBitCache.containsKey(stringLength)) {
			return stringToBitCache.get(stringLength);
		}

		/**
		* Derive the N value
		**/
		BigInteger base = inCharsetLength.pow(stringLength);
		BigInteger comp = value2_BigInteger;
		int n = 1;

		while (base.compareTo(comp) > 0) {
			++n;
			comp = comp.multiply(value2_BigInteger);
			// divd = inCharsetLength.pow(n);
		}
		--n; // get the last known valid value

		/**
		* Store into the Memoization cache
		**/
		stringToBitCache.put(stringLength, n);
		return n;
	}

	/**
	*
	* Varient of stringToBitLength for minimum bytelength support required.
	*
	* @param  The string length to decode
	*
	* @return Lowest byte length required to fit the whole string
	**/
	protected int stringToDecodeByteLength(int stringLength) {
		/**
		* Gets maximum byte length that can fill bit space
		**/
		int byteLength = stringToBitLength(stringLength) / 8;

		/**
		* Check for excess bit space, which would require 1 more byte
		**/
		int mod = stringToBitLength(stringLength) % 8;
		if (mod != 0) {
			/**
			* Return with 1 more
			**/
			return byteLength + 1;
		}

		/**
		* Return the exact fit
		**/
		return byteLength;
	}

	/**
	*
	* Conversion from string to byte array to baseX string.
	*
	* @param  Byte Array values to encode
	*
	* @return Encoded string
	**/
	public String encode(byte[] bArr) {

		/**
		* String length needed
		**/
		int stringlength = bitToStringLength(bArr.length * 8);

		/**
		* Variables setup
		**/
		int remainder;
		StringBuilder ret = new StringBuilder();

		/**
		* Byte array as a single huge BigInt
		**/
		BigInteger bArrValue = new BigInteger(1, bArr);

		/**
		* Pair of divided value, and remaider
		**/
		BigInteger[] dSplit = new BigInteger[] { bArrValue, null };

		/**
		* For every string character needed (to fit the byte array),
		* derive its value, by dividing the current value by charlength.
		**/
		for (int a = 0; a < stringlength; ++a) {
			dSplit = dSplit[0].divideAndRemainder(inCharsetLength);

			/**
			* Use the remainder, to get the char
			**/
			remainder = dSplit[1].intValue();

			/**
			* As the bArrValue is always positive based,
			* Remainder can always be assumed to be positive.
			*
			* Its simply elementry math =)
			*
			* Hence no remainder <= 0 checks is required
			**/
			ret.append(inCharset.charAt(remainder));

		}

		/**
		* Reverse the string value before returning
		* This confroms to most base encoding format
		**/
		return ret.reverse().toString();
	}


	/**
	* Conversion from encoded string to the byte array, note that if the
	* maximum value of the custom base, does not match standard byte spacing,
	* the final byte array count may be higher then actually needed.
	*
	* Use the byteLength varient, if the exact byte space is known.
	*
	* @param  Encoded string to convert
	*
	* @return Decoded byte array
	**/
	public byte[] decode(String encodedString) {
		return decode(encodedString, -1);
	}

	/**
	* The byteLength varients outputs the data up to the given size.
	*
	* Note that prefix extra bits will be loss if encoded string value
	* is larger then the byteLength can hold.
	*
	* Similarly, blank byte values will be appeneded
	* if byteLength is larger then the actual encoded value.
	*
	* byteLength of -1, will use the automatically derived byte length from
	* string length.
	*
	* @param  Encoded string to convert
	* @param  Byte length to use
	*
	* @return Decoded byte array
	**/
	public byte[] decode(String encodedString, int byteLength) {
		return decode(encodedString, byteLength, true);
	}

	/**
	* A varient where, encoding loss handling can be set as strict,
	* Such that an exception is thrown on mismatch of data length.
	*
	* @param  Encoded string to convert
	* @param  Byte length to use
	* @param  Set this to false, to make the byte encoding length strict
	*
	* @return Decoded byte array
	**/
	public byte[] decode(String encodedString, int byteLength, boolean acceptEncodingLoss) {

		/**
		* Variable setup
		**/
		int stringlength = encodedString.length();
		int indx;

		/**
		* Derive max byte length : auto if -1
		**/
		if (byteLength < 0) {
			byteLength = stringToDecodeByteLength(stringlength);
		}

		/**
		* Start off with blank value
		**/
		BigInteger encodedValue = value0_BigInteger;

		/**
		* No reversal is done, as reversal is performed on the "encode" step
		* This is the same as base64 format.
		*
		* encodedString = StringBuilder(encodedString).reverse().toString();
		**/

		/**
		* Iterate the characters and get the encoded value
		**/
		for (char character : encodedString.toCharArray()) {
			indx = inCharset.indexOf(character);
			if (indx < 0) {
				throw new IllegalArgumentException("Invalid character `" + character
					+ "` for encoded string:" + encodedString);
			}

			/**
			* Process each character into the BigInteger value
			**/
			encodedValue = encodedValue.multiply(inCharsetLength).add(BigInteger.valueOf(indx));
		}

		/**
		* Converts the BigInteger to a byte array value
		**/
		byte[] fullEncodedValue = encodedValue.toByteArray();

		/**
		* Exact desired byteLength match found, returns
		**/
		if (fullEncodedValue.length == byteLength) {
			return fullEncodedValue;
		}

		/**
		* The actual return array (to format to new byte length)
		**/
		byte[] retValue = new byte[byteLength];

		/**
		* Note that initializing the array with 0 is not needed in java
		* as this will be its default behaviour
		*
		* See: http:*stackoverflow.com/a/16475488
		*-----------------------------------------------------------------
		* for (int a = 0; a < byteLength; ++a) {
		* 	retValue[a] = 0;
		* }
		**/

		if (fullEncodedValue.length > byteLength) {
			//
			// Actual value is larger, then the targeted length. Note there might be bit value loss
			//

			// Position to start the data copy from, position prior values is "discarded"
			int copyFrom = fullEncodedValue.length - byteLength;

			// Does an encoding loss check if acceptEncodingLoss is set to false.
			//
			// Check the first few additional bytes, if its "zero", and hence suffer from no encoding lost.
			// If a non zero value is found. Assume values will be lost to encoding, hence throw an error.
			if (!acceptEncodingLoss) {
				checkForEncodingLoss(fullEncodedValue, copyFrom, byteLength, encodedString);
			}

			// Copies the value over to the final return array
			System.arraycopy( //
				fullEncodedValue, // original value
				copyFrom, // Start copying from
				retValue, // Copy to target
				0, // Start from position 0
				byteLength // With all the remainding data
				); //

		} else {
			//
			// is definately less, as equal is already checked above
			//

			// Copies the value over to the final return array
			System.arraycopy( //
				fullEncodedValue, // original value
				0, // Start from 0 position
				retValue, //Return value
				// start position (all before it will be zero)
				retValue.length - fullEncodedValue.length, //
				fullEncodedValue.length // all the data
				);
		}

		// The actual return array
		return retValue;
	}

	/**
	* Does an encoding loss check if acceptEncodingLoss is set to false.
	*
	* Check the first few additional bytes, if its "zero", and hence suffer from no encoding lost.
	* If a non zero value is found. Assume values will be lost to encoding, hence throw an error.
	*
	* @param  The byte array to check
	* @param  The byte values positions to check up to (not including)
	* @param  Byte length targeted (for error throwing)
	* @param  Encoding string (for error throwing)
	**/
	protected void checkForEncodingLoss(byte[] fullEncodedValue, int limit, int byteLength,
		String encodedString) {
		for (int a = 0; a < limit; ++a) {
			if (fullEncodedValue[a] != 0) {
				throw new IllegalArgumentException("Encoded value loss for given byteLength("
					+ byteLength + ") for input encodedString: " + encodedString);
			}
		}
	}

	//-----------------------------------------------
	// MD5, SHA1 hashing support utility functions
	//-----------------------------------------------

	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do md5 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String md5hash(byte[] byteArr) {
		return encode(DigestUtils.md5(byteArr));
	}
	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do md5 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String md5hash(String str) {
		return encode(DigestUtils.md5(str));
	}
	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do sha1 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String sha1hash(byte[] byteArr) {
		return encode(DigestUtils.sha1(byteArr));
	}
	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do sha1 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String sha1hash(String str) {
		return encode(DigestUtils.sha1(str));
	}
	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do sha256 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String sha256hash(byte[] byteArr) {
		return encode(DigestUtils.sha256(byteArr));
	}
	/**
	* Hashes the input byte array, into the baseX format
	*
	* @param  The byte array to do sha256 hash
	*
	* @return  Hash result in the encoded base format
	**/
	public String sha256hash(String str) {
		return encode(DigestUtils.sha256(str));
	}

}
