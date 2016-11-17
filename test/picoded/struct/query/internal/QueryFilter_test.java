package picoded.struct.query.internal;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.struct.MutablePair;
import picoded.struct.query.Query;
import picoded.struct.query.condition.LessThan;
import picoded.struct.query.condition.LessThanOrEquals;
import picoded.struct.query.condition.Like;
import picoded.struct.query.condition.MoreThan;
import picoded.struct.query.condition.MoreThanOrEquals;
import picoded.struct.query.condition.NotEquals;
import picoded.struct.query.internal.QueryFilter;

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
	
	@Test
	public void filterQueryArguments() {
		MutablePair<String, Integer> res = null;
		
		assertNotNull(res = QueryFilter.filterQueryArguments("A = ? AND B = ?"));
		assertEquals("A = :0 AND B = :1", res.getLeft());
		assertEquals(2, res.getRight().intValue());
	}
	
	@Test
	public void argumentsArrayToMap() {
		Map<String, Object> ref = new HashMap<String, Object>();
		ref.put("0", "|=");
		ref.put("1", "$=");
		
		assertEquals(ref, QueryFilter.argumentsArrayToMap(null, new Object[] { "|=", "$=" }));
	}
	
	@Test
	public void enforceRequiredWhitespace() {
		assertEquals("A <= :0 AND B >= :1 AND C != :2 AND ( D < :3 AND E = :4 )",
			QueryFilter.enforceRequiredWhitespace("A<=:0  AND  B>=:1 AND C!=:2 AND (D<:3 AND E=:4)"));
	}
	
	@Test
	public void QueryFilterTest() {
		assertNotNull(new QueryFilter());
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensInvalidTest() {
		assertNotNull(QueryFilter.basicQueryFromTokens(null, "", "", ""));
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensInvalid1Test() {
		assertNotNull(QueryFilter.basicQueryFromTokens(null, "", "", null));
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensInvalid2Test() {
		assertNotNull(QueryFilter.basicQueryFromTokens(null, "", "", "abc"));
	}
	
	@Test
	public void basicQueryFromTokensTest() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<", ":abc"));
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<=", ":abc"));
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", ">", ":abc"));
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", ">=", ":abc"));
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "LIKE", ":abc"));
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "!=", ":abc"));
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensExceptionTest() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<<", ":abc"));
	}
	
	@Test
	public void collapseQueryTokensWithoutBracketsTest() {
		
	}
	
	@Test
	public void combinationQueryTest() {
		String combinationType = "AND";
		List<Query> childQuery = new ArrayList<Query>();
		Query query = Query.build("me = :good AND life = :awsome AND new = :world");
		childQuery.add(query);
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.combinationQuery(combinationType, childQuery, paramsMap));
		combinationType = "OR";
		assertNotNull(QueryFilter.combinationQuery(combinationType, childQuery, paramsMap));
		combinationType = "NOT";
		assertNotNull(QueryFilter.combinationQuery(combinationType, childQuery, paramsMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void combinationQueryInvalidTest() {
		String combinationType = "EQUAL";
		List<Query> childQuery = new ArrayList<Query>();
		Query query = Query.build("me = :good AND life = :awsome AND new = :world");
		childQuery.add(query);
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.combinationQuery(combinationType, childQuery, paramsMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void refactorQueryTest() {
		String query = "me = :good AND life = :awsome AND new = :world";
		Map<String, Object> baseMap = new HashMap<>();
		String[] argArr = new String[] { "a" };
		assertNotNull(QueryFilter.refactorQuery(query, baseMap, argArr));
	}
}
