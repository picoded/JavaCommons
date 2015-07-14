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

public class RESTBuilder_test {
	
	//-------------------------------
	// Test variables
	//-------------------------------
	
	/// Base RESTBuilder object to test on, automatic setup
	protected RESTBuilder restObj = null;
	
	//-------------------------------
	// Test methods to use as "API's"
	//-------------------------------

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
		restObj.getNamespace("hello.world").put(RESTBuilder.RequestTypeSet.GET, helloFunction );
		
		Map<String, Object> ret = restObj.getNamespace("hello.world").call(RESTBuilder.RequestTypeSet.GET);
		
		Map<String, Object> retCheck = new HashMap<String, Object>();
		retCheck.put("hello","world");
		assertEquals( retCheck, ret );
		
		retCheck.put("echo", "echo");
		ret = restObj.getNamespace("hello.world").call(RESTBuilder.RequestTypeSet.GET, retCheck);
		assertEquals( retCheck, ret );
	}
	
}
