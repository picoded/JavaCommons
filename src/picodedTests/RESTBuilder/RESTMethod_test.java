package picodedTests.RESTBuilder;

// Target test class
import picoded.RESTBuilder.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

// Various utility includes
import java.util.*;
import java.lang.reflect.*;

import picoded.enums.HttpRequestType;

public class RESTMethod_test {
	
	//-------------------------------
	// Test variables
	//-------------------------------
	
	/// Base RESTBuilder object to test on, automatic setup
	protected RESTMethod restObj = null;

	/// Test hello echo function
	protected RESTFunction helloFunction = (req,res) -> {
		res.put("hello","world");
		
		if( req.get("echo") != null ) {
			res.put( "echo", req.get("echo") );
		}
		
		return res;
	};
	
	//-------------------------------
	// Test setup functions
	//-------------------------------
	
	@Before
	public void setUp() {
		restObj = new RESTMethod("test");
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
	public void equalityTest() {
		HttpRequestType a = RESTBuilder.RequestType.GET;
		HttpRequestType b = RESTBuilder.RequestType.GET;
		HttpRequestType c = RESTBuilder.RequestType.POST;
		
		assertEquals(a, b);
		assertNotEquals(a, c);
	}
	
	@Test
	public void helloMethod() {
		restObj.put(RESTBuilder.RequestType.GET, helloFunction );
		
		Map<String, Object> ret = restObj.call(RESTBuilder.RequestType.GET);
		
		Map<String, Object> retCheck = new HashMap<String, Object>();
		retCheck.put("hello","world");
		assertEquals( retCheck, ret );
		
		retCheck.put("echo", "echo");
		ret = restObj.call(RESTBuilder.RequestType.GET, retCheck);
		assertEquals( retCheck, ret );
	}
	
}
