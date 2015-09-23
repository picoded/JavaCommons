package picodedTests.struct;

// Target test class
import picoded.JStruct.*;
import picoded.struct.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// other includes
import java.util.*;

///
/// Test Case for picoded.struct.LayeredMap
///
public class LayeredMap_test {
	
	public Map<String, Object> layer1 = null;
	public Map<String, Object> layer2 = null;
	public Map<String, Object> layer3 = null;
	
	public LayeredMap<String, Object> lmObj = null;
	
	@Before
	public void setUp() {
		lmObj = new LayeredMap<String, Object>();
		
		layer1 = new HashMap<String, Object>();
		layer2 = new HashMap<String, Object>();
		layer3 = new HashMap<String, Object>();
		
		lmObj.layers().add(layer1);
		lmObj.layers().add(layer2);
		lmObj.layers().add(layer3);
	}
	
	@After
	public void tearDown() {
		lmObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(lmObj);
	}
	
	@Test
	public void newObjectEqualitySanityCheck() {
		assertNotEquals(new Object(), new Object());
	}
	
	/// Test a simple get, not overlapping
	@Test
	public void basicGet() {
		assertEquals(3, lmObj.layers().size());
		
		// How one should move forward...
		layer1.put("live", "Believe.");
		layer2.put("your", "Achieve.");
		layer3.put("life", "Receive.");
		
		assertEquals("Believe.", lmObj.get("live"));
		assertEquals("Achieve.", lmObj.get("your"));
		assertEquals("Receive.", lmObj.get("life"));
		
		assertEquals(3, lmObj.keySet().size());
	}
	
	/// Test an overlaping get
	@Test
	public void layeredGet() {
		assertEquals(3, lmObj.layers().size());
		
		// What you shouldnt be thinking...
		layer2.put("It is", "unknown");
		layer3.put("It is", "impossible");
		assertEquals("unknown", lmObj.get("It is"));
		
		// What you should believe instead...
		layer1.put("It is", "possible");
		assertEquals("possible", lmObj.get("It is"));
	}
	
	/// Test basic put to all layers
	@Test
	public void basicPut() {
		assertEquals(3, lmObj.layers().size());
		
		// If your following a dream....
		assertNull(lmObj.put("Focus", "Focus"));
		assertEquals("Focus", layer1.get("Focus"));
		assertEquals("Focus", layer2.get("Focus"));
		assertEquals("Focus", layer3.get("Focus"));
	}
}
