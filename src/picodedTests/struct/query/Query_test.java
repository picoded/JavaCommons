package picodedTests.struct.query;

// Target test class
import picoded.struct.query.condition.*;
import picoded.struct.query.internal.*;
import picoded.struct.query.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import picoded.struct.*;
import picoded.struct.query.*;
import picoded.struct.query.internal.*;

///
/// Test Case for picoded.struct.query.condition.*
///
public class Query_test {
	
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
		Object[] paramArr = new Object[] { "adv", "sup"};
		assertNotNull(queryObj = Query.build("( adv = ? ) AND ( a = ? )", paramArr));
	}
	
}
