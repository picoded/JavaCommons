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

public class Equals_test {
	
	private Equals equals = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(equals);
	}
	
	@Test
	public void typeTest() {
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		equals = new Equals("key", "myKey", defaultArgMap);
		assertEquals(QueryType.EQUALS, equals.type());
	}
	
	@Test
	public void testValuesTest() {
		Map<String, Object> defaultArgMap = new HashMap<String, Object>();
		equals = new Equals("key", "myKey", defaultArgMap);
		assertFalse(equals.testValues(null, null));
	}
	
}
