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

public class MoreThanOrEquals_test {
	
	private MoreThanOrEquals moreThanOrEquals = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void blankTest() {
		assertNull(moreThanOrEquals);
	}
	
	@Test
	public void typeTest() {
		moreThanOrEquals = construct();
		assertEquals(QueryType.MORE_THAN_OR_EQUALS, moreThanOrEquals.type());
	}
	
	@Test
	public void testValuesTest() {
		moreThanOrEquals = construct();
		assertFalse(moreThanOrEquals.testValues(null, null));
	}
	
	@Test
	public void operatorSymbolTest() {
		moreThanOrEquals = construct();
		assertEquals(">=", moreThanOrEquals.operatorSymbol());
	}
	
	private MoreThanOrEquals construct() {
		Map<String, Object> defaultArgMap = new HashMap<>();
		return new MoreThanOrEquals("key", "myKey", defaultArgMap);
	}
}
