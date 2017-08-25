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

public class NotEquals_test {
	
	private NotEquals notequals = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(notequals);
	}
	
	@Test
	public void typeTest() {
		notequals = construct();
		assertEquals(QueryType.NOT_EQUALS, notequals.type());
	}
	
	@Test
	public void operatorSymbolTest() {
		notequals = construct();
		assertEquals("!=", notequals.operatorSymbol());
	}
	
	private NotEquals construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new NotEquals("key", "myKey", defaultArgMap);
	}
}
