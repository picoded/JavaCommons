package picoded.struct.query.condition;

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

import picoded.struct.query.Query;
import picoded.struct.query.QueryType;

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
	
	private Or construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new Or(Arrays.asList(new Query[] { new Equals("hello", "hello", defaultArgMap),
			new Equals("my", "my", defaultArgMap) }), defaultArgMap);
	}
}
