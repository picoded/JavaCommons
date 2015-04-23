package picodedTests.servlet;

// Target test class
import picoded.servlet.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

// Various utility includes
import java.util.*;
import java.lang.reflect.*;

public class RESTBuilder_test {
	
	//-------------------------------
	// Test variables
	//-------------------------------
	
	/// Base RESTBuilder object to test on, automatic setup
	protected RESTBuilder restObj = null;
	
	//-------------------------------
	// Test methods to use as "API's"
	//-------------------------------
	
	public static String hello() {
		return "world";
	}
	
	public static String echo(String a1) {
		if (a1 != null && a1.length() > 0) {
			return "echo: " + a1;
		}
		return null;
	}
	
	public static String mapTest(RESTRequest hello, Object world) {
		String res = "";
		if (hello.containsKey("hello")) {
			res += hello.get("hello").toString();
		}
		
		if (world != null) {
			res += world.toString();
		}
		
		return res;
	}
	
	//-------------------------------
	// Test setup functions
	//-------------------------------
	
	@Before
	public void setUp() {
		restObj = new RESTBuilder();
	}
	
	@After
	public void tearDown() {
		
	}
	
	//-------------------------------
	// Basic sanity test
	//-------------------------------
	
	@Test
	public void constructTest() {
		assertNotNull(restObj);
	}
	
	@Test
	public void helloMethod() {
		assertEquals("world", hello());
		
		Method m = null;
		try {
			assertNotNull(m = RESTBuilder_test.class.getMethod("hello"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertEquals("hello", m.getName());
		
		assertNotNull(restObj.apiMethod("test.hello"));
		assertNotNull(restObj.apiMethod("test.hello").setGET(this, m));
		
		assertEquals("world", restObj.apiMethod("test.hello").GET());
		assertEquals("world", restObj.apiMethod("test.hello").GET(null, "one", "two"));
	}
	
	@Test
	public void echoMethod() {
		Method m = null;
		try {
			assertNotNull(m = RESTBuilder_test.class.getMethod("echo", String.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertEquals("echo", m.getName());
		
		assertNotNull(restObj.apiMethod("test.echo"));
		assertNotNull(restObj.apiMethod("test.echo").setGET(this, m));
		
		assertEquals(null, restObj.apiMethod("test.echo").GET());
		assertEquals("echo: one", restObj.apiMethod("test.echo").GET("one"));
		assertEquals("echo: two", restObj.apiMethod("test.echo").GET("two"));
	}
	
	@Test
	public void simpleSet() {
		assertNotNull(restObj.apiMethod("test.hello").setGET(this, "hello"));
		assertEquals("world", restObj.apiMethod("test.hello").GET());
	}
	
	@Test
	public void complexRequest() {
		
		HashMap<String, String> dMap = new HashMap<String, String>();
		dMap.put("hello", "hello");
		
		assertNotNull(restObj.apiMethod("test.map").setGET(this, "mapTest").setDefaultGET(dMap, new Object[] { "world" }));
		
		assertEquals("world", restObj.apiMethod("test.map").GET());
	}
}