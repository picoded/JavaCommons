package picoded.conv;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

// Apache reference
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;

/// The actual test suite
public class Base62_test extends BaseX_test {
	
	@Before
	public void setUp() {
		baseObj = new Base62();
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
	
	///
	/// Test charset
	///
	@Test
	public void charset() {
		assertEquals(62, Base62.DEFAULT_CHARSET.length());
		assertEquals(Base62.DEFAULT_CHARSET, (new Base62()).charset());
	}
}
