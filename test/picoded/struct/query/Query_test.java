package picoded.struct.query;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.struct.ArrayListMap;
import picoded.struct.ProxyGenericConvertMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.CALLS_REAL_METHODS;


///
/// Test Case for picoded.struct.query.condition.*
///
@SuppressWarnings("unused")
public class Query_test {
	
	private Query queryMock = mock(Query.class, CALLS_REAL_METHODS);
	
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
	// Query test
	//--------------------------------------------------------------------
	
	// Simple equality test
	@Test
	public void simpleQuery() {
		Query queryObj = null;
		Object[] paramArr = new Object[] { "world" };
		
		assertNotNull(queryObj = Query.build("my = ?", paramArr));
		assertEquals("\"my\" = :0", queryObj.toString());
	}
	
	@Test
	public void multipleAnd() {
		assertEquals("\"me\" = :good AND \"life\" = :awsome", Query.build("me = :good AND life = :awsome").toString());
		assertEquals("\"me\" = :good AND \"life\" = :awsome AND \"new\" = :world",
			Query.build("me = :good AND life = :awsome AND new = :world").toString());
	}
	
	@Test
	public void nested() {
		assertEquals("(\"me\" = :good AND \"life\" = :awsome) OR \"every\" = :one",
			Query.build("(me = :good AND life = :awsome) OR every = :one").toString());
	}
	
	//
	// Like query check
	//
	@Test
	public void likeQuery() {
		assertEquals("(\"me\" LIKE :good AND \"life\" LIKE :awsome) OR \"every\" LIKE :one",
			Query.build("(me LIKE :good AND life LIKE :awsome) OR every LIKE :one").toString());
	}
	
	//
	// Complex query exception
	//
	@Test
	public void dualNestedQuery() {
		Query queryObj = null;
		Object[] paramArr = new Object[] { "adv", "sup", "prx", "a", "b" };
		assertNotNull(queryObj = Query.build("( adv = ? OR sup = ? OR prx = ? ) AND ( a = ? OR b = ? )", paramArr));
	}
	
	//
	// Simplified version of dualNestedQuery, that causes an exception
	//
	@Test
	public void dualNestedQuery_simple() {
		Query queryObj = null;
		Object[] paramArr = new Object[] { "adv", "sup" };
		assertNotNull(queryObj = Query.build("( adv = ? ) AND ( a = ? )", paramArr));
	}
	
	@Test
	public void fieldNameTest() {
		assertNull(queryMock.fieldName());
	}
	
	@Test
	public void argumentNameTest() {
		assertNull(queryMock.argumentName());
	}
	
	@Test
	public void defaultArgumentMapTest() {
		assertNull(queryMock.defaultArgumentMap());
	}
	
	@Test
	public void defaultArgumentValueTest() {
		assertNull(queryMock.defaultArgumentValue());
	}
	
	@Test
	public void defaultArgumentValueNoNullTest() {
		Map<String, Object> map = new HashMap<String, Object>();
		when(queryMock.defaultArgumentMap()).thenReturn(map);
		assertNull(queryMock.defaultArgumentValue());
		
		map.put("key", "value");
		when(queryMock.defaultArgumentMap()).thenReturn(map);
		when(queryMock.argumentName()).thenReturn("key");
		assertNotNull(queryMock.defaultArgumentValue());
	}
	
	@Test
	public void isBasicOperatorTest() {
		assertFalse(queryMock.isBasicOperator());
	}
	
	@Test
	public void isCombinationOperatorTest() {
		assertFalse(queryMock.isCombinationOperator());
	}
	
	@Test
	public void childrenQueryTest() {
		assertNull(queryMock.childrenQuery());
	}
	
	@Test
	public void fieldQueryMapTest() {
		assertNotNull(queryMock.fieldQueryMap());
	}
	
	@Test
	public void fieldQueryMap2Test() {
		ArrayListMap<String, Query> ret = new ArrayListMap<>();
		assertNotNull(queryMock.fieldQueryMap(ret));
	}
	
	@Test
	public void fieldQueryMap2IsBasicOperatorTrueTest() {
		ArrayListMap<String, Query> ret = new ArrayListMap<String, Query>();
		ArrayList<Query> list = new ArrayList<Query>();
		list.add(Query.build("my = ?"));
		ret.put("key", list);
		when(queryMock.isBasicOperator()).thenReturn(true);
		when(queryMock.fieldName()).thenReturn("key");
		assertNotNull(queryMock.fieldQueryMap(ret));
	}
	
	@Test
	public void fieldQueryMap2IsBasicOperatorFalseTest() {
		ArrayListMap<String, Query> ret = new ArrayListMap<String, Query>();
		ArrayList<Query> list = new ArrayList<Query>();
		list.add(Query.build("my = ?"));
		ret.put("key", list);
		when(queryMock.isCombinationOperator()).thenReturn(true);
		when(queryMock.childrenQuery()).thenReturn(list);
		assertNotNull(queryMock.fieldQueryMap(ret));
	}
	
	@Test
	public void replaceQueryTest() {
		assertNull(queryMock.replaceQuery(queryMock, null));
		
		when(queryMock.isCombinationOperator()).thenReturn(true);
		List<Query> subList = new ArrayList<Query>();
		subList.add(Query.build("my = ?"));
		when(queryMock.childrenQuery()).thenReturn(subList);
		Query uery = Query.build("my = ?");
		assertNotNull(queryMock.replaceQuery(uery, null));
		
	}
	
	@Test
	public void queryArgumentsListTest() {
		assertNotNull(queryMock.queryArgumentsList());
	}
	
	@Test
	public void queryArgumentsList2Test() {
		List<Object> ret = new ArrayList<Object>();
		assertEquals(ret, queryMock.queryArgumentsList(ret));
	}
	
	@Test
	public void queryArgumentsList2AlternateTest() {
		List<Object> ret = new ArrayList<Object>();
		when(queryMock.isBasicOperator()).thenReturn(true);
		assertNotNull(queryMock.queryArgumentsList(ret));
		
		ArrayList<Query> list = new ArrayList<Query>();
		list.add(Query.build("my = ?"));
		when(queryMock.isCombinationOperator()).thenReturn(true);
		when(queryMock.childrenQuery()).thenReturn(list);
		assertNotNull(queryMock.queryArgumentsList(ret));
	}
	
	@Test
	public void queryArgumentsMapTest() {
		assertNotNull(queryMock.queryArgumentsMap());
	}
	
	@Test
	public void queryArgumentsMap2Test() {
		Map<String, Object> ret = new HashMap<String, Object>();
		assertNotNull(queryMock.queryArgumentsMap(ret));
	}
	
	@Test
	public void queryArgumentsMap2AlternateTest() {
		Map<String, Object> ret = new HashMap<String, Object>();
		when(queryMock.isBasicOperator()).thenReturn(true);
		assertNotNull(queryMock.queryArgumentsMap(ret));
		
		ArrayList<Query> list = new ArrayList<Query>();
		list.add(Query.build("my = ?"));
		when(queryMock.isCombinationOperator()).thenReturn(true);
		when(queryMock.childrenQuery()).thenReturn(list);
		assertNotNull(queryMock.queryArgumentsMap(ret));
	}
	
	@Test
	public void toSqlStringTest() {
		assertNotNull(queryMock.toSqlString());
	}
	
	@Test
	public void searchTest() {
		Map<String, String> set = new HashMap<String, String>();
		assertNotNull(queryMock.search(set));
		
		
	}
}
