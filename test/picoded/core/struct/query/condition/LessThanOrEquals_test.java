package picoded.core.struct.query.condition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.query.Query;
import picoded.core.struct.query.QueryType;

public class LessThanOrEquals_test {
	
	private LessThanOrEquals lessThanOrEquals = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(lessThanOrEquals);
	}
	
	@Test
	public void typeTest() {
		lessThanOrEquals = construct();
		assertEquals(QueryType.LESS_THAN_OR_EQUALS, lessThanOrEquals.type());
	}
	
	@Test
	public void testValuesTest() {
		lessThanOrEquals = construct();
		assertFalse(lessThanOrEquals.testValues(null, null));
	}
	
	@Test
	public void operatorSymbolTest() {
		lessThanOrEquals = construct();
		assertEquals("<=", lessThanOrEquals.operatorSymbol());
	}
	
	private LessThanOrEquals construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new LessThanOrEquals("key", "myKey", defaultArgMap);
	}
}
