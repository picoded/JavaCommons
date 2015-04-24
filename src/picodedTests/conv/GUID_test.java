package picodedTests.conv;

// Target test class
import picoded.conv.GUID;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class GUID_test {
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	///
	/// GUID basic tests
	///
	@Test
	public void basicTests() {
		
		// basic test
		UUID u = null;
		assertNotNull(u = GUID.randomUUID());
		
		// long pair test
		assertNotNull(GUID.longPair());
		assertNotNull(GUID.longPair(u));
		assertEquals(u, GUID.fromLongPair(GUID.longPair(u)));
		
		// guid collision test
		assertNotEquals(GUID.randomUUID(), GUID.fromLongPair(GUID.longPair(u)));
		
		// byte array test
		assertNotNull(GUID.byteArray());
		assertNotNull(GUID.byteArray(u));
		assertEquals(u, GUID.fromByteArray(GUID.byteArray(u)));
		
		// base64 test
		assertNotNull(GUID.base64());
		assertNotNull(GUID.base64(u));
		assertEquals(u, GUID.fromBase64(GUID.base64(u)));
		;
		
		// base58 test
		assertNotNull(GUID.base58());
		assertNotNull(GUID.base58(u));
		assertEquals(u, GUID.fromBase58(GUID.base58(u)));
	}
}