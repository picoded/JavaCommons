package picoded.core.struct.query.condition;

// Target test class

// Test Case include
import org.junit.*;

import picoded.core.struct.query.Query;
import picoded.core.struct.query.QueryType;
import static org.junit.Assert.*;

import java.util.*;

///
/// Test Case for picoded.core.struct.query.condition.*
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
		arguments_a.put("key_hello", "hello");
		
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
	
	/// Test key / value based search
	@Test
	public void keyValueMultipleSearch() {
		Query cond = new Equals("key", "key_hello", arguments_a);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		
		cond = new Equals("key", "hello", arguments_a);
		assertFalse(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		
		cond = new Equals("val", "my", arguments_a);
		assertTrue(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
	}
	
	/// Test simple equality checks
	@Test
	public void notEqualsTest() {
		
		Query cond = new NotEquals("key", "key_hello", arguments_a);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		
		cond = new NotEquals("hello", "hello", arguments_a);
		assertFalse(cond.test(sample_a));
	}
	
	@Test
	public void notTest() {
		List<Query> child = new ArrayList<Query>();
		Query query = new NotEquals("key", "key_hello", arguments_a);
		child.add(query);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "key_hello");
		Query cond = new Not(child, map);
		assertTrue(cond.test(sample_a));
		assertTrue(cond.test(sample_b));
		assertTrue(cond.test(sample_b, map));
	}
	
	@Test
	public void notAlternatePathTest() {
		List<Query> child = new ArrayList<Query>();
		Query query = new NotEquals("key", "key_hello", arguments_a);
		child.add(query);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "key_hello");
		map.put("key_hello", "key_hello");
		Query cond = new Not(child, map);
		assertFalse(cond.test(sample_a));
		assertFalse(cond.test(sample_b));
		assertFalse(cond.test(sample_b, map));
	}
	
	@Test
	public void CombinationBaseTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, defaultArgMap);
		assertNotNull(combinationBase);
	}
	
	@Test
	public void CombinationBaseThreeParamsTest() {
		CombinationBase combinationBase = new CombinationBase(null, null, null);
		assertNotNull(combinationBase);
		Query rightQuery = new NotEquals("key", "key_hello", arguments_a);
		Query leftQuery = new NotEquals("key", "key_hello", arguments_a);
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		combinationBase = new CombinationBase(leftQuery, rightQuery, defaultArgMap);
	}
	
	@Test
	public void childrenQueryTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, defaultArgMap);
		assertNotNull(combinationBase.childrenQuery());
	}
	
	@Test
	public void keyValuesMapTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Query query = new NotEquals("key", "key_hello", arguments_a);
		childQuery.add(query);
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, defaultArgMap);
		assertNotNull(combinationBase.keyValuesMap());
	}
	
	@Test
	public void operatorSymbolTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, defaultArgMap);
		assertEquals("AND", combinationBase.operatorSymbol());
	}
	
	@Test
	public void typeTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, defaultArgMap);
		assertEquals(QueryType.AND, combinationBase.type());
	}
	
	@Test
	public void testTest() {
		List<Query> childQuery = new ArrayList<Query>();
		Query query = new NotEquals("key", "key_hello", arguments_a);
		Map<String, Object> map = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, map);
		Query cond = new Not(childQuery, map);
		assertFalse(combinationBase.test(cond, map));
		childQuery.add(query);
		map.put("key", "key_hello");
		combinationBase = new CombinationBase(childQuery, map);
		assertFalse(combinationBase.test(null, map));
		//assertTrue(combinationBase.test("key", map));
	}
	
	@Test
	public void toStringTest() {
		Query query = new Or(Arrays.asList(new Query[] { new Equals("hello", "hello", arguments_a),
			new Equals("my", "my", arguments_a) }), arguments_a);
		List<Query> childQuery = new ArrayList<Query>();
		childQuery.add(query);
		Map<String, Object> map = new HashMap<String, Object>();
		CombinationBase combinationBase = new CombinationBase(childQuery, map);
		assertEquals("(\"hello\" = :hello OR \"my\" = :my)", combinationBase.toString());
	}
}
