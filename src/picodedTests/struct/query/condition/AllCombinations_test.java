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
public class AllCombinations_test {
	
	//
	// Test Setup
	//--------------------------------------------------------------------
	
	/// Map sample, used to setup test cases
	public Map<String, Object> sample_a = null;
	public Map<String, Object> sample_b = null;
	
	public Map<String, Object> arguments_a = null;
	public Map<String, Object> arguments_b = null;
	
	@Before
	public void setUp() {
		sample_a = new HashMap<String, Object>();
		sample_b = new HashMap<String, Object>();
		
		arguments_a = new HashMap<String, Object>();
		arguments_b = new HashMap<String, Object>();
		
		sample_a.put("hello", "world");
		sample_a.put("my", "perfect world");
		
		sample_b.put("hello", "world");
		sample_b.put("my", "imperfect world");
		
		arguments_a.put("hello", "world");
		arguments_a.put("my", "perfect world");
		
		arguments_b.put("hello", "world");
		arguments_b.put("my", "imperfect world");
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
		
		Query cond = new And(Arrays.asList(new Query[] { new Equals("hello", "hello", arguments_a),
			new Equals("my", "my", arguments_a) }), arguments_a);
		
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		assertFalse(cond.test(sample_a, arguments_b));
		assertTrue(cond.test(sample_b, arguments_b));
		
		assertEquals("\"hello\" = :hello AND \"my\" = :my", cond.toString());
	}
}
