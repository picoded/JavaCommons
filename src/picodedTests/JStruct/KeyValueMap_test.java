package picodedTests.JStruct;

// Target test class
import picoded.JStruct.*;
import picoded.JStruct.internal.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class KeyValueMap_test {
	
	/// Test object
	public KeyValueMap kvmObj = null;
	
	/// To override for implementation
	///------------------------------------------------------
	public KeyValueMap implementationConstructor() {
		JStruct jsObj = new JStruct();
		return (new JStruct()).getKeyValueMap("test");
	}
	
	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		kvmObj = implementationConstructor();
		kvmObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		kvmObj.systemTeardown();
		kvmObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(kvmObj);
		
		//run maintaince, no exception?
		kvmObj.maintenance();
	}
	
	/// utility
	///------------------------------------------------------
	public long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis() / 1000L);
	}
	
	/// basic test
	///------------------------------------------------------
	
	@Test
	public void simpleHasPutHasGet() throws Exception {
		assertFalse( kvmObj.containsKey("hello") );
		kvmObj.put("hello", "world");
		assertTrue( kvmObj.containsKey("hello") );
		assertEquals( "world", kvmObj.get("hello") );
	}
	
	@Test
	public void getExpireTime() throws Exception {
		long expireTime = currentSystemTimeInSeconds() * 2;
		kvmObj.putWithExpiry("yes", "no", expireTime);

		assertNotNull(kvmObj.getExpiry("yes"));
		assertEquals(expireTime, kvmObj.getExpiry("yes"));
	} 
	
	@Test
	public void setExpireTime() throws Exception{
		long expireTime = currentSystemTimeInSeconds() * 2;
		kvmObj.putWithExpiry("yes", "no", expireTime);

		long newExpireTime = kvmObj.getExpiry("yes") * 2;
		kvmObj.setExpiry("yes", newExpireTime);

		long fetchedExpireTime = kvmObj.getExpiry("yes");
		
		assertNotNull(fetchedExpireTime );
		assertEquals( fetchedExpireTime, newExpireTime );
	}
	
	@Test
	public void setLifeSpan() throws Exception{
		long lifespanTime = 4*24*60*60*60;
		kvmObj.putWithLifespan("yes", "no", lifespanTime);
		
		long newLifespanTime = kvmObj.getExpiry("yes");
		kvmObj.setLifeSpan("yes", newLifespanTime);

		assertNotNull(kvmObj.getLifespan("yes"));
	}

	@Test
	public void testColumnExpiration() throws Exception {
		//set column expiration time to current time + 30 secs.
		long expirationTime = currentSystemTimeInSeconds() + 1;
		kvmObj.putWithExpiry("yes", "no", expirationTime);

		//before the expiration time key will not be null.
		assertNotNull(kvmObj.get("yes"));

		//sleep the execution for 31 secs so that key gets expired.
		Thread.sleep(2000);

		//key should be null after expiration time.
		assertEquals( null, kvmObj.get("yes"));
	}
	
	@Test
	public void getKeysTest() throws Exception {
		assertEquals( new HashSet<String>(), kvmObj.getKeys("world") );
		
		kvmObj.put("yes", "no");
		kvmObj.put("hello", "world");
		kvmObj.put("this", "world");
		kvmObj.put("is", "sparta");
		
		assertEquals("no", kvmObj.get("yes"));
		assertEquals("world", kvmObj.get("hello"));
		assertEquals("world", kvmObj.get("this"));
		assertEquals("sparta", kvmObj.get("is"));
		
		assertEquals( new HashSet<String>(Arrays.asList(new String[] { "hello", "this" })), kvmObj.getKeys("world") );
	}
	
	/// nonce test
	///------------------------------------------------------
	
	@Test
	public void simpleNonce() {
		String nonce;
		assertNotNull( nonce = kvmObj.generateNonce("hello") );
		assertEquals( "hello", kvmObj.get(nonce) );
	}

}
