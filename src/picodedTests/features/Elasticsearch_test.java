package picodedTests.features;

// Target test class
import picoded.JCache.*;
import picoded.JCache.embedded.*;

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
import java.io.File;

import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

///
/// Test Case for JCache performance of Locks
///
public class Elasticsearch_test {
	
	private final static Logger LOGGER = Logger.getLogger(Elasticsearch_test.class.getName());
	
	public EmbeddedElasticsearchServer esObj = null;
	
	@Before
	public void setUp() throws InterruptedException {
		esObj = new EmbeddedElasticsearchServer(TestConfig.randomTablePrefix(), -1, new File("./test-files/tmp/elasticsearch"), true);
	}
	
	@After
	public void tearDown() throws InterruptedException { 
		if (esObj != null) {
			esObj.closeAndDelete();
			esObj = null;
		}
	}
	
	@Test
	public void notNull() {
		assertNotNull(esObj);
	}
}
