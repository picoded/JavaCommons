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
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

/// Test the AccountTable API specifically
public class AccountTableApi_test extends ApiModule_test {
	
	//----------------------------------------------------------------------------------------
	//
	//  Test setup
	//
	//----------------------------------------------------------------------------------------
	
	/// The test servlet to use
	public static class AccountTableApiTestServlet extends ApiModuleTestServlet {
		@Override
		public ApiModule moduleSetup(CommonStack stack) {
			AccountTable table = new AccountTable(stack, "account");
			AccountTableApi ret = new AccountTableApi(table);
			// table.loginThrottle = (inAo, failures) -> {
			// System.out.println("this was not ran <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			// 	return (long) 2;
			// };
			if (!table.hasLoginName("laughing-man")) {
				AccountObject ao = table.newEntry("laughing-man");
				ao.put(PROPERTIES_EMAIL, "laughing-man@testlalala.com");
				ao.setPassword("The Catcher in the Rye");
			}
			
			return ret;
		}
	}
	
	public CorePage setupServlet() {
		return new AccountTableApiTestServlet();
	}
	
	//----------------------------------------------------------------------------------------
	//
	//  Test running
	//
	//----------------------------------------------------------------------------------------
	
	@Test
	public void loginProcessFlow() {
		// reuse result map
		Map<String, Object> res = null;
		
		// Check for invalid login
		assertEquals(Boolean.FALSE, requestJSON(API_ACCOUNT_LOGIN, null).get(RESULT));
		
		// Does a failed login
		Map<String, Object> loginParams = new HashMap<String, Object>();
		loginParams.put(LOGINNAME, "laughing-man");
		loginParams.put(PASSWORD, "Is the enemy");
		res = requestJSON(API_ACCOUNT_LOGIN, loginParams);
		
		assertEquals(Boolean.FALSE, res.get(RESULT));
		assertEquals(ERROR_FAIL_LOGIN, res.get(ERROR));
		
		// Check for invalid login
		assertEquals(Boolean.FALSE, requestJSON(API_ACCOUNT_LOGIN, null).get(RESULT));
		
		// Does the actual login
		loginParams.put(LOGINNAME, "laughing-man");
		loginParams.put(PASSWORD, "The Catcher in the Rye");
		
		// Request and check result
		res = requestJSON(API_ACCOUNT_LOGIN, loginParams);
		assertNull(res.get(ERROR));
		assertNull(res.get(INFO));
		assertEquals(Boolean.TRUE, res.get(RESULT));
		// Validate that login is valid
		res = requestJSON(API_ACCOUNT_LOGIN, null);
		assertEquals(Boolean.TRUE, res.get(RESULT));
		
		// Log out current user
		res = requestJSON(API_ACCOUNT_LOGOUT, null);
		assertEquals(Boolean.TRUE, res.get(RESULT));
		
		// Validate that logout is valid
		res = requestJSON(API_ACCOUNT_LOGIN, null);
		assertEquals(Boolean.FALSE, res.get(RESULT));
		
		// Login using email address
		loginParams.put(LOGINNAME, "laughing-man");
		loginParams.put(PASSWORD, "The Catcher in the Rye");
		res = requestJSON(API_ACCOUNT_LOGIN, loginParams);
		assertNull(res.get(ERROR));
		assertNull(res.get(INFO));
		assertEquals(Boolean.TRUE, res.get(RESULT));
		// Log out current user
		res = requestJSON(API_ACCOUNT_LOGOUT, null);
		assertEquals(Boolean.TRUE, res.get(RESULT));
	}
	
	//
	// @Test
	// public void loginLockingIncrement() {
	// 	// reuse result map
	// 	Map<String, Object> res = null;
	// 	// Checks that the test begins with the user not logged in
	// 	res = requestJSON(API_ACCOUNT_RESULT, null);
	// 	assertEquals(Boolean.FALSE, res.get(RESULT));
	//
	// 	Map<String, Object> loginParams = new HashMap<String, Object>();
	// 	loginParams.put(LOGINNAME, "laughing-man");
	// 	loginParams.put(PASSWORD, "Is the enemy");
	// 	int doTestLoop = 3, currentLoop = 0;
	// 	while (currentLoop < doTestLoop) {
	// 		res = requestJSON(API_ACCOUNT_LOGIN, loginParams);
	// 		// System.out.println(res.get(ERROR)+" <<<<<<<<<<");
	// 		Map<String, Object> params = new HashMap<String, Object>();
	// 		params.put(ACCOUNT_NAME, "laughing-man");
	// 		int waitTime = (int) requestJSON(API_ACCOUNT_LOCKTIME, params).get("lockTime");
	// 		if (currentLoop % 2 == 0) {
	// 			assertEquals(Boolean.FALSE, res.get(RESULT));
	// 			assertEquals("It failed on the Loop Number: " + (currentLoop + 1) + " with waitTime: "
	// 				+ waitTime + "\n" + "Likely failure is due to insufficient Thread.sleep()\n",
	// 				ERROR_FAIL_LOGIN, res.get(ERROR));
	// 		} else {
	// 			assertThat("It failed on the Loop Number: " + (currentLoop + 1) + " with waitTime: "
	// 				+ waitTime + "\n", res.get(ERROR).toString(), containsString("user locked out"));
	// 			try {
	// 				Thread.sleep(waitTime * 1000);
	// 			} catch (InterruptedException ie) {
	// 			}
	// 		}
	// 		currentLoop++;
	// 	}
	// }
	//
	@Test
	public void createNewUserAccount() {
		Map<String, Object> res = null;
		Map<String, Object> createDetails = new HashMap<String, Object>();
		Map<String, Object> meta = new HashMap<String, Object>();
		meta.put(PROPERTIES_EMAIL, "tste@lalalala.com");
		// createDetails
		res = requestJSON(API_ACCOUNT_NEW, createDetails);
		assertEquals(ERROR_NO_LOGINNAME, res.get(ERROR));
		
		createDetails.put(LOGINNAME, "little-boy");
		res = requestJSON(API_ACCOUNT_NEW, createDetails);
		assertEquals(ERROR_NO_PASSWORD, res.get(ERROR));
		// Successfully created account
		createDetails.put(PASSWORD, "sooo smallll");
		res = requestJSON(API_ACCOUNT_NEW, createDetails);
		assertNotNull(res.get(DATA));
		assertNull(res.get(ERROR));
		
		String accountID = res.get(ACCOUNT_ID).toString();
		
		//Create same user again
		res = requestJSON(API_ACCOUNT_NEW, createDetails);
		assertEquals("Object already exists in account Table", res.get(ERROR));
		assertEquals(accountID, res.get(ACCOUNT_ID));
	}
	
	// @Test
	// public void createNewGroup() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	Map<String, Object> createDetails = new HashMap<String, Object>();
	// 	createDetails.put(LOGINNAME, "boy band");
	// 	createDetails.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, createDetails);
	// 	assertNull(res.get(ERROR));
	//
	// 	// Checks if it is a group
	//
	// 	// Creates the same group
	// 	res = requestJSON(API_ACCOUNT_NEW, createDetails);
	// 	assertEquals("Object already exists in account Table", res.get(ERROR));
	//
	// 	// Not using the correct value to create group
	// 	createDetails.put(LOGINNAME, "girl band");
	// 	createDetails.put(IS_GROUP, "random Words");
	// 	res = requestJSON(API_ACCOUNT_NEW, createDetails);
	// 	assertNotNull(res.get(ERROR));
	//
	// 	createDetails.put(IS_GROUP, "1");
	// 	res = requestJSON(API_ACCOUNT_NEW, createDetails);
	// 	assertNull(res.get(ERROR));
	// 	String accountID = res.getString(ACCOUNT_ID);
	//
	// 	createDetails.clear();
	// 	createDetails.put(GROUP_ID, accountID);
	// 	res = requestJSON(API_GROUP_GRP_ROLES, createDetails);
	// 	ArrayList<String> expectedRoles = new ArrayList<String>();
	// 	expectedRoles.add("member");
	// 	expectedRoles.add("admin");
	// 	assertEquals(expectedRoles, res.get(LIST));
	//
	// 	createDetails.clear();
	// 	createDetails.put(LOGINNAME, "bbbbb");
	// 	createDetails.put(IS_GROUP, "1");
	// 	expectedRoles.clear();
	// 	expectedRoles.add("grandma");
	// 	expectedRoles.add("grandpa");
	// 	expectedRoles.add("admin");
	// 	createDetails.put(DEFAULT_ROLES, false);
	// 	createDetails.put(ROLE, expectedRoles);
	// 	res = requestJSON(API_ACCOUNT_NEW, createDetails);
	// 	assertNull(res.get(ERROR));
	// 	accountID = res.getString(ACCOUNT_ID);
	//
	// 	createDetails.clear();
	// 	createDetails.put(GROUP_ID, accountID);
	// 	res = requestJSON(API_GROUP_GRP_ROLES, createDetails);
	// 	assertEquals(expectedRoles, res.get(LIST));
	// }
	//
	// @Test
	// public void groupRoles() {
	// 	// reuse result map
	// 	GenericConvertMap<String, Object> res = null;
	// 	res = requestJSON(API_GROUP_GRP_ROLES, null);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	// 	Map<String, Object> params = new HashMap<String, Object>();
	//
	// 	params.put(GROUP_ID, "randomID HAHAHAHA");
	// 	res = requestJSON(API_GROUP_GRP_ROLES, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	// Add in a group specifically for this test
	// 	params.clear();
	// 	params.put(LOGINNAME, "Macho");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull(res.get(ERROR));
	//
	// 	params.clear();
	// 	params.put(GROUP_ID, res.getString(ACCOUNT_ID));
	// 	res = requestJSON(API_GROUP_GRP_ROLES, params);
	// 	assertNull(res.get(ERROR));
	// 	ArrayList<String> expectedRoles = new ArrayList<String>();
	// 	expectedRoles.add("member");
	// 	expectedRoles.add("admin");
	// 	assertEquals(expectedRoles, res.get(LIST));
	// }
	//
	// @Test
	// public void addAndRemoveMemberToGroupTest() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>();
	// 	List<String> groupID = new ArrayList<String>();
	// 	for (int idx = 1; idx <= 3; idx++) {
	// 		params.put(LOGINNAME, "member " + idx);
	// 		params.put(PASSWORD, "password");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("addAndRemoveMemberToGroupTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 	}
	// 	// Ensure that there is an existing group
	// 	for (int idx = 0; idx <= 3; idx++) {
	// 		params.put(LOGINNAME, "group " + idx);
	// 		params.put(IS_GROUP, true);
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull(
	// 			"addAndRemoveMemberToGroupTest: Something wrong in creating group " + idx + ".",
	// 			res.get(ERROR));
	// 		groupID.add(res.getString(ACCOUNT_ID));
	// 	}
	// 	// Adding roles to non group user account (member 3)
	// 	params.clear();
	// 	params.put(GROUP_ID, userID.get(2));
	// 	params.put(ROLE, "knight");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertNull("addAndRemoveMemberToGroupTest: Something wrong in adding role to group.",
	// 		res.get(ERROR));
	//
	// 	List<String> removeUserList = new ArrayList<String>(), addUserList = new ArrayList<String>();
	// 	List<String> expectedFailResult = new ArrayList<String>(), expectedPassResult = new ArrayList<String>();
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// 1st Test: Submit nothing at all
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, null);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	//
	// 	// 2nd Test: Non existence group
	// 	params.clear();
	// 	params.put(GROUP_ID, "randomID HAHHAHAHA");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	// 3rd Test: Remove non existence members & existence members not in group from Existence group (group 1)
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	removeUserList.add("This is random ID");
	// 	removeUserList.add(userID.get(0));
	// 	params.put(REMOVE_LIST, removeUserList);
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	expectedFailResult.add("ID: This is random ID, Error: " + ERROR_NO_USER);
	// 	expectedFailResult.add("ID: " + userID.get(0) + ", Error: User is not in group.");
	// 	assertEquals(expectedFailResult, res.get(FAIL_REMOVE));
	//
	// 	// 4th Test: Add non existence members without role into existence group (group 1)
	// 	params.clear();
	// 	addUserList.add("This is another random ID");
	// 	addUserList.add("One for the road");
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(ERROR_NO_ROLE, res.get(ERROR));
	//
	// 	// 5th Test: Add non existence members and existence member with non existence role into group (group 2)
	// 	expectedFailResult.clear();
	// 	expectedFailResult.add("ID: This is another random ID, Error: " + ERROR_NO_USER);
	// 	expectedFailResult.add("ID: One for the road, Error: " + ERROR_NO_USER);
	// 	expectedFailResult.add("ID: " + userID.get(1) + ", Error: "
	// 		+ "User is already in group or role is not found.");
	// 	addUserList.add(userID.get(1));
	// 	params.put(ROLE, "this is random role");
	// 	params.put(ADD_LIST, addUserList);
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	//
	// 	// 6th Test: Add existence members and repeated member with existence role into group (group 2)
	// 	expectedFailResult.clear();
	// 	params.clear();
	// 	addUserList.clear();
	// 	addUserList.add(userID.get(0));
	// 	addUserList.add(userID.get(1));
	// 	addUserList.add(userID.get(1));
	// 	params.put(ROLE, "member");
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(1));
	// 	expectedFailResult.add("ID: " + userID.get(1) + ", Error: "
	// 		+ "User is already in group or role is not found.");
	// 	expectedPassResult.add(userID.get(0));
	// 	expectedPassResult.add(userID.get(1));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	// 	assertEquals(expectedPassResult, res.get(SUCCESS_ADD));
	//
	// 	// 7th Test: Remove existence members and repeated members from existence group (group 2)
	// 	expectedFailResult.clear();
	// 	expectedPassResult.clear();
	// 	params.clear();
	// 	removeUserList.clear();
	// 	removeUserList.add(userID.get(0));
	// 	removeUserList.add(userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(1));
	// 	params.put(REMOVE_LIST, removeUserList);
	// 	expectedFailResult.add("ID: " + userID.get(0) + ", Error: User is not in group.");
	// 	expectedPassResult.add(userID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(expectedFailResult, res.get(FAIL_REMOVE));
	// 	assertEquals(expectedPassResult, res.get(SUCCESS_REMOVE));
	//
	// 	// 8th Test: Remove valid users from non group account (member 2)
	// 	expectedFailResult.clear();
	// 	params.clear();
	// 	removeUserList.clear();
	// 	removeUserList.add(userID.get(0));
	// 	params.put(GROUP_ID, userID.get(1));
	// 	params.put(REMOVE_LIST, removeUserList);
	// 	expectedFailResult.add("ID: " + userID.get(1) + ", Error: This is not a group.");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(expectedFailResult, res.get(FAIL_REMOVE));
	//
	// 	// 9th Test: Adding valid users to user account that do not have roles a.k.a not a group yet (member 2)
	// 	expectedFailResult.clear();
	// 	params.clear();
	// 	addUserList.clear();
	// 	addUserList.add(userID.get(0));
	// 	params.put(GROUP_ID, userID.get(1));
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(ROLE, "member");
	// 	expectedFailResult.add("ID: " + userID.get(0)
	// 		+ ", Error: User is already in group or role is not found.");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	//
	// 	// 10th Test: Adding valid users to user account that is a group (member 3)
	// 	expectedPassResult.clear();
	// 	params.clear();
	// 	addUserList.clear();
	// 	addUserList.add(userID.get(0));
	// 	params.put(GROUP_ID, userID.get(2));
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(ROLE, "knight");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	expectedPassResult.add(userID.get(0));
	// 	assertEquals(expectedPassResult, res.get(SUCCESS_ADD));
	//
	// 	// 11th Test: Add the same user to the same group (member 3)
	// 	expectedFailResult.clear();
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	expectedFailResult.add("ID: " + userID.get(0)
	// 		+ ", Error: User is already in group or role is not found.");
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	// }
	//
	// //
	// // @Test
	// // public void getMemberMeta(){
	// // 	Map<String,Object> res = null;
	// // 	/// -----------------------------------------
	// // 	/// Preparation before commencement of Test
	// // 	/// -----------------------------------------
	// // 	// Ensure that there is an existing user
	// // 	Map<String,Object> params = new HashMap<String,Object>();
	// // 	params.put(LOGINNAME, "membermeta");
	// // 	params.put(PASSWORD, "password");
	// // 	res = requestJSON(API_ACCOUNT_NEW, params);
	// // 	assertNull("MemberMetaTest: Something wrong in adding user.", res.get(ERROR));
	// // 	// Ensure that there is an existing group
	// // 	params.clear();
	// // 	params.put(LOGINNAME, "memberMetaGroup");
	// // 	params.put(IS_GROUP, true);
	// // 	res = requestJSON(API_ACCOUNT_NEW, params);
	// // 	assertNull("MemberMetaTest: Something wrong in adding group.", res.get(ERROR));
	// // 	// Ensure that the user is added to the group
	// // 	params.clear();
	// // 	params.put(LOGINNAME, "membermeta");
	// // 	params.put(GROUPNAME, "memberMetaGroup");
	// // 	params.put(ROLE, "member");
	// // 	res = requestJSON("addMember", params);
	// // 	assertNull("MemberMetaTest: Something wrong in adding member to group.", res.get(ERROR));
	// // 	/// -----------------------------------------
	// // 	/// End of Preparation before commencement of Test
	// // 	/// -----------------------------------------
	// //
	// // 	// Invalid user, group and role
	// // 	res = requestJSON("getMemberMeta", null);
	// // 	assertEquals(ERROR_NO_LOGINNAME, res.get(ERROR));
	// //
	// // 	params.clear();
	// // 	params.put(LOGINNAME, "wrong member");
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertEquals(ERROR_NO_GROUPNAME, res.get(ERROR));
	// //
	// // 	params.put(GROUPNAME, "wrong group");
	// // 	params.put(ROLE, "unknown role");
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	// //
	// // 	// Valid group
	// // 	params.put(GROUPNAME, "memberMetaGroup");
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	// //
	// // 	// Valid user
	// // 	params.put(LOGINNAME, "membermeta");
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// //
	// // 	// Valid role
	// // 	params.put(ROLE, "member");
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertNull(res.get(ERROR));
	// // 	assertNotNull(res.get(META));
	// //
	// // 	// No role at all
	// // 	params.remove(ROLE);
	// // 	res = requestJSON("getMemberMeta", params);
	// // 	assertNull(res.get(ERROR));
	// // 	assertNotNull(res.get(META));
	// // }
	// //
	// //
	// @Test
	// public void getMemberRole() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	params.put(LOGINNAME, "memberrole");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("getMemberRole: Something wrong in adding user.", res.get(ERROR));
	// 	String userID = res.getString(ACCOUNT_ID);
	//
	// 	params.put(LOGINNAME, "memberNotInGroup");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("getMemberRole: Something wrong in adding user.", res.get(ERROR));
	// 	String wrongUserID = res.getString(ACCOUNT_ID);
	// 	// Ensure that there is an existing group
	// 	params.clear();
	// 	params.put(LOGINNAME, "memberRoleGroup");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("getMemberRole: Something wrong in adding group.", res.get(ERROR));
	// 	String groupID = res.getString(ACCOUNT_ID);
	// 	// Ensure that the user is added to the group
	// 	params.clear();
	// 	String[] userIDList = new String[] { userID };
	// 	params.put(ADD_LIST, userIDList);
	// 	params.put(GROUP_ID, groupID);
	// 	params.put(ROLE, "member");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull("getMemberRole: Something wrong in adding member to group.", res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// 1st Test: Empty submission
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, null);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	//
	// 	// 2nd Test: Invalid groupID
	// 	params.clear();
	// 	params.put(GROUP_ID, "wrong group ID");
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_USER_ID, res.get(ERROR));
	//
	// 	// 3rd Test: Invalid member ID
	// 	params.put(USER_ID, "wrong member ID");
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	// 4th Test: Valid groupID
	// 	params.put(GROUP_ID, groupID);
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	//
	// 	// 5th Test: Existence wrong userID
	// 	params.put(USER_ID, wrongUserID);
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals("No role for user is found.", res.get(ERROR));
	//
	// 	// 6th Test: Valid UserID
	// 	params.put(USER_ID, userID);
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertNull(res.get(ERROR));
	// 	assertNotNull(res.get(SINGLE_RESULT_VALUE));
	// }
	//
	// @Test
	// public void addNewMembershipRole() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "addNewMembershipRole");
	// 	params.put(IS_GROUP, true);
	// 	params.put(DEFAULT_ROLES, false);
	// 	ArrayList<String> initialRole = new ArrayList<String>();
	// 	initialRole.add("dragon");
	// 	initialRole.add("tiger");
	// 	initialRole.add("lion");
	// 	initialRole.add("admin");
	// 	params.put(ROLE, initialRole);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	String accountID = res.getString(ACCOUNT_ID);
	// 	assertNull("AddMemberShipTest: Something wrong in adding group.", res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	params.clear();
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, null);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	//
	// 	params.put(GROUP_ID, "wrong group ID");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_ROLE, res.get(ERROR));
	//
	// 	params.put(ROLE, "roleToAdd");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	params.put(GROUP_ID, accountID);
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertNull(res.get(ERROR));
	// 	initialRole.add("roleToAdd");
	// 	assertEquals(initialRole, res.getStringMap(META).get(PROPERTIES_MEMBERSHIP_ROLE));
	// }
	//
	// @Test
	// public void removeMembershipRoleFromGroup() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "removeMembershipRole");
	// 	params.put(IS_GROUP, true);
	// 	params.put(DEFAULT_ROLES, false);
	// 	ArrayList<String> initialRole = new ArrayList<String>();
	// 	initialRole.add("dragon");
	// 	initialRole.add("tiger");
	// 	initialRole.add("lion");
	// 	initialRole.add("admin");
	// 	params.put(ROLE, initialRole);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	String groupID = res.getString(ACCOUNT_ID);
	// 	assertNull("removeMembershipTest: Something wrong in adding group.", res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	params.clear();
	// 	res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, null);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	//
	// 	params.put(GROUP_ID, "wrong group ID");
	// 	res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_ROLE, res.get(ERROR));
	//
	// 	params.put(ROLE, "roleToRemove");
	// 	res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	params.put(GROUP_ID, groupID);
	// 	res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, params);
	// 	assertEquals("No such role is found.", res.get(ERROR));
	//
	// 	params.put(ROLE, "dragon");
	// 	initialRole.remove("dragon");
	// 	res = requestJSON(API_GROUP_ADMIN_REM_MEM_ROLE, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(initialRole, res.getStringMap(META).get("membershipRoles"));
	// }
	//
	// @Test
	// public void multiLevelGroupOwnership() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "group1");
	// 	params.put(IS_GROUP, true);
	// 	params.put(DEFAULT_ROLES, false);
	// 	ArrayList<String> initialRole = new ArrayList<String>();
	// 	initialRole.add("dragon");
	// 	initialRole.add("tiger");
	// 	initialRole.add("lion");
	// 	params.put(ROLE, initialRole);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	String groupID = res.getString(ACCOUNT_ID);
	// 	assertNull("MultiLevelGroupTest: Something wrong in adding group.", res.get(ERROR));
	//
	// 	// Ensure that there is an existing users
	// 	List<String> userID = new ArrayList<String>();
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "user" + idx);
	// 		params.put(PASSWORD, "thisismypassword");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("MultiLevelGroupTest: Something wrong in adding user" + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 	}
	// 	List<String> expectedResult = new ArrayList<String>();
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// Adding the user1 to group1
	// 	params.clear();
	// 	String[] userIDList = new String[] { userID.get(0) };
	// 	params.put(ADD_LIST, userIDList);
	// 	params.put(GROUP_ID, groupID);
	// 	params.put(ROLE, "dragon");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedResult, res.get(FAIL_ADD));
	// 	// Reaffirm the result
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID);
	// 	params.put(USER_ID, userID.get(0));
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals("dragon", res.get(SINGLE_RESULT_VALUE));
	//
	// 	// Adding membershipRoles to user1
	// 	params.clear();
	// 	params.put(ROLE, "knight");
	// 	params.put(GROUP_ID, userID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertNull(res.get(ERROR));
	//
	// 	// Adding user2 to user1
	// 	params.clear();
	// 	userIDList = new String[] { userID.get(1) };
	// 	params.put(ADD_LIST, userIDList);
	// 	params.put(GROUP_ID, userID.get(0));
	// 	params.put(ROLE, "knight");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedResult, res.get(FAIL_ADD));
	//
	// 	// Reaffirm the result
	// 	params.put(GROUP_ID, userID.get(0));
	// 	params.put(USER_ID, userID.get(1));
	// 	res = requestJSON(API_GROUP_GET_MEM_ROLE, params);
	// 	assertEquals("knight", res.get(SINGLE_RESULT_VALUE));
	// }
	//
	// @Test
	// public void getMemberListInfo() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
	// 	List<String> addUserList1 = new ArrayList<String>(), addUserList2 = new ArrayList<String>();
	// 	List<String> expectedFailResult = new ArrayList<String>();
	// 	// Ensure that there is an existing group
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.put(LOGINNAME, "exampleGrp" + idx);
	// 		params.put(IS_GROUP, true);
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("getMemberListInfoTest: Something wrong in creating group " + idx + ".",
	// 			res.get(ERROR));
	// 		groupID.add(res.getString(ACCOUNT_ID));
	// 	}
	// 	// Ensure that there is an existing user
	// 	for (int idx = 1; idx <= 5; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "member" + idx);
	// 		params.put(PASSWORD, "password");
	// 		Map<String, Object> meta = new HashMap<String, Object>();
	// 		meta.put(PROPERTIES_EMAIL, "member" + idx + "@testlalala.com");
	// 		params.put(META, meta);
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("getMemberListInfoTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 		if (idx % 2 == 0) {
	// 			addUserList1.add(res.getString(ACCOUNT_ID));
	// 		} else {
	// 			addUserList2.add(res.getString(ACCOUNT_ID));
	// 		}
	// 	}
	// 	params.clear();
	// 	params.put(ROLE, "member");
	// 	params.put(ADD_LIST, addUserList1);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	//
	// 	params.clear();
	// 	params.put(ROLE, "member");
	// 	params.put(ADD_LIST, addUserList2);
	// 	params.put(GROUP_ID, groupID.get(1));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	//
	// 	// Make member 5 a group account
	// 	params.clear();
	// 	params.put(ROLE, "little-boy");
	// 	params.put(GROUP_ID, userID.get(4));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
	// 	assertNull(res.get(ERROR));
	//
	// 	res = requestJSON(API_GROUP_GRP_ROLES, params);
	// 	// Add some members into member 5
	// 	params.clear();
	// 	params.put(ROLE, "little-boy");
	// 	params.put(ADD_LIST, addUserList2);
	// 	params.put(GROUP_ID, userID.get(4));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	//
	// 	List<List<String>> expectedResult = new ArrayList<List<String>>();
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// 1st Test: Empty Submission
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, null);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	// 2nd Test: Invalid Group ID
	// 	params.clear();
	// 	params.put(GROUP_ID, "smlj group ID");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	// 3rd Test: Valid Group ID unknown headers ( group 1 )
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(HEADERS, "['first', 'second', 'third']");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	expectedResult.clear();
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
	// 	assertEquals(expectedResult, res.get(DATA));
	// 	assertTrue(res.getInt(RECORDS_TOTAL) == 2);
	// 	assertTrue(res.getInt(RECORDS_FILTERED) == 2);
	//
	// 	// 4th Test: Valid Group ID invalid headers ( group 1 )
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(HEADERS, " this is an invalid header");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	expectedResult.clear();
	// 	expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(1), "member")));
	// 	expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(3), "member")));
	// 	assertThat("Something is wrong with the lists", res.getList(DATA),
	// 		containsInAnyOrder(expectedResult.toArray()));
	// 	assertTrue(res.getInt(RECORDS_TOTAL) == 2);
	// 	assertTrue(res.getInt(RECORDS_FILTERED) == 2);
	//
	// 	// 5th Test: Valid Group ID default headers ( group 1 )
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	assertThat("Something is wrong with the lists", res.getList(DATA),
	// 		containsInAnyOrder(expectedResult.toArray()));
	// 	assertTrue(res.getInt(RECORDS_TOTAL) == 2);
	// 	assertTrue(res.getInt(RECORDS_FILTERED) == 2);
	//
	// 	// 6th Test: Valid Group ID custom headers ( group 1 )
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(HEADERS, "['" + PROPERTIES_ROLE
	// 		+ "', 'group__oid', 'account_email', 'randomHeader']");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	expectedResult.clear();
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0),
	// 		"member2@testlalala.com", "")));
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0),
	// 		"member4@testlalala.com", "")));
	// 	assertTrue(res.getInt(RECORDS_TOTAL) == 2);
	// 	assertTrue(res.getInt(RECORDS_FILTERED) == 2);
	// 	assertThat("Something is wrong with the lists", res.getList(DATA),
	// 		containsInAnyOrder(expectedResult.toArray()));
	//
	// 	// 7th Test: No groupID, user who is not a group is logged in
	// 	params.clear();
	// 	params.put(LOGINNAME, "member2");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	//
	// 	params.clear();
	// 	params.put(HEADERS, "['" + PROPERTIES_ROLE
	// 		+ "', 'group__oid', 'account_email', 'randomHeader']");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	//
	// 	// 8th Test: No groupID, user is group and is logged in
	// 	params.clear();
	// 	params.put(LOGINNAME, "member5");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	//
	// 	params.clear();
	// 	params.put(HEADERS, "['" + PROPERTIES_ROLE
	// 		+ "', 'group__oid', 'account_email', 'randomHeader']");
	// 	res = requestJSON(API_GROUP_ADMIN_GET_MEM_LIST_INFO, params);
	// 	expectedResult.clear();
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
	// 		"member1@testlalala.com", "")));
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
	// 		"member3@testlalala.com", "")));
	// 	expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4),
	// 		"member5@testlalala.com", "")));
	// 	assertTrue(res.getInt(RECORDS_TOTAL) == 3);
	// 	assertTrue(res.getInt(RECORDS_FILTERED) == 3);
	// 	assertThat("Something is wrong with the lists", res.getList(DATA),
	// 		containsInAnyOrder(expectedResult.toArray()));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	//
	// }
	//
	// @Test
	// public void singleMemberMeta() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
	// 	List<String> addUserList = new ArrayList<String>();
	// 	List<String> expectedFailResult = new ArrayList<String>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "exampleGrp");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("singleMemberMetaTest: Something wrong in creating group.", res.get(ERROR));
	// 	groupID.add(res.getString(ACCOUNT_ID));
	// 	for (int idx = 1; idx <= 3; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "single" + idx);
	// 		params.put(PASSWORD, "password");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("singleMemberMetaTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 		if (idx % 2 == 1) {
	// 			addUserList.add(res.getString(ACCOUNT_ID));
	// 		}
	// 	}
	// 	params.clear();
	// 	params.put(ROLE, "member");
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	// 	List<List<String>> expectedResult = new ArrayList<List<String>>();
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// 1st Test: Empty Submission
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, null);
	// 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	// 	// 2nd Test: Invalid userID
	// 	params.clear();
	// 	params.put(USER_ID, "randomID");
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	// 	// 3rd Test: Valid accountID, account not in group ( single 2 )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	// 	// 4th Test: Valid accountID, account not in group, invalid groupID, no roles ( single 2 )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	params.put(GROUP_ID, "anyhowGroupID");
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	// 	// 5th Test: Valid accountID, account not in group, Valid group, no roles ( single 2, exampleGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// 	// 6th Test: Valid accountID, account in group, Valid group, no roles ( single 1, exampleGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals("member", res.getStringMap(META).get(PROPERTIES_ROLE));
	// 	// 7th Test: Valid accountID, account in group, Valid group, with roles ( single 1, exampleGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(ROLE, "member");
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals("member", res.getStringMap(META).get(PROPERTIES_ROLE));
	// 	// 8th Test: Valid accountID, account in group, Valid group, with wrong roles ( single 1, exampleGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(ROLE, "wrongRole");
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// 	// 9th Test: No accountID, user logged in, user not group ( single2, exampleGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "single2");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// 	// 10th Test:  No accountID, user logged in, user in group ( single1, exampleGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "single3");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertEquals("member", res.getStringMap(META).get(PROPERTIES_ROLE));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// }
	//
	// @Test
	// public void updateMemberMetaInfo() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
	// 	List<String> addUserList = new ArrayList<String>();
	// 	List<String> expectedFailResult = new ArrayList<String>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "exampleUpdateGrp");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("updateMemberMetaInfoTest: Something wrong in creating group.", res.get(ERROR));
	// 	groupID.add(res.getString(ACCOUNT_ID));
	// 	for (int idx = 1; idx <= 3; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "update" + idx);
	// 		params.put(PASSWORD, "password");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("updateMemberMetaInfoTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 		if (idx % 2 == 1) {
	// 			addUserList.add(res.getString(ACCOUNT_ID));
	// 		}
	// 	}
	// 	params.clear();
	// 	params.put(ROLE, "member");
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals(expectedFailResult, res.get(FAIL_ADD));
	// 	List<List<String>> expectedResult = new ArrayList<List<String>>();
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// 1st Test: Empty Submission
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, null);
	// 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	// 	// 2nd Test: Invalid userID
	// 	params.clear();
	// 	params.put(USER_ID, "randomID");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NO_USER, res.get(ERROR));
	// 	// 3rd Test: Valid accountID, account not in group, no groupID ( update 2 )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NO_GROUP_ID, res.get(ERROR));
	// 	// 4th Test: Valid accountID, account not in group, invalid groupID, no metaObj ( update 2 )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	params.put(GROUP_ID, "anyhowGroupID");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NO_META, res.get(ERROR));
	// 	// 5th Test: Valid userID not in group, invalid groupID, invalid metaObj ( update 2 )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	params.put(GROUP_ID, "anyhowGroupID");
	// 	params.put(META, "anyhowmetaobj");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	// 	// 6th Test: Valid userID not in group, valid groupID, metaObj ( update 2, exampleUpdateGrp )
	// 	// Get a random person's member metaObj
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	res = requestJSON(API_GROUP_GET_SINGLE_MEM_META, params);
	// 	assertNull(res.get(ERROR));
	// 	assertEquals("member", res.getStringMap(META).get(PROPERTIES_ROLE));
	// 	Map<String, Object> metaObj = res.getStringMap(META);
	// 	metaObj.put("aRandomProp", "aRandomValue");
	//
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(1));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, res.getStringMap(META));
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// 	// 7th Test: Valid userID in group, valid groupID, invalid metaObj ( update 1, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, "randomMetaObj");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_INVALID_FORMAT_JSON, res.get(ERROR));
	// 	// 8th Test: Valid userID in group, valid groupID, valid metaObj ( update 1, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(USER_ID, userID.get(0));
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, metaObj);
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertTrue(res.getBoolean(SUCCESS));
	// 	assertEquals("delta", res.get(UPDATE_MODE));
	// 	// 9th Test: No userID, user logged in, not in group, valid groupID, valid metaObj ( update2, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "update2");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, metaObj);
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertEquals(ERROR_NOT_IN_GROUP_OR_ROLE, res.get(ERROR));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// 	// 10th Test: No userID, user logged in, in group, valid groupID, valid metaObj ( update1, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "update1");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, metaObj);
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertTrue(res.getBoolean(SUCCESS));
	// 	assertEquals("delta", res.get(UPDATE_MODE));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// 	// 11th Test: No userID, user logged in, in group, valid groupID, valid metaObj, full ( update3, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "update3");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, metaObj);
	// 	params.put(UPDATE_MODE, "full");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertTrue(res.getBoolean(SUCCESS));
	// 	assertEquals("full", res.get(UPDATE_MODE));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// 	// 12th Test: No userID, user logged in, in group, valid groupID, valid metaObj, random update ( update3, exampleUpdateGrp )
	// 	params.clear();
	// 	params.put(LOGINNAME, "update3");
	// 	params.put(PASSWORD, "password");
	// 	res = requestJSON(API_ACCOUNT_LOGIN, params);
	// 	assertNull(res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(META, metaObj);
	// 	params.put(UPDATE_MODE, "waerawer");
	// 	res = requestJSON(API_GROUP_UPDATE_MEM_META, params);
	// 	assertTrue(res.getBoolean(SUCCESS));
	// 	assertEquals("delta", res.get(UPDATE_MODE));
	// 	res = requestJSON(API_ACCOUNT_LOGOUT, null);
	// 	assertEquals(Boolean.TRUE, res.get(RESULT));
	// }
	
	@Test
	public void passwordReset() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing user
		params.clear();
		params.put(LOGINNAME, "reset");
		params.put(PASSWORD, "password");
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("passwordResetTest: Something wrong in adding user.", res.get(ERROR));
		userID.add(res.getString(ACCOUNT_ID));
		
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_PASS_RESET, ERROR_NO_USER, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Invalid userID
		params.clear();
		params.put(ACCOUNT_ID, "randomID");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid userID, No old password
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_NO_PASSWORD, ERROR);
		// 4th Test: Valid userID, old password, no new password
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(OLD_PASSWORD, "password");
		ts.setAndExecuteGTC(params, ERROR_NO_NEW_PASSWORD, ERROR);
		// 5th Test: Valid userID, old password, new password, no repeatPass
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(OLD_PASSWORD, "password");
		params.put(NEW_PASSWORD, "password");
		ts.setAndExecuteGTC(params, ERROR_NO_NEW_REPEAT_PASSWORD, ERROR);
		// 6th Test: Valid userID, old password, new password, incorrect repeatPass
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(OLD_PASSWORD, "password");
		params.put(NEW_PASSWORD, "passwordnew");
		params.put(REPEAT_PASSWORD, "passwordHAHAHHA");
		ts.setAndExecuteGTC(params, ERROR_PASS_NOT_EQUAL, ERROR);
		// 7th Test: Valid userID, incorrect old password, new password, correct repeatPass
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(OLD_PASSWORD, "wrongOldPasswr");
		params.put(NEW_PASSWORD, "passwordnew");
		params.put(REPEAT_PASSWORD, "passwordnew");
		ts.setAndExecuteGTC(params, ERROR_PASS_INCORRECT, ERROR);
		// 8th Test: Valid userID, correct old password, new password, correct repeatPass
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(OLD_PASSWORD, "password");
		params.put(NEW_PASSWORD, "passwordnew");
		params.put(REPEAT_PASSWORD, "passwordnew");
		ts.setAndExecuteGTC(params, true, SUCCESS);
		// 9th Test: no userID, user not logged in, correct old password, new password, correct repeatPass
		params.clear();
		params.put(OLD_PASSWORD, "password");
		params.put(NEW_PASSWORD, "passwordnewlol");
		params.put(REPEAT_PASSWORD, "passwordnewlol");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 10th Test: no userID, user logged in, correct old password, new password, correct repeatPass
		ts.loginUser("reset", "passwordnew");
		params.clear();
		params.put(OLD_PASSWORD, "passwordnew");
		params.put(NEW_PASSWORD, "passwordnewnew");
		params.put(REPEAT_PASSWORD, "passwordnewnew");
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
	}
	
	@Test
	public void setLoginNameTest() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing user
		params.clear();
		params.put(LOGINNAME, "setTestName");
		params.put(PASSWORD, "password");
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getInfoByNameTest: Something wrong in adding user.", res.get(ERROR));
		userID.add(res.getString(ACCOUNT_ID));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_SET_LOGIN_NAME, ERROR_NO_LOGINNAME, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: No account ID
		params.clear();
		params.put(LOGINNAME, "anotherLoginName");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid accountID, login name in use
		params.clear();
		params.put(LOGINNAME, "setTestName");
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setAndExecuteGTC(params, ERROR_LOGIN_NAME_EXISTS, ERROR);
		// 4th Test: Valid accountID, valid login name
		params.clear();
		params.put(LOGINNAME, "setTestNameAnother");
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setAndExecuteGTC(params, true, RESULT);
		// 5th Test; User logged in, name change
		ts.loginUser("setTestNameAnother", "password");
		params.clear();
		params.put(LOGINNAME, "setTestNameBack");
		ts.setAndExecuteGTC(params, true, RESULT);
		ts.logout();
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
		params.clear();
		params.put(LOGINNAME, "infoName");
		params.put(PASSWORD, "password");
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getInfoByNameTest: Something wrong in adding user.", res.get(ERROR));
		userID.add(res.getString(ACCOUNT_ID));
		
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_INFO, ERROR_NO_USER, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Invalid username
		params.clear();
		params.put(LOGINNAME, "randomName");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid username
		params.clear();
		params.put(LOGINNAME, "infoName");
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
		// 4th Test: No user ID, user logged in
		ts.loginUser("infoName", "password");
		params.clear();
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
	}
	
	@Test
	public void getInfoByID() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing user
		params.clear();
		params.put(LOGINNAME, "infoID");
		params.put(PASSWORD, "password");
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("getInfoByIDTest: Something wrong in adding user.", res.get(ERROR));
		userID.add(res.getString(ACCOUNT_ID));
		
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_INFO, ERROR_NO_USER, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Invalid userID
		params.clear();
		params.put(ACCOUNT_ID, "randomID");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid ID
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
		// 4th Test: No user ID, user logged in
		ts.loginUser("infoID", "password");
		params.clear();
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
		ts.logout();
	}
	
	@Test
	public void updateUserInfo() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>();
		// Ensure that there is an existing user
		params.clear();
		params.put(LOGINNAME, "updateUser");
		params.put(PASSWORD, "password");
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("updateUserInfoTest: Something wrong in adding user.", res.get(ERROR));
		userID.add(res.getString(ACCOUNT_ID));
		
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_INFO, ERROR_NO_USER, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Invalid accountID
		params.clear();
		params.put(ACCOUNT_ID, "randomID");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid ID
		Map<String, Object> data = new HashMap<>();
		data.put("wassup", "hi");
		data.put("email", "jaja@lol.com");
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		params.put(DATA, data);
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
		res = GenericConvert.toGenericConvertStringMap(ConvertJSON.fromObject(ts.getRes()));
		res = GenericConvert.toGenericConvertStringMap(res.getStringMap(RESULT));
		System.out.println(ConvertJSON.fromObject(res));
		assertTrue(res.getString("email").equalsIgnoreCase("jaja@lol.com"));
		// 4th Test: No user ID, user logged in
		ts.loginUser("updateUser", "password");
		params.clear();
		ts.setAndExecuteGTC(params, userID.get(0), ACCOUNT_ID);
		ts.logout();
	}
	
	//
	// // builder.put(path+API_GROUP_GET_LIST_GRP_ID_MEM, getListOfGroupIDOfMember);
	// @Test
	// public void getListOfGroupIDOfMember() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
	// 	List<String> expectedResult = new ArrayList<String>();
	// 	// Ensure that there is an existing group
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.put(LOGINNAME, "groupNumber" + idx);
	// 		params.put(IS_GROUP, true);
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("getListOfGroupIDOfMemberTest: Something wrong in creating group " + idx + ".",
	// 			res.get(ERROR));
	// 		groupID.add(res.getString(ACCOUNT_ID));
	// 		expectedResult.add(res.getString(ACCOUNT_ID));
	// 	}
	// 	// Ensure that there is an existing user
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "memberList" + idx);
	// 		params.put(PASSWORD, "password");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 		if (idx % 2 == 1) {
	// 			addUserList.add(res.getString(ACCOUNT_ID));
	// 		}
	// 	}
	// 	// Ensure that user is in both group
	// 	params.clear();
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(ROLE, "member");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user to group",
	// 		res.get(ERROR));
	//
	// 	params.put(GROUP_ID, groupID.get(1));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull("getListOfGroupIDOfMemberTest: Something wrong in adding user to group",
	// 		res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// 1st Test: Empty Submission
	// 	TestSet ts = new TestSet(null, API_GROUP_GET_LIST_GRP_ID_MEM, ERROR_NO_USER, ERROR);
	// 	ts.executeGenericTestCase();
	// 	// 2nd Test: Invalid userID
	// 	params.clear();
	// 	params.put(USER_ID, "randomID");
	// 	ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
	// 	// 3rd Test: Valid User with group ( memberList1 )
	// 	params.put(USER_ID, userID.get(0));
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// 	// 4th Test: No userID, user is logged
	// 	ts.loginUser("memberList1", "password");
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// 	ts.logout();
	// 	// 5th Test: Valid user with no group ( memberList2 )
	// 	params.put(USER_ID, userID.get(1));
	// 	expectedResult.clear();
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// }
	//
	// @Test
	// public void getListOfGroupObjectOfMember() {
	// 	GenericConvertMap<String, Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	Map<String, Object> params = new HashMap<String, Object>();
	// 	List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
	// 	List<Object> expectedResult = new ArrayList<Object>();
	// 	// Ensure that there is an existing group
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.put(LOGINNAME, "grpOb" + idx);
	// 		params.put(IS_GROUP, true);
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull("getListOfGroupObjectOfMemberTest: Something wrong in creating group " + idx
	// 			+ ".", res.get(ERROR));
	// 		groupID.add(res.getString(ACCOUNT_ID));
	// 		params.clear();
	// 		params.put(USER_ID, res.get(ACCOUNT_ID));
	// 		res = requestJSON(API_ACCOUNT_INFO_ID, params);
	// 		assertNull("getListOfGroupObjectOfMemberTest: Something wrong in adding group info " + idx
	// 			+ ".", res.get(ERROR));
	// 		expectedResult.add(res);
	// 	}
	// 	// Ensure that there is an existing user
	// 	for (int idx = 1; idx <= 2; idx++) {
	// 		params.clear();
	// 		params.put(LOGINNAME, "memLi" + idx);
	// 		params.put(PASSWORD, "password");
	// 		res = requestJSON(API_ACCOUNT_NEW, params);
	// 		assertNull(
	// 			"getListOfGroupObjectOfMemberTest: Something wrong in adding user " + idx + ".",
	// 			res.get(ERROR));
	// 		userID.add(res.getString(ACCOUNT_ID));
	// 		if (idx % 2 == 1) {
	// 			addUserList.add(res.getString(ACCOUNT_ID));
	// 		}
	// 	}
	// 	// Ensure that user is in both group
	// 	params.clear();
	// 	params.put(ADD_LIST, addUserList);
	// 	params.put(GROUP_ID, groupID.get(0));
	// 	params.put(ROLE, "member");
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(ERROR));
	//
	// 	params.put(GROUP_ID, groupID.get(1));
	// 	res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// 1st Test: Empty Submission
	// 	TestSet ts = new TestSet(null, API_GROUP_GET_LIST_GRP_OBJ_MEM, ERROR_NO_USER, ERROR);
	// 	ts.executeGenericTestCase();
	// 	// 2nd Test: Invalid userID
	// 	params.clear();
	// 	params.put(USER_ID, "randomID");
	// 	ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
	// 	// 3rd Test: Valid User with group ( memLi1 )
	// 	params.put(USER_ID, userID.get(0));
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// 	// 4th Test: No userID, user is logged
	// 	ts.loginUser("memLi1", "password");
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// 	ts.logout();
	// 	// 5th Test: Valid user with no group ( memLi2 )
	// 	params.put(USER_ID, userID.get(1));
	// 	expectedResult.clear();
	// 	ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
	// }
	//
	@Test
	public void removeAccount() {
		GenericConvertMap<String, Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<Object> expectedResult = new ArrayList<Object>();
		// Ensure that there is an existing group
		params.put(LOGINNAME, "grpRemove");
		params.put(IS_GROUP, true);
		res = requestJSON(API_ACCOUNT_NEW, params);
		assertNull("removeAccountTest: Something wrong in creating group.", res.get(ERROR));
		groupID.add(res.getString(ACCOUNT_ID));
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 2; idx++) {
			params.clear();
			params.put(LOGINNAME, "remove" + idx);
			params.put(PASSWORD, "password");
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("removeAccountTest: Something wrong in adding user " + idx + ".",
				res.get(ERROR));
			userID.add(res.getString(ACCOUNT_ID));
			if (idx % 2 == 1) {
				addUserList.add(res.getString(ACCOUNT_ID));
			}
		}
		// // Ensure that user is in group
		// params.clear();
		// params.put(ADD_LIST, addUserList);
		// params.put(GROUP_ID, groupID.get(0));
		// params.put(ROLE, "member");
		// res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		// assertNull("removeAccountTest: Something wrong in adding user to group", res.get(ERROR));
		// // Ensure that there is a member of a member of a group
		// params.clear();
		// params.put(GROUP_ID, userID.get(0));
		// params.put(ROLE, "rowABoat");
		// res = requestJSON(API_GROUP_ADMIN_ADD_MEM_ROLE, params);
		// assertNull("removeAccountTest: Something wrong in adding user to group", res.get(ERROR));
		// params.clear();
		// addUserList.clear();
		// addUserList.add(userID.get(1));
		// params.put(ADD_LIST, addUserList);
		// params.put(GROUP_ID, userID.get(0));
		// params.put(ROLE, "rowABoat");
		// res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		// assertNull("removeAccountTest: Something wrong in adding user to group", res.get(ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_ADMIN_REMOVE, ERROR_NO_USER, ERROR);
		ts.executeGenericTestCase();
		// 2nd Test: Invalid userID
		params.clear();
		params.put(ACCOUNT_ID, "randomID");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// 3rd Test: Valid User ID
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setAndExecuteGTC(params, true, RESULT);
		// Affirmation of Result - No account Found
		params.clear();
		params.put(ACCOUNT_ID, userID.get(0));
		ts.setURL("account/info/get");
		ts.setAndExecuteGTC(params, ERROR_NO_USER, ERROR);
		// // Affirmation of Result - No member Found in valid group
		// params.clear();
		// params.put(GROUP_ID, groupID.get(0));
		// expectedResult.clear();
		// ts.setURL(API_GROUP_ADMIN_GET_MEM_LIST_INFO);
		// ts.setAndExecuteLTC(params, expectedResult, DATA, "The list has some issues");
		// // Affirmation of Result - No group found in previous member
		// params.clear();
		// params.put(ACCOUNT_ID, userID.get(1));
		// ts.setURL(API_GROUP_GET_LIST_GRP_ID_MEM);
		// ts.setAndExecuteLTC(params, expectedResult, LIST, "The list has some issues");
		// 4th Test: No userID, user is logged in
		ts.loginUser("remove2", "password");
		ts.setURL(API_ACCOUNT_ADMIN_REMOVE);
		ts.setAndExecuteGTC(null, true, RESULT);
		// Affirmation of Result - Log in to deleted user
		ts.setURL(API_ACCOUNT_LOGIN);
		params.put(LOGINNAME, "remove2");
		params.put(PASSWORD, "password");
		ts.setAndExecuteGTC(params, ERROR_FAIL_LOGIN, ERROR);
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
		// Ensure that there is an existing group
		for (int idx = 1; idx <= 2; idx++) {
			params.put(LOGINNAME, "getTestGrp" + idx);
			params.put(IS_GROUP, true);
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getUserOrGroupListTest: Something wrong in creating group " + idx + ".",
				res.get(ERROR));
			groupID.add(res.getString(ACCOUNT_ID));
			// System.out.println(groupID.get(idx-1)+" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
		// Ensure that there is an existing user
		for (int idx = 1; idx <= 2; idx++) {
			params.clear();
			params.put(LOGINNAME, "getTestMemb" + idx);
			params.put(PASSWORD, "password");
			res = requestJSON(API_ACCOUNT_NEW, params);
			assertNull("getUserOrGroupListTest: Something wrong in adding user " + idx + ".",
				res.get(ERROR));
			userID.add(res.getString(ACCOUNT_ID));
			if (idx % 2 == 1) {
				addUserList.add(res.getString(ACCOUNT_ID));
			}
			// System.out.println(userID.get(idx-1)+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			expectedResult.add(userID.get(idx - 1));
		}
		// Ensure that user is in both group
		params.clear();
		params.put(ADD_LIST, addUserList);
		params.put(GROUP_ID, groupID.get(0));
		params.put(ROLE, "member");
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getUserOrGroupListTest: Something wrong in adding user to group", res.get(ERROR));
		
		params.put(GROUP_ID, groupID.get(1));
		res = requestJSON(API_GROUP_ADMIN_ADD_REM_MEM, params);
		assertNull("getUserOrGroupListTest: Something wrong in adding user to group", res.get(ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Empty Submission
		TestSet ts = new TestSet(null, API_ACCOUNT_LIST, expectedResult, DATA);
		ts.executeTrueTestCase();
		// 2nd Test: Invalid userID
		// params.clear();
		// ts.setAndExecuteLTC(params, expectedResult, DATA, "The list has some problems");
	}
	
	class TestSet {
		private Map<String, Object> params = null;
		private String url = "";
		private Object expectedResult = "";
		private String resultToGetFrom = "";
		GenericConvertMap<String, Object> res = null;
		
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
			List<Object> result = ConvertJSON.toList(ConvertJSON.fromObject(res.get(DATA)));
			assertTrue(result.size() > 0);
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
			params.put(LOGINNAME, name);
			params.put(PASSWORD, pass);
			assertNull(requestJSON(API_ACCOUNT_LOGIN, params).get(ERROR));
		}
		
		public void logout() {
			assertEquals(Boolean.TRUE, requestJSON(API_ACCOUNT_LOGOUT, null).get(RESULT));
		}
	}
	//
	// @Test
	// public void getListOfMemberObjectOfGroup(){
	// 	GenericConvertMap<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "groupObjToRetrieve");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(LOGINNAME, "groupObjWithNoMember");
	// 	params.put(IS_GROUP, true);
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(LOGINNAME, "user test 1");
	// 	params.put(PASSWORD, "thisismypassword");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(ERROR));
	//
	// 	params.put(LOGINNAME, "user test 2");
	// 	params.put(PASSWORD, "thisismypassword");
	// 	res = requestJSON(API_ACCOUNT_NEW, params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(ERROR));
	//
	// 	// Ensure that group has both member
	// 	params.clear();
	// 	params.put(LOGINNAME, "user test 1");
	// 	params.put(GROUPNAME, "groupObjToRetrieve");
	// 	params.put(ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(ERROR));
	//
	// 	params.put(LOGINNAME, "user test 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	res = requestJSON("getListOfMemberIDInGroup", null);
	// 	assertEquals(ERROR_NO_GROUPNAME, res.get(ERROR));
	// 	params.clear();
	// 	params.put(GROUPNAME, "anyhowgroup");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertEquals(ERROR_NO_GROUP, res.get(ERROR));
	//
	// 	params.put(GROUPNAME, "groupObjToRetrieve");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(LIST).size() == 2);
	//
	// 	params.put(GROUPNAME, "groupObjWithNoMember");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(LIST).size() == 0);
	// }
}
