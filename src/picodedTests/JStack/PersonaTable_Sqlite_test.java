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

public class PersonaTable_Sqlite_test extends JStackData_testBase_test {


	// Metatable setup
	//-----------------------------------------------

	protected String ptTableName = null;

	protected PersonaTable ptObj = null;

	@Override
	public void testObjSetup() throws JStackException {
		ptTableName = "P" + TestConfig.randomTablePrefix();

		ptObj = new PersonaTable(JStackObj, ptTableName);
		ptObj.stackSetup();
	}

	@Override
	public void testObjTeardown() throws JStackException {
		ptObj.stackTeardown();
		ptObj = null;
	}

	// Misc tests
	//-----------------------------------------------

	@Test
	public void constructor() {
		assertNotNull(ptObj);
	}

	// Basic tests
	//-----------------------------------------------

	@Test
	public void basicHasAddHasGet() throws JStackException {
		String guid;
		PersonaObject p;

		assertFalse( ptObj.containsKey("hello") );
		assertNotNull( p = ptObj.newObject() );
		assertNotNull( guid = p._oid() );

		assertTrue( ptObj.containsKey(guid) );

	}

}
