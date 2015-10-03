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

import picodedTests.JCache.LocalCacheSetup;
import picodedTests.TestConfig;

import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.redisson.*;

/// This class acts as a test utility, for all the various JStackData type tests
public class JStackData_testBase_test {
	
	// JCache backend server setup (locally)
	//-----------------------------------------------
	
	/// Hazelcast server related vars
	static public String hazelcastClusterName = null;
	static public ClientConfig hazelcastConfig = null;
	
	/// Hazelcast setup function, repeated calls are "safe"
	public static ClientConfig hazelcastSetup() {
		if (hazelcastConfig == null) {
			// one-time initialization code
			hazelcastClusterName = LocalCacheSetup.setupHazelcastServer();
			
			// Config to use
			hazelcastConfig = new ClientConfig();
			hazelcastConfig.getGroupConfig().setName(hazelcastClusterName);
			hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		}
		return hazelcastConfig;
	}
	
	/// Redis server related vars
	static public String redissonConfigString;
	static public org.redisson.Config redissonConfig;
	static public int redisPort = 0;
	
	/// Setsup the redis testing server, repeated calls are "safe"
	public static org.redisson.Config redisSetup() {
		if (redissonConfig == null) {
			// one-time initialization code
			redisPort = LocalCacheSetup.setupRedisServer();
			
			// Config to use
			redissonConfigString = "127.0.0.1:" + redisPort;
			redissonConfig = new org.redisson.Config();
			redissonConfig.useSingleServer().setAddress(redissonConfigString);
		}
		return redissonConfig;
	}
	
	/// Dispose the hzelcast / redis testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Tears down hazelcast (if needed)
		if (hazelcastConfig != null) {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			// one-time cleanup code
			LocalCacheSetup.teardownHazelcastServer();
			
			hazelcastConfig = null;
		}
		
		// Tears down redis (if needed)
		if (redissonConfig != null) {
			//try {
			//	Thread.sleep(5000);
			//} catch (Exception e) {
			//	throw new RuntimeException(e);
			//}
			// one-time cleanup code
			LocalCacheSetup.teardownRedisServer();
			
			hazelcastConfig = null;
		}
	}
	
	// Main over-ride functions
	//-----------------------------------------------
	
	// JCache object cache
	public JCache JCacheObj = null;
	
	// Over-ride and setup the JCache as needed
	public JCache JCacheObj() {
		return null;
	}
	
	// JSql object cached
	public JSql JSqlObj = null;
	
	// Over-ride and setup the JSql as needed
	public JSql JSqlObj() {
		return JSql.sqlite();
	}
	
	// The actual test obj setup, after JStack
	public void testObjSetup() throws JStackException {
		
	}
	
	// The actual test obj teardown, before JStack
	public void testObjTeardown() throws JStackException {
		
	}
	
	@Before
	public void setUp() throws JStackException {
		JStackSetup();
		testObjSetup();
	}
	
	@After
	public void tearDown() throws JStackException {
		testObjTeardown();
		JStackTearDown();
	}
	
	// JStack setup
	//-----------------------------------------------
	
	public JStack JStackObj = null;
	public JStackLayer JStackLayerArray[] = null;
	
	public JStack JStackSetup() {
		JCacheObj = JCacheObj();
		JSqlObj = JSqlObj();
		
		if (JCacheObj == null && JSqlObj == null) {
			throw new RuntimeException("Both JCache, and JSql cannot be blank");
		}
		
		if (JCacheObj == null) {
			JStackLayerArray = new JStackLayer[] { JSqlObj };
		} else if (JSqlObj == null) {
			JStackLayerArray = new JStackLayer[] { JCacheObj };
		} else {
			JStackLayerArray = new JStackLayer[] { JCacheObj, JSqlObj };
		}
		
		JStackObj = new JStack(JStackLayerArray);
		
		return JStackObj;
	}
	
	public void JStackTearDown() {
		JStackObj = null;
	}
	
	// Basic JStack unit testing
	@Test
	public void stackCheck() {
		assertNotNull(JStackSetup());
	}
	
}
