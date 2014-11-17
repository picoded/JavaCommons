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
	
	private CaseInsensitiveHashMap<String,String> tObj = null;
	
	@Before
	public void setUp() {
		tObj = new CaseInsensitiveHashMap<String,String>();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void basicPutAndGet() {
		tObj.put("TEST", "a");
		tObj.put("Hello", "WORLD");
		tObj.put("test", "WORLD");
		
		assertEquals( "WORLD" , tObj.get("tEsT") );
		assertEquals( "WORLD" , tObj.get("hello") );
	}
}