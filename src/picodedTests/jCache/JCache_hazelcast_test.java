package picodedTests.jCache;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;


import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import picoded.jCache.*;
import picodedTests.jCache.LocalCacheSetup;
import java.util.concurrent.ConcurrentMap;

public class JCache_hazelcast_test extends picodedTests.jCache.JCache_redis_test {
	
	static protected String hazelcastClusterName;
	static protected ClientConfig hazelcastConfig;
	
	static protected JCache hazelcastJCacheObj = null;
	
	/// Setsup the testing server
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		hazelcastClusterName = LocalCacheSetup.setupHazelcastServer();
		
		// Config to use
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(hazelcastClusterName);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		
	}
	
	/// Dispose the testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Close JCache if needed (reduce false error)
		if( hazelcastJCacheObj != null ) {
			hazelcastJCacheObj.dispose();
			hazelcastJCacheObj = null;
		}
		
		try {
			Thread.sleep(5000);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		// one-time cleanup code
		LocalCacheSetup.teardownHazelcastServer();
	}
	
	/// Setsup the JCache object
	@Before
	public void setUp() {
		JCacheObj = JCache.hazelcast(hazelcastConfig);
		hazelcastJCacheObj = JCacheObj;
	}
	
	/// Dispose the JCache object
	@After
	public void tearDown() {
		if(JCacheObj != null) {
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
		JCacheObj = JCache.hazelcast(hazelcastConfig);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
		
		/// Constructor by string
		JCacheObj = JCache.hazelcast(hazelcastClusterName);
		assertNotNull("JCacheObj object must not be null", JCacheObj);
		JCacheObj.dispose();
		JCacheObj = null;
	}
	
}