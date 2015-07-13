package picodedTests.webTemplateEngines;

// Target test class
import picoded.webTemplateEngines.*;
import picoded.conv.ConvertJSON;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// java includes
import java.util.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class FormGenerator_test {
	
	public FormGenerator testObj = null;
	
	@Before
	public void setUp() {
		testObj = new FormGenerator();
	}
	
	@After
	public void tearDown() {
		testObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(testObj);
	}
	
	@Test
	public void simpleDiv() {
		assertEquals("<div></div>", FormGenerator.htmlNodeGenerator("div", null, null, null));
		assertEquals("<h1>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, null, "hello world"));
		
		assertEquals("<h1 type='hidden'>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, ConvertJSON.toMap("{ \"type\":\"hidden\" }"), "hello world"));
	}
	
	public void basicObjectTests() {
		Map<String,Object> format = new HashMap<String,Object>();
		
		format.put("type", "title");
		assertEquals("<h3 class=\"title\">title</h3>", FormGenerator.templateObjectToHtml(format));
		
		format.put("type", "hello world");
		assertEquals("<h3 class=\"title\">hello world</h3>", FormGenerator.templateObjectToHtml(format));
		
	}
}
