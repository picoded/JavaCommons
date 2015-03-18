package picodedTests.jCache;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import org.redisson.*;
import picoded.jCache.*;
import picodedTests.jCache.LocalCacheSetup;
import java.util.concurrent.ConcurrentMap;

public class JCache_redis_test {
	
	static protected String redissonConfigString;
	static protected Config redissonConfig;
	static protected int redisPort = 0;
	
	static protected JCache redisJCacheObj = null;
	
	/// JCache object to run test against
	public JCache JCacheObj;
	
	/// Setsup the testing server
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		redisPort = LocalCacheSetup.setupRedisServer();
		
		// Config to use
		redissonConfigString = "127.0.0.1:" + redisPort;
		
		redissonConfig = new Config();
		redissonConfig.useSingleServer().setAddress(redissonConfigString);
	}
	
	/// Dispose the testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Close JCache if needed (reduce false error)
		if (redisJCacheObj != null) {
			redisJCacheObj.dispose();
			redisJCacheObj = null;
		}
		
		// one-time cleanup code
		LocalCacheSetup.teardownRedisServer();
	}
	
	/// Setsup the JCache object
	@Before
	public void setUp() {
		JCacheObj = JCache.redis(redissonConfig);
		redisJCacheObj = JCacheObj;
	}
	
	/// Dispose the JCache object
	@After
	public void tearDown() {
		if (JCacheObj != null) {
			JCacheObj.dispose();
			JCacheObj = null;
		}
	}
	
	@Test
	public void constructorTest() {
		/// Test case setup
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
		
		/// Constructor by config object
		JCacheObj = JCache.redis(redissonConfig);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		
		/// Constructor by string
		JCacheObj = JCache.redis(redissonConfigString);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
	}
	
	@Test
	public void simplePutAndGet() throws JCacheException {
		ConcurrentMap<String, String> rMap = JCacheObj.getMap("testMap");
		
		assertNull(rMap.get("testIsNull"));
		rMap.put("hello", "world");
		assertEquals("world", rMap.get("hello"));
	}
}