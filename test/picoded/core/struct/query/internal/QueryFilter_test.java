package picoded.core.struct.query.internal;

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

import picoded.core.struct.MutablePair;
import picoded.core.struct.query.Query;
import picoded.core.struct.query.condition.Equals;
import picoded.core.struct.query.internal.QueryFilter;

///
/// Test Case for picoded.core.struct.query.condition.*
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
	public void basicQueryFromTokensInvalid4Test() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<", null));
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensInvalid5Test() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<", "abc"));
	}
	
	@Test(expected = RuntimeException.class)
	public void basicQueryFromTokensExceptionTest() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.basicQueryFromTokens(paramsMap, "abc", "<<", ":abc"));
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
	
	@Test(expected = RuntimeException.class)
	public void findCompleteEnclosureInvalidClosingBracketTest() {
		List<Object> list = new ArrayList<Object>();
		list.add(")");
		assertNotNull(QueryFilter.findCompleteEnclosure(list));
	}
	
	@Test(expected = RuntimeException.class)
	public void findCompleteEnclosureInvalidStartBracketTest() {
		List<Object> list = new ArrayList<Object>();
		list.add("(");
		assertNotNull(QueryFilter.findCompleteEnclosure(list));
	}
	
	@Test(expected = IllegalAccessError.class)
	public void QueryFilterTest() {
		assertNotNull(new QueryFilter());
	}
	
	@Test(expected = RuntimeException.class)
	public void collapseQueryTokensWithoutBracketsInvalidTest() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		tokens.add(new StringBuilder("A = ? AND B = ?"));
		assertNotNull(QueryFilter.collapseQueryTokensWithoutBrackets(tokens, paramMap));
		
	}
	
	@Test
	public void collapseQueryTokensWithoutBracketsTest() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		tokens.add(new Equals("key", "key_hello", paramMap));
		tokens.add("AND");
		tokens.add(new Equals("key1", "key1_hello", paramMap));
		tokens.add("OR");
		tokens.add(new Equals("key2", "key2_hello", paramMap));
		assertNotNull(QueryFilter.collapseQueryTokensWithoutBrackets(tokens, paramMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void collapseQueryTokensWithoutBracketsInvalidOperationTest() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		tokens.add("AND");
		tokens.add("OR");
		assertNotNull(QueryFilter.collapseQueryTokensWithoutBrackets(tokens, paramMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void collapseQueryTokensWithoutBracketsNullCombinationTypeOperationTest() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		assertNotNull(QueryFilter.collapseQueryTokensWithoutBrackets(tokens, paramMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void collapseQueryTokensWithoutBracketsWrongOperationTest() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		tokens.add(new Equals("key", "key_hello", paramMap));
		tokens.add("ANDA");
		tokens.add(new Equals("key1", "key1_hello", paramMap));
		assertNotNull(QueryFilter.collapseQueryTokensWithoutBrackets(tokens, paramMap));
	}
	
	@Test(expected = RuntimeException.class)
	public void processCombinationTypeInvalidTest() {
		assertNotNull(QueryFilter.processCombinationType(null, null, null));
	}
	
	@Test(expected = RuntimeException.class)
	public void processCombinationTypeInvalid1Test() {
		List<Object> tokens = new ArrayList<Object>();
		assertNotNull(QueryFilter.processCombinationType(tokens, null, null));
	}
	
	@Test(expected = RuntimeException.class)
	public void processCombinationTypeInvalid2Test() {
		List<Object> tokens = new ArrayList<Object>();
		List<Query> child = new ArrayList<Query>();
		assertNotNull(QueryFilter.processCombinationType(tokens, null, child));
	}
	
	@Test(expected = RuntimeException.class)
	//"Missing combination token: "
	public void processCombinationTypeInvalid3Test() {
		List<Object> tokens = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		tokens.add(new Equals("key", "key_hello", paramMap));
		List<Query> child = new ArrayList<Query>();
		assertNotNull(QueryFilter.processCombinationType(tokens, null, child));
	}
	
	@Test
	public void processCombinationTypeSingleChildTest() {
		List<Object> tokens = new ArrayList<Object>();
		List<Query> child = new ArrayList<Query>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Query query = new Equals("key1", "key1_hello", paramMap);
		child.add(query);
		assertEquals(query, QueryFilter.processCombinationType(tokens, null, child));
	}
	
	@Test
	public void processCombinationTypeChildrenTest() {
		List<Object> tokens = new ArrayList<Object>();
		List<Query> child = new ArrayList<Query>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Query query = new Equals("key1", "key1_hello", paramMap);
		child.add(new Equals("key1", "key1_hello", paramMap));
		child.add(query);
		assertNotNull(QueryFilter.processCombinationType(tokens, null, child));
	}
}
