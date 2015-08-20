package picodedTests.struct.query.condition;

// Target test class
import picoded.struct.query.condition.*;
import picoded.struct.query.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;

///
/// Test Case for picoded.struct.query.condition.*
///
public class AllConditions_test {
	
	//
	// Test Setup
	//--------------------------------------------------------------------
	
	/// Map sample, used to setup test cases
	public Map<String,Object> sample_a = null;
	public Map<String,Object> sample_b = null;
	
	public Map<String,Object> arguments_a = null;
	public Map<String,Object> arguments_b = null;
	
	@Before
	public void setUp() {
		sample_a = new HashMap<String,Object>();
		sample_b = new HashMap<String,Object>();
		
		arguments_a = new HashMap<String,Object>();
		arguments_b = new HashMap<String,Object>();
		
		sample_a.put("hello", "world");
		sample_b.put("hello", "perfect world");
		
		arguments_a.put("my", "world");
		arguments_b.put("my", "perfect world");
	}
	
	@After
	public void tearDown() {
		
	}
	
	/// Used to pass an empty test
	@Test
	public void blankTest() {
		assertNotNull(sample_a);
	}
	
	//
	// Conditions test
	//--------------------------------------------------------------------
	
	/// Test simple equality checks
	@Test
	public void equals() {
		
		/// For example, the following SQL query will make this condition query
		///
		/// sql.query( "SELECT * FROM collection WHERE hello=:my:", { my:"world"} );
		///
		
		/// arguments_a is the argument value map
		Query cond = new Equals("hello", "my", arguments_a);
		assertNotNull(cond);
		
		//
		// Check using constructed arguments
		//
		
		/// sample_a["hello"] == arguments_a["my"]
		assertTrue( cond.test(sample_a) );
		
		/// sample_b["hello"] == arguments_a["my"]
		assertFalse( cond.test(sample_b) );
		
		//
		// Check using provided arguments
		//	
		
		/// sample_a["hello"] == arguments_b["my"]
		assertFalse( cond.test(sample_a, arguments_b) );
		
		/// sample_b["hello"] == arguments_b["my"]
		assertTrue( cond.test(sample_b, arguments_b) );
		
	}
}
