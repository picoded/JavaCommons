package picodedTests.struct.query;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.struct.query.Query;

///
/// Test Case for picoded.struct.query.condition.*
///
@SuppressWarnings("unused")
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
	// Like query check
	//
	@Test
	public void likeQuery() {
		assertEquals("(\"me\" LIKE :good AND \"life\" LIKE :awsome) OR \"every\" LIKE :one",
			Query.build("(me LIKE :good AND life LIKE :awsome) OR every LIKE :one").toString());
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
		Object[] paramArr = new Object[] { "adv", "sup" };
		assertNotNull(queryObj = Query.build("( adv = ? ) AND ( a = ? )", paramArr));
	}
	
}
