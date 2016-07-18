package picodedTests.struct.query.internal;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.struct.MutablePair;
import picoded.struct.query.internal.QueryFilter;

///
/// Test Case for picoded.struct.query.condition.*
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
	
}
