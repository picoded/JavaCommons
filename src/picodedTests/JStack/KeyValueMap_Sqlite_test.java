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

public class KeyValueMap_Sqlite_test extends JStackData_testBase_test {
	
	// KeyValueMap setup
	//-----------------------------------------------
	
	protected String kvTableName = null;
	
	protected KeyValueMap kvObj = null;
	
	// The actual test obj setup, after JStack
	@Override
	public void testObjSetup() throws JStackException {
		kvTableName = "K" + TestConfig.randomTablePrefix();
		kvObj = new KeyValueMap(JStackObj, kvTableName);
		kvObj.stackSetup();
	}
	
	// The actual test obj teardown, before JStack
	@Override
	public void testObjTeardown() throws JStackException {
		kvObj.stackTeardown();
		kvObj = null;
	}
	
	@Test
	public void simpleHasPutHasGet() throws JStackException {
		assertFalse( kvObj.containsKey("hello") );
		kvObj.put("hello", "world");
		assertTrue( kvObj.containsKey("hello") );
		assertEquals( "world", kvObj.get("hello") );
		
	}
	
}