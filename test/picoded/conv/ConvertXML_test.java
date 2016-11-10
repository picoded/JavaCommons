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
	
	static String shortMochaTestXml = ""+
		"<testsuite name=\"Mocha Tests\" tests=\"1\" failures=\"1\" errors=\"1\" skipped=\"0\" timestamp=\"Mon, 07 Nov 2016 10:01:53 GMT\" time=\"3.35\">"+
			"<testcase classname=\"feature-test\" name=\"scenario-test\" time=\"3.342\">"+
				"<failure>Unexpected failure</failure>"+
			"</testcase>"+
		"</testsuite>";
	
	@Test
	@SuppressWarnings("unchecked")
	public void nestedMapConversion() {
		Map<String,Object> testsuite = ConvertXML.toMap(shortMochaTestXml);
		assertNotNull(testsuite);
		
		assertNotNull( testsuite.get("ChildNodes") );
		ArrayList<Object> testcaseList = (ArrayList<Object>)(testsuite.get("ChildNodes"));
		
		assertEquals( 1, testcaseList.size() );
		Map<String,Object> testcase = (Map<String,Object>)testcaseList.get(0);
		
		assertEquals( "scenario-test", testcase.get("name") );
		ArrayList<Object> failureList = (ArrayList<Object>)(testcase.get("ChildNodes"));
		
		assertEquals( 1, failureList.size() );
		Map<String,Object> failure = (Map<String,Object>)failureList.get(0);
		
		assertEquals( "failure", failure.get("TagName") );
		assertEquals( "Unexpected failure", failure.get("NodeValue") );
	}
	
	@Test
	public void mapCollapse() {
		Map<String, Object> collapsedMap = ConvertXML.toCollapsedMap(shortMochaTestXml);
		assertNotNull(collapsedMap);
		
		// @TODO more extensive testing
		//assertEquals("", collapsedMap);
	}
}
