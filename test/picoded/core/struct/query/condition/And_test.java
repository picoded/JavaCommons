package picoded.core.struct.query.condition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.query.Query;
import picoded.core.struct.query.QueryType;

public class And_test {
	
	private And and = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(and);
	}
	
	@Test
	public void andTest() {
		Map<String, Object> sample_a = new HashMap<String, Object>();
		sample_a.put("hello", "world");
		sample_a.put("my", "perfect world");
		Query query = new NotEquals("key", "key_hello", sample_a);
		List<Query> childQuery = new ArrayList<Query>();
		Query cond = new Not(childQuery, sample_a);
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		and = new And(query, cond, defaultArgMap);
		assertNotNull(and);
		assertEquals(QueryType.AND, and.type());
	}
	
}
