package picodedTests.RESTBuilder;

// Target test class
import picoded.RESTBuilder.RESTRequest;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

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
	// Test setup functions
	//-------------------------------
	
	@Before
	public void setUp() {
		restObj = new RESTRequest();
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
	
//	@Test
	public void namespaceTest(){
		//if the values seem weird, its because im replicating the values i had when testing account login
		//also i had to change it from protected to public to test, and ive changed them back to protected so i cant test them.
		//but it works now. >_> 
		
//		restObj.registeredNamespace = new String[]{"account", "members", "list", "*"};
//		restObj.rawRequestNamespace = new String[]{"account", "members", "list", "GroupA"};
//		
//		String[] wildcardNamespace = restObj.wildCardNamespace();
//		assertNotNull(wildcardNamespace);
	}
	
}
