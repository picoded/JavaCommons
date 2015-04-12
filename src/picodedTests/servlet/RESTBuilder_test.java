package picodedTests.servlet;

// Target test class
import picoded.servlet.RESTBuilder;

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
		
		assertNotNull(restObj.apiMethod("test.hello"));
		assertNotNull(restObj.apiMethod("test.hello").setGet(this, m));
		
		assertEquals("world", restObj.apiMethod("test.hello").get());
	}
	
}