package picodedTests.benchmarks;

// Target test class
import picoded.jCache.*;

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
import picodedTests.jCache.LocalCacheSetup;

///
/// Test Case for JCache performance of Locks
///
public class JCacheLocks_hazelcast_test {
	
	private final static Logger LOGGER = Logger.getLogger(JCacheLocks_hazelcast_test.class.getName());
	
	public String descPrefix = "";
	public JCache jCacheObj = null;
	
	/// Overwriteable test scale, used to modify the test case intensity
	static int testScale_lockCount = 10000;
	
	@Before
	public void setUp() throws InterruptedException {
		this.setUpJCache();
	}
	
	@After
	public void tearDown() throws InterruptedException {
		if (jCacheObj != null) {
			jCacheObj.dispose();
			jCacheObj = null;
		}
	}
	
	/// Setsup the hazelcast cluster read / write
	public void setUpJCache() throws InterruptedException {
		descPrefix = "[Hazelcast] ";
		jCacheObj = JCache.hazelcast(LocalCacheSetup.setupHazelcastServer());
	}
	
	//--------------------------------------
	// JCache server setup / teardown
	//--------------------------------------
	
	public static void teardownJCacheServer() throws InterruptedException {
		Thread.sleep(250); //prevents socket closing false positive 'error'
		LocalCacheSetup.teardownHazelcastServer();
		LocalCacheSetup.teardownRedisServer();
		Thread.sleep(250); //prevents socket closing false positive 'error'
	}
	
	//--------------------------------------
	// Timing logging / output function
	//--------------------------------------
	
	public static ArrayList<String> structureName = new ArrayList<String>();
	public static ArrayList<String> structureDesc = new ArrayList<String>();
	public static ArrayList<Long> structureLockTime = new ArrayList<Long>();
	
	@BeforeClass
	public static void setupTimingArrays() throws InterruptedException {
		structureName = new ArrayList<String>();
		structureDesc = new ArrayList<String>();
		structureLockTime = new ArrayList<Long>();
	}
	
	@AfterClass
	public static void logOutInfoTimings() throws InterruptedException {
		teardownJCacheServer();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n");
		sb.append("==============================================================================\n");
		sb.append("> Completed timing test of " + structureName.size() + " JCache locking format\n");
		for (int t = 0; t < structureName.size(); ++t) {
			sb.append("------------------------------------------------------------------------------\n");
			sb.append("[" + structureName.get(t) + "] : " + structureDesc.get(t) + "\n");
			sb.append("> Lock timing (milliseconds) : " + (structureLockTime.get(t).longValue()) + "\n");
		}
		sb.append("==============================================================================\n");
		
		LOGGER.info(sb.toString());
	}
	
	//--------------------------------------
	// Utility functions
	//--------------------------------------
	
	/// Records down test timings to display in post test timing report
	public static void addTimings(String name, String desc, long lock) {
		structureName.add(name);
		structureDesc.add(desc);
		structureLockTime.add(lock);
	}
	
	//--------------------------------------
	// Test functions
	//--------------------------------------
	
	@Test
	public void continousLockAndUnlock() throws JCacheException {
		Lock t;
		
		long startTime = System.currentTimeMillis();
		
		for (int a = 0; a < testScale_lockCount; ++a) {
			t = jCacheObj.getLock("lock-" + a);
			try {
				t.lock();
			} finally {
				t.unlock();
			}
		}
		
		long endinTime = System.currentTimeMillis();
		long diffTime = (endinTime - startTime);
		
		addTimings("continousLockAndUnlock", descPrefix + testScale_lockCount + " Continous Lock & Unlock", diffTime);
	}
	
	@Test
	public void sequentialLockAndUnlock() throws JCacheException {
		Lock t;
		
		long startTime = System.currentTimeMillis();
		
		for (int a = 0; a < testScale_lockCount; ++a) {
			t = jCacheObj.getLock("slock-" + a);
			t.lock();
		}
		
		for (int a = 0; a < testScale_lockCount; ++a) {
			t = jCacheObj.getLock("slock-" + a);
			t.unlock();
		}
		
		long endinTime = System.currentTimeMillis();
		long diffTime = (endinTime - startTime);
		
		addTimings("sequentialLockAndUnlock", descPrefix + testScale_lockCount + " Sequential Lock & Unlock", diffTime);
	}
}
