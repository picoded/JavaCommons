package picodedTests.JStack;

import org.junit.*;

import static org.junit.Assert.*;

import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;

import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

import picodedTests.TestConfig;

public class KeyValueMap_Sqlite_test extends JStackData_testBase_test {
	
	// KeyValueMap setup
	//-----------------------------------------------
	
	protected String kvTableName = null;
	
	protected KeyValueMap kvObj = null;
	
	// The actual test obj setup, after JStack
	@Override
	public void testObjSetup() throws JStackException {
		kvTableName = "K" + TestConfig.randomTablePrefix();
		kvObj = new KeyValueMap(JStackObj, kvTableName);
		kvObj.stackSetup();
	}
	
	// The actual test obj teardown, before JStack
	@Override
	public void testObjTeardown() throws JStackException {
		kvObj.stackTeardown();
		kvObj = null;
	}
	
	@Test
	public void simpleHasPutHasGet() throws JStackException {
		assertFalse( kvObj.containsKey("hello") );
		kvObj.put("hello", "world");
		assertTrue( kvObj.containsKey("hello") );
		assertEquals( "world", kvObj.get("hello") );
		
	}
	
	@Test
	public void simpleNonce() throws JStackException {
		String nonce;
		
		assertNotNull( nonce = kvObj.generateNonce("hello") );
		assertEquals( "hello", kvObj.get(nonce) );
	}
	
	@Test
	public void getKeysTest() throws JStackException {
		assertArrayEquals( new String[] { }, kvObj.getKeys("world") );
		
		kvObj.put("yes", "no");
		kvObj.put("hello", "world");
		kvObj.put("this", "world");
		kvObj.put("is", "sparta");
		
		assertEquals("no", kvObj.get("yes"));
		assertEquals("world", kvObj.get("hello"));
		assertEquals("world", kvObj.get("this"));
		assertEquals("sparta", kvObj.get("is"));
		
		assertArrayEquals( new String[] { "hello", "this" }, kvObj.getKeys("world") );
	}
	
	@Test
	public void getExpireTime() throws JStackException {
		long expireTime = currentSystemTime_seconds() * 2;
		kvObj.putWithExpiry("yes", "no", expireTime);

		assertNotNull(kvObj.getExpiry("yes"));
		assertEquals(expireTime, kvObj.getExpiry("yes"));
	} 
	
	@Test
	public void setExpireTime() throws JStackException{
		long expireTime = currentSystemTime_seconds() * 2;
		kvObj.putWithExpiry("yes", "no", expireTime);

		long newExpireTime = kvObj.getExpiry("yes") * 2;
		kvObj.setExpiry("yes", newExpireTime);

		long fetchedExpireTime = kvObj.getExpiry("yes");
		
		assertNotNull(fetchedExpireTime );
		assertEquals( fetchedExpireTime, newExpireTime );
	} 
	
	@Test
	public void setLifeSpan() throws JStackException{
		long lifespanTime = 4*24*60*60*60;
		kvObj.putWithLifespan("yes", "no", lifespanTime);
		
		long newLifespanTime = kvObj.getExpiry("yes");
		kvObj.setLifeSpan("yes", newLifespanTime);

	   assertNotNull(kvObj.getLifespan("yes"));
	}
	
	@Test
	public void testColumnExpiration() throws Exception {
	   //set column expiration time to current time + 30 secs.
	   long expirationTime = currentSystemTime_seconds() + 30;
	   kvObj.putWithExpiry("yes", "no", expirationTime);

	   //before the expiration time key will not be null.
	   assertNotNull(kvObj.get("yes"));

	   //sleep the execution for 31 secs so that key gets expired.
	   Thread.sleep(31000);

	   //key should be null after expiration time.
	   assertEquals( null, kvObj.get("yes"));
	}

	private long currentSystemTime_seconds() {
		return (System.currentTimeMillis() / 1000L);
	}
}
