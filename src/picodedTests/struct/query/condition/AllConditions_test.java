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
	public Map<String,Object> sample_c = null;
	public Map<String,Object> sample_d = null;
	
	@Before
	public void setUp() {
		sample_a = new HashMap<String,Object>();
		sample_b = new HashMap<String,Object>();
		sample_c = new HashMap<String,Object>();
		sample_d = new HashMap<String,Object>();
		
		sample_a.put("hello", "world");
		sample_b.put("hello", "perfect world");
		
		sample_c.put("my", "world");
		sample_d.put("my", "perfect world");
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
		Query cond = new Equals("hello", "my", sample_c);
		
		assertNotNull(cond);
		assertTrue( cond.test(sample_a) );
		assertFalse( cond.test(sample_b) );
		
		assertFalse( cond.test(sample_a, sample_d) );
		assertTrue( cond.test(sample_b, sample_d) );
		
	}
}
