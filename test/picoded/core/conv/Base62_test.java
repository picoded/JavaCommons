package picoded.core.conv;

// Junit includes
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
// Apache reference

/// The actual test suite
public class Base62_test extends BaseX_test {
	
	@Before
	public void setUp() {
		baseObj = Base62.getInstance();
	}
	
	@Test
	public void validCharset() {
		baseObj = new Base62(Base62.DEFAULT_CHARSET);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalidCharset() {
		baseObj = new Base62(Base58.DEFAULT_CHARSET);
	}
	
	@Test
	public void Base62_specific() {
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
