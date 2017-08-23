package picoded.dstack.struct.simple;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

// Test Case include
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Test depends
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

public class StructSimple_AtomicLongMap_test {
	
	// To override for implementation
	//-----------------------------------------------------
	
	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public AtomicLongMap implementationConstructor() {
		return new StructSimple_AtomicLongMap();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	public AtomicLongMap testObj = null;
	
	@Before
	public void setUp() {
		testObj = implementationConstructor();
		testObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void putTest() {
		assertNull(testObj.put("key", null));
		assertEquals((Object) 0l, testObj.get("key"));
		
		assertNull(testObj.put("key", 0));
		assertEquals(0, (long) testObj.get("key"));
		
		assertNull(testObj.put("key", (Long) 90l));
		assertEquals(90, (long) testObj.get("key"));
	}
	
	@Test
	public void getAndAddTest() {
		// Setup
		assertNull(testObj.put("key", 90l));
		
		// Test getAndAdd
		assertEquals(90l, (long) testObj.getAndAdd("key", (Long) 90l));
		assertEquals(180l, (long) testObj.getAndAdd("key", (Long) 90l));
		
		// Test getAndAdd from null
		assertEquals((Object) 0l, testObj.getAndAdd("key1", (Long) 90l));
		assertEquals((Object) 90l, testObj.getAndAdd("key1", (Long) 90l));
		assertEquals((Object) 180l, testObj.getAndAdd("key1", (Long) 90l));
	}
	
	@Test
	public void getAndIncrementTest() {
		testObj.put("key", 90l);
		assertEquals((Object) 90l, testObj.getAndIncrement("key"));
		assertEquals((Object) 0l, testObj.getAndIncrement("key1"));
	}
	
	@Test
	public void incrementAndGetTest() {
		assertEquals((Object) 1l, testObj.incrementAndGet("key"));
		testObj.put("key", 90l);
		assertEquals((Object) 91l, testObj.incrementAndGet("key"));
	}
	
	@Test
	public void weakCompareAndSetTest() {
		testObj.put("key", 90l);
		assertFalse(testObj.weakCompareAndSet("key", (Long) 80l, (Long) 80l));
		assertTrue(testObj.weakCompareAndSet("key", (Long) 90l, (Long) 80l));
	}
}
