package picoded.core.struct.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryType_test {
	
	private QueryType queryType = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void initializeTypeMapsTest() {
		QueryType.initializeTypeMaps();
	}
	
	@Test
	public void fromTypeObjectTest() {
		queryType = QueryType.fromTypeObject(null);
		assertNull(queryType);
		
		QueryType queryType = QueryType.AND;
		queryType = QueryType.fromTypeObject(queryType);
		assertEquals(QueryType.AND, queryType);
		
		queryType = QueryType.fromTypeObject(2);
		assertEquals(QueryType.NOT, queryType);
		
		queryType = QueryType.fromTypeObject("OR");
		assertEquals(QueryType.OR, queryType);
	}
	
	@Test(expected = RuntimeException.class)
	public void fromTypeObjectInvalidTest() {
		queryType = QueryType.fromTypeObject("XYZ");
		assertEquals(QueryType.OR, queryType);
	}
}
