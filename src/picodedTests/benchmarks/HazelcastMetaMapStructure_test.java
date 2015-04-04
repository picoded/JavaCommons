package picodedTests.benchmarks;

// Target test class
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

// Hazelcast package
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

///
/// Test Case for Hazelcast performance, between different meta map data structures.
///
/// - All meta in 1 object
/// - Split meta fields per object
///
public class HazelcastMetaMapStructure_test {
	
	private final static Logger LOGGER = Logger.getLogger(HazelcastMetaMapStructure_test.class.getName());
	
	String clusterName = null;
	HazelcastInstance hcClient = null;
	
	/// Overwriteable test scale, used to modify the test case intensity
	static int testScale_objCount = 10;
	static int testScale_metaCount = 5;
	
	/// Setsup the hazelcast cluster read / write
	@Before
	public void setUp() throws InterruptedException {
		
		Thread.sleep(250); //prevents socket closing false positive 'error'
		
		clusterName = LocalCacheSetup.setupHazelcastServer();
		
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.getGroupConfig().setName(clusterName);
		clientConfig.setProperty("hazelcast.logging.type", "none");
		
		hcClient = HazelcastClient.newHazelcastClient(clientConfig);
		
		//Warm up the test cache
		allMetaPerKey_write("warmup", "wu", "mp", "vp", testScale_objCount, testScale_metaCount);
		
	}
	
	/// Tears down the hazelcast cluster
	@After
	public void tearDown() throws InterruptedException {
		Thread.sleep(250); //prevents socket closing false positive 'error'
		
		if (hcClient != null) {
			hcClient.shutdown();
			hcClient = null;
		}
		
		Thread.sleep(250); //prevents socket closing false positive 'error'
		
		LocalCacheSetup.teardownHazelcastServer();
	}
	
	public static ArrayList<String> structureName = new ArrayList<String>();
	public static ArrayList<String> structureDesc = new ArrayList<String>();
	public static ArrayList<Long> structureWriteTime = new ArrayList<Long>();
	public static ArrayList<Long> structureReadTime = new ArrayList<Long>();
	
	@BeforeClass
	public static void setupTimingArrays() {
		structureName = new ArrayList<String>();
		structureDesc = new ArrayList<String>();
		structureWriteTime = new ArrayList<Long>();
		structureReadTime = new ArrayList<Long>();
		
		// Setup the test scale
		so_ObjCount = testScale_objCount;
		so_metaCount = testScale_metaCount;
		so_objDec = testScale_objCount + " Objects, " + testScale_metaCount + " meta values";
		
		lo_ObjCount = testScale_objCount;
		lo_metaCount = testScale_metaCount;
		lo_objDec = testScale_objCount + " Objects, " + testScale_metaCount + " meta values";
	}
	
	@AfterClass
	public static void logOutInfoTimings() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n");
		sb.append("==============================================================================\n");
		sb.append("> Completed timing test of " + structureName.size() + " meta map structures\n");
		for (int t = 0; t < structureName.size(); ++t) {
			sb.append("------------------------------------------------------------------------------\n");
			sb.append("[" + structureName.get(t) + "] : " + structureDesc.get(t) + "\n");
			sb.append("> Write timing (milliseconds) : " + (structureWriteTime.get(t).longValue()) + "\n");
			sb.append("> Read timing (milliseconds)  : " + (structureReadTime.get(t).longValue()) + "\n");
		}
		sb.append("==============================================================================\n");
		
