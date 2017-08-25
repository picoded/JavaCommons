package picoded.core.struct.query.condition;

// Target test class
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.query.Query;
import picoded.core.struct.query.condition.Equals;
import picoded.core.struct.query.condition.LessThan;
import picoded.core.struct.query.condition.LessThanOrEquals;
import picoded.core.struct.query.condition.Like;
import picoded.core.struct.query.condition.MoreThan;
import picoded.core.struct.query.condition.MoreThanOrEquals;

///
/// Test Case for picoded.core.struct.query.condition.*
///
public class AllConditions_test {
	
	//
	// Test Setup
	//--------------------------------------------------------------------
	
	/// Map sample, used to setup test cases
	public Map<String, Object> sample_a = null;
	public Map<String, Object> sample_b = null;
	public Map<String, Object> sample_c = null;
	public Map<String, Object> sample_d = null;
	
	@Before
	public void setUp() {
		sample_a = new HashMap<String, Object>();
		sample_b = new HashMap<String, Object>();
		sample_c = new HashMap<String, Object>();
		sample_d = new HashMap<String, Object>();
		
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
		
		/// Note atleast 1 side should be "numeric", 
		/// for numeric compare to trigger
		sample_c.put("my", "world");
		sample_c.put("int", new Integer(5));
		sample_c.put("double", new Double(5.55));
		sample_c.put("float", new Float(5.5));
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
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		assertFalse(cond.test(sample_a, sample_d));
		assertTrue(cond.test(sample_b, sample_d));
		
	}
	
	@Test
	public void lessThan() {
		
		//if a string starts with a number, number parse will work
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
	
	@Test
	public void moreThan() {
		
		//if a string starts with a number, number parse will work
		Query cond = new MoreThan("int", "int", sample_c);
		assertNotNull(cond);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		
		cond = new MoreThan("double", "double", sample_c);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		
		cond = new MoreThan("float", "float", sample_c);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		
		cond = new MoreThan("string", "string", sample_c);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
	}
	
	public void setupEqualitySamples() {
		
		sample_a = new HashMap<String, Object>();
		sample_b = new HashMap<String, Object>();
		sample_c = new HashMap<String, Object>();
		sample_d = new HashMap<String, Object>();
		
		sample_a.put("int", 4);
		sample_a.put("double", 4.99);
		sample_a.put("float", 4.9);
		sample_a.put("string", "aacde");
		sample_a.put("stringNumber", "4.9");
		
		sample_b.put("int", 5);
		sample_b.put("double", 5.00);
		sample_b.put("float", 5.00);
		sample_b.put("string", "abcde");
		sample_b.put("stringNumber", "5");
		
		sample_c.put("int", 6);
		sample_c.put("double", 5.01);
		sample_c.put("float", 5.1);
		sample_c.put("string", "accde");
		sample_c.put("stringNumber", "5.11");
		
		sample_d.put("int", 5);
		sample_d.put("double", 5.00);
		sample_d.put("float", 5.0);
		sample_d.put("string", "abcde");
		sample_d.put("stringNumber", "5");
		
	}
	
	@Test
	public void lessThanOrEquals() {
		setupEqualitySamples();
		
		//if a string starts with a number, number parse will work
		Query cond = new LessThanOrEquals("int", "int", sample_d);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		
		cond = new LessThanOrEquals("double", "double", sample_d);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		
		cond = new LessThanOrEquals("float", "float", sample_d);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		
		cond = new LessThanOrEquals("string", "string", sample_d);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		
		cond = new LessThanOrEquals("stringNumber", "stringNumber", sample_d);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
	}
	
	@Test
	public void moreThanOrEquals() {
		setupEqualitySamples();
		
		//if a string starts with a number, number parse will work
		Query cond = new MoreThanOrEquals("int", "int", sample_d);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_c));
		
		cond = new MoreThanOrEquals("double", "double", sample_d);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_c));
		
		cond = new MoreThanOrEquals("float", "float", sample_d);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_c));
		
		cond = new MoreThanOrEquals("string", "string", sample_d);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_c));
		
		cond = new MoreThanOrEquals("stringNumber", "stringNumber", sample_d);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_c));
	}
	
	@Test
	public void like() {
		Query cond = new Like("hello", "my", sample_c);
		
		assertNotNull(cond);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		assertFalse(cond.test(sample_a, sample_d));
		assertTrue(cond.test(sample_b, sample_d));
		
	}
	
	@Test
	public void likeEquality() {
		setupEqualitySamples();
		
		//if a string starts with a number, number parse will work
		Map<String, Object> testFields = new HashMap<String, Object>();
		testFields.put("abcde", "abcde");
		testFields.put("ab%", "ab%");
		testFields.put("%bcde", "%bcde");
		testFields.put("%bcd%", "%bcd%");
		
		Query cond = new Like("string", "abcde", testFields);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		assertTrue(cond.test(sample_d));
		
		cond = new Like("string", "ab%", testFields);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		assertTrue(cond.test(sample_d));
		
		cond = new Like("string", "%bcde", testFields);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		assertTrue(cond.test(sample_d));
		
		cond = new Like("string", "%bcd%", testFields);
		assertFalse(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertFalse(cond.test(sample_c));
		assertTrue(cond.test(sample_d));
		
	}
}
