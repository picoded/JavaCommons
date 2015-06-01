package picodedTests.benchmarks;

// Target test class
import picoded.JCache.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

///
/// Test Case for JCache performance of Locks
///
public class JCacheLocks_redis_test extends JCacheLocks_hazelcast_test {
	/// Setsup the hazelcast cluster read / write
	public void setUpJCache() throws InterruptedException {
		descPrefix = "[Redis] ";
		JCacheObj = JCache.redis("127.0.0.1:" + LocalCacheSetup.setupRedisServer());
	}
}
