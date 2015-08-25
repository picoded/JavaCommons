package picodedTests.JStruct;

// Target test class
import picoded.JStruct.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.commons.lang3.RandomUtils;

import picoded.conv.*;
import picoded.struct.*;

import picodedTests.TestConfig;

// MetaTable base test class
public class MetaTable_test {
	
	/// Test object
	public MetaTable mtObj = null;
	
	/// To override for implementation
	///------------------------------------------------------
	public MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}
	
	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		mtObj = implementationConstructor();
		mtObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		if( mtObj != null ) {
			mtObj.systemTeardown();
		}
		mtObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(mtObj);
		
		//run maintaince, no exception?
		// mtObj.maintenance();
	}
	
	// Test cases
	//-----------------------------------------------
	
	// Test utility used to generate random maps
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

	// @Test
	// public void invalidSetup() { //Numeric as table prefix tend to cuase problems
	// 	MetaTable m;
	// 
	// 	try {
	// 		m = new MetaTable(JStackObj, "1" + TestConfig.randomTablePrefix());
	// 		fail(); // if we got here, no exception was thrown, which is bad
	// 	} catch (Exception e) {
	// 		final String expected = "Invalid table name (cannot start with numbers)";
	// 		assertTrue("Missing Exception - " + expected, e.getMessage().indexOf(expected) >= 0);
	// 	}
	// }

	@Test
	public void newObjectTest() {
		MetaObject moObj = null;
		
		assertNotNull( moObj = mtObj.newObject() );
		moObj.put("be", "happy");
		moObj.saveDelta();
		
		String guid = null;
		assertNotNull( guid = moObj._oid() );
		
		assertNotNull( moObj = mtObj.get(guid) );
		assertEquals( "happy", moObj.get("be") );
	}
	
	@Test
	public void basicTest()  {
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
	
		HashMap<String, Object> objMap = randomObjMap();
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
	
		objMap.put("_oid", guid);
		assertEquals(objMap, (Map<String,Object>)mtObj.get(guid));
	
		objMap = randomObjMap();
		assertNotNull(guid = mtObj.append(null, objMap)._oid());
		objMap.put("_oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	/// Checks if a blank object gets saved
	@Test
	public void blankObjectSave()  {
		String guid = null;
		MetaObject p = null;
		assertFalse( mtObj.containsKey("hello") );
		assertNotNull( p = mtObj.newObject() );
		assertNotNull( guid = p._oid() );
		p.saveDelta();
		
		assertTrue( mtObj.containsKey(guid) );
	}
	
	HashMap<String, Object> genNumStrObj(int number, String str) {
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(number));
		objMap.put("str_val", str);
		return objMap;
	}
	
	@Test
	public void indexBasedTest()  {
	
		mtObj.append(null, genNumStrObj(1, "this"));
		mtObj.append(null, genNumStrObj(2, "is"));
		mtObj.append(null, genNumStrObj(3, "hello"));
		mtObj.append(null, genNumStrObj(4, "world"));
		mtObj.append(null, genNumStrObj(5, "program"));
		mtObj.append(null, genNumStrObj(6, "in"));
		mtObj.append(null, genNumStrObj(7, "this"));
		
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(7, qRes.length);
		
		assertNotNull(qRes = mtObj.query("num > ? AND num < ?", new Object[] { 2, 5 }, "num ASC"));
		assertEquals(2, qRes.length);
		assertEquals("hello", qRes[0].get("str_val"));
		assertEquals("world", qRes[1].get("str_val"));
		
		assertNotNull(qRes = mtObj.query("str_val = ?", new Object[] { "this" }));
		assertEquals(2, qRes.length);
		
		assertNotNull(qRes = mtObj.query("num > ?", new Object[] { 2 }, "num ASC", 2, 2));
		assertEquals(2, qRes.length);
		assertEquals("program", qRes[0].get("str_val"));
		assertEquals("in", qRes[1].get("str_val"));
	
	}
	
	///
	/// An exception occurs, if a query fetch occurs with an empty table
	///
	@Test
	public void issue47_exceptionWhenTableIsEmpty()  {
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(0, qRes.length);
	}
	
	///
	/// Bad view index due to inner join instead of left join. Testing.
	///
	/// AKA: Incomplete object does not appear in view index
	///
	@Test
	public void innerJoinFlaw()  {
		mtObj.append(null, genNumStrObj(1, "hello world"));
	
		HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("num", new Integer(2));
		mtObj.append( null, objMap );
	
		objMap = new CaseInsensitiveHashMap<String, Object>();
		objMap.put("str_val", "nope");
		mtObj.append( null, objMap );
	
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(3, qRes.length);
	
		assertNotNull(qRes = mtObj.query("str_val = ?", new Object[] { "nope" }));
		assertEquals(1, qRes.length);
	
		assertNotNull(qRes = mtObj.query("num = ?", new Object[] { 1 }));
		assertEquals(1, qRes.length);
	
		assertNotNull(qRes = mtObj.query("num <= ?", new Object[] { 2 }));
		assertEquals(2, qRes.length);
	}
	
	@Test
	public void missingStrError()  {
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("num", 123);
	
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
	
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(1, qRes.length);
	
		objMap.put("_oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void missingNumWithSomeoneElse()  {
		mtObj.append(null, genNumStrObj(1, "hello world"));
	
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("str_val", "^_^");
	
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
	
		MetaObject[] qRes = null;
		assertNotNull(qRes = mtObj.query(null, null));
		assertEquals(2, qRes.length);
	
		assertTrue( guid.equals(qRes[0]._oid()) || guid.equals(qRes[1]._oid()) );
	
		objMap.put("_oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void getFromKeyName_basic()  {
		
		mtObj.append(null, genNumStrObj(1, "one"));
		mtObj.append(null, genNumStrObj(2, "two"));
		
		MetaObject[] list = null;
		assertNotNull( list = mtObj.getFromKeyName("num") );
		assertEquals( 2, list.length );
		
		String str = null;
		assertNotNull( str = list[0].getString("str_val") );
		assertTrue( str.equals("one") || str.equals("two") );
		
		assertNotNull( str = list[1].getString("str_val") );
		assertTrue( str.equals("one") || str.equals("two") );
		
	}
	
	@Test
	public void nonIndexedKeySaveCheck()  {
		
		// Generates single node
		mtObj.append(null, genNumStrObj(1, "hello world"));
		MetaObject[] list = null;
		MetaObject node = null;
		
		// Fetch that single node
		assertNotNull( list = mtObj.getFromKeyName("num") );
		assertEquals( 1, list.length );
		assertNotNull( node = list[0] );
		
		// Put non indexed key in node, and save
		node.put("NotIndexedKey", "123");
		node.saveDelta();
		
		// Get the value, to check
		assertEquals( "123", mtObj.get( node._oid() ).get("NotIndexedKey") );
		
		// Refetch node, and get data, and validate
		assertNotNull( list = mtObj.getFromKeyName("num") );
		assertEquals( 1, list.length );
		assertNotNull( list[0] );
		assertEquals( node._oid(), list[0]._oid() );
		assertEquals( "123", node.get("NotIndexedKey") );
		assertEquals( "123", list[0].get("NotIndexedKey") );
	}
	
	@Test
	public void getFromKeyName_customKeys()  {
		
		// Generates single node
		mtObj.append(null, genNumStrObj(1, "hello world"));
		MetaObject[] list = null;
		MetaObject node = null;
		
		// Fetch that single node
		assertNotNull( list = mtObj.getFromKeyName("num") );
		assertEquals( 1, list.length );
		assertNotNull( node = list[0] );
		
		// Put non indexed key in node, and save
		node.put("NotIndexedKey", "123");
		node.saveDelta();
		
		// Refetch node, and get data, and validate
		assertNotNull( list = mtObj.getFromKeyName("num") );
		assertEquals( 1, list.length );
		assertNotNull( list[0] );
		assertEquals( node._oid(), list[0]._oid() );
		assertEquals( "123", node.get("NotIndexedKey") );
		assertEquals( "123", list[0].get("NotIndexedKey") );
		
		// Fetch non indexed key
		assertNotNull( list = mtObj.getFromKeyName("NotIndexedKey") );
		assertEquals( 1, list.length );
		
		// Assert equality
		assertEquals( node._oid(), list[0]._oid() );
		
	}
	
	// Array values tests
	//-----------------------------------------------
	@Test
	public void jsonStorageTest()  {
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("name", "Hello");
		data.put("arrs", new ArrayList<String>( Arrays.asList(new String[] { "oh", "no" }) ) );
		
		MetaObject mo = null;
		assertNotNull( mo = mtObj.append(null, data) );
		mo.saveDelta();
		
		MetaObject to = null;
		assertNotNull( to = mtObj.get( mo._oid() ) );
		
		data.put("_oid", mo._oid());
		assertEquals(data, to);
	}
	
	// Mapping tests
	//-----------------------------------------------
	
	@Test
	public void testSingleMappingSystem() {
		mtObj.typeMap().clear();
		
		mtObj.putType("num", "INTEGER");
		mtObj.putType("float", "FLOAT");
		mtObj.putType("double", "double");
		mtObj.putType("long", "long");
		
		assertEquals(mtObj.getType("num"), MetaType.INTEGER);
		assertEquals(mtObj.getType("float"), MetaType.FLOAT);
		assertEquals(mtObj.getType("double"), MetaType.DOUBLE);
		assertEquals(mtObj.getType("long"), MetaType.LONG);
	}
	
	@Test
	public void testMapMappingSystem() {
		mtObj.typeMap().clear();
	
		HashMap<String, Object> mapping = new HashMap<String, Object>();
		mapping.put("num", "INTEGER");
		mapping.put("float", "FLOAT");
		mapping.put("double", "double");
		mapping.put("long", "long");
		mapping.put("mixed", "MIXED");
		mapping.put("uuid-array", "UUID_ARRAY");
	
		mtObj.setMappingType(mapping);
	
		assertEquals(mtObj.getType("num"), MetaType.INTEGER);
		assertEquals(mtObj.getType("float"), MetaType.FLOAT);
		assertEquals(mtObj.getType("double"), MetaType.DOUBLE);
		assertEquals(mtObj.getType("long"), MetaType.LONG);
		assertEquals(mtObj.getType("mixed"), MetaType.MIXED);
		assertEquals(mtObj.getType("uuid-array"), MetaType.UUID_ARRAY);
	}
	
}
