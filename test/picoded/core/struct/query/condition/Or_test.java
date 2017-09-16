package picoded.core.struct.query.condition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.query.Query;
import picoded.core.struct.query.QueryType;

public class Or_test {
	
	private Or or = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(or);
	}
	
	@Test
	public void typeTest() {
		or = construct();
		assertEquals(QueryType.OR, or.type());
	}
	
	@Test
	public void operatorSymbolTest() {
		or = construct();
		assertEquals("OR", or.operatorSymbol());
	}
	
	@Test
	public void testTest() {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Query> childQuery = new ArrayList<Query>();
		CombinationBase combinationBase = new CombinationBase(childQuery, map);
		Query leftQuery = new Or(childQuery, map);
		Map<String, Object> sample_a = new HashMap<String, Object>();
		sample_a.put("hello", "world");
		sample_a.put("my", "perfect world");
		Query rightQuery = new Like("hello", "world", sample_a);
		Map<String, Object> defaultArgMap = new HashMap<>();
		defaultArgMap.put("hello", "world");
		or = new Or(leftQuery, rightQuery, defaultArgMap);
		assertFalse(or.test("hello", defaultArgMap));
		
	}
	
	private Or construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new Or(Arrays.asList(new Query[] { new Equals("hello", "hello", defaultArgMap),
			new Equals("my", "my", defaultArgMap) }), defaultArgMap);
	}
}
