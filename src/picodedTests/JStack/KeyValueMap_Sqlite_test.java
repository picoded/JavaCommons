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

public class KeyValueMap_Sqlite_test {
	
	@Test
	public void blank() {
		assertTrue(true);
	}
	
	/*
	 
	protected jSql jSqlObj;
	protected keyValuePair kvpObj;
	
	@Before
	public void setUp() throws jSqlException {
		//create connection
		jSqlObj = jSql.sqlite();
		
		//resets clean slate
		jSqlObj.execute( "DROP TABLE IF EXISTS KeyValueTest" );
		kvpObj = new keyValuePair(jSqlObj, "KeyValueTest");
		kvpObj.tableSetup();
	}
	
	@After
	public void tearDown() {
		if( jSqlObj != null ) {
			jSqlObj.dispose();
			jSqlObj = null;
		}
		kvpObj = null;
	}
	
	@Test
	public void constructorTest() {
		assertNotNull("jSql constructed object must not be null", jSqlObj);
		assertNotNull("keyValuePair constructed object must not be null", kvpObj);
	}
	
	@Test
	public void putkeyValue_againstKeyAndValue() throws jSqlException{
		String kStr = "hello", kyStr = "hi";
		String vStr = "world", vlStr = "world";
		String clrValues = kvpObj.clear();
		assertNull(clrValues);
		assertTrue(kvpObj.put(kStr, vStr));
		
		assertTrue(kvpObj.put(kyStr, vlStr));
		
		String getValueA =  kvpObj.get(kStr);
		assertEquals("GetValue rd1", getValueA, vStr );
		
		String getValueB =  kvpObj.get(kyStr);
		assertEquals("GetValue rd1", getValueB, vlStr );
	}
	
	@Test
	public void putkeyValue_expectedExceptions() throws jSqlException{
		String kStr = "hello", kyStr = "hi",keyStr = "how" ;
		String vStr = "world", vlStr = "world";
		kvpObj.clear();
		
		assertTrue(kvpObj.put(kStr, vStr));
		assertTrue(kvpObj.put(kyStr, vlStr));
		
		try {
			kvpObj.put(kStr, vStr);
		} catch (jSqlException e) {
			// TODO Auto-generated catch block
			assertNotNull("Exception caught as intended", e);
			
		}
		
		try {
			kvpObj.put(null, null);
			
		} catch (jSqlException e) {
			// TODO Auto-generated catch block
			assertNotNull("Exception caught as intended", e);
			
		}
		
		try {
			kvpObj.put(keyStr, null);
			
		} catch (jSqlException e) {
			// TODO Auto-generated catch block
			assertNotNull("Exception caught as intended", e);
			
		}
	}
	
	@Test
	public void removeValue_againstKeyAndValue() throws jSqlException{
		String kStr = "hello", kyStr = "hi";
		String vStr = "world", vlStr = "world";
		String clrValues = kvpObj.clear();
		assertEquals("Remove values equals", clrValues, null);
		assertTrue(kvpObj.put(kStr, vStr));
		
		assertTrue(kvpObj.put(kyStr, vlStr));
		
		assertTrue("Remove value kStr", kvpObj.remove(kStr));
		assertTrue("Remove values vStr and vlStr", kvpObj.removeValues(vStr));
	}
	
	@Test
	public void removeValue_expectedExceptions() throws jSqlException{
		String kStr = null;
		try {
			kvpObj.remove(kStr);
			
		} catch (jSqlException e) {
			// TODO Auto-generated catch block
			assertNotNull("Exception caught as intended", e);
			
		}
		
	}
	
	@Test
	public void removeValues_isEmptyTest() throws jSqlException{
		String kStr = "hello", kyStr = "hi", keyStr = "how" ;
		String vStr = "world", vlStr = "world", valStr = "hworld";
		String clrValues = kvpObj.clear();
		assertNull(clrValues);
		
		
		assertTrue(kvpObj.put(kStr, vStr));
		assertTrue(kvpObj.put(kyStr, vlStr));
		assertTrue(kvpObj.put(keyStr, valStr));
		
		assertTrue("Remove value kStr", kvpObj.remove(kStr));
		assertTrue("Remove values vStr and vlStr", kvpObj.removeValues(vStr));
		assertFalse("Check for empty after remove values",kvpObj.isEmpty());
		
		clrValues = kvpObj.clear();
		assertEquals("Remove values equals", clrValues, null);
		
		assertTrue("Empty after remove values",kvpObj.isEmpty());
	}
	
	@Test
	public void containsTest_againstKeyAndValue() throws jSqlException{
		String kStr = "hello", kyStr = "hi", keyStr = "how" ;
		String vStr = "world", vlStr = "world", valStr = "hworld";
		
		String clrValues = kvpObj.clear();
		assertEquals("Remove values equals", clrValues, null);
		assertTrue(kvpObj.put(kStr, vStr));
		assertTrue(kvpObj.put(kyStr, vlStr));
		assertTrue(kvpObj.put(keyStr, valStr));
		
		assertTrue("Contains key if kStr exist", kvpObj.containsKey(kStr));
		assertTrue("Contains value if vStr exist", kvpObj.containsValue(vStr));
		assertEquals("Contains Value vStr, vlStr Equals", kvpObj.containsValue(vStr), kvpObj.containsValue(vlStr));
	}
	
	@Test
	public void checkSizeAndEmptyTest() throws jSqlException{
		String kStr = "hello", kyStr = "hi", keyStr = "how" ;
		String vStr = "world", vlStr = "world", valStr = "hworld";
		
		String clrValues = kvpObj.clear();
		assertEquals("Remove values equals", clrValues, null);
		
		assertTrue(kvpObj.isEmpty());
		assertSame(0, kvpObj.size());
		
		assertTrue(kvpObj.put(kStr, vStr));
		assertTrue(kvpObj.put(kyStr, vlStr));
		assertTrue(kvpObj.put(keyStr, valStr));
		
		assertNotSame(0, kvpObj.size());
		
		assertTrue(kvpObj.remove(kyStr));
		
		assertFalse(kvpObj.isEmpty());
		
		assertTrue(kvpObj.removeValues(vStr));
		
		assertNotSame(0, kvpObj.size());
		
		clrValues = kvpObj.clear();
		assertEquals("Remove values equals", clrValues, null);
		
		assertTrue(kvpObj.isEmpty());
	}
	
	// */
	
