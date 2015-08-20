package picodedTests.struct.query.internal;

// Target test class
import picoded.struct.query.condition.*;
import picoded.struct.query.internal.*;
import picoded.struct.query.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import java.struct.*;

///
/// Test Case for picoded.struct.query.condition.*
///
public class QueryFilter_test {
	
	//
	// Test Setup
	//--------------------------------------------------------------------
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	/// Used to pass an empty test
	@Test
	public void blankTest() {
		assertNotNull("");
	}
	
	//
	// Conditions test
	//--------------------------------------------------------------------
	
	/// Test simple equality checks
	@Test
	public void equals() {
		MutablePair<String,Integer> res = null;
		
	}
}
