package picoded.conv;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

// Apache reference
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;

// Java reference
import java.lang.reflect.InvocationTargetException;

/// The actual test suite
public class BaseX_test {
	
	// Test run multiplier
	protected int testRunMultiplier = 500;
	protected int stringAndByteMaxLength = 100;
	protected int stringAndByteFixedLength = 22;
	
	// The actual test object
	protected BaseX baseObj = null;
	
	@Before
	public void setUp() {
		baseObj = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
	}
	
	@After
	public void tearDown() {
		baseObj = null;
	}
	
	///
	/// Charset fetch / length
	///
	@Test
	public void charsetFetch() {
		assertNotNull(baseObj.charset());
		assertEquals(baseObj.charset().length(), baseObj.inCharsetLength.intValue());
	}
	
	///
	/// random string length conversion test
	///
	@Test
	public void stringToBitLengthAndBack() {
		// min, max
		int strLen = RandomUtils.nextInt(1, stringAndByteMaxLength);
		
		// Convert string length and back
		assertEquals( strLen, baseObj.bitToStringLength(baseObj.stringToBitLength(strLen)) );
		
		// Fixed length test (gurantee memoizer hit)
		strLen = stringAndByteFixedLength;
		assertEquals( strLen, baseObj.bitToStringLength(baseObj.stringToBitLength(strLen)) );
	}
	
	@Test
	public void stringToBitLengthAndBackMultiple() {
		for (int a = 0; a < testRunMultiplier; ++a) {
			stringToBitLengthAndBack();
		}
	}
	
	///
	/// random base conversion charset
	///
	@Test
	public void encodeAndDecodeOnce() {
		// min, max
		int byteLen = RandomUtils.nextInt(1, stringAndByteMaxLength);
		
		// raw byteArray to encode
		String encodedString;
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		// Encode the byte array to string
		assertNotNull(encodedString = baseObj.encode(byteArr));
		assertArrayEquals(byteArr, baseObj.decode(encodedString, byteLen));
		
		// Fixed length test (gurantee memoizer hit)
		byteLen = stringAndByteFixedLength;
		byteArr = RandomUtils.nextBytes(byteLen);
		
		// Encode the byte array to string
		assertNotNull(encodedString = baseObj.encode(byteArr));
		assertArrayEquals(byteArr, baseObj.decode(encodedString, byteLen));
		
	}
	
	@Test
	public void encodeAndDecodeMultiple() {
		for (int a = 0; a < testRunMultiplier; ++a) {
			encodeAndDecodeOnce();
		}
	}
	
	///
	/// random hash test
	///
	@Test
	public void hashAllTheStuff() {
		// min, max
		int byteLen = RandomUtils.nextInt(1, stringAndByteMaxLength);
		
		// raw byteArray to encode
		byte[] randArr = RandomUtils.nextBytes(byteLen);
		String randStr = baseObj.encode(randArr);
		
		assertNotNull( baseObj.md5hash(randArr) );
		assertNotNull( baseObj.md5hash(randStr) );
		
		assertNotNull( baseObj.sha1hash(randArr) );
		assertNotNull( baseObj.sha1hash(randStr) );
		
		assertNotNull( baseObj.sha256hash(randArr) );
		assertNotNull( baseObj.sha256hash(randStr) );
		
		// Fixed length test varient
		byteLen = stringAndByteFixedLength;
		randArr = RandomUtils.nextBytes(byteLen);
		randStr = baseObj.encode(randArr);
		
		assertNotNull( baseObj.md5hash(randArr) );
		assertNotNull( baseObj.md5hash(randStr) );
		
		assertNotNull( baseObj.sha1hash(randArr) );
		assertNotNull( baseObj.sha1hash(randStr) );
		
		assertNotNull( baseObj.sha256hash(randArr) );
		assertNotNull( baseObj.sha256hash(randStr) );
	}
	
	@Test
	public void hashAllTheStuffMultiple() {
		for (int a = 0; a < testRunMultiplier; ++a) {
			hashAllTheStuff();
		}
	}
	
	///-----------------------------------------------
	///
	/// Exception testing coverage
	///
	///-----------------------------------------------
	
	///
	/// Intentionally recreates the class object with a single char string - which is always invalid
	///
	/// Note: (expected=IllegalArgumentException.class), was recasted as InvocationTargetException
	@Test (expected=InvocationTargetException.class)
	public void invalidCharsetLength() throws Exception {
		baseObj.getClass().getDeclaredConstructor(String.class).newInstance("i");
	}
	
	///
	/// Intentionally recreates the class object with a null char string - which is always invalid
	///
	/// Note: (expected=IllegalArgumentException.class), was recasted as InvocationTargetException
	@Test (expected=InvocationTargetException.class)
	public void nullCharset() throws Exception {
		baseObj.getClass().getDeclaredConstructor(String.class).newInstance(new Object[] {null});
	}
	
	///
	/// Intentionally calls with special characters, to induce error
	///
	@Test (expected=IllegalArgumentException.class)
	public void invalidDecodeChar() throws Exception {
		baseObj.decode("invalidChars->~!@#$%^&*()_+[]\\;',./<>");
	}
	
	///
	/// Intentionally induce encoding error
	///
	@Test (expected=IllegalArgumentException.class)
	public void decodingLossError() throws Exception {
		
		// Byte to use for initialize
		byte fb = (byte)255;
		
		// 8 bytes long btw
		byte[] lotsOfBits = new byte[] { fb, fb, fb, fb, fb, fb, fb, fb };
		String encoded = baseObj.encode(lotsOfBits);
		
		// Decode with strict check, and insufficent bytes (throws an error)
		baseObj.decode(encoded, lotsOfBits.length - 1, false);
	}
	
}
