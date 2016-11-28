package picoded.JStruct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.JStruct.internal.JStruct_MetaTable;

public class AccountObject_test {
	// / Test object
	public AccountTable accountTable = null;
	public AccountObject accountObject = null;
	
	// / To override for implementation
	// /------------------------------------------------------
	public AccountTable implementationConstructor() {
		return (new JStruct()).getAccountTable("test");
	}
	
	// / Setup and sanity test
	// /------------------------------------------------------
	@Before
	public void setUp() {
		accountTable = implementationConstructor();
		accountTable.systemSetup();
		String grpName = "hello-group";
		assertFalse(accountTable.containsKey(grpName));
		assertNull(accountTable.get(grpName));
		accountTable.systemSetup();
		accountObject = accountTable.newObject();
		JStruct_MetaTable jStruct_MetaTable = (JStruct_MetaTable) (new JStruct())
			.getMetaTable("test");
		accountObject = new AccountObject(accountTable, jStruct_MetaTable, accountObject._oid(), true);
	}
	
	@After
	public void tearDown() {
		if (accountTable != null) {
			accountTable.systemTeardown();
		}
		accountTable = null;
	}
	
	@Test
	public void setPasswordTest() {
		assertTrue(accountObject.setPassword(null));
		assertTrue(accountObject.setPassword("test@123"));
		assertTrue(accountObject.setPassword("test@1234", "test@123"));
		assertFalse(accountObject.setPassword("test", "test"));
	}
	
	@Test
	public void validatePasswordTest() {
		assertFalse(accountObject.validatePassword(""));
		assertFalse(accountObject.validatePassword("test@1234"));
	}
	
	@Test
	public void hasPasswordTest() {
		assertFalse(accountObject.hasPassword());
		accountObject.removePassword();
	}
	
	@Test(expected = Exception.class)
	public void setNamesTest() throws Exception {
		assertNotNull(accountObject.setName(null));
	}
	
	@Test(expected = Exception.class)
	public void setNamesTest1() throws Exception {
		assertNotNull(accountObject.setName(""));
	}
	
	@Test
	public void getNamesTest() {
		assertNotNull(accountObject.setName("user1"));
		assertNotNull(accountObject.setName("user2"));
		assertNotNull(accountObject.setName("user3"));
		assertNotNull(accountObject.getNames());
		accountObject.removeName("user3");
		assertFalse(accountObject.setUniqueName("user1"));
		assertTrue(accountObject.setUniqueName("JavaAdminUser"));
	}
	
	@Test
	public void setGroupStatusTest() {
		accountObject.setGroupStatus(false);
		assertFalse(accountObject.isGroup());
		accountObject.setGroupStatus(true);
		assertFalse(accountObject.isGroup());
	}
	
	@Test
	public void addMemberTest() {
		assertNotNull(accountObject.setMember(accountObject, "admin"));
		assertNull(accountObject.addMember(accountObject, "admin"));
		assertTrue(accountObject.removeMember(accountObject));
	}
	
	@Test
	public void isSuperUserTest() {
		assertFalse(accountObject.isSuperUser());
	}
}
