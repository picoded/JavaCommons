package picoded.JStruct;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import picoded.security.NxtCrypt;

// Test depends

public class AccountTable_test extends Mockito {
	
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
		String usrName = "user1";
		AccountObject usrObj;
		accTableObj.removeFromID(null);
		accTableObj.removeFromID("");
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		accTableObj.removeFromID(grpObj.get("_oid").toString());
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
		accTableObj.removeFromID(grpObj.get("_oid").toString());
		assertNotNull(usrList = grpObj.getMembersAccountObject());
		assertEquals(1, usrList.length);
		assertEquals(usrObj._oid(), usrList[0]._oid());
		accTableObj.removeFromID("111wwww");
		assertNotNull(accTableObj.getUsersByGroupAndRole(new String[] {}, new String[] { "guest",
			"member", "manager", "admin" }));
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
	public void generateSessionTest() {
		String grpName = "hello-group";
		AccountObject grpObj;
		assertFalse(accTableObj.containsKey(grpName));
		assertNull(accTableObj.get(grpName));
		assertNotNull(grpObj = accTableObj.newObject(grpName));
		assertNotNull(accTableObj.generateSession(grpObj.get("_oid").toString(), 1, "nonceSalt",
			"ipString", "browserAgent"));
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
	
	@Test
	public void getUsersByGroupAndRoleTest() {
		assertNotNull(accTableObj.setSuperUserGroupName("hello-group"));
		assertNotNull(accTableObj.setSuperUserGroupName("userGroup"));
		assertNotNull(accTableObj.getUsersByGroupAndRole(null, null));
		assertNotNull(accTableObj.getUsersByGroupAndRole(null, new String[] {}));
		assertNotNull(accTableObj.getUsersByGroupAndRole(new String[] {}, new String[] { "guest",
			"member", "manager", "admin" }));
	}
	
	@Test
	public void getRequestUserTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		assertNull(accTableObj.getRequestUser(request));
		request
			.setAttribute("javax.servlet.include.path_info", "http://localhost:8080/App/logout=-1");
		assertNull(accTableObj.getRequestUser(request));
	}
	
	@Test
	public void loginAccountTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		String usrName = "gust";
		AccountObject usrObj = null;
		accTableObj.loginAccount(request, response, usrObj, null, true);
		assertFalse(accTableObj.containsKey(usrName));
		assertNull(accTableObj.get(usrName));
		assertNotNull(usrObj = accTableObj.newObject(usrName));
		usrObj.setName("test");
		usrObj.setPassword("test123");
		assertNotNull(accTableObj.loginAccount(request, response, usrObj, "test123", true));
		assertNotNull(accTableObj.loginAccount(request, response, usrObj, "test123", false));
		assertNull(accTableObj.loginAccount(request, response, "test123", "test", false));
	}
	
	@Test
	public void logoutAccountTest() {
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/requestURI");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String usrName = "gust";
		AccountObject usrObj = null;
		accTableObj.logoutAccount(null, null);
		assertFalse(accTableObj.containsKey(usrName));
		assertNull(accTableObj.get(usrName));
		assertNotNull(usrObj = accTableObj.newObject(usrName));
		usrObj.setName("test");
		usrObj.setPassword("test123");
		usrObj.saveDelta();
		assertFalse(accTableObj.logoutAccount(request, null));
		request.setContextPath(null);
		assertTrue(accTableObj.logoutAccount(request, response));
		request.setContextPath("");
		assertTrue(accTableObj.logoutAccount(request, response));
		request.setContextPath("/");
		accTableObj.isHttpOnly = true;
		accTableObj.isSecureOnly = true;
		accTableObj.cookieDomain = null;
		assertTrue(accTableObj.logoutAccount(request, response));
		accTableObj.cookieDomain = "";
		assertTrue(accTableObj.logoutAccount(request, response));
		accTableObj.cookieDomain = "test.com";
		assertTrue(accTableObj.logoutAccount(request, response));
		request.setPathInfo(null);
		assertNull(accTableObj.getRequestUser(request, response));
		request.setPathInfo("logout");
		assertNull(accTableObj.getRequestUser(request, response));
		request.setPathInfo("test");
		javax.servlet.http.Cookie[] cookieJar = new javax.servlet.http.Cookie[5];
		cookieJar[0] = new javax.servlet.http.Cookie("Account_User", "User");
		cookieJar[1] = new javax.servlet.http.Cookie("Account_Nonc", "Nonc");
		cookieJar[2] = new javax.servlet.http.Cookie("Account_Hash", "Hash");
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", "Rmbr");
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", null);
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[1] = new javax.servlet.http.Cookie("Account_Nonc", null);
		cookieJar[2] = new javax.servlet.http.Cookie("Account_Hash", "Hash");
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", "Rmbr");
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", "Puid");
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[1] = new javax.servlet.http.Cookie("Account_Nonc", "Nonc");
		cookieJar[2] = new javax.servlet.http.Cookie("Account_Hash", null);
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", "Rmbr");
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", "Puid");
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[1] = new javax.servlet.http.Cookie("Account_Nonc", "Nonc");
		cookieJar[2] = new javax.servlet.http.Cookie("Account_Hash", "Hash");
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", null);
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", "Puid");
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", "1");
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", "PuidPuidPuidPuidPuidPuid");
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", "PuidPuidPuidPui");
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", usrObj._oid());
		request.setCookies(cookieJar);
		assertNull(accTableObj.getRequestUser(request, response));
		
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", "[]");
		assertNull(accTableObj.getRequestUser(request, response));
		
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", "[null]");
		assertNull(accTableObj.getRequestUser(request, response));
		
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", "[test]");
		assertNull(accTableObj.getRequestUser(request, response));
		
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", usrObj._oid());
		request.setCookies(cookieJar);
		String testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"id-9\"] ]";
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", testJSON);
		assertNull(accTableObj.getRequestUser(request, response));
		
		String passHash = usrObj.getPasswordHash();
		String computedCookieHash = NxtCrypt.getSaltedHash(passHash, "[\"id-1\",11]");
		computedCookieHash = computedCookieHash.replaceAll("\\W", "");
		cookieJar[2] = new javax.servlet.http.Cookie("Account_Hash", computedCookieHash);
		cookieJar[4] = new javax.servlet.http.Cookie("Account_Puid", usrObj._oid());
		request.setCookies(cookieJar);
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", testJSON);
		assertNotNull(usrObj = accTableObj.getRequestUser(request, response));
		
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", null);
		request.setCookies(cookieJar);
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", testJSON);
		assertNotNull(usrObj = accTableObj.getRequestUser(request, response));
		cookieJar[3] = new javax.servlet.http.Cookie("Account_Rmbr", "Rmbr");
		request.setCookies(cookieJar);
		accTableObj.keyValueMapAccountSessions.put(usrObj._oid() + "-Nonc", testJSON);
		assertNotNull(usrObj = accTableObj.getRequestUser(request, response));
	}
}
