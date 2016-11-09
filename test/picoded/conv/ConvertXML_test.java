package picoded.conv;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

// Java libs used
import java.util.*;

// Apache lib used
import org.apache.commons.lang3.ArrayUtils;

///
/// Test Case for picoded.conv.ConvertJSON
///
public class ConvertXML_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	//
	// Test the conversions
	//
	
	@Test
	public void basicMapConversion() {
		String xml = "<hello mood='good'>world</hello>";
		
		Map<String,Object> res = ConvertXML.toMap(xml);
		assertNotNull(res);
		
		Map<String,Object> exp = new HashMap<String,Object>();
		exp.put("NodeValue", "world");
		exp.put("mood","good");
		exp.put("TagName","hello");
		
		assertEquals(exp, res);
	}
	
}
