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
		sample_a.put("int", 3);
		sample_a.put("double", "3.33");
		sample_a.put("float", "3.3");
		sample_a.put("string", "abc");
		
		sample_b.put("hello", "perfect world");
		sample_b.put("int", 10);
		sample_b.put("double", "10.22");
		sample_b.put("float", "10.5");
		sample_b.put("string", "bdc");
		
		sample_c.put("my", "world");
		sample_c.put("int", 5);
		sample_c.put("double", "5.55");
		sample_c.put("float", "5.5");
		sample_c.put("string", "bcd");
		
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
	
	@Test
	public void lessThan(){
		
		Query cond = new LessThan("int", "int", sample_c);
		assertNotNull(cond);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		cond = new LessThan("double", "double", sample_c);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		cond = new LessThan("float", "float", sample_c);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		cond = new LessThan("string", "string", sample_c);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
	}
}
