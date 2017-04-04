package picoded.dstack.struct.simple;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

// Test Case include
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Test depends
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

public class StructSimple_KeyValue_test {
	
	// Test object for reuse
	public KeyValue testObj = null;
	
	// To override for implementation
	//-----------------------------------------------------
	
	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public KeyValue implementationConstructor() {
		return new StructSimple_KeyValue();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void systemSetup() {
		testObj = implementationConstructor();
		testObj.systemSetup();
	}
	
	@After
	public void systemDestroy() {
		if (testObj != null) {
			testObj.systemDestroy();
		}
		testObj = null;
	}
	
	@Test
	public void constructorSetupAndMaintenance() {
		// not null check
		assertNotNull(testObj);
		
		// run maintaince, no exception?
		testObj.maintenance();
		
		// run incremental maintaince, no exception?
		testObj.incrementalMaintenance();
	}
	
	// utility
	//-----------------------------------------------------
	public long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis() / 1000L);
	}
	
	// basic test
	//-----------------------------------------------------
	
	@Test
	public void simpleHasPutHasGet() throws Exception {
		assertFalse(testObj.containsKey("hello"));
		testObj.put("hello", "world");
		assertEquals("world", testObj.get("hello"));
		assertTrue(testObj.containsKey("hello"));
		assertEquals("world", testObj.get("hello"));
	}
	
	@Test
	public void getExpireTime() throws Exception {
		long expireTime = currentSystemTimeInSeconds() * 2;
		testObj.putWithExpiry("yes", "no", expireTime);
		
		assertNotNull(testObj.getExpiry("yes"));
		assertEquals(expireTime, testObj.getExpiry("yes"));
	}
	
	@Test
	public void setExpireTime() throws Exception {
		long expireTime = currentSystemTimeInSeconds() * 2;
		testObj.putWithExpiry("yes", "no", expireTime);
		
		long newExpireTime = testObj.getExpiry("yes") * 2;
		testObj.setExpiry("yes", newExpireTime);
		
		long fetchedExpireTime = testObj.getExpiry("yes");
		assertNotNull(fetchedExpireTime);
		assertEquals(fetchedExpireTime, newExpireTime);
	}
	
	@Test
	public void setLifeSpan() throws Exception {
		long lifespanTime = 4 * 24 * 60 * 60 * 60;
		testObj.putWithLifespan("yes", "no", lifespanTime);
		
		long newLifespanTime = testObj.getExpiry("yes");
		testObj.setLifeSpan("yes", newLifespanTime);
		
		assertNotNull(testObj.getLifespan("yes"));
	}
	
	@Test
	public void keySetTest() throws Exception {
		assertEquals(new HashSet<String>(), testObj.keySet());
		assertEquals(new HashSet<String>(), testObj.keySet("world"));
		
		testObj.put("yes", "no");
		testObj.put("hello", "world");
		testObj.put("this", "world");
		testObj.put("is", "sparta");
		
		assertEquals("no", testObj.get("yes"));
		assertEquals("world", testObj.get("hello"));
		assertEquals("world", testObj.get("this"));
		assertEquals("sparta", testObj.get("is"));
		
		assertEquals(new HashSet<String>(Arrays.asList(new String[] { "hello", "this" })),
			testObj.keySet("world"));
	}
	
	@Test
	public void SLOW_testColumnExpiration() throws Exception {
		// set column expiration time to current time + 2 secs.
		long expirationTime = currentSystemTimeInSeconds() + 2;
		testObj.putWithExpiry("yes", "no", expirationTime);
		
		// before the expiration time key will not be null.
		assertNotNull(testObj.get("yes"));
		
		// sleep the execution for 4 secs so the inserted key gets expired.
		Thread.sleep(4000);
		
		// key should be null after expiration time.
		assertEquals(null, testObj.get("yes"));
	}
	
}
