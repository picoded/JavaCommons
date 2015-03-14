package picodedTests.jCache;

import redis.embedded.*;
import java.lang.Exception;

/// Test utiltiy function, used to setup a local Redis / hazelCast cache instance
public class LocalCacheSetup {

	/// The redis server instance
	private static RedisServer rServer = null;

	/// Starts up redis at the default port
	static public void setupRedisServer() {
		if(rServer != null) {
			throw new RuntimeException("Local Redis server already started");
		}
		
		try {
			rServer = new RedisServer(6379); //default port
			rServer.start();
		} catch(Exception e) {
			throw new RuntimeException("Local Redis server setup error", e);
		}
		
	}
	
	/// Tears down the redis test server
	static public void teardownRedisServer() {
		if(rServer == null) {
			throw new RuntimeException("No local Redis server to 'teardown'");
		}
		
		rServer.stop();
		rServer = null;
	}
	
	
}