		LOGGER.info(sb.toString());
	}
	
	//--------------------------------------
	// Utility functions
	//--------------------------------------
	
	/// Records down test timings to display in post test timing report
	public static void addTimings(String name, String desc, long write, long read) {
		structureName.add(name);
		structureDesc.add(desc);
		structureWriteTime.add(write);
		structureReadTime.add(read);
	}
	
	public int calcSkipOffset(int metaCount, int readPerc) {
		return Math.abs((int) (Math.round(100.0 / ((double) readPerc))));
	}
	
	//@Test
	//public void skipOffsetTest() {
	//	assertEquals(1, calcSkipOffset(0, 100));
	//	assertEquals(2, calcSkipOffset(0, 100));
	//	assertEquals(3, calcSkipOffset(0, 33));
	//	assertEquals(4, calcSkipOffset(0, 25));
	//	assertEquals(5, calcSkipOffset(0, 20));
	//}
	
	//--------------------------------------
	// Utility functions for all meta per object
	//--------------------------------------
	
	/// Utility function that inserts all meta values into a single cache key, and returns the timign
	public long allMetaPerKey_write( //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount //
	) {
		long startTime = System.currentTimeMillis();
		int oc, mc;
		
		Map<String, Object> hcMap = hcClient.getMap(mapName);
		HashMap<String, String> objectMap;
		
		for (oc = 0; oc < objCount; ++oc) {
			objectMap = new HashMap<String, String>();
			for (mc = 0; mc < metaCount; ++mc) {
				objectMap.put(metaPrefix + mc, valuePrefix + mc);
			}
			hcMap.put(objPrefix + oc, objectMap);
		}
		
		long endinTime = System.currentTimeMillis();
		return (endinTime - startTime);
	}
	
	/// Utility function that inserts all meta values into a single cache key, and returns the timign
	public long allMetaPerKey_read( //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount, int readPerc //
	) {
		int skipOffset = calcSkipOffset(metaCount, readPerc);
		
		long startTime = System.currentTimeMillis();
		int oc, mc;
		
		Map<String, Map<String, String>> hcMap = hcClient.getMap(mapName);
		Map<String, String> objectMap;
		
		for (oc = 0; oc < objCount; ++oc) {
			objectMap = hcMap.get(objPrefix + oc);
			for (mc = 0; mc < metaCount; mc += skipOffset) {
				if (!(valuePrefix + mc).equals(objectMap.get(metaPrefix + mc))) {
					throw new RuntimeException("Invalid value found in cache");
				}
			}
		}
		
		long endinTime = System.currentTimeMillis();
		return (endinTime - startTime);
	}
	
	/// Utility function that reads the various meta values, and log the timings
	public void allMetaPerKey_shared( //
		String structName, String structDesc, //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount, int readPerc //
	) {
		
		long write = allMetaPerKey_write(mapName, objPrefix, metaPrefix, valuePrefix, objCount, metaCount);
		long read = allMetaPerKey_read(mapName, objPrefix, metaPrefix, valuePrefix, objCount, metaCount, readPerc);
		
		addTimings(structName, structDesc, write, read);
	}
	
	//--------------------------------------
	// Utility functions for multiple meta per object
	//--------------------------------------
	
	/// Utility function that inserts split meta values into a single cache key, and returns the timign
	public long splitMetaPerKey_write( //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount //
	) {
		long startTime = System.currentTimeMillis();
		int oc, mc;
		
		Map<String, Object> hcMap = hcClient.getMap(mapName);
		
		for (oc = 0; oc < objCount; ++oc) {
			for (mc = 0; mc < metaCount; ++mc) {
				hcMap.put(objPrefix + oc + "$" + metaPrefix + mc, valuePrefix + mc);
			}
		}
		
		long endinTime = System.currentTimeMillis();
		return (endinTime - startTime);
	}
	
	/// Utility function that inserts split meta values into a single cache key, and returns the timign
	public long splitMetaPerKey_read( //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount, int readPerc //
	) {
		int skipOffset = calcSkipOffset(metaCount, readPerc);
		
		long startTime = System.currentTimeMillis();
		int oc, mc;
		
		Map<String, Map<String, String>> hcMap = hcClient.getMap(mapName);
		Map<String, String> objectMap;
		
		for (oc = 0; oc < objCount; ++oc) {
			for (mc = 0; mc < metaCount; mc += skipOffset) {
				if (!(valuePrefix + mc).equals(hcMap.get(objPrefix + oc + "$" + metaPrefix + mc))) {
					throw new RuntimeException("Invalid value found in cache");
				}
			}
		}
		
		long endinTime = System.currentTimeMillis();
		return (endinTime - startTime);
	}
	
	/// Utility function that reads the various meta values, and log the timings
	public void splitMetaPerKey_shared( //
		String structName, String structDesc, //
		String mapName, //
		String objPrefix, String metaPrefix, String valuePrefix, //
		int objCount, int metaCount, int readPerc //
	) {
		
		long write = splitMetaPerKey_write(mapName, objPrefix, metaPrefix, valuePrefix, objCount, metaCount);
		long read = splitMetaPerKey_read(mapName, objPrefix, metaPrefix, valuePrefix, objCount, metaCount, readPerc);
		
		addTimings(structName, structDesc, write, read);
	}
	
	///
	/// Test for SMALL Object
	///
	
	static int so_ObjCount = 10000;
	static int so_metaCount = 50;
	static String so_objDec = "10,000 Objects, 50 meta values";
	static String so_metaValue = "value";
	
	@Test
	public void soAll_100() {
		allMetaPerKey_shared(//
			"ALL SMALL Object - 100% Usage", "ALL Key to 1 Object, " + so_objDec + ", 100% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 100);
	}
	
	@Test
	public void soAll_50() {
		allMetaPerKey_shared(//
			"ALL SMALL Object - 50% Usage", "ALL Key to 1 Object, " + so_objDec + ", 50% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 50);
	}
	
	@Test
	public void soAll_33() {
		allMetaPerKey_shared(//
			"ALL SMALL Object - 33% Usage", "ALL Key to 1 Object, " + so_objDec + ", 33% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 33);
	}
	
	@Test
	public void soAll_25() {
		allMetaPerKey_shared(//
			"ALL SMALL Object - 25% Usage", "ALL Key to 1 Object, " + so_objDec + ", 25% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 25);
	}
	
	@Test
	public void soSplit_100() {
		splitMetaPerKey_shared(//
			"SPLIT SMALL Object - 100% Usage", "SPLIT Key to 1 Object, " + so_objDec + ", 100% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 100);
	}
	
	@Test
	public void soSplit_50() {
		splitMetaPerKey_shared(//
			"SPLIT SMALL Object - 50% Usage", "SPLIT Key to 1 Object, " + so_objDec + ", 50% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 50);
	}
	
	@Test
	public void soSplit_33() {
		splitMetaPerKey_shared(//
			"SPLIT SMALL Object - 33% Usage", "SPLIT Key to 1 Object, " + so_objDec + ", 33% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 33);
	}
	
	@Test
	public void soSplit_25() {
		splitMetaPerKey_shared(//
			"SPLIT SMALL Object - 25% Usage", "SPLIT Key to 1 Object, " + so_objDec + ", 25% read rate", //
			"soObj", "so", "meta", so_metaValue, so_ObjCount, so_metaCount, 25);
	}
	
	///
	/// Test for LARGE Object
	///
	
	static int lo_ObjCount = 10000;
	static int lo_metaCount = 50;
	static String lo_objDec = "10,000 Objects, 50 meta values";
	static String lo_metaValue = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ullamcorper eros ac tortor venenatis, vel elementum velit rutrum. Morbi nec magna vitae libero mollis suscipit vitae vel est. Maecenas et massa aliquet tellus ultricies tristique. Fusce in odio odio. Cras quis tellus tortor. Aenean ac arcu vitae nisl molestie tempor congue nec odio. Phasellus ut tristique mi. Nullam ut maximus massa. Duis a neque sapien. Vivamus quis sagittis est, vitae blandit neque. Nulla congue iaculis orci in hendrerit. Ut pulvinar lacus sit amet rutrum eleifend.";
	
	@Test
	public void loAll_100() {
		allMetaPerKey_shared(//
			"ALL LARGE Object - 100% Usage", "ALL Key to 1 Object, " + lo_objDec + ", 100% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 100);
	}
	
	@Test
	public void loAll_50() {
		allMetaPerKey_shared(//
			"ALL LARGE Object - 50% Usage", "ALL Key to 1 Object, " + lo_objDec + ", 50% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 50);
	}
	
	@Test
	public void loAll_33() {
		allMetaPerKey_shared(//
			"ALL LARGE Object - 33% Usage", "ALL Key to 1 Object, " + lo_objDec + ", 33% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 33);
	}
	
	@Test
	public void loAll_25() {
		allMetaPerKey_shared(//
			"ALL LARGE Object - 25% Usage", "ALL Key to 1 Object, " + lo_objDec + ", 25% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 25);
	}
	
	@Test
	public void loSplit_100() {
		splitMetaPerKey_shared(//
			"SPLIT LARGE Object - 100% Usage", "SPLIT Key to 1 Object, " + lo_objDec + ", 100% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 100);
	}
	
	@Test
	public void loSplit_50() {
		splitMetaPerKey_shared(//
			"SPLIT LARGE Object - 50% Usage", "SPLIT Key to 1 Object, " + lo_objDec + ", 50% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 50);
	}
	
	@Test
	public void loSplit_33() {
		splitMetaPerKey_shared(//
			"SPLIT LARGE Object - 33% Usage", "SPLIT Key to 1 Object, " + lo_objDec + ", 33% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 33);
	}
	
	@Test
	public void loSplit_25() {
		splitMetaPerKey_shared(//
			"SPLIT LARGE Object - 25% Usage", "SPLIT Key to 1 Object, " + lo_objDec + ", 25% read rate", //
			"loObj", "so", "meta", lo_metaValue, lo_ObjCount, lo_metaCount, 25);
	}
	
}
