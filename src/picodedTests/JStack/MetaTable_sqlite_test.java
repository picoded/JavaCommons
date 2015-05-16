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
		mtObj = new MetaTable(JStackObj, "M"+TestConfig.randomTablePrefix() );
		
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
			m = new MetaTable(JStackObj, "1"+TestConfig.randomTablePrefix() );
			fail(); // if we got here, no exception was thrown, which is bad
		}
		catch (Exception e) {
			final String expected = "Invalid table name (cannot start with numbers)";
			assertTrue( "Missing Exception - "+expected, e.getMessage().indexOf(expected) >= 0 );
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
		assertEquals(guid, mtObj.append(guid, objMap));
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
		
		objMap = randomObjMap();
		assertNotNull(guid = mtObj.append(null, objMap));
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
	}
	
	@Test
	public void basicTestMultiple() throws JStackException {
		for (int a = 0; a < 1000; ++a) {
			basicTest();
		}
	}
	
	@Test
	public void indexBasedTest() throws JStackException {
		
	}
	
}