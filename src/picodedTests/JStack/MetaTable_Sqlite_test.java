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

public class MetaTable_Sqlite_test {
	
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
	
	protected String mtTableName = null;
	
	protected MetaTable mtObj = null;
	
	protected void mtObjSetup() throws JStackException {
		mtTableName = "M" + TestConfig.randomTablePrefix();
		
		mtObj = new MetaTable(JStackObj, mtTableName);
		
		mtObj.putType("num", new MetaType(MetaType.TYPE_INTEGER));
		mtObj.putType("str_val", new MetaType(MetaType.TYPE_STRING));
		
		mtObj.stackSetup();
	}
	
	protected void mtObjTearDown() throws JStackException {
		mtObj.stackTeardown();
	}
	
	@Test
	public void stringTest() {
		assertEquals("hello", ("hello").toString() );
		assertEquals("hello", ((Object)"hello").toString() );
	}
	
	@Test
	public void testSingleMappingSystem() throws JStackException
	{
		//System.out.println("Starting single mapping test");
		mtObj.clearTypeMapping();
		
		mtObj.putType("num", "INTEGER");
		mtObj.putType("float", "TYPE_FLOAT");
		mtObj.putType("double", "double");
		mtObj.putType("long", "type_long");
		
		assertEquals(mtObj.getType("num").valueType(), MetaType.TYPE_INTEGER);
		assertEquals(mtObj.getType("float").valueType(), MetaType.TYPE_FLOAT);
		assertEquals(mtObj.getType("double").valueType(), MetaType.TYPE_DOUBLE);
		assertEquals(mtObj.getType("long").valueType(), MetaType.TYPE_LONG);
	}
	
	@Test
	public void testMapMappingSystem() throws JStackException
	{
		//System.out.println("Starting map mapping test");
		mtObj.clearTypeMapping();
		
		HashMap<String, Object> mapping = new HashMap<String, Object>();
		mapping.put("num", "INTEGER");
		mapping.put("float", "TYPE_FLOAT");
		mapping.put("double", "double");
		mapping.put("long", "type_long");
		mapping.put("disabled", "disabled");
		mapping.put("mixed", "TYPE_MIXED");
		mapping.put("mixed-array", "type_mixed_array");
		
		mtObj.setMapping(mapping);
		
		assertEquals(mtObj.getType("num").valueType(), MetaType.TYPE_INTEGER);
		assertEquals(mtObj.getType("float").valueType(), MetaType.TYPE_FLOAT);
		assertEquals(mtObj.getType("double").valueType(), MetaType.TYPE_DOUBLE);
		assertEquals(mtObj.getType("long").valueType(), MetaType.TYPE_LONG);
		assertEquals(mtObj.getType("disabled").valueType(), MetaType.TYPE_DISABLED);
		assertEquals(mtObj.getType("mixed").valueType(), MetaType.TYPE_MIXED);
		assertEquals(mtObj.getType("mixed-array").valueType(), MetaType.TYPE_MIXED_ARRAY);
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
	public void tearDown() throws JStackException {
		mtObjTearDown();
		JStackTearDown();
	}
	
	// Test cases
	//-----------------------------------------------
	
	protected HashMap<String, Object> randomObjMap() {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
		objMap.put(GUID.base58(), GUID.base58());
		objMap.put(GUID.base58(), GUID.base58());
		
		objMap.put("num", RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put("str_val", GUID.base58());
		
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
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(iteration * 2, qRes.length);
	}
	
	HashMap<String, Object> genNumStrObj(int number, String str) {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(number));
		objMap.put("str_val", str);
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
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(7, qRes.length);
		
		assertNotNull(qRes = mtObj.queryObjects("num > ? AND num < ?", new Object[] { 2, 5 }, "num ASC"));
		assertEquals(2, qRes.length);
		assertEquals("hello", qRes[0].get("str_val"));
		assertEquals("world", qRes[1].get("str_val"));
		
		assertNotNull(qRes = mtObj.queryObjects("str_val LIKE ?", new Object[] { "this" }));
		assertEquals(2, qRes.length);
		
		assertNotNull(qRes = mtObj.queryObjects("num > ?", new Object[] { 2 }, "num ASC", 2, 2));
		assertEquals(2, qRes.length);
		assertEquals("program", qRes[0].get("str_val"));
		assertEquals("in", qRes[1].get("str_val"));
		
	}
	
	
	///
	/// An exception occurs, if a query fetch occurs with an empty table
	///
	@Test
	public void issue47_exceptionWhenTableIsEmpty() throws JStackException {
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(0, qRes.length);
	}
	
	///
	/// Bad view index due to inner join instead of left join. Testing.
	///
	/// AKA: Incomplete object does not appear in view index
	///
	@Test
	public void innerJoinFlaw() throws JStackException {
		mtObj.append(null, genNumStrObj(1, "hello world"));
		
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(2));
		mtObj.append( null, objMap );
		
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("str_val", "nope");
		mtObj.append( null, objMap );
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(3, qRes.length);
		
		assertNotNull(qRes = mtObj.queryObjects("str_val LIKE ?", new Object[] { "nope" }));
		assertEquals(1, qRes.length);
		
		assertNotNull(qRes = mtObj.queryObjects("num = ?", new Object[] { 1 }));
		assertEquals(1, qRes.length);
		
		assertNotNull(qRes = mtObj.queryObjects("num <= ?", new Object[] { 2 }));
		assertEquals(2, qRes.length);
	}
	
	///
	/// Bad view index part 2?
	///
	/// Ok, its actually a bug, where objects with 1 or less parameters are "ignored" (not including oid)
	///
	@Test
	public void missingNumError() throws JStackException {
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("str_val", "^_^");
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(1, qRes.length);
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void missingStrError() throws JStackException {
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("num", 123);
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(1, qRes.length);
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void missingNumWithSomeoneElse() throws JStackException {
		mtObj.append(null, genNumStrObj(1, "hello world"));
		
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("str_val", "^_^");
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.queryObjects(null, null));
		assertEquals(2, qRes.length);
		
		assertTrue( guid.equals(qRes[0]._oid()) || guid.equals(qRes[1]._oid()) );
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
}