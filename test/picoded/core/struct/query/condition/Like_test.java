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

public class Like_test {
	
	private Like like = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(like);
	}
	
	@Test
	public void typeTest() {
		like = construct();
		assertEquals(QueryType.LIKE, like.type());
	}
	
	@Test
	public void testValuesTest() {
		like = construct();
		assertFalse(like.testValues(null, null));
	}
	
	@Test
	public void operatorSymbolTest() {
		like = construct();
		assertEquals("LIKE", like.operatorSymbol());
	}
	
	private Like construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new Like("key", "myKey", defaultArgMap);
	}
}
