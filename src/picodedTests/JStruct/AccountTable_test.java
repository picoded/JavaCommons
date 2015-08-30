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

public class AccountTable_test  {

	
	/// Test object
	public AccountTable accTableObj = null;

	/// To override for implementation
	///------------------------------------------------------
	public AccountTable implementationConstructor() {
		return (new JStruct()).getAccountTable("test");
	}

	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		accTableObj = implementationConstructor();
		accTableObj.systemSetup();
	}

	@After
	public void tearDown() {
		if( accTableObj != null ) {
			accTableObj.systemTeardown();
		}
		accTableObj = null;
	}

	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(accTableObj);
		
		//run maintaince, no exception?
		// mtObj.maintenance();
	}
	
	// Basic tests
	//-----------------------------------------------

	@Test
	public void basicHasAddHasGet_viaID() {
		String guid;
		AccountObject p;
		
		assertFalse( accTableObj.containsID("hello") );
		assertNull( accTableObj.getFromID("hello") );
		
		assertNotNull( p = accTableObj.newObject() );
		assertNotNull( guid = p._oid() );
		p.saveDelta();

		assertTrue( accTableObj.containsID(guid) );
		assertNotNull( p = accTableObj.getFromID(guid) );
	}
	
	@Test
	public void basicHasAddHasGet_viaName() {
		String guid;
		AccountObject p;
		String name = "hello";
		
		assertFalse( accTableObj.containsKey(name) );
		assertNull( accTableObj.get(name) );
		
		assertNotNull( p = accTableObj.newObject(name) );
		assertNotNull( guid = p._oid() );
		p.saveDelta();

		assertTrue( accTableObj.containsName(name) );
		assertNotNull( p = accTableObj.getFromName(name) );
	}

	// Group tests
	//-----------------------------------------------

	@Test
	public void basicGroupMembership() {
		String grpName = "hello-group";
		String usrName = "user1";
		AccountObject grpObj;
		AccountObject usrObj;
		
		assertFalse( accTableObj.containsKey(grpName) );
		assertNull( accTableObj.get(grpName) );
		
		assertNotNull( grpObj = accTableObj.newObject(grpName) );
		assertArrayEquals( new String[0], grpObj.getMembers_id() );
		grpObj.saveDelta();
		
		assertFalse( accTableObj.containsKey(usrName) );
		assertNull( accTableObj.get(usrName) );
		assertNotNull( usrObj = accTableObj.newObject(usrName) );
		
		assertArrayEquals( new String[] { }, grpObj.getMembers_id() );
		usrObj.saveDelta();
		
		assertNotNull( grpObj.addMember( usrObj, "guest" ) );
		assertArrayEquals( "addMember failed?", new String[] { usrObj._oid() }, grpObj.getMembers_id() );
		grpObj.saveDelta();
		
		AccountObject[] usrList = null;
		assertNotNull( usrList = grpObj.getMembers() );
		assertEquals( 1, usrList.length );
		assertEquals( usrObj._oid(), usrList[0]._oid() );
		
		AccountObject[] grpList = null;
		assertNotNull( grpList = usrObj.getGroups() );
		assertEquals( 1, grpList.length );
		assertEquals( grpObj._oid(), grpList[0]._oid() );
	}
}
