package picodedTests.conv;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.conv.Base62;
import picoded.conv.BaseX;
// Classes used in test case

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
	
	// /
	// / Test bit to string length converters
	// /
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
		assertEquals("007", b.encode(new byte[] { 7 }));
		assertEquals("010", b.encode(new byte[] { 8 }));
		assertEquals("040", b.encode(new byte[] { 32 }));
		assertEquals("100", b.encode(new byte[] { 64 }));
		
		assertArrayEquals(new byte[] { 0 }, b.decode("000"));
		assertArrayEquals(new byte[] { 7 }, b.decode("007"));
		assertArrayEquals(new byte[] { 8 }, b.decode("010"));
		assertArrayEquals(new byte[] { 32 }, b.decode("040"));
		assertArrayEquals(new byte[] { 64 }, b.decode("100"));
		
		assertEquals("000000", b.encode(new byte[] { 0, 0 }));
		assertEquals("003400", b.encode(new byte[] { 7, 0 }));
		assertEquals("000007", b.encode(new byte[] { 0, 7 }));
		assertArrayEquals(new byte[] { 0, 0 }, b.decode("000000", 2));
		assertArrayEquals(new byte[] { 7, 0 }, b.decode("003400", 2));
		assertArrayEquals(new byte[] { 0, 7 }, b.decode("000007", 2));
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
		
		assertEquals(27, b.bitToStringLength(160));
	}
	
	@Test
	public void base64_helloWorld() {
		BaseX b = null;
		assertNotNull(b = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"));
		
		// raw byteArray to encode
		byte[] byteArr = ("Hello World!").getBytes();
		
		// encodeBase64String
		String b64str = null;
		String bXstr = null;
		
		assertNotNull(b64str = Base64.encodeBase64String(byteArr));
		assertEquals(b64str, (bXstr = b.encode(byteArr)));
		
		assertArrayEquals(byteArr, Base64.decodeBase64(b64str));
		assertArrayEquals(byteArr, b.decode(bXstr));
	}
	
	@Test
	public void base64_encodeAndDecodeOnce() {
		BaseX b = null;
		assertNotNull(b = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"));
		
		assertEquals(27, b.bitToStringLength(160));
		
		// min, max
		// (2^8)^12 / (64^16) = 1
		int byteLen = 12; // RandomUtils.nextInt(1, 20);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		// encodeBase64String
		String b64str = null;
		String bXstr = null;
		
		assertNotNull(b64str = Base64.encodeBase64String(byteArr));
		assertEquals(b64str, (bXstr = b.encode(byteArr)));
		
		assertArrayEquals("For encoded string: " + b64str, byteArr, Base64.decodeBase64(b64str));
		assertArrayEquals("For encoded string: " + bXstr, byteArr, b.decode(bXstr));
	}
	
	@Test
	public void base64_encodeAndDecodeMultiple() {
		for (int a = 0; a < 500; ++a) {
			base64_encodeAndDecodeOnce();
		}
	}
	
	@Test
	public void base64_edgeCases() {
		BaseX b = null;
		byte[] byteArr = null;
		assertNotNull(b = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"));
		
		String[] edgeCases = new String[] { "AFvcGhtpwLHfjTWe" };
		
		for (int a = 0; a < edgeCases.length; ++a) {
			assertNotNull(byteArr = Base64.decodeBase64(edgeCases[a]));
			assertArrayEquals("For encoded string: " + edgeCases[a], byteArr, b.decode(edgeCases[a]));
			
			assertEquals(edgeCases[a], Base64.encodeBase64String(byteArr));
			assertEquals(edgeCases[a], b.encode(byteArr));
		}
	}
	
	// /
	// / Test charset
	// /
	@Test
	public void charset() {
		assertEquals(62, Base62.defaultCharSet.length());
		assertEquals(Base62.defaultCharSet, (new Base62()).charset());
	}
	
	// /
	// / random base conversion charset
	// /
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
	
	@Test
	public void encodeAndDecodeMultiple() {
		for (int a = 0; a < 500; ++a) {
			encodeAndDecodeOnce();
		}
	}
}