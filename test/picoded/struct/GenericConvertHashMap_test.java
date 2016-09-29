package picoded.struct;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class GenericConvertHashMap_test {
	UnsupportedDefaultMap<String, String> genericConvertHashMap = null;
	
	@Before
	public void setUp() {
		genericConvertHashMap = new GenericConvertHashMap<>();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test 
	public void getTest() {
		assertNull(genericConvertHashMap.get("key"));
	}
	
	@Test 
	public void putTest() {
		genericConvertHashMap.put("key", "value");
		assertEquals(1, genericConvertHashMap.size());
	}
}
