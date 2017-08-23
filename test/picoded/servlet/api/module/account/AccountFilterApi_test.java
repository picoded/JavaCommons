package picoded.servlet.api.module.account;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import org.junit.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import picoded.servlet.*;
import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.core.conv.*;
import picoded.core.common.*;
import picoded.dstack.*;
import picoded.dstack.module.account.*;
import picoded.core.struct.GenericConvertMap;

import static picoded.servlet.api.module.account.AccountConstantStrings.*;

/// Test the AccountTable API specifically
public class AccountFilterApi_test extends ApiModule_test {
	
	//----------------------------------------------------------------------------------------
	//
	//  Test setup
	//
	//----------------------------------------------------------------------------------------
	
	/// The test servlet to use
	public static class AccountFilterApiTestServlet extends ApiModuleTestServlet {
		@Override
		public ApiModule moduleSetup(CommonStack stack) {
			AccountTable table = new AccountTable(stack, "account");
			AccountFilterApi ret = new AccountFilterApi(table);
			if (!table.hasLoginID(SUPERUSERNAME)) {
				AccountObject ao = table.newObject(SUPERUSERNAME);
				ao.setPassword(VALIDPASSWORD);
				// Add to superUserGrp
				
				AccountObject superUserGrp = table.newObject(table.getSuperUserGroupName());
				superUserGrp.setMembershipRoles(table.defaultMembershipRoles());
				DataObject mo = superUserGrp.addMember(ao, "admin");
			}
			return ret;
		}
	}
	
	/// Variables
	public static final String VALIDPASSWORD = "Password123";
	public static final String SUPERUSERNAME = "laughing-man@mailawer.com";
	
	public CorePage setupServlet() {
		return new AccountFilterApiTestServlet();
	}
	
	//----------------------------------------------------------------------------------------
	//
	//  Test running
	//
	//----------------------------------------------------------------------------------------
	
	@Test
	public void isLoginFailure() {
		assertNull(requestJSON(API_ACCOUNT_IS_LOGIN, null).get("INFO"));
		assertNull(requestJSON(API_ACCOUNT_IS_LOGIN, null).get(RES_ERROR));
		assertEquals(Boolean.FALSE, requestJSON(API_ACCOUNT_IS_LOGIN, null).get(RES_RETURN));
	}
	
	@Test
	public void loginProcessFlow() {
		GenericConvertMap<String, Object> res = null;
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_LOGIN, ERROR_NO_LOGIN_PASSWORD, RES_ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Email format invalid
		Map<String, Object> loginParams = new HashMap<String, Object>();
		loginParams.put(REQ_USERNAME, "laughing-man");
		loginParams.put(REQ_PASSWORD, "Qwe123hahhaa");
		ts.setAndExecuteGTC(loginParams, ERROR_FAIL_LOGIN, RES_ERROR);
		// 3rd Test: Invalid login
		loginParams.put(REQ_USERNAME, SUPERUSERNAME);
		ts.setAndExecuteGTC(loginParams, ERROR_FAIL_LOGIN, RES_ERROR);
		// 4th Test: Valid Email and Password
		loginParams.put(REQ_PASSWORD, VALIDPASSWORD);
		ts.setAndExecuteGTC(loginParams, Boolean.TRUE, RES_IS_LOGIN);
		
		// 5th Test: Logout user
		ts.setURL("account/logout");
		ts.setAndExecuteGTC(null, Boolean.TRUE, RES_RETURN);
		
		// Validate that logout is valid
		ts.setURL("account/isLogin");
		ts.setAndExecuteGTC(null, Boolean.FALSE, RES_RETURN);
	}
	
	@Test
	public void loginLockingIncrement() {
		// reuse result map
		Map<String, Object> res = null;
		// Checks that the test begins with the user not logged in
		res = requestJSON(API_ACCOUNT_IS_LOGIN, null);
		assertEquals(Boolean.FALSE, res.get(RES_RETURN));
		
		Map<String, Object> loginParams = new HashMap<String, Object>();
		loginParams.put(REQ_USERNAME, SUPERUSERNAME);
		loginParams.put(REQ_PASSWORD, "Is the enemy");
		int doTestLoop = 3, currentLoop = 0;
		while (currentLoop < doTestLoop) {
			res = requestJSON(API_ACCOUNT_LOGIN, loginParams);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(REQ_ACCOUNT_NAME, SUPERUSERNAME);
			int waitTime = (int) requestJSON(API_ACCOUNT_LOCKTIME, params).get("lockTime");
			if (currentLoop % 2 == 0) {
				assertEquals(Boolean.FALSE, res.get(RES_IS_LOGIN));
				assertEquals("It failed on the Loop Number: " + (currentLoop + 1) + " with waitTime: "
					+ waitTime + "\n" + "Likely failure is due to insufficient Thread.sleep()\n",
					ERROR_FAIL_LOGIN, res.get(RES_ERROR));
			} else {
				assertThat("It failed on the Loop Number: " + (currentLoop + 1) + " with waitTime: "
					+ waitTime + "\n", res.get(RES_ERROR).toString(), containsString("user locked out"));
				try {
					Thread.sleep(waitTime * 1000);
				} catch (InterruptedException ie) {
				}
			}
			currentLoop++;
		}
	}
	
	@Test
	public void createNewUserAccount() {
		GenericConvertMap<String, Object> res = null;
		Map<String, Object> createDetails = new HashMap<String, Object>();
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_NEW, ERROR_INVALID_FORMAT_EMAIL, RES_ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Email format invalid
		Map<String, Object> loginParams = new HashMap<String, Object>();
		loginParams.put(REQ_USERNAME, "testing");
		ts.setAndExecuteGTC(loginParams, ERROR_INVALID_FORMAT_EMAIL, RES_ERROR);
		// 3rd Test: No Password
		loginParams.put(REQ_USERNAME, "testing-man@gmail.com");
		ts.setAndExecuteGTC(loginParams, ERROR_NO_PASSWORD, RES_ERROR);
		// 4th Test: Password complexity failed (length of string < 6)
		loginParams.put(REQ_PASSWORD, "1");
		ts.setAndExecuteGTC(loginParams, "|" + ERROR_PASSWORD_COMPLEXITY, RES_ERROR);
		// 5th Test: Password complexity failed (length of string > 6, no uppercase)
		loginParams.put(REQ_PASSWORD, "1asdbce");
		ts.setAndExecuteGTC(loginParams, "|" + ERROR_PASSWORD_COMPLEXITY, RES_ERROR);
		// 6th Test: Password complexity failed (length of string > 6, uppercase, no lowercase)
		loginParams.put(REQ_PASSWORD, "AWDLKAW");
		ts.setAndExecuteGTC(loginParams, "|" + ERROR_PASSWORD_COMPLEXITY, RES_ERROR);
		// 7th Test: Password complexity failed (length of string > 6, uppercase, lowercase, no dights)
		loginParams.put(REQ_PASSWORD, "AWDLKawe");
		ts.setAndExecuteGTC(loginParams, "|" + ERROR_PASSWORD_COMPLEXITY, RES_ERROR);
		// 8th Test: Password complexity Passed (length of string > 6, uppercase, lowercase, dights)
		loginParams.put(REQ_PASSWORD, "Qqwe12");
		ts.setAndExecuteGTC(loginParams, null, RES_ERROR);
		// 9th Test: Create same user again
		String accountID = ts.getRes().get(RES_ACCOUNT_ID).toString();
		ts.setAndExecuteGTC(loginParams, accountID, RES_ACCOUNT_ID);
		ts.setAndExecuteGTC(loginParams, "Object already exists in account Table", RES_ERROR);
	}
	
