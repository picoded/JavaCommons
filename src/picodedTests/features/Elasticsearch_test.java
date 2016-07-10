package picodedTests.features;

// Target test class
import picoded.JCache.*;
import picoded.JCache.embedded.*;
import picoded.conv.*;

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
/// Test feature implementation of elasticsearch
///
public class Elasticsearch_test {
	
	private final static Logger LOGGER = Logger.getLogger(Elasticsearch_test.class.getName());
	
	// Cluster name
	public String clusterName = null;
	
	// Embedded server
	public EmbeddedElasticsearchServer esObj = null;
	
	// Client connection
	ElasticsearchClient client = null;
	
	@Before
	public void setUp() throws InterruptedException {
		clusterName = TestConfig.randomTablePrefix();
		
		File store = new File("./test-files/tmp/elasticsearch");
		esObj = new EmbeddedElasticsearchServer(clusterName, null, store, true);
		setupClient();
	}
	
	@After
	public void tearDown() throws InterruptedException {
		if (esObj != null) {
			esObj.closeAndDelete();
			esObj = null;
		}
	}
	
	/// To override for differentt test?
	public void setupClient() {
		client = esObj.ElasticsearchClient();
	}
	
	// For manual debugging
	public void debuggingSleep() {
		try {
			Thread.sleep(60 * 60 * 1000);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void notNull() {
		assertNotNull(esObj);
	}
	
	@Test
	public void simpleGetPutGet() {
		// Test data
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("hello", "world");
		
		// Client connection
		ElasticsearchClient client = esObj.ElasticsearchClient();
		assertNotNull(client);
		
		// Empty data check
		assertFalse(client.hasIndex("this"));
		assertNull(client.get("this", "is", "1"));
		
		// Index "table" setup check
		client.createIndexIfNotExists("this");
		client.createIndexIfNotExists("this");
		assertTrue(client.hasIndex("this"));
		
		// Data put, and get
		assertEquals("1", client.put("this", "is", "1", data));
		assertEquals(data, client.get("this", "is", "1"));
	}
	
	@Test
	public void searchQueryAndCount() {
		// Setup
		//----------------------------------
		
		// Test data
		Map<String, Object> data = new HashMap<String, Object>();
		
		// Client & index setup
		assertNotNull(client);
		client.createIndexIfNotExists("this");
		
		// Blank count
		client.refreshIndex();
		assertEquals(0, client.getSearchCount("this", "is", ConvertJSON.toMap("{ \"match_all\" : {} }")));
		
		// Data setup
		//----------------------------------
		data.put("msg", "one");
		data.put("val", 101);
		assertEquals("1", client.put("this", "is", "1", data));
		
		data.put("msg", "to two");
		data.put("val", 102);
		assertEquals("2", client.put("this", "is", "2", data));
		
		data.put("msg", "to three");
		data.put("val", 103);
		assertEquals("3", client.put("this", "is", "3", data));
		
		data.put("msg", "to four");
		data.put("val", 104);
		assertEquals("4", client.put("this", "is", "4", data));
		
		client.refreshIndex("this");
		
		// Count checking
		//----------------------------------
		
		// Full count
		assertEquals(4, client.getSearchCount("this", "is", ConvertJSON.toMap("{ \"match_all\" : {} }")));
		
		// Partial matching
		assertEquals(3, client.getSearchCount("this", "is", ConvertJSON.toMap("{ \"term\" : { \"msg\" : \"to\" } }")));
		
		// Filtered matching
		assertEquals(
			1,
			client.getSearchCount("this", "is",
				ConvertJSON.toMap("{ \"bool\" : { \"filter\" : { \"term\" : { \"val\" : 102 } } } }")));
		
		// Search checking
		//----------------------------------
		
		// Search for ID's
		ArrayList<String> ref3 = new ArrayList<String>();
		ref3.add("3");
		assertEquals(
			ref3,
			client.getSearchMapIds("this", "is",
				ConvertJSON.toMap("{ \"bool\" : { \"filter\" : { \"term\" : { \"val\" : 103 } } } }"), 0, 10));
		
		// Search for map
		ArrayList<Map<String, Object>> ref4 = new ArrayList<Map<String, Object>>();
		ref4.add(data);
		assertEquals(
			ref4,
			client.getSearchMaps("this", "is",
				ConvertJSON.toMap("{ \"bool\" : { \"filter\" : { \"term\" : { \"val\" : 104 } } } }"), 0, 10));
		
	}
}
