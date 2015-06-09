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

/// This class acts as a test utility, for all the various JStackData type tests
public class JStackData_testBase_test {
	
	// JCache backend server setup (locally)
	//-----------------------------------------------
	
	/// Hazelcast server related vars
	static protected String hazelcastClusterName = null;
	static protected ClientConfig hazelcastConfig = null;
	
	/// Hazelcast setup function, repeated calls are "safe"
	public static ClientConfig hazelcastSetup() {
		if( hazelcastConfig == null ) {
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
	static protected String redissonConfigString;
	static protected Config redissonConfig;
	static protected int redisPort = 0;
	
	/// Setsup the redis testing server, repeated calls are "safe"
	public static Config redisSetup() {
		if(redissonConfig == null) {
			// one-time initialization code
			redisPort = LocalCacheSetup.setupRedisServer();
			
			// Config to use
			redissonConfigString = "127.0.0.1:" + redisPort;
			redissonConfig = new Config();
			redissonConfig.useSingleServer().setAddress(redissonConfigString);
		}
		return redissonConfig;
	}
	
	/// Dispose the hzelcast / redis testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Tears down hazelcast (if needed)
		if( hazelcastConfig != null ) {
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
		if( redissonConfig != null ) {
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
	
	
	
	
	@Test
	public void blank() {
		assertTrue(true);
	}
	
	
}