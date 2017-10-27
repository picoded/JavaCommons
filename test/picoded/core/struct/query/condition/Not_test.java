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

public class Not_test {
	
	private Not not = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(not);
	}
	
	@Test
	public void typeTest() {
		not = construct();
		assertEquals(QueryType.NOT, not.type());
	}
	
	@Test
	public void operatorSymbolTest() {
		not = construct();
		assertEquals("NOT", not.operatorSymbol());
	}
	
	private Not construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new Not(Arrays.asList(new Query[] { new Equals("hello", "hello", defaultArgMap),
			new Equals("my", "my", defaultArgMap) }), defaultArgMap);
	}
}
