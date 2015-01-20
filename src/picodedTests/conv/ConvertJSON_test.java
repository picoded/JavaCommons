package picodedTests.conv;

// Target test class
import picoded.conv.ConvertJSON;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class ConvertJSON_test {
	
	HashMap<String, String> tMap;
	HashMap<String, String> tMap2;
	
	ArrayList<String> tList;
	
	String tStr;
	String tStr2;
	
	@Before
	public void setUp() {
		tMap = new HashMap<String, String>();
	}

	@After
	public void tearDown() {

	}

	///
	/// Test the following functions
	///
	/// + ConvertJSON.fromMap
	/// + ConvertJSON.fromObject
	///
	@Test
	public void basicMapConversion() {
		
		tMap.put("Hello", "WORLD");
		tMap.put("WORLD", "Hello");
		
		assertEquals("{\"Hello\":\"WORLD\",\"WORLD\":\"Hello\"}", (tStr=ConvertJSON.fromMap(tMap)) );
		assertEquals(tMap, ConvertJSON.toMap(tStr) );
		
	}
}