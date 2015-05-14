package picodedTests.JStack;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;

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
		mtObj = new MetaTable(JStackObj);
		mtObj.stackSetup();
	}
	
	protected void mtObjTearDown() {
		
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
	
	@Test
	public void constructor() {
		assertNotNull(JStackObj);
	}
	
	@Test
	public void basicTest() throws JStackException {
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		
		HashMap<String, Object> objMap = null;
		
		objMap = new HashMap<String, Object>();
		objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
		objMap.put(GUID.base58(), GUID.base58());
		objMap.put(GUID.base58(), GUID.base58());
		
		assertEquals(guid, mtObj.put(guid, objMap));
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
		
		objMap = new HashMap<String, Object>();
		objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
		objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
		objMap.put(GUID.base58(), GUID.base58());
		objMap.put(GUID.base58(), GUID.base58());
		
		assertNotNUll(guid = mtObj.put(null, objMap));
		assertEquals(guid, mtObj.put(guid, objMap));
	}
	
	@Test
	public void basicTestMultiple() throws JStackException {
		for (int a = 0; a < 5000; ++a) {
			basicTest();
		}
	}
	
}