	@Test
	public void createNewGroup() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		for (int idx = 1; idx <= 2; idx++) {
			params.put(REQ_USERNAME, "member" + idx + "@gmail.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("createNewGroup Filter: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
		}
		ArrayList<String> expectedRoles = new ArrayList<String>();
		expectedRoles.add("member");
		expectedRoles.add("admin");
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		Map<String, Object> createDetails = new HashMap<String, Object>();
		// 1st Test: Check if user is logged in
		createDetails.put(REQ_USERNAME, "boy band");
		createDetails.put(REQ_IS_GROUP, true);
		TestSet ts = new TestSet(createDetails, API_ACCOUNT_NEW, ERROR_USER_NOT_LOGIN, RES_ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Check if it is a group (not a group)
		// Login the user
		ts.loginUser("member1@gmail.com", VALIDPASSWORD);
		createDetails.put(REQ_USERNAME, "boy band");
		createDetails.put(REQ_IS_GROUP, "random Words");
		ts.setAndExecuteGTC(createDetails, ERROR_INVALID_FORMAT_EMAIL, RES_ERROR);
		// 3rd Test: Creates a group successfully (default roles)
		createDetails.put(REQ_USERNAME, "boy band");
		createDetails.put(REQ_IS_GROUP, true);
		ts.setAndExecuteGTC(createDetails, null, RES_ERROR);
		// 4th Test: Creates the same group
		createDetails.put(REQ_USERNAME, "boy band");
		createDetails.put(REQ_IS_GROUP, true);
		ts.setAndExecuteGTC(createDetails, "Object already exists in account Table", RES_ERROR);
		// 5th Test: Create group with custom roles
		expectedRoles.clear();
		expectedRoles.add("grandma");
		expectedRoles.add("grandpa");
		expectedRoles.add("admin");
		
		createDetails.clear();
		createDetails.put(REQ_USERNAME, "bbbbb");
		createDetails.put(REQ_IS_GROUP, "1");
		createDetails.put(REQ_DEFAULT_ROLES, false);
		createDetails.put(REQ_ROLE, expectedRoles);
		ts.setAndExecuteGTC(createDetails, null, RES_ERROR);
		ts.logout();
		String groupID = ts.getRes().getString(RES_ACCOUNT_ID);
		// Verify the roles
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		createDetails.clear();
		createDetails.put(REQ_GROUP_ID, groupID);
		res = requestJSON(API_GROUP_GRP_ROLES, createDetails);
		assertEquals(expectedRoles, res.get(RES_LIST));
		ts.logout();
	}
	
	@Test
	public void groupRoles() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		for (int idx = 1; idx <= 2; idx++) {
			params.put(REQ_USERNAME, "grpR" + idx + "@gmail.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("groupRoles Filter: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
		}
		TestSet ts = new TestSet(null, API_ACCOUNT_NEW, null, null);
		ts.loginUser("grpR1@gmail.com", VALIDPASSWORD);
		ArrayList<String> expectedRoles = new ArrayList<String>();
		expectedRoles.add("member");
		expectedRoles.add("admin");
		// Use member to create a group (member1@gmail.com should be the admin)
		params.clear();
		params.put(REQ_USERNAME, "myGroup");
		params.put(REQ_IS_GROUP, "1");
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		ts.setURL(API_GROUP_GET_MEM_ROLE);
		String groupID = ts.getRes().getString(RES_ACCOUNT_ID);
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_GROUP_ID, groupID);
		ts.setAndExecuteGTC(params, "admin", RES_SINGLE_RETURN_VALUE);
		ts.logout();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: No login, retrieve group roles
		ts.setURL(API_GROUP_GRP_ROLES);
		ts.setAndExecuteGTC(params, ERROR_USER_NOT_LOGIN, RES_ERROR);
		// 2nd Test: Login user, retrieve group not belonging to itself
		ts.loginUser("grpR2@gmail.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 3rd Test: Login valid user, retrieve group
		ts.loginUser("grpR1@gmail.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void addAndRemoveMemberToGroupTest() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		List<String> groupID = new ArrayList<String>();
		for (int idx = 1; idx <= 3; idx++) {
			params.put(REQ_USERNAME, "ARmember" + idx + "@gmail.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("addAndRemoveMemberToGroupTest Filter: Something wrong in adding user " + idx
				+ ".", res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
		}
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, null, null, null);
		for (int idx = 1; idx <= 3; idx++) {
			ts.loginUser("ARmember" + idx + "@gmail.com", VALIDPASSWORD);
			params.put(REQ_USERNAME, "group " + idx);
			params.put(REQ_IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("addAndRemoveMemberToGroupTest Filter: Something wrong in creating group "
				+ idx + ".", res.get(RES_ERROR));
			groupID.add(res.getString(RES_ACCOUNT_ID));
			ts.logout();
		}
		// Adding roles to non group user account (member 3)
		ts.loginUser("ARmember3@gmail.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_GROUP_ID, userID.get(2));
		params.put(REQ_ROLE, "knight");
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertNull("addAndRemoveMemberToGroupTest Filter: Something wrong in adding role to group.",
			res.get(RES_ERROR));
		ts.logout();
		
		List<String> removeUserList = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>(), expectedPassResult = new ArrayList<String>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Submit nothing at all User not logged in
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, null);
		assertEquals(ERROR_USER_NOT_LOGIN, res.get(RES_ERROR));
		
		// 2nd Test: Non existence group
		ts.loginUser("ARmember2@gmail.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_GROUP_ID, "randomID HAHHAHAHA");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(ERROR_NO_PRIVILEGES, res.get(RES_ERROR));
		ts.logout();
		// 3rd Test: Remove non existence members & existence members not in group from Existence group (group 1)
		ts.loginUser("ARmember1@gmail.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_GROUP_ID, groupID.get(0));
		removeUserList.add("This is random ID");
		removeUserList.add(userID.get(1));
		params.put(REQ_REMOVE_LIST, removeUserList);
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		expectedFailResult.add("ID: This is random ID, Error: " + ERROR_NO_USER);
		expectedFailResult.add("ID: " + userID.get(1) + ", Error: User is not in group.");
		// assertEquals(expectedFailResult, res.get(RES_FAIL_REMOVE));
		assertThat("The list is wrong.", res.getList(RES_FAIL_REMOVE),
			containsInAnyOrder(expectedFailResult.toArray()));
		
		// 4th Test: Add non existence members without role into existence group (group 1)
		params.clear();
		addUserList.add("This is another random ID");
		addUserList.add("One for the road");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(ERROR_NO_ROLE, res.get(RES_ERROR));
		ts.logout();
		
		// 5th Test: Add non existence members and existence member with non existence role into group (group 2)
		ts.loginUser("ARmember2@gmail.com", VALIDPASSWORD);
		expectedFailResult.clear();
		expectedFailResult.add("ID: This is another random ID, Error: " + ERROR_NO_USER);
		expectedFailResult.add("ID: One for the road, Error: " + ERROR_NO_USER);
		expectedFailResult.add("ID: " + userID.get(0) + ", Error: "
			+ "User is already in group or role is not found.");
		addUserList.add(userID.get(0));
		params.put(REQ_ROLE, "this is random role");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		
		// 6th Test: Add existence members and repeated member with existence role into group (group 2)
		expectedFailResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		addUserList.add(userID.get(1)); // the admin itself
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(1));
		expectedFailResult.add("ID: " + userID.get(1) + ", Error: "
			+ "User is already in group or role is not found.");
		expectedPassResult.add(userID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		assertEquals(expectedPassResult, res.get(RES_SUCCESS_ADD));
		
		// 7th Test: Remove existence members and repeated members from existence group (group 2)
		expectedFailResult.clear();
		expectedPassResult.clear();
		params.clear();
		removeUserList.clear();
		removeUserList.add(userID.get(0));
		removeUserList.add(userID.get(0));
		params.put(REQ_GROUP_ID, groupID.get(1));
		params.put(REQ_REMOVE_LIST, removeUserList);
		expectedFailResult.add("ID: " + userID.get(0) + ", Error: User is not in group.");
		expectedPassResult.add(userID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(expectedFailResult, res.get(RES_FAIL_REMOVE));
		assertEquals(expectedPassResult, res.get(RES_SUCCESS_REMOVE));
		
		// 8th Test: Remove valid users from non group account (member 2)
		expectedFailResult.clear();
		params.clear();
		removeUserList.clear();
		removeUserList.add(userID.get(0));
		params.put(REQ_GROUP_ID, userID.get(1));
		params.put(REQ_REMOVE_LIST, removeUserList);
		expectedFailResult.add("ID: " + userID.get(1) + ", Error: This is not a group.");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(expectedFailResult, res.get(RES_FAIL_REMOVE));
		
		// 9th Test: Adding valid users to user account that do not have roles a.k.a not a group yet (member 2)
		expectedFailResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		params.put(REQ_GROUP_ID, userID.get(1));
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_ROLE, "member");
		expectedFailResult.add("ID: " + userID.get(0)
			+ ", Error: User is already in group or role is not found.");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		ts.logout();
		// 10th Test: Adding valid users to user account that is a group (member 3)
		ts.loginUser("ARmember3@gmail.com", VALIDPASSWORD);
		expectedPassResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		params.put(REQ_GROUP_ID, userID.get(2));
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_ROLE, "knight");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		expectedPassResult.add(userID.get(0));
		assertEquals(expectedPassResult, res.get(RES_SUCCESS_ADD));
		
		// 11th Test: Add the same user to the same group (member 3)
		expectedFailResult.clear();
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		expectedFailResult.add("ID: " + userID.get(0)
			+ ", Error: User is already in group or role is not found.");
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
	}
	
	@Test
	public void getMemberRole() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(REQ_USERNAME, "memberrole@mailmail.com");
		params.put(REQ_PASSWORD, VALIDPASSWORD);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getMemberRole: Something wrong in adding user.", res.get(RES_ERROR));
		String userID = res.getString(RES_ACCOUNT_ID);
		
		params.put(REQ_USERNAME, "memberNotInGroup@mailmail.com");
		params.put(REQ_PASSWORD, VALIDPASSWORD);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getMemberRole: Something wrong in adding user.", res.get(RES_ERROR));
		String wrongUserID = res.getString(RES_ACCOUNT_ID);
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, API_GROUP_GET_MEM_ROLE, null, null);
		ts.loginUser("memberrole@mailmail.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_USERNAME, "memberRoleGroup");
		params.put(REQ_IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getMemberRole: Something wrong in adding group.", res.get(RES_ERROR));
		String groupID = res.getString(RES_ACCOUNT_ID);
		ts.logout();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Empty submission not logged in
		ts.setAndExecuteGTC(null, ERROR_USER_NOT_LOGIN, RES_ERROR);
		
		// 2nd Test: Invalid groupID
		ts.loginUser("memberrole@mailmail.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_GROUP_ID, "wrong group ID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 3rd Test: Invalid member ID
		params.put(REQ_USER_ID, "wrong member ID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 4th Test: Valid groupID
		params.put(REQ_USER_ID, "wrong member ID");
		params.put(REQ_GROUP_ID, groupID);
		ts.setAndExecuteGTC(params, ERROR_NO_USER, RES_ERROR);
		
		// 5th Test: Existence wrong userID
		params.put(REQ_USER_ID, wrongUserID);
		ts.setAndExecuteGTC(params, "No role for user is found.", RES_ERROR);
		
		// 6th Test: Valid UserID
		params.put(REQ_USER_ID, userID);
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void addNewMembershipRole() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(REQ_USERNAME, "addRole@mailmail.com");
		params.put(REQ_PASSWORD, VALIDPASSWORD);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("addNewMembershipRole Filter: Something wrong in adding user.", res.get(RES_ERROR));
		String userID = res.getString(RES_ACCOUNT_ID);
		params.put(REQ_USERNAME, "addRole11@mailmail.com");
		params.put(REQ_PASSWORD, VALIDPASSWORD);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("addNewMembershipRole Filter: Something wrong in adding user.", res.get(RES_ERROR));
		String user2ID = res.getString(RES_ACCOUNT_ID);
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, API_GROUP_ADMIN_ADD_MEM_ROLE, null, null);
		ts.loginUser("addRole@mailmail.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "addNewMembershipRole");
		params.put(REQ_IS_GROUP, true);
		params.put(REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		initialRole.add("admin");
		params.put(REQ_ROLE, initialRole);
		res = requestJSON(API_ACCOUNT_NEW, params);
		String groupID = res.getString(RES_ACCOUNT_ID);
		assertNull("addNewMembershipRole Filter: Something wrong in adding group.",
			res.get(RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 2nd Test: Logged in valid user, wrong group
		params.clear();
		params.put(REQ_GROUP_ID, "wrong group ID");
		ts.setURL(API_GROUP_ADMIN_ADD_MEM_ROLE);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 3rd Test: Valid user, invalid group
		params.put(REQ_ROLE, "roleToAdd");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 4th Test: Valid user, valid group
		params.put(REQ_GROUP_ID, groupID);
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertNull(res.get(RES_ERROR));
		initialRole.add("roleToAdd");
		assertEquals(initialRole, res.getStringMap(RES_META).get(PROPERTIES_MEMBERSHIP_ROLE));
		// 6th Test: Other user attempts to make other user become groups
		ts.loginUser("addRole@mailmail.com", VALIDPASSWORD);
		params.put(REQ_GROUP_ID, user2ID);
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertEquals(ERROR_NO_PRIVILEGES, res.get(RES_ERROR));
		ts.logout();
	}
	
	@Test
	public void removeMembershipRoleFromGroup() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(REQ_USERNAME, "removeRole@mailmail.com");
		params.put(REQ_PASSWORD, VALIDPASSWORD);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("removeMembershipRoleFromGroup Filter: Something wrong in adding user.",
			res.get(RES_ERROR));
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, API_GROUP_ADMIN_REM_MEM_ROLE, null, null);
		ts.loginUser("removeRole@mailmail.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "removeMembershipRole");
		params.put(REQ_IS_GROUP, true);
		params.put(REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		initialRole.add("admin");
		params.put(REQ_ROLE, initialRole);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("removeMembershipTest: Something wrong in adding group.", res.get(RES_ERROR));
		String groupID = res.getString(RES_ACCOUNT_ID);
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Empty Submission
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 2nd Test: Logged in valid user, wrong group
		params.clear();
		params.put(REQ_GROUP_ID, "wrong group ID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 3rd Test: Valid user, invalid group
		params.put(REQ_ROLE, "roleToRemove");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 4th Test: Remove unknown role
		params.put(REQ_GROUP_ID, groupID);
		ts.setAndExecuteGTC(params, "No such role is found.", RES_ERROR);
		
		// 5th Test: Remove Legitimate role
		params.put(REQ_ROLE, "dragon");
		initialRole.remove("dragon");
		res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(initialRole, res.getStringMap(RES_META).get("membershipRoles"));
	}
	
	@Test
	public void multiLevelGroupOwnership() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		// Ensure that there is an existing users
		List<String> userID = new ArrayList<String>();
		for (int idx = 1; idx <= 3; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "user" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("MultiLevelGroupTest: Something wrong in adding user" + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
		}
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, null, null, null);
		ts.loginUser("user1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "group1");
		params.put(REQ_IS_GROUP, true);
		params.put(REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		initialRole.add("admin");
		params.put(REQ_ROLE, initialRole);
		res = requestJSON(API_ACCOUNT_NEW, params);
		String groupID = res.getString(RES_ACCOUNT_ID);
		assertNull("MultiLevelGroupTest: Something wrong in adding group.", res.get(RES_ERROR));
		
		List<String> expectedResult = new ArrayList<String>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Adding the user2 to group1
		params.clear();
		String[] userIDList = new String[] { userID.get(1) };
		params.put(REQ_ADD_LIST, userIDList);
		params.put(REQ_GROUP_ID, groupID);
		params.put(REQ_ROLE, "dragon");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedResult, res.get(RES_FAIL_ADD));
		// Reaffirm the result
		params.clear();
		params.put(REQ_GROUP_ID, groupID);
		params.put(REQ_USER_ID, userID.get(1));
		res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
		assertEquals("dragon", res.get(RES_SINGLE_RETURN_VALUE));
		
		// 2nd Test: Adding membershipRoles to user1
		params.clear();
		params.put(REQ_ROLE, "knight");
		params.put(REQ_GROUP_ID, userID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertNull(res.get(RES_ERROR));
		
		// 3rd Test: Adding user2 to user1
		params.clear();
		userIDList = new String[] { userID.get(1) };
		params.put(REQ_ADD_LIST, userIDList);
		params.put(REQ_GROUP_ID, userID.get(0));
		params.put(REQ_ROLE, "knight");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedResult, res.get(RES_FAIL_ADD));
		
		// Reaffirm the result
		params.put(REQ_GROUP_ID, userID.get(0));
		params.put(REQ_USER_ID, userID.get(1));
		res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
		assertEquals("knight", res.get(RES_SINGLE_RETURN_VALUE));
		ts.logout();
		
		// 4th Test: login other user to add another user
		ts.loginUser("user2@mailawer.com", VALIDPASSWORD);
		params.clear();
		userIDList = new String[] { userID.get(2) };
		params.put(REQ_ADD_LIST, userIDList);
		params.put(REQ_GROUP_ID, userID.get(0));
		params.put(REQ_ROLE, "knight");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertEquals(ERROR_NO_PRIVILEGES, res.get(RES_ERROR));
		ts.logout();
	}
	
	@Test
	public void getMemberListInfo() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
		List<String> addUserList1 = new ArrayList<String>(), addUserList2 = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>();
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 5; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "member" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getMemberListInfoTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList1.add(res.getString(RES_ACCOUNT_ID));
			} else {
				addUserList2.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, null, null, null);
		for (int idx = 1; idx <= 2; idx++) {
			ts.loginUser("member" + idx + "@mailawer.com", VALIDPASSWORD);
			params.put(REQ_USERNAME, "exampleGrp" + idx);
			params.put(REQ_IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getMemberListInfoTest: Something wrong in creating group " + idx + ".",
				res.get(RES_ERROR));
			groupID.add(res.getString(RES_ACCOUNT_ID));
			ts.logout();
		}
		ts.loginUser("member1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList1);
		params.put(REQ_GROUP_ID, groupID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		ts.logout();
		
		ts.loginUser("member2@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList2);
		params.put(REQ_GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		ts.logout();
		// Make member 5 a group account
		ts.loginUser("member5@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ROLE, "little-boy");
		params.put(REQ_GROUP_ID, userID.get(4));
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertNull(res.get(RES_ERROR));
		
		res = requestJSON(API_GROUP_GRP_ROLES, params);
		// Add some members into member 5
		params.clear();
		params.put(REQ_ROLE, "little-boy");
		params.put(REQ_ADD_LIST, addUserList2);
		params.put(REQ_GROUP_ID, userID.get(4));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Empty Submission of member 5
		ts.setURL(API_GROUP_ADMIN_GET_MEM_LIST_INFO);
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 2nd Test: Invalid Group ID
		params.clear();
		params.put(REQ_GROUP_ID, "smlj group ID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 3rd Test: Valid Group ID unknown headers ( group 1 )
		ts.loginUser("member1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_HEADERS, "['first', 'second', 'third']");
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
		ts.setAndExecuteLTC(params, expectedResult, RES_DATA, "The list has some issues.");
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		assertTrue(ts.getRes().getInt(RES_RECORDS_TOTAL) == 3);
		assertTrue(ts.getRes().getInt(RES_RECORDS_FILTERED) == 3);
		
		// 4th Test: Valid Group ID invalid headers ( group 1 )
		params.clear();
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_HEADERS, " this is an invalid header");
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(1), "member")));
		expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(3), "member")));
		expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(0), "admin")));
		ts.setAndExecuteLTC(params, expectedResult, RES_DATA, "The list has some issues.");
		assertTrue(ts.getRes().getInt(RES_RECORDS_TOTAL) == 3);
		assertTrue(ts.getRes().getInt(RES_RECORDS_FILTERED) == 3);
		
		// 5th Test: Valid Group ID default headers ( group 1 )
		params.clear();
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setAndExecuteLTC(params, expectedResult, RES_DATA, "The list has some issues.");
		assertTrue(ts.getRes().getInt(RES_RECORDS_TOTAL) == 3);
		assertTrue(ts.getRes().getInt(RES_RECORDS_FILTERED) == 3);
		
		// 6th Test: Valid Group ID custom headers ( group 1 )
		params.clear();
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_HEADERS, "['" + PROPERTIES_ROLE
			+ "', 'group__oid', 'account_email', 'randomHeader']");
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0),
			"member2@mailawer.com", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0),
			"member4@mailawer.com", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("admin", groupID.get(0),
			"member1@mailawer.com", "")));
		ts.setAndExecuteLTC(params, expectedResult, RES_DATA, "The list has some issues.");
		assertTrue(ts.getRes().getInt(RES_RECORDS_TOTAL) == 3);
		assertTrue(ts.getRes().getInt(RES_RECORDS_FILTERED) == 3);
		ts.logout();
		
		// 7th Test: No groupID, user who is not a group is logged in
		ts.loginUser("member2@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_HEADERS, "['" + PROPERTIES_ROLE
			+ "', 'group__oid', 'account_email', 'randomHeader']");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		
		// 8th Test: user is group and is logged in
		ts.loginUser("member5@mailawer.com", VALIDPASSWORD);
		
		params.clear();
		params.put(REQ_HEADERS, "['" + PROPERTIES_ROLE
			+ "', 'group__oid', 'account_email', 'randomHeader']");
		params.put(REQ_GROUP_ID, userID.get(4));
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
			"member1@mailawer.com", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
			"member3@mailawer.com", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
			"member5@mailawer.com", "")));
		ts.setAndExecuteLTC(params, expectedResult, RES_DATA, "The list has some issues.");
		assertTrue(ts.getRes().getInt(RES_RECORDS_TOTAL) == 3);
		assertTrue(ts.getRes().getInt(RES_RECORDS_FILTERED) == 3);
		ts.logout();
		
		// 9th Test: Logged in user accessing other groups
		ts.loginUser("member2@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_HEADERS, "['" + PROPERTIES_ROLE
			+ "', 'group__oid', 'account_email', 'randomHeader']");
		params.put(REQ_GROUP_ID, userID.get(4));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
		// 10th Test: Logged in user listing own group without admin membership role
		params.clear();
		params.put(REQ_HEADERS, "['" + PROPERTIES_ROLE
			+ "', 'group__oid', 'account_email', 'randomHeader']");
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void singleMemberMeta() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
		List<String> addUserList = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>();
		// Ensure that there is an existing user
		TestSet ts = new TestSet(null, API_ACCOUNT_NEW, null, null);
		for (int idx = 1; idx <= 3; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "single" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("singleMemberMetaTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		// Ensure that there is an existing group
		ts.loginUser("single1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "exampleGrp");
		params.put(REQ_IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("singleMemberMetaTest: Something wrong in creating group.", res.get(RES_ERROR));
		groupID.add(res.getString(RES_ACCOUNT_ID));
		params.clear();
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull(res.get(RES_ERROR));
		assertEquals(expectedFailResult, res.get(RES_FAIL_ADD));
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Valid admin user logged in, Empty Submission
		ts.setURL(API_GROUP_GET_SINGLE_MEM_META);
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 2nd Test: Valid admin user, Invalid userID
		params.clear();
		params.put(REQ_USER_ID, "randomID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Valid accountID, account not in group ( single 3 )
		params.clear();
		params.put(REQ_USER_ID, userID.get(2));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 4th Test: Valid accountID, account not in group, invalid groupID, no roles ( single 3 )
		params.clear();
		params.put(REQ_USER_ID, userID.get(2));
		params.put(REQ_GROUP_ID, "anyhowGroupID");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// res = requestJSON("get_single_member_meta", params);
		// assertEquals(ERROR_NO_GROUP, res.get(RES_ERROR));
		// 5th Test: Valid accountID, account not in group, Valid group, no roles ( single 3, exampleGrp )
		params.clear();
		params.put(REQ_USER_ID, userID.get(2));
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NOT_IN_GROUP_OR_ROLE, RES_ERROR);
		// res = requestJSON("get_single_member_meta", params);
		// assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(RES_ERROR));
		// 6th Test: Valid accountID, account in group, Valid group, no roles ( single 2, exampleGrp )
		params.clear();
		params.put(REQ_USER_ID, userID.get(1));
		params.put(REQ_GROUP_ID, groupID.get(0));
		res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
		assertNull(res.get(RES_ERROR));
		assertEquals("member", res.getStringMap(RES_META).get(PROPERTIES_ROLE));
		// 7th Test: Valid accountID, account in group, Valid group, with roles ( single 2, exampleGrp )
		params.clear();
		params.put(REQ_USER_ID, userID.get(1));
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "member");
		res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
		assertNull(res.get(RES_ERROR));
		assertEquals("member", res.getStringMap(RES_META).get(PROPERTIES_ROLE));
		// 8th Test: Valid accountID, account in group, Valid group, with wrong roles ( single 2, exampleGrp )
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "wrongRole");
		res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
		assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(RES_ERROR));
		ts.logout();
		// 9th Test: user logged in, user not group, getting information from group( single3, exampleGrp )
		ts.loginUser("single3@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 10th Test:  user logged in, user in group not admin retrieve itself( single2, exampleGrp )
		ts.loginUser("single2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(1));
		params.put(REQ_GROUP_ID, groupID.get(0));
		res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
		assertEquals("member", res.getStringMap(RES_META).get(PROPERTIES_ROLE));
		// 11th Test:  user logged in, user in group not admin retrieve others( single2, exampleGrp )
		ts.loginUser("single2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_GROUP_ID, groupID.get(0));
		
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void updateMemberMetaInfo() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
		List<String> addUserList = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>();
		// Ensure that there is an existing group
		TestSet ts = new TestSet(null, null, null, null);
		for (int idx = 1; idx <= 5; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "update" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("updateMemberMetaInfoTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		
		ts.loginUser("update1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "exampleUpdateGrp");
		params.put(REQ_IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("updateMemberMetaInfoTest: Something wrong in creating group.", res.get(RES_ERROR));
		groupID.add(res.getString(RES_ACCOUNT_ID));
		
		params.clear();
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setURL(API_GROUP_ADMIN_ADD_REM_MEM);
		ts.setAndExecuteLTC(params, expectedFailResult, RES_FAIL_ADD, "The list has issue.");
		ts.logout();
		
		ts.loginUser("update3@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "updateGrpTest");
		params.put(REQ_IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("updateMemberMetaInfoTest: Something wrong in creating group.", res.get(RES_ERROR));
		groupID.add(res.getString(RES_ACCOUNT_ID));
		
		params.clear();
		params.put(REQ_ROLE, "member");
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(1));
		ts.setURL(API_GROUP_ADMIN_ADD_REM_MEM);
		ts.setAndExecuteLTC(params, expectedFailResult, RES_FAIL_ADD, "The list has issue.");
		ts.logout();
		
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		// Set up a meta for usage
		ts.loginUser("update1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setURL(API_GROUP_GET_SINGLE_MEM_META);
		ts.setAndExecuteGSP(params, "admin", RES_META, PROPERTIES_ROLE);
		Map<String, Object> metaObj = ts.getRes().getStringMap(RES_META);
		metaObj.put("aRandomProp", "aRandomValue");
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		
		// 1st Test: Legit admin, editing other members
		ts.setURL(API_GROUP_UPDATE_MEM_META);
		params.clear();
		params.put(REQ_META, metaObj);
		params.put(REQ_USER_ID, userID.get(1));
		params.put(REQ_GROUP_ID, groupID.get(0));
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		ts.logout();
		// 2nd Test: Member editing itself
		ts.loginUser("update2@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, null, RES_ERROR);
		// 3rd Test: Member editing other members
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 4th Test: Admin editing other group members (although the group member is in its group)
		ts.loginUser("update1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(1));
		params.put(REQ_GROUP_ID, groupID.get(1));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 5th Test: Non Members editing group members
		ts.loginUser("update5@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void passwordReset() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing users
		for (int idx = 1; idx <= 3; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "reset" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("passwordResetTest: Something wrong in adding user.", res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
		}
		TestSet ts = new TestSet(null, API_ACCOUNT_PASS_RESET, null, null);
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Password complexity failed
		ts.loginUser("reset1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		params.put(REQ_OLD_PASSWORD, VALIDPASSWORD);
		params.put(REQ_NEW_PASSWORD, "paowapowe");
		params.put(REQ_REPEAT_PASSWORD, "paowapowe");
		ts.setAndExecuteGTC(params, ERROR_PASSWORD_COMPLEXITY, RES_ERROR);
		// 2nd Test: Changing password for others
		params.put(REQ_USER_ID, userID.get(1));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 3rd Test: Superuser changing password for others
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		params.put(REQ_NEW_PASSWORD, "tryOut123");
		params.put(REQ_REPEAT_PASSWORD, "tryOut123");
		ts.setAndExecuteGTC(params, Boolean.TRUE, RES_SUCCESS);
		ts.logout();
		// 4th Test: Changing own password
		ts.loginUser("reset3@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(2));
		ts.setAndExecuteGTC(params, Boolean.TRUE, RES_SUCCESS);
		ts.logout();
		// 5th Test: Non Logged in User changing passwords
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
	}
	
	@Test
	public void getInfoByName() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing user
		TestSet ts = new TestSet(null, API_ACCOUNT_NEW, null, null);
		for (int idx = 1; idx <= 2; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "infoName" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			ts.setAndExecuteGTC(params, null, RES_ERROR);
			userID.add(ts.getRes().getString(RES_ACCOUNT_ID));
		}
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Non logged in users
		ts.setURL(API_ACCOUNT_INFO_NAME);
		params.clear();
		params.put(REQ_USERNAME, "infoName1@mailawer.com");
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 2nd Test: Logged in users retrieving other user's information
		ts.loginUser("infoName2@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Logged in users retrieving its own information
		ts.loginUser("infoName2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "infoName2@mailawer.com");
		ts.setAndExecuteGTC(params, userID.get(1), RES_ACCOUNT_ID);
		ts.logout();
		// 4th Test: SuperUsers retrieving users information
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		params.put(REQ_USERNAME, "infoName2@mailawer.com");
		ts.setAndExecuteGTC(params, userID.get(1), RES_ACCOUNT_ID);
		ts.logout();
	}
	
	@Test
	public void getInfoByID() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		TestSet ts = new TestSet(null, API_ACCOUNT_NEW, null, null);
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 2; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "infoID" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			ts.setAndExecuteGTC(params, null, RES_ERROR);
			userID.add(ts.getRes().getString(RES_ACCOUNT_ID));
		}
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Non logged in users
		ts.setURL(API_ACCOUNT_INFO_ID);
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 2nd Test: Logged in users retrieving other user's information
		ts.loginUser("infoID2@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Logged in users retrieving its own information
		ts.loginUser("infoID2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(1));
		ts.setAndExecuteGTC(params, userID.get(1), RES_ACCOUNT_ID);
		ts.logout();
		// 4th Test: SuperUsers retrieving users information
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(1));
		ts.setAndExecuteGTC(params, userID.get(1), RES_ACCOUNT_ID);
		ts.logout();
	}
	
	// builder.put(path+"getListOfGroupIDOfMember", getListOfGroupIDOfMember);
	@Test
	public void getListOfGroupIDOfMember() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<String> expectedResult = new ArrayList<String>();
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 4; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "memberList" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		TestSet ts = new TestSet(null, API_GROUP_GET_LIST_GRP_ID_MEM, ERROR_NO_USER, RES_ERROR);
		// Ensure that there is an existing group
		for (int idx = 1; idx <= 2; idx++) {
			ts.loginUser("memberList" + idx + "@mailawer.com", VALIDPASSWORD);
			params.put(REQ_USERNAME, "groupNumber" + idx);
			params.put(REQ_IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getListOfGroupIDOfMemberTest: Something wrong in creating group " + idx + ".",
				res.get(RES_ERROR));
			groupID.add(res.getString(RES_ACCOUNT_ID));
			expectedResult.add(res.getString(RES_ACCOUNT_ID));
			ts.logout();
		}
		// Ensure that 1 user is in both group
		ts.loginUser("memberList1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "member");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user to group",
			res.get(RES_ERROR));
		ts.logout();
		ts.loginUser("memberList2@mailawer.com", VALIDPASSWORD);
		addUserList.add(userID.get(2));
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user to group",
			res.get(RES_ERROR));
		ts.logout();
		
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Non logged in user accessing group information of members
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_USER_NOT_LOGIN, RES_ERROR);
		// 2nd Test: Logged in user accessing other member's group information
		ts.loginUser("memberList4@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Logged in user accessing its own group information
		params.put(REQ_USER_ID, userID.get(3));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		ts.logout();
		// 4th Test: Logged in user accessing other group's member's group information
		ts.loginUser("memberList3@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 5th Test: Superuser accessing user's group information
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		expectedResult.clear();
		expectedResult.add(groupID.get(0));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		ts.logout();
		// 6th Test: Group admin accessing own group member's information
		ts.loginUser("memberList1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_USER_ID, userID.get(1));
		expectedResult.add(groupID.get(1));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		// 7th Test: Group admin accessing other group member's information
		params.put(REQ_USER_ID, userID.get(2));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void getListOfGroupObjectOfMember() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<Object> expectedResult = new ArrayList<Object>(), groupInfo = new ArrayList<Object>();
		;
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 4; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "memLi" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull(
				"getListOfGroupObjectOfMemberTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		
		TestSet ts = new TestSet(null, API_GROUP_GET_LIST_GRP_OBJ_MEM, ERROR_NO_USER, RES_ERROR);
		// Ensure that there is an existing group
		for (int idx = 1; idx <= 2; idx++) {
			ts.loginUser("memLi" + idx + "@mailawer.com", VALIDPASSWORD);
			params.put(REQ_USERNAME, "grpOb" + idx);
			params.put(REQ_IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getListOfGroupObjectOfMemberTest: Something wrong in creating group " + idx
				+ ".", res.get(RES_ERROR));
			groupID.add(res.getString(RES_ACCOUNT_ID));
			params.clear();
			params.put(REQ_USER_ID, res.get(RES_ACCOUNT_ID));
			res = requestJSON(API_ACCOUNT_INFO_ID, params);
			assertNull("getListOfGroupObjectOfMemberTest: Something wrong in retrieving group info "
				+ idx + ".", res.get(RES_ERROR));
			groupInfo.add(res);
			ts.logout();
		}
		
		// Ensure that user is in both group
		ts.loginUser("memLi1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "member");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("ListGroupTest: Something wrong in adding user to group", res.get(RES_ERROR));
		ts.logout();
		ts.loginUser("memLi2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("ListGroupTest: Something wrong in adding user to group", res.get(RES_ERROR));
		ts.logout();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Non logged in user accessing group information of members
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_USER_NOT_LOGIN, RES_ERROR);
		// 2nd Test: Logged in user accessing other member's group information
		ts.loginUser("memLi4@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Logged in user accessing its own group information
		params.put(REQ_USER_ID, userID.get(3));
		expectedResult.clear();
		expectedResult.add(groupInfo.get(1));
		expectedResult.add(groupInfo.get(0));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		ts.logout();
		// 4th Test: Logged in user accessing other group's member's group information
		ts.loginUser("memLi3@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 5th Test: Superuser accessing user's group information
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		expectedResult.clear();
		expectedResult.add(groupInfo.get(0));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		ts.logout();
		// 6th Test: Group admin accessing own group member's information
		ts.loginUser("memLi1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(1));
		expectedResult.clear();
		expectedResult.add(groupInfo.get(1));
		expectedResult.add(groupInfo.get(0));
		ts.setAndExecuteLTC(params, expectedResult, RES_LIST, "The list has some issues");
		// 7th Test: Group admin accessing other group member's information
		params.put(REQ_USER_ID, userID.get(2));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
	}
	
	@Test
	public void removeAccount() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<Object> expectedResult = new ArrayList<Object>();
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 5; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "remove" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("removeAccountTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 0) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
		}
		TestSet ts = new TestSet(null, API_ACCOUNT_ADMIN_REMOVE, null, null);
		// Ensure that there is an existing group
		ts.loginUser("remove1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USERNAME, "grpRemove");
		params.put(REQ_IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("removeAccountTest: Something wrong in creating group.", res.get(RES_ERROR));
		groupID.add(res.getString(RES_ACCOUNT_ID));
		// Ensure that user is in group
		params.clear();
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "member");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("removeAccountTest: Something wrong in adding user to group", res.get(RES_ERROR));
		// Ensure that there is a member of a member of a group
		params.clear();
		params.put(REQ_GROUP_ID, userID.get(0));
		params.put(REQ_ROLE, "rowABoat");
		res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		assertNull("removeAccountTest: Something wrong in adding user to group", res.get(RES_ERROR));
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(2));
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, userID.get(0));
		params.put(REQ_ROLE, "rowABoat");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("removeAccountTest: Something wrong in adding user to group", res.get(RES_ERROR));
		ts.logout();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Non logged in user removing account
		params.clear();
		params.put(REQ_USER_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 2nd Test: Logged in user removing other account
		ts.loginUser("remove2@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 3rd Test: Logged in user remove its account
		params.put(REQ_USER_ID, userID.get(1));
		ts.setAndExecuteGTC(params, Boolean.TRUE, RES_SUCCESS);
		// 4th Test: Superuser removing account
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(2));
		ts.setAndExecuteGTC(params, Boolean.TRUE, RES_SUCCESS);
		ts.logout();
		// 5th Test: Admin of group remove member account
		ts.loginUser("remove1@mailawer.com", VALIDPASSWORD);
		params.put(REQ_USER_ID, userID.get(3));
		ts.setAndExecuteGTC(params, ERROR_NO_PRIVILEGES, RES_ERROR);
		
	}
	
	@Test
	public void getUserOrGroupList() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<String> expectedResult = new ArrayList<String>();
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 2; idx++) {
			params.clear();
			params.put(REQ_USERNAME, "getTestMemb" + idx + "@mailawer.com");
			params.put(REQ_PASSWORD, VALIDPASSWORD);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getUserOrGroupListTest: Something wrong in adding user " + idx + ".",
				res.get(RES_ERROR));
			userID.add(res.getString(RES_ACCOUNT_ID));
			if (idx % 2 == 1) {
				addUserList.add(res.getString(RES_ACCOUNT_ID));
			}
			expectedResult.add(userID.get(idx - 1));
		}
		TestSet ts = new TestSet(null, API_ACCOUNT_ADMIN_GET_U_G_LIST, null, null);
		// Ensure that there is an existing group
		for (int idx = 1; idx <= 2; idx++) {
			ts.loginUser("getTestMemb" + idx + "@mailawer.com", VALIDPASSWORD);
			params.put(REQ_USERNAME, "getTestGrp" + idx);
			params.put(REQ_IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getUserOrGroupListTest: Something wrong in creating group " + idx + ".",
				res.get(RES_ERROR));
			groupID.add(res.getString(RES_ACCOUNT_ID));
			ts.logout();
		}
		
		// Ensure that user is in both group
		ts.loginUser("getTestMemb1@mailawer.com", VALIDPASSWORD);
		params.clear();
		params.put(REQ_ADD_LIST, addUserList);
		params.put(REQ_GROUP_ID, groupID.get(0));
		params.put(REQ_ROLE, "member");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getUserOrGroupListTest: Something wrong in adding user to group",
			res.get(RES_ERROR));
		ts.logout();
		
		ts.loginUser("getTestMemb2@mailawer.com", VALIDPASSWORD);
		params.put(REQ_GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getUserOrGroupListTest: Something wrong in adding user to group",
			res.get(RES_ERROR));
		ts.logout();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Not logged in user
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		// 2nd Test: Logged in user not superuser
		ts.loginUser("getTestMemb2@mailawer.com", VALIDPASSWORD);
		ts.setAndExecuteGTC(null, ERROR_NO_PRIVILEGES, RES_ERROR);
		ts.logout();
		// 3rd Test: Superuser search
		ts.loginUser(SUPERUSERNAME, VALIDPASSWORD);
		ts = new TestSet(null, API_ACCOUNT_ADMIN_GET_U_G_LIST, expectedResult, RES_DATA);
		ts.executeTrueTestCase();
	}
	
	class TestSet {
		private Map<String, Object> params = null;
		private String url = "";
		private Object expectedResult = "";
		private String resultToGetFrom = "";
		private GenericConvertMap<String, Object> res = null;
		
		public TestSet(Map<String, Object> params, String url, Object expectedResult,
			String resultToGetFrom) {
			this.params = params;
			this.url = url;
			this.expectedResult = expectedResult;
			this.resultToGetFrom = resultToGetFrom;
		}
		
		public Map<String, Object> getParams() {
			return params;
		}
		
		public void setURL(String url) {
			this.url = url;
		}
		
		public String getURL() {
			return url;
		}
		
		public Object getExpectedResult() {
			return expectedResult;
		}
		
		public String getResultToGetFrom() {
			return resultToGetFrom;
		}
		
		public GenericConvertMap<String, Object> getRes() {
			return res;
		}
		
		public void executeGenericSpecifyProp(String prop) {
			res = requestJSON(url, params);
			assertEquals(expectedResult, res.getStringMap(resultToGetFrom).get(prop));
		}
		
		public void executeGenericTestCase() {
			res = requestJSON(url, params);
			assertEquals(expectedResult, res.get(resultToGetFrom));
		}
		
		public void executeListTestCase(String errorMsg) {
			res = requestJSON(url, params);
			List<Object> result = ConvertJSON.toList(ConvertJSON.fromObject(expectedResult));
			assertThat(errorMsg, res.getList(resultToGetFrom), containsInAnyOrder(result.toArray()));
		}
		
		public void executeTrueTestCase() {
			res = requestJSON(url, params);
			List<Object> result = ConvertJSON.toList(ConvertJSON.fromObject(res.get(RES_DATA)));
			assertTrue(result.size() > 0);
		}
		
		public void setAndExecuteGSP(Map<String, Object> params, Object expectedResult,
			String resultToGetFrom, String prop) {
			this.params = params;
			this.expectedResult = expectedResult;
			this.resultToGetFrom = resultToGetFrom;
			executeGenericSpecifyProp(prop);
		}
		
		public void setAndExecuteGTC(Map<String, Object> params, Object expectedResult,
			String resultToGetFrom) {
			this.params = params;
			this.expectedResult = expectedResult;
			this.resultToGetFrom = resultToGetFrom;
			executeGenericTestCase();
		}
		
		public void setAndExecuteLTC(Map<String, Object> params, Object expectedResult,
			String resultToGetFrom, String errorMsg) {
			this.params = params;
			this.expectedResult = expectedResult;
			this.resultToGetFrom = resultToGetFrom;
			executeListTestCase(errorMsg);
		}
		
		public void loginUser(String name, String pass) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(REQ_USERNAME, name);
			params.put(REQ_PASSWORD, pass);
			assertNull(requestJSON("account/login", params).get(RES_ERROR));
		}
		
		public void logout() {
			assertEquals(Boolean.TRUE, requestJSON("account/logout", null).get(RES_RETURN));
		}
	}
	
	// @Test
	// public void getListOfMemberObjectOfGroup(){
	// 	GenericConvertMap<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(REQ_USERNAME, "groupObjToRetrieve");
	// 	params.put(REQ_IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(REQ_USERNAME, "groupObjWithNoMember");
	// 	params.put(REQ_IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(RES_ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(REQ_USERNAME, "user test 1");
	// 	params.put(REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(RES_ERROR));
	//
	// 	params.put(REQ_USERNAME, "user test 2");
	// 	params.put(REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(RES_ERROR));
	//
	// 	// Ensure that group has both member
	// 	params.clear();
	// 	params.put(REQ_USERNAME, "user test 1");
	// 	params.put(REQ_GROUPNAME, "groupObjToRetrieve");
	// 	params.put(REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(RES_ERROR));
	//
	// 	params.put(REQ_USERNAME, "user test 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	res = requestJSON("getListOfMemberIDInGroup", null);
	// 	assertEquals(ERROR_NO_GROUPNAME, res.get(RES_ERROR));
	// 	params.clear();
	// 	params.put(REQ_GROUPNAME, "anyhowgroup");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(RES_ERROR));
	//
	// 	params.put(REQ_GROUPNAME, "groupObjToRetrieve");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(RES_LIST).size() == 2);
	//
	// 	params.put(REQ_GROUPNAME, "groupObjWithNoMember");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(RES_LIST).size() == 0);
	// }
}
