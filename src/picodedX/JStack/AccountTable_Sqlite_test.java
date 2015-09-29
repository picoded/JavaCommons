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

public class AccountTable_Sqlite_test extends JStackData_testBase_test {
	
	// Metatable setup
	//-----------------------------------------------
	
	protected String ptTableName = null;
	
	protected AccountTable ptObj = null;
	
	@Override
	public void testObjSetup() throws JStackException {
		ptTableName = "P" + TestConfig.randomTablePrefix();
		
		ptObj = new AccountTable(JStackObj, ptTableName);
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
	public void basicHasAddHasGet_viaID() throws JStackException {
		String guid;
		AccountObject p;
		
		assertFalse(ptObj.containsID("hello"));
		assertNull(ptObj.getFromID("hello"));
		
		assertNotNull(p = ptObj.newObject());
		assertNotNull(guid = p._oid());
		
		assertTrue(ptObj.containsID(guid));
		assertNotNull(p = ptObj.getFromID(guid));
	}
	
	@Test
	public void basicHasAddHasGet_viaName() throws JStackException {
		String guid;
		AccountObject p;
		String name = "hello";
		
		assertFalse(ptObj.containsKey(name));
		assertNull(ptObj.get(name));
		
		assertNotNull(p = ptObj.newObject(name));
		assertNotNull(guid = p._oid());
		
		assertTrue(ptObj.containsKey(name));
		assertNotNull(p = ptObj.get(name));
	}
	
	// Group tests
	//-----------------------------------------------
	
	@Test
	public void basicGroupMembership() throws JStackException {
		String grpName = "hello-group";
		String usrName = "user1";
		AccountObject grpObj;
		AccountObject usrObj;
		
		assertFalse(ptObj.containsKey(grpName));
		assertNull(ptObj.get(grpName));
		
		assertNotNull(grpObj = ptObj.newObject(grpName));
		assertArrayEquals(new String[0], grpObj.getMembers_id());
		
		assertFalse(ptObj.containsKey(usrName));
		assertNull(ptObj.get(usrName));
		assertNotNull(usrObj = ptObj.newObject(usrName));
		
		assertArrayEquals(new String[] {}, grpObj.getMembers_id());
		
		assertNotNull(grpObj.addMember(usrObj, "guest"));
		assertArrayEquals("addMember failed?", new String[] { usrObj._oid() }, grpObj.getMembers_id());
		
		AccountObject[] usrList = null;
		assertNotNull(usrList = grpObj.getMembers());
		assertEquals(1, usrList.length);
		assertEquals(usrObj._oid(), usrList[0]._oid());
		
		AccountObject[] grpList = null;
		assertNotNull(grpList = usrObj.getGroups());
		assertEquals(1, grpList.length);
		assertEquals(grpObj._oid(), grpList[0]._oid());
	}
}
