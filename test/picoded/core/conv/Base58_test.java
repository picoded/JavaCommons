package picoded.core.conv;

// Junit includes
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
// Apache reference

///
/// Test Case for picoded.core.struct.CaseInsensitiveHashMap
///
public class Base58_test extends BaseX_test {
	
	@Before
	public void setUp() {
		baseObj = Base58.getInstance();
	}
	
	@Test
	public void validCharset() {
		baseObj = new Base58(Base58.DEFAULT_CHARSET);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalidCharset() {
		baseObj = new Base58(Base62.DEFAULT_CHARSET);
	}
	
	@Test
	public void guid_length_test() {
		
		int byteLen = 16; //RandomUtils.nextInt(1, 20);
		
		// raw byteArray to encode
		byte[] byteArr = RandomUtils.nextBytes(byteLen);
		
		//encodeBase64String
		String b58str = null;
		String b62str = null;
		
		assertNotNull(b58str = Base58.getInstance().encode(byteArr));
		assertNotNull(b62str = Base62.getInstance().encode(byteArr));
		
		assertArrayEquals(byteArr, Base58.getInstance().decode(b58str, byteLen));
		assertArrayEquals(byteArr, Base62.getInstance().decode(b62str, byteLen));
		
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
	public void Base58and62_string_test() {
		assertEquals(27, Base58.getInstance().bitToStringLength(160) - 1);
		assertEquals(27, Base62.getInstance().bitToStringLength(160));
	}
}
