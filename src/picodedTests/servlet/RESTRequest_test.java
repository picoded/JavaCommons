package picodedTests.servlet;

// Target test class
import picoded.servlet.RESTRequest;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

// Various utility includes
import java.util.*;
import java.lang.reflect.*;

public class RESTRequest_test {
	
	//-------------------------------
	// Test variables
	//-------------------------------
	
	/// Base RESTBuilder object to test on, automatic setup
	protected RESTRequest restObj = null;
	
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
		//restObj = new RESTRequest();
	}
	
	@After
	public void tearDown() {
		
	}
	
	//-------------------------------
	// Basic sanity test
	//-------------------------------
	@Test
	public void helloMethod() {
		Method m = null;
		try {
			assertNotNull(m = RESTRequest_test.class.getMethod("hello"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(restObj = new RESTRequest(this, m, false, null, null, null, null));
		assertEquals("world", restObj.call());
	}
	
	@Test
	public void echoMethod() {
		Method m = null;
		try {
			assertNotNull(m = RESTRequest_test.class.getMethod("echo", String.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(restObj = new RESTRequest(this, m, false, null, new Object[] { "one" }, null, null));
		assertEquals("echo: one", restObj.call());
		
		assertNotNull(restObj = new RESTRequest(this, m, false, null, new Object[] { "two" }, null, null));
		assertEquals("echo: two", restObj.call());
	}
}