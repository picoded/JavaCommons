package picodedTests.struct;

// Target test class
import picoded.struct.CaseInsensitiveHashMap;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class CaseInsensitiveHashMap_test {

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/// Test the following functions
	///
	/// + CaseInsensitiveHashMap.put
	/// + CaseInsensitiveHashMap.get
	@Test
	public void basicPutAndGet() {
		CaseInsensitiveHashMap<String, String> tObj = new CaseInsensitiveHashMap<String, String>();

		assertNull(tObj.put("TEST", "a"));
		assertNull(tObj.put("Hello", "WORLD"));

		assertEquals("a", tObj.put("test", "WORLD"));

		assertEquals("WORLD", tObj.get("tEsT"));
		assertEquals("WORLD", tObj.get("hello"));
	}
}