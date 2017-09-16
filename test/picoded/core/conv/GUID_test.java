package picoded.core.conv;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

// Java libs used
import java.util.UUID;

///
/// Test Case for picoded.core.struct.CaseInsensitiveHashMap
///
public class GUID_test {
	
	// Test run multiplier
	protected int testRunMultiplier = 5000;
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new GUID();
	}
	
	///
	/// GUID basic tests
	///
	@Test
	public void guidTestSet() {
		
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
		
		// base58 test
		assertNotNull(GUID.base58());
		assertNotNull(GUID.base58(u));
		assertEquals(u, GUID.fromBase58(GUID.base58(u)));
	}
	
	@Test
	public void guidTestMultiple() {
		for (int a = 0; a < testRunMultiplier; ++a) {
			guidTestSet();
		}
	}
	
}