	//GetKeys support removed for oracle competibility
	/*
	@Test
	public void getKeysTest() throws jSqlException{
		String kStr1 = "key1", kStr2 = "key2", kStr3 = "key3" ;
		String vStr1 = "value1", vStr3 = "value3";
		
		kvpObj.clear();
		
		assertTrue(kvpObj.put(kStr1, vStr1));
		assertTrue(kvpObj.put(kStr2, vStr1));
		assertTrue(kvpObj.put(kStr3, vStr3));
		
		ArrayList<String> expected = new ArrayList<String>();
		expected.add(kStr1);
		expected.add(kStr2);
	   
		assertEquals(expected, kvpObj.getKeys("value1"));
	}
	
	@Test
	public void getKeysTest_expectedExceptions() throws jSqlException{
		String kStr1 = "key1", kStr2 = "key2", kStr3 = "key3" ;
		String vStr1 = "value1", vStr3 = "value3";
		
		kvpObj.clear();
		
		assertTrue(kvpObj.put(kStr1, vStr1));
		assertTrue(kvpObj.put(kStr2, vStr1));
		assertTrue(kvpObj.put(kStr3, vStr3));
		
		assertNull(kvpObj.getKeys("unknownkey"));
		
		try {
	    	kvpObj.getKeys(null);
		} catch (jSqlException e) {
			assertNotNull("Exception caught as intended",e);
		}
		
	}
	*/
}