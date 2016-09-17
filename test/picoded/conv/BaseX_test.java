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
	/// Intentionally recreates the class object with a single char string - which is always invalid
	///
	/// Note: (expected=IllegalArgumentException.class), was recasted as InvocationTargetException
	@Test (expected=InvocationTargetException.class)
	public void invalidCharsetLength() throws Exception {
		baseObj.getClass().getDeclaredConstructor(String.class).newInstance("i");
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
		int strLen = RandomUtils.nextInt(1, 50);
		
		// Convert string length and back
		assertEquals( strLen, baseObj.bitToStringLength(baseObj.stringToBitLength(strLen)) );
	}
	
	@Test
	public void stringToBitLengthAndBackMultiple() {
		for (int a = 0; a < 500; ++a) {
			stringToBitLengthAndBack();
		}
	}
	
	///
	/// random base conversion charset
	///
	@Test
	public void encodeAndDecodeOnce() {
		// min, max
		int byteLen = RandomUtils.nextInt(1, 50);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		// Encode the byte array to string
		String encodedString;
		assertNotNull(encodedString = baseObj.encode(byteArr));
		assertArrayEquals(byteArr, baseObj.decode(encodedString, byteLen));
	}
	
	@Test
	public void encodeAndDecodeMultiple() {
		for (int a = 0; a < 500; ++a) {
			encodeAndDecodeOnce();
		}
	}
}
