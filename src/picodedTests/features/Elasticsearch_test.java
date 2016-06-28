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
		File store = new File("./test-files/tmp/elasticsearch");
		esObj = new EmbeddedElasticsearchServer(TestConfig.randomTablePrefix(), -1, store, true);
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
	
	@Test 
	public void simpleGetPutGet() {
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("hello","world");
		
		ElasticsearchClient client = esObj.ElasticsearchClient();
		
		assertNotNull( client );
		assertNull( client.get("this", "is", "1") );
		//assertEquals( "1", client.put("this", "is", "1", data) );
		//assertEquals( data, client.get("this", "is", "1") );
		
	}
}
