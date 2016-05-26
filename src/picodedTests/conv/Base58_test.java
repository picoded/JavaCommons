package picodedTests.conv;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.RandomUtils;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.conv.Base58;
import picoded.conv.Base62;
import picoded.conv.BaseX;
// Classes used in test case

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class Base58_test extends Base62_test {
	
	@Before
	public void setUp() {
		baseObj = new Base58();
	}
	
	@Test
	public void guid_length_test() {
		
		int byteLen = 16; //RandomUtils.nextInt(1, 20);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		//encodeBase64String
		String b58str = null;
		String b62str = null;
		
		assertNotNull(b58str = Base58.obj.encode(byteArr));
		assertNotNull(b62str = Base62.obj.encode(byteArr));
		
		assertArrayEquals(byteArr, Base58.obj.decode(b58str, byteLen));
		assertArrayEquals(byteArr, Base62.obj.decode(b62str, byteLen));
		
		assertEquals(b58str.length(), b62str.length());
	}
	
	@Test
	public void guid_range_test() {
		String b64Range = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		
		int max = 64;
		int min = 57;
		
		for (int i = max; i >= min; --i) {
			BaseX b = new BaseX(b64Range.substring(0, i));
			assertEquals("GUID range test failed, when X = " + i, 22, b.bitToStringLength(128));
		}
	}
	
	@Test
	public void base58and62_sha1_test() {
		assertEquals(27, Base58.obj.bitToStringLength(160)-1);
		assertEquals(27, Base62.obj.bitToStringLength(160));
	}
}