package picoded.JStruct;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

// Test depends

public class AccountTable_test {
	
	// / Test object
	public AccountTable accTableObj = null;
	
	// / To override for implementation
	// /------------------------------------------------------
	public AccountTable implementationConstructor() {
		return (new JStruct()).getAccountTable("test");
	}
	
	// / Setup and sanity test
	// /------------------------------------------------------
	@Before
	public void setUp() {
		accTableObj = implementationConstructor();
		accTableObj.systemSetup();
		String grpName = "hello-group";
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		accTableObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		if (accTableObj != null) {
			accTableObj.systemTeardown();
		}
		accTableObj = null;
	}
	
	@Test
	public void constructorTest() {
		// not null check
		assertNotNull(accTableObj);
		// run maintaince, no exception?
		// mtObj.maintenance();
	}
	
	// Basic tests
	// -----------------------------------------------
	
	@Test
	public void basicHasAddHasGet_viaID() {
		String guid;
		AccountObject p;
		
		assertFalse(accTableObj.containsID("hello"));
		assertNull(accTableObj.getFromID("hello"));
		
		assertNotNull(p = accTableObj.newObject());
		assertNotNull(guid = p._oid());
		p.saveDelta();
		
		assertTrue(accTableObj.containsID(guid));
		assertNotNull(p = accTableObj.getFromID(guid));
	}
	
	@Test
	public void basicHasAddHasGet_viaName() {
		AccountObject p;
		String name = "hello";
		
		assertFalse(accTableObj.containsKey(name));
		assertNull(accTableObj.get(name));
		
		assertNotNull(p = accTableObj.newObject(name));
		assertNotNull(p._oid());
		p.saveDelta();
		
		assertTrue(accTableObj.containsName(name));
		assertNotNull(p = accTableObj.getFromName(name));
	}
	
	// Group tests
	// -----------------------------------------------
	
	@Test
	public void basicGroupMembership() {
		String grpName = "hello-group";
		String usrName = "user1";
		AccountObject grpObj;
		AccountObject usrObj;
		
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertArrayEquals(new String[0], grpObj.getMembers_id());
		grpObj.saveDelta();
		
		assertFalse(accTableObj.containsKey(usrName));
		assertNull(accTableObj.get(usrName));
		assertNotNull(usrObj = accTableObj.newObject(usrName));
		
		assertArrayEquals(new String[] {}, grpObj.getMembers_id());
		usrObj.saveDelta();
		
		assertNotNull(grpObj.addMember(usrObj, "guest"));
		assertArrayEquals("addMember failed?", new String[] { usrObj._oid() }, grpObj.getMembers_id());
		grpObj.saveDelta();
		
		AccountObject[] usrList = null;
		assertNotNull(usrList = grpObj.getMembersAccountObject());
		assertEquals(1, usrList.length);
		assertEquals(usrObj._oid(), usrList[0]._oid());
		
		AccountObject[] grpList = null;
		assertNotNull(grpList = usrObj.getGroups());
		assertEquals(1, grpList.length);
		assertEquals(grpObj._oid(), grpList[0]._oid());
	}
	
	@Test
	public void logLoginFailureTest() {
		AccountObject usrObj;
		usrObj = accTableObj.newObject();
		assertNotNull(usrObj);
		usrObj.logLoginFailure("Test-1");
	}
	
	@Test
	public void getNextLoginTimeAllowedTest() {
		AccountObject usrObj;
		usrObj = accTableObj.newObject();
		assertNotNull(usrObj);
		usrObj.logLoginFailure("Test-1");
		assertEquals(usrObj.getNextLoginTimeAllowed("Test-1"), 2);
	}
	
	@Test
	public void getTimeElapsedNextLoginTest() {
		AccountObject usrObj;
		usrObj = accTableObj.newObject();
		int elapsedValue = (int) (System.currentTimeMillis() / 1000) + 2;
		assertEquals(usrObj.getTimeElapsedNextLogin("Test-1"), elapsedValue);
	}
	
	@Test
	public void getTimeElapsedNextLoginTestAfterFailAttempt() {
		AccountObject usrObj;
		usrObj = accTableObj.newObject();
		int elapsedValue = (int) (System.currentTimeMillis() / 1000) + 2;
		assertEquals(usrObj.getTimeElapsedNextLogin("Test-1"), elapsedValue);
		usrObj.addDelay("Test-1");
		elapsedValue = (int) (System.currentTimeMillis() / 1000) + 2;
		assertEquals(usrObj.getTimeElapsedNextLogin("Test-1"), elapsedValue);
	}
	
	@Test
	public void addDelayTest() {
		AccountObject usrObj;
		usrObj = accTableObj.newObject();
		usrObj.addDelay("Test-1");
		usrObj.addDelay("Test-1");
		int elapsedValue = (int) (System.currentTimeMillis() / 1000) + 4;
		assertEquals(usrObj.getTimeElapsedNextLogin("Test-1"), elapsedValue);
	}
	
	@Test
	public void accountMetaTableTest() {
		assertNotNull(accTableObj.accountMetaTable());
	}
	
	@Test
	public void groupChildRoleTest() {
		assertNotNull(accTableObj.groupChildRole());
	}
	
	@Test
	public void groupChildMetaTest() {
		assertNotNull(accTableObj.groupChildMeta());
	}
	
	@Test
	public void removeTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertNull(accTableObj.remove(grpObj.get("_oid")));
	}
	
	@Test
	public void keySetTest() {
		assertNotNull(accTableObj.keySet());
	}
	
	@Test
	public void newObjectTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertNotNull(accTableObj.newObject("test"));
		assertNull(accTableObj.newObject("test"));
		assertNull(accTableObj.newObject("hello-group"));
		assertNotNull(accTableObj.newObject(grpObj.get("_oid").toString()));
	}
	
	@Test
	public void removeFromNameTest() {
		accTableObj.removeFromName("_oid");
		String grpName = "hello-group";
		@SuppressWarnings("unused")
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		accTableObj.removeFromName("hello-group");
	}
	
	@Test
	public void removeFromIDTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		accTableObj.removeFromID(grpObj.get("_oid").toString());
	}
	
	@Test
	public void getGroupChildMetaKeyTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertNotNull(accTableObj.getGroupChildMetaKey(grpObj.get("_oid").toString(), "hello"));
	}
	
	@Test
	public void getSessionInfoTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertNull(accTableObj.getSessionInfo(grpObj.get("_oid").toString(),
			String.valueOf(accTableObj.nonceSize)));
	}
	
	@Test
	public void membershipRolesTest() {
		assertNotNull(accTableObj.membershipRoles());
		accTableObj.addMembershipRole("gust0");
		accTableObj.addMembershipRole("gust0");
	}
	
	@Test(expected = RuntimeException.class)
	public void validateMembershipRoleTest() throws Exception {
		accTableObj.validateMembershipRole("gust123");
	}
	
	@Test
	public void setSuperUserGroupNameTest() {
		assertNotNull(accTableObj.setSuperUserGroupName("userGroup"));
	}
}
