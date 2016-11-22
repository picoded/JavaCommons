package picoded.JStruct;

// Target test class
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
// Test Case include
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JStruct_test {
	
	private static JStruct jStructObj = new JStruct();
	private static KeyValueMap keyValueMap;
	private static MetaTable metaTable;
	private static AccountTable accountTable;
	private static AtomicLongMap atomicLongMap;
	
	@BeforeClass
	public static void setUp() {
		jStructObj.systemSetup();
	}
	
	@AfterClass
	public static void tearDown() {
		jStructObj.systemTeardown();
		jStructObj = null;
	}
	
	@Test
	public void getKeyValueMapTest() {
		ConcurrentHashMap<String, KeyValueMap> keyValueMapCache = new ConcurrentHashMap<String, KeyValueMap>();
		keyValueMap = jStructObj.getKeyValueMap("test");
		keyValueMapCache.put("TEST", keyValueMap);
		jStructObj.keyValueMapCache = keyValueMapCache;
		keyValueMap = jStructObj.getKeyValueMap("test");
		assertNotNull(keyValueMap);
		assertTrue(keyValueMap.isEmpty());
	}
	
	@Test
	public void getMetaTableTest() {
		ConcurrentHashMap<String, MetaTable> metaTableCache = new ConcurrentHashMap<String, MetaTable>();
		metaTable = jStructObj.getMetaTable("test");
		metaTableCache.put("TEST", metaTable);
		jStructObj.metaTableCache = metaTableCache;
		metaTable = jStructObj.getMetaTable("test");
		assertNotNull(metaTable);
		assertTrue(metaTable.isEmpty());
	}
	
	@Test
	public void getAccountTableTest() {
		ConcurrentHashMap<String, AccountTable> accountTableCache = new ConcurrentHashMap<String, AccountTable>();
		accountTable = jStructObj.getAccountTable("test");
		accountTableCache.put("TEST", accountTable);
		jStructObj.accountTableCache = accountTableCache;
		accountTable = jStructObj.getAccountTable("test");
		assertNotNull(accountTable);
		assertTrue(accountTable.isEmpty());
	}
	
	@Test
	public void getAtomicLongMapTest() {
		ConcurrentHashMap<String, AtomicLongMap> atomicLongMapCache = new ConcurrentHashMap<String, AtomicLongMap>();
		atomicLongMap = jStructObj.getAtomicLongMap("test");
		atomicLongMapCache.put("TEST", atomicLongMap);
		jStructObj.atomicLongMapCache = atomicLongMapCache;
		atomicLongMap = jStructObj.getAtomicLongMap("test");
		assertNotNull(accountTable);
		assertTrue(accountTable.isEmpty());
	}
	
	@Test
	public void preloadJStructTypeTest() {
		jStructObj.preloadJStructType("AccountTable", "AccountTable");
		jStructObj.preloadJStructType("MetaTable", "MetaTable");
		jStructObj.preloadJStructType("KeyValueMap", "KeyValueMap");
		jStructObj.preloadJStructType("AtomicLongMap", "AtomicLongMap");
	}
	
	@Test(expected = Exception.class)
	public void preloadJStructTypeTest1() throws Exception {
		jStructObj.preloadJStructType("test", "test");
	}
}