package picodedTests.jCache;

import java.lang.Exception;
import picodedTests.TestConfig;

import redis.embedded.*;

import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// Test utiltiy function, used to setup a local Redis / hazelCast cache instance
public class LocalCacheSetup {
	
	/// The redis server instance
	private static RedisServer rServer = null;
	
	/// Starts up redis at the default port
	static public int setupRedisServer() {
		if (rServer != null) {
			throw new RuntimeException("Local Redis server already started");
		}
		
		try {
			rServer = new RedisServer(6379); //default port
			rServer.start();
			return 6379;
		} catch (Exception e) {
			throw new RuntimeException("Local Redis server setup error", e);
		}
		//return -1;
	}
	
	/// Tears down the redis test server
	static public void teardownRedisServer() {
		if (rServer == null) {
			throw new RuntimeException("No local Redis server to 'teardown'");
		}
		
		rServer.stop();
		rServer = null;
	}
	
	/// The redis server instance
	private static HazelcastInstance hcServer = null;
	
	/// Starts up redis at the default port
	static public String setupHazelcastServer() {
		if (hcServer != null) {
			throw new RuntimeException("Local hazelcast server already started");
		}
		
		String clusterName = TestConfig.randomTablePrefix();
		try {
			
			Config clusterConfig = new Config();
			clusterConfig.getGroupConfig().setName(clusterName);
			clusterConfig.setProperty("hazelcast.logging.type", "none");
			
			hcServer = Hazelcast.newHazelcastInstance(clusterConfig);
			
			return clusterName;
		} catch (Exception e) {
			throw new RuntimeException("Local hazelcast server setup error", e);
		}
		//return -1;
	}
	
	/// Tears down the redis test server
	static public void teardownHazelcastServer() {
		if (hcServer == null) {
			throw new RuntimeException("No local hazelcast server to 'teardown'");
		}
		
		hcServer.shutdown();
		rServer = null;
	}
	
}