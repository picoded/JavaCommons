package picodedTests.jCache;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import java.util.concurrent.ConcurrentMap;
import org.redisson.*;
import picodedTests.jCache.LocalCacheSetup;

public class Redission_simple_test {
	
	static protected Config redissionConfig;
	
	protected Redisson redissionObj;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		
		// Config to use
		redissionConfig= new Config();
		redissionConfig.useSingleServer().setAddress("127.0.0.1:6379");
		
		LocalCacheSetup.setupRedisServer();
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
		LocalCacheSetup.teardownRedisServer();
	}
	
	@Before
	public void setUp() {
		redissionObj = Redisson.create(redissionConfig);
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void sqliteInMemoryConstructor() {
		assertNotNull("Redission constructed object must not be null", redissionObj);
	}
	
	
	@Test
	public void simplePutAndGet() {
		ConcurrentMap<String, String> rMap = redissionObj.getMap("testMap");
		
		assertNull( rMap.get("testIsNull") );
		rMap.put("hello", "world");
		assertEquals("world", rMap.get("hello") );
	}
}