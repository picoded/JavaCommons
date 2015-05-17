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

import picodedTests.TestConfig;

public class MetaTable_sqlite_test {
	
	// JStack setup
	//-----------------------------------------------
	
	protected JStack JStackObj = null;
	
	protected void JStackSetup() {
		JStackObj = new JStack(JSql.sqlite());
	}
	
	protected void JStackTearDown() {
		JStackObj = null;
	}
	
	// Metatable setup
	//-----------------------------------------------
	
	protected MetaTable mtObj = null;
	
	protected void mtObjSetup() throws JStackException {
		mtObj = new MetaTable(JStackObj, "M" + TestConfig.randomTablePrefix());
		
		mtObj.putType("num", new MetaType(MetaType.TYPE_INTEGER));
		mtObj.putType("str", new MetaType(MetaType.TYPE_STRING));
		
		mtObj.stackSetup();
	}
	
	protected void mtObjTearDown() {
		
	}
	
	@Test
	public void invalidSetup() {
		MetaTable m;
		
		try {
			m = new MetaTable(JStackObj, "1" + TestConfig.randomTablePrefix());
			fail(); // if we got here, no exception was thrown, which is bad
		} catch (Exception e) {
			final String expected = "Invalid table name (cannot start with numbers)";
			assertTrue("Missing Exception - " + expected, e.getMessage().indexOf(expected) >= 0);
		}
	}
	
	// Actual setup / teardown
	//-----------------------------------------------
	
	@Before
	public void setUp() throws JStackException {
		JStackSetup();
		mtObjSetup();
	}
	
	@After
	public void tearDown() {
		mtObjTearDown();
		JStackTearDown();
	}
	
	// Test cases
	//-----------------------------------------------
	
	HashMap<String, Object> randomObjMap() {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
		objMap.put(GUID.base58(), GUID.base58());
		objMap.put(GUID.base58(), GUID.base58());
		
		objMap.put("num", RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put("str", GUID.base58());
		
		return objMap;
	}
	
	@Test
	public void constructor() {
		assertNotNull(JStackObj);
	}
	
	@Test
	public void basicTest() throws JStackException {
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		
		HashMap<String, Object> objMap = randomObjMap();
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
		
		objMap = randomObjMap();
		assertNotNull(guid = mtObj.append(null, objMap)._oid());
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void basicTestMultiple() throws JStackException {
		
		// Useful for debugging
		JStackObj = new JStack(JSql.sqlite("./test-files/tmp/sqliteTest.db"));
		mtObjSetup();
		
		int iteration = 100;
		for (int a = 0; a < iteration; ++a) {
			basicTest();
		}
		
		Map<String, ArrayList<Object>> qRes = null;
		
		assertNotNull(qRes = mtObj.queryData(null, null, null));
		assertNotNull(qRes.get("_oid"));
		assertEquals(iteration * 2, qRes.get("_oid").size());
	}
	
	HashMap<String, Object> genNumStrObj(int number, String str) {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(number));
		objMap.put("str", str);
		return objMap;
	}
	
	@Test
	public void indexBasedTest() throws JStackException {
		
		mtObj.append(null, genNumStrObj(1, "this"));
		mtObj.append(null, genNumStrObj(2, "is"));
		mtObj.append(null, genNumStrObj(3, "hello"));
		mtObj.append(null, genNumStrObj(4, "world"));
		mtObj.append(null, genNumStrObj(5, "program"));
		mtObj.append(null, genNumStrObj(6, "in"));
		mtObj.append(null, genNumStrObj(7, "this"));
		
		Map<String, ArrayList<Object>> qRes = null;
		assertNotNull(qRes = mtObj.queryData(null, null, null));
		assertEquals(7, qRes.get("_oid").size());
		
		assertNotNull(qRes = mtObj.queryData(null, "num > ? AND num < ?", new Object[] { 2, 5 }, "num ASC"));
		assertEquals(2, qRes.get("_oid").size());
		assertEquals("hello", qRes.get("str").get(0));
		assertEquals("world", qRes.get("str").get(1));
		
		assertNotNull(qRes = mtObj.queryData(null, "str = ?", new Object[] { "this" }));
		assertEquals(2, qRes.get("_oid").size());
		
		assertNotNull(qRes = mtObj.queryData(null, "num > ?", new Object[] { 2 }, "num ASC", 2, 2));
		assertEquals(2, qRes.get("_oid").size());
		assertEquals("program", qRes.get("str").get(0));
		assertEquals("in", qRes.get("str").get(1));
		
	}
	
}