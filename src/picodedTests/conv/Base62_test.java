package picodedTests.conv;

// Target test class
import picoded.conv.Base62;
import picoded.conv.BaseX;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.codec.binary.Base64;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class Base62_test {
	
	protected BaseX baseObj = null;
	
	@Before
	public void setUp() {
		baseObj = new Base62();
	}
	
	@After
	public void tearDown() {
		
	}
	
	///
	/// Test bit to string length converters
	///
	@Test
	public void base8_specific() {
		BaseX b = null;
		
		assertNotNull(b = new BaseX("01234567"));
		
		assertEquals(1, b.bitToStringLength(1));
		assertEquals(1, b.bitToStringLength(2));
		assertEquals(1, b.bitToStringLength(3));
		
		assertEquals(2, b.bitToStringLength(4));
		assertEquals(2, b.bitToStringLength(5));
		assertEquals(2, b.bitToStringLength(6));
		
		assertEquals(3, b.bitToStringLength(7));
		assertEquals(3, b.bitToStringLength(8));
		assertEquals(3, b.bitToStringLength(9));
		
		assertEquals("000", b.encode(new byte[] { 0 }));
		assertEquals("700", b.encode(new byte[] { 7 }));
		assertEquals("010", b.encode(new byte[] { 8 }));
		assertEquals("040", b.encode(new byte[] { 32 }));
		assertEquals("001", b.encode(new byte[] { 64 }));
		
		assertArrayEquals(new byte[] { 0 }, b.decode("000"));
		assertArrayEquals(new byte[] { 7 }, b.decode("700"));
		assertArrayEquals(new byte[] { 8 }, b.decode("010"));
		assertArrayEquals(new byte[] { 32 }, b.decode("040"));
		assertArrayEquals(new byte[] { 64 }, b.decode("001"));
		
		//assertEquals("000000", b.encode(new byte[] { 0, 0 }));
		//assertEquals("004300", b.encode(new byte[] { 7, 0 }));
		//assertEquals("700000", b.encode(new byte[] { 0, 7 }));
		
		//assertArrayEquals(new byte[] { 0, 0 }, b.decode("000000"));
		//assertArrayEquals(new byte[] { 7, 0 }, b.decode("004300"));
		//assertArrayEquals(new byte[] { 0, 7 }, b.decode("700000"));
	}
	
	@Test
	public void base62_specific() {
		BaseX b = null;
		
		assertNotNull(b = new Base62());
		
		assertEquals(1, b.bitToStringLength(1));
		assertEquals(1, b.bitToStringLength(2));
		assertEquals(1, b.bitToStringLength(3));
		assertEquals(1, b.bitToStringLength(4));
		assertEquals(1, b.bitToStringLength(5));
		
		assertEquals(2, b.bitToStringLength(6));
		assertEquals(2, b.bitToStringLength(7));
		assertEquals(2, b.bitToStringLength(8));
		assertEquals(2, b.bitToStringLength(9));
		assertEquals(2, b.bitToStringLength(10));
		
		assertEquals(22, b.bitToStringLength(128));
	}
	
	@Test
	public void base64_encodeAndDecodeOnce() {
		BaseX b = null;
		assertNotNull(b = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"));
		
		// min, max
		// (2^8)^12 / (64^16) = 1
		int byteLen = 12; //RandomUtils.nextInt(1, 20);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		//encodeBase64String
		String b64str = null;
		String bXstr = null;
		
		assertNotNull(b64str = Base64.encodeBase64String(byteArr));
		assertEquals(b64str, (bXstr = b.encode(byteArr)));
		
		assertArrayEquals(byteArr, Base64.decodeBase64(b64str));
		assertArrayEquals(byteArr, b.decode(bXstr));
	}
	
	@Test
	public void base64_encodeAndDecodeMultiple() {
		for (int a = 0; a < 50; ++a) {
			base64_encodeAndDecodeOnce();
		}
	}
	
	///
	/// Test charset
	///
	@Test
	public void charset() {
		assertEquals(62, Base62.defaultCharSet.length());
		assertEquals(Base62.defaultCharSet, baseObj.charset());
	}
	
	///
	/// random base conversion charset
	///
	@Test
	public void encodeAndDecodeOnce() {
		
		// min, max
		int byteLen = RandomUtils.nextInt(1, 20);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		// Encode the byte array to string
		String encodedString;
		assertNotNull(encodedString = baseObj.encode(byteArr));
		assertArrayEquals(byteArr, baseObj.decode(encodedString, byteLen));
	}
}