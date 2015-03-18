package picodedTests.jCache;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import java.util.concurrent.ConcurrentMap;
import org.redisson.*;
import picodedTests.jCache.LocalCacheSetup;

public class Redisson_simple_test {
	
	static protected Config redissonConfig;
	static protected int redisPort = 0;
	
	protected Redisson redissonObj;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		redisPort = LocalCacheSetup.setupRedisServer();
		
		// Config to use
		redissonConfig = new Config();
		redissonConfig.useSingleServer().setAddress("127.0.0.1:" + redisPort);
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
		LocalCacheSetup.teardownRedisServer();
	}
	
	@Before
	public void setUp() {
		redissonObj = Redisson.create(redissonConfig);
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void sqliteInMemoryConstructor() {
		assertNotNull("redisson constructed object must not be null", redissonObj);
	}
	
	@Test
	public void simplePutAndGet() {
		ConcurrentMap<String, String> rMap = redissonObj.getMap("testMap");
		
		assertNull(rMap.get("testIsNull"));
		rMap.put("hello", "world");
		assertEquals("world", rMap.get("hello"));
	}
}