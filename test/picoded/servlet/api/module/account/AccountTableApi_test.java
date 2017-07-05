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
import picoded.conv.*;
import picoded.set.*;
import picoded.dstack.*;
import picoded.dstack.module.account.*;
import picoded.struct.GenericConvertMap;

import picoded.servlet.api.module.account.Account_Strings;

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
			// 	System.out.println("this was not ran");
			// 	return (long) 2;
			// };
			if( !table.hasLoginID("laughing-man") ) {
				AccountObject ao = table.newObject("laughing-man");
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
	public void isLoginFailure() {
		assertNull( requestJSON("isLogin", null).get("INFO") );
		assertNull( requestJSON("isLogin", null).get(Account_Strings.RES_ERROR) );
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Account_Strings.RES_RETURN) );
	}

	@Test
	public void loginProcessFlow() {
		// reuse result map
		Map<String,Object> res = null;

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Account_Strings.RES_RETURN) );

		// Does a failed login
		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put(Account_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Account_Strings.REQ_PASSWORD, "Is the enemy");
		res = requestJSON("login", loginParams);

		assertEquals( Boolean.FALSE, res.get("isLogin") );
		assertEquals( Account_Strings.ERROR_FAIL_LOGIN, res.get(Account_Strings.RES_ERROR) );

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Account_Strings.RES_RETURN) );

		// Does the actual login
		loginParams.put(Account_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Account_Strings.REQ_PASSWORD, "The Catcher in the Rye");

		// Request and check result
		res = requestJSON("login", loginParams);
		assertNull( res.get(Account_Strings.RES_ERROR) );
		assertNull( res.get("INFO") );
		assertEquals( Boolean.TRUE, res.get("isLogin") );
		// Validate that login is valid
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );

		// Log out current user
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );

		// Validate that logout is valid
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.FALSE, res.get(Account_Strings.RES_RETURN) );
	}

	@Test
	public void loginLockingIncrement() {
		// reuse result map
		Map<String,Object> res = null;
		// Checks that the test begins with the user not logged in
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.FALSE, res.get(Account_Strings.RES_RETURN) );

		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put(Account_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Account_Strings.REQ_PASSWORD, "Is the enemy");
		int doTestLoop = 3, currentLoop = 0;
		while(currentLoop < doTestLoop){
			res = requestJSON("login", loginParams);
			// System.out.println(res.get(Account_Strings.RES_ERROR)+" <<<<<<<<<<");
			Map<String, Object> params = new HashMap<String,Object>();
			params.put(Account_Strings.REQ_ACCOUNT_NAME, "laughing-man");
			int waitTime = (int) requestJSON("lockTime", params).get("lockTime");
			if(currentLoop%2==0){
				assertEquals( Boolean.FALSE, res.get(Account_Strings.RES_IS_LOGIN) );
				assertEquals("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n"+
											"Likely failure is due to insufficient Thread.sleep()\n",
											Account_Strings.ERROR_FAIL_LOGIN, res.get(Account_Strings.RES_ERROR) );
			}else{
				assertThat("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n",
				 						res.get(Account_Strings.RES_ERROR).toString(), containsString("user locked out") );
				try{
					Thread.sleep(waitTime*1000);
				}catch(InterruptedException ie){}
			}
			currentLoop++;
		}
	}

	@Test
	public void createNewUserAccount() {
		Map<String,Object> res = null;
		Map<String,Object> createDetails = new HashMap<String,Object>();

		res = requestJSON("new", createDetails);
		assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));

		createDetails.put(Account_Strings.REQ_USERNAME, "little-boy");
		res = requestJSON("new", createDetails);
		assertEquals(Account_Strings.ERROR_NO_PASSWORD, res.get(Account_Strings.RES_ERROR));
		// Successfully created account
		createDetails.put(Account_Strings.REQ_PASSWORD, "sooo smallll");
		res = requestJSON("new", createDetails);
		assertNotNull( res.get(Account_Strings.RES_META));
		assertNull(res.get(Account_Strings.RES_ERROR));

		String accountID = res.get(Account_Strings.RES_ACCOUNT_ID).toString();

		//Create same user again
		res = requestJSON("new", createDetails);
		assertEquals("Object already exists in account Table", res.get(Account_Strings.RES_ERROR));
		assertEquals(accountID, res.get(Account_Strings.RES_ACCOUNT_ID));
	}

	@Test
	public void createNewGroup() {
		GenericConvertMap<String,Object> res = null;
		Map<String,Object> createDetails = new HashMap<String,Object>();
		createDetails.put(Account_Strings.REQ_USERNAME, "boy band");
		createDetails.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", createDetails);
		assertNull(res.get(Account_Strings.RES_ERROR));

		// Checks if it is a group

		// Creates the same group
		res = requestJSON("new", createDetails);
		assertEquals("Object already exists in account Table", res.get(Account_Strings.RES_ERROR));

		// Not using the correct value to create group
		createDetails.put(Account_Strings.REQ_USERNAME, "girl band");
		createDetails.put(Account_Strings.REQ_IS_GROUP, "random Words");
		res = requestJSON("new", createDetails);
		assertNotNull(res.get(Account_Strings.RES_ERROR));

		createDetails.put(Account_Strings.REQ_IS_GROUP, "1");
		res = requestJSON("new", createDetails);
		assertNull(res.get(Account_Strings.RES_ERROR));
		String accountID = res.getString(Account_Strings.RES_ACCOUNT_ID);

		createDetails.clear();
		createDetails.put(Account_Strings.REQ_GROUP_ID, accountID);
		res = requestJSON("groupRoles", createDetails);
		ArrayList<String> expectedRoles = new ArrayList<String>();
		expectedRoles.add("member");
		expectedRoles.add("admin");
		assertEquals(expectedRoles, res.get(Account_Strings.RES_LIST));

		createDetails.clear();
		createDetails.put(Account_Strings.REQ_USERNAME, "bbbbb");
		createDetails.put(Account_Strings.REQ_IS_GROUP, "1");
		expectedRoles.clear();
		expectedRoles.add("grandma");
		expectedRoles.add("grandpa");
		createDetails.put(Account_Strings.REQ_DEFAULT_ROLES, false);
		createDetails.put(Account_Strings.REQ_ROLE, expectedRoles);
		res = requestJSON("new", createDetails);
		assertNull(res.get(Account_Strings.RES_ERROR));
		accountID = res.getString(Account_Strings.RES_ACCOUNT_ID);

		createDetails.clear();
		createDetails.put(Account_Strings.REQ_GROUP_ID, accountID);
		res = requestJSON("groupRoles", createDetails);
		assertEquals(expectedRoles, res.get(Account_Strings.RES_LIST));
	}

	@Test
	public void groupRoles(){
		// reuse result map
		GenericConvertMap<String,Object> res = null;
		res = requestJSON("groupRoles", null);
		assertEquals(Account_Strings.ERROR_NO_GROUP_ID, res.get(Account_Strings.RES_ERROR));
		Map<String,Object> params = new HashMap<String,Object>();

		params.put(Account_Strings.REQ_GROUP_ID, "randomID HAHAHAHA");
		res = requestJSON("groupRoles", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Add in a group specifically for this test
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "Macho");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, res.getString(Account_Strings.RES_ACCOUNT_ID));
		res = requestJSON("groupRoles", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		ArrayList<String> expectedRoles = new ArrayList<String>();
		expectedRoles.add("member");
		expectedRoles.add("admin");
		assertEquals(expectedRoles, res.get(Account_Strings.RES_LIST));
	}

	@Test
	public void addAndRemoveMemberToGroupTest() {
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		List<String> userID = new ArrayList<String>();
		List<String> groupID = new ArrayList<String>();
		for( int idx = 1; idx <= 3; idx ++ ) {
			params.put(Account_Strings.REQ_USERNAME, "member " + idx);
			params.put(Account_Strings.REQ_PASSWORD, "password");
			res = requestJSON("new", params);
			assertNull("addAndRemoveMemberToGroupTest: Something wrong in adding user " + idx + ".", res.get(Account_Strings.RES_ERROR));
			userID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
		}
		// Ensure that there is an existing group
		for( int idx = 0; idx <= 3; idx ++ ) {
			params.put(Account_Strings.REQ_USERNAME, "group "+idx);
			params.put(Account_Strings.REQ_IS_GROUP, true);
			res = requestJSON("new", params);
			assertNull("addAndRemoveMemberToGroupTest: Something wrong in creating group " + idx + ".", res.get(Account_Strings.RES_ERROR));
			groupID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
		}
		// Adding roles to non group user account (member 3)
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(2));
		params.put(Account_Strings.REQ_ROLE, "knight");
		res = requestJSON("addMembershipRole", params);
		assertNull("addAndRemoveMemberToGroupTest: Something wrong in adding role to group.", res.get(Account_Strings.RES_ERROR));

		List<String> removeUserList = new ArrayList<String>(), addUserList = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>(), expectedPassResult = new ArrayList<String>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------
		// 1st Test: Submit nothing at all
		res = requestJSON("add_remove_member", null);
		assertEquals(Account_Strings.ERROR_NO_GROUP_ID, res.get(Account_Strings.RES_ERROR));

		// 2nd Test: Non existence group
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, "randomID HAHHAHAHA");
		res = requestJSON("add_remove_member", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// 3rd Test: Remove non existence members & existence members not in group from Existence group (group 1)
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		removeUserList.add("This is random ID");
		removeUserList.add(userID.get(0));
		params.put(Account_Strings.REQ_REMOVE_LIST, removeUserList);
		res = requestJSON("add_remove_member", params);
		expectedFailResult.add("ID: This is random ID, Error: " + Account_Strings.ERROR_NO_USER);
		expectedFailResult.add("ID: " + userID.get(0) + ", Error: User is not in group.");
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_REMOVE));

		// 4th Test: Add non existence members without role into existence group (group 1)
		params.clear();
		addUserList.add("This is another random ID");
		addUserList.add("One for the road");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("add_remove_member", params);
		assertEquals(Account_Strings.ERROR_NO_ROLE, res.get(Account_Strings.RES_ERROR));

		// 5th Test: Add non existence members and existence member with non existence role into group (group 2)
		expectedFailResult.clear();
		expectedFailResult.add("ID: This is another random ID, Error: " + Account_Strings.ERROR_NO_USER);
		expectedFailResult.add("ID: One for the road, Error: " + Account_Strings.ERROR_NO_USER);
		expectedFailResult.add("ID: " + userID.get(1) +", Error: "+ "User is already in group or role is not found.");
		addUserList.add(userID.get(1));
		params.put(Account_Strings.REQ_ROLE, "this is random role");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		res = requestJSON("add_remove_member", params);
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));

		// 6th Test: Add existence members and repeated member with existence role into group (group 2)
		expectedFailResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		addUserList.add(userID.get(1));
		addUserList.add(userID.get(1));
		params.put(Account_Strings.REQ_ROLE, "member");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(1));
		expectedFailResult.add("ID: " + userID.get(1) +", Error: "+ "User is already in group or role is not found.");
		expectedPassResult.add(userID.get(0));
		expectedPassResult.add(userID.get(1));
		res = requestJSON("add_remove_member", params);
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));
		assertEquals(expectedPassResult, res.get(Account_Strings.RES_SUCCESS_ADD));

		// 7th Test: Remove existence members and repeated members from existence group (group 2)
		expectedFailResult.clear();
		expectedPassResult.clear();
		params.clear();
		removeUserList.clear();
		removeUserList.add(userID.get(0));
		removeUserList.add(userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(1));
		params.put(Account_Strings.REQ_REMOVE_LIST, removeUserList);
		expectedFailResult.add("ID: " + userID.get(0) + ", Error: User is not in group.");
		expectedPassResult.add(userID.get(0));
		res = requestJSON("add_remove_member", params);
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_REMOVE));
		assertEquals(expectedPassResult, res.get(Account_Strings.RES_SUCCESS_REMOVE));

		// 8th Test: Remove valid users from non group account (member 2)
		expectedFailResult.clear();
		params.clear();
		removeUserList.clear();
		removeUserList.add(userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(1));
		params.put(Account_Strings.REQ_REMOVE_LIST, removeUserList);
		expectedFailResult.add("ID: " + userID.get(1) + ", Error: This is not a group.");
		res = requestJSON("add_remove_member", params);
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_REMOVE));

		// 9th Test: Adding valid users to user account that do not have roles a.k.a not a group yet (member 2)
		expectedFailResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(1));
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		params.put(Account_Strings.REQ_ROLE, "member");
		expectedFailResult.add("ID: " + userID.get(0) +", Error: User is already in group or role is not found.");
		res = requestJSON("add_remove_member", params);
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));

		// 10th Test: Adding valid users to user account that is a group (member 3)
		expectedPassResult.clear();
		params.clear();
		addUserList.clear();
		addUserList.add(userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(2));
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		params.put(Account_Strings.REQ_ROLE, "knight");
		res = requestJSON("add_remove_member", params);
		expectedPassResult.add(userID.get(0));
		assertEquals(expectedPassResult, res.get(Account_Strings.RES_SUCCESS_ADD));

		// 11th Test: Add the same user to the same group (member 3)
		expectedFailResult.clear();
		res = requestJSON("add_remove_member", params);
		expectedFailResult.add("ID: " + userID.get(0) +", Error: User is already in group or role is not found.");
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));
	}
	//
	// @Test
	// public void getMemberMeta(){
	// 	Map<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	params.put(Account_Strings.REQ_USERNAME, "membermeta");
	// 	params.put(Account_Strings.REQ_PASSWORD, "password");
	// 	res = requestJSON("new", params);
	// 	assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "memberMetaGroup");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("MemberMetaTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that the user is added to the group
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "membermeta");
	// 	params.put(Account_Strings.REQ_GROUPNAME, "memberMetaGroup");
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("MemberMetaTest: Something wrong in adding member to group.", res.get(Account_Strings.RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	// Invalid user, group and role
	// 	res = requestJSON("getMemberMeta", null);
	// 	assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "wrong member");
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
	// 	params.put(Account_Strings.REQ_ROLE, "unknown role");
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));
	//
	// 	// Valid group
	// 	params.put(Account_Strings.REQ_GROUPNAME, "memberMetaGroup");
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));
	//
	// 	// Valid user
	// 	params.put(Account_Strings.REQ_USERNAME, "membermeta");
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertEquals("User is not in group or not in specified role.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Valid role
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertNull(res.get(Account_Strings.RES_ERROR));
	// 	assertNotNull(res.get(Account_Strings.RES_META));
	//
	// 	// No role at all
	// 	params.remove(Account_Strings.REQ_ROLE);
	// 	res = requestJSON("getMemberMeta", params);
	// 	assertNull(res.get(Account_Strings.RES_ERROR));
	// 	assertNotNull(res.get(Account_Strings.RES_META));
	// }
	//
	//
	@Test
	public void getMemberRole(){
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(Account_Strings.REQ_USERNAME, "memberrole");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("getMemberRole: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		String userID = res.getString(Account_Strings.RES_ACCOUNT_ID);

		params.put(Account_Strings.REQ_USERNAME, "memberNotInGroup");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("getMemberRole: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		String wrongUserID = res.getString(Account_Strings.RES_ACCOUNT_ID);
		// Ensure that there is an existing group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "memberRoleGroup");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("getMemberRole: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		String groupID = res.getString(Account_Strings.RES_ACCOUNT_ID);
		// Ensure that the user is added to the group
		params.clear();
		String [] userIDList = new String[]{userID};
		params.put(Account_Strings.REQ_ADD_LIST, userIDList);
		params.put(Account_Strings.REQ_GROUP_ID, groupID);
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("add_remove_member", params);
		assertNull("getMemberRole: Something wrong in adding member to group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// 1st Test: Empty submission
		res = requestJSON("getMemberRole", null);
		assertEquals(Account_Strings.ERROR_NO_GROUP_ID, res.get(Account_Strings.RES_ERROR));

		// 2nd Test: Invalid groupID
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, "wrong group ID");
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_USER_ID, res.get(Account_Strings.RES_ERROR));

		// 3rd Test: Invalid member ID
		params.put(Account_Strings.REQ_USER_ID, "wrong member ID");
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// 4th Test: Valid groupID
		params.put(Account_Strings.REQ_GROUP_ID, groupID);
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));

		// 5th Test: Existence wrong userID
		params.put(Account_Strings.REQ_USER_ID, wrongUserID);
		res = requestJSON("getMemberRole", params);
		assertEquals("No role for user is found.", res.get(Account_Strings.RES_ERROR));

		// 6th Test: Valid UserID
		params.put(Account_Strings.REQ_USER_ID, userID);
		res = requestJSON("getMemberRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_SINGLE_RETURN_VALUE));
	}

	@Test
	public void addNewMembershipRole() {
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String,Object> params = new HashMap<String,Object>();
		// Ensure that there is an existing group
		params.put(Account_Strings.REQ_USERNAME, "addNewMembershipRole");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		params.put(Account_Strings.REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		params.put(Account_Strings.REQ_ROLE, initialRole);
		res = requestJSON("new", params);
		String accountID = res.getString(Account_Strings.REQ_ACCOUNT_ID);
		assertNull("AddMemberShipTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		params.clear();
		res = requestJSON("addMembershipRole", null);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUP_ID, "wrong group ID");
		res = requestJSON("addMembershipRole", params);
		assertEquals(Account_Strings.ERROR_NO_ROLE, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_ROLE, "roleToAdd");
		res = requestJSON("addMembershipRole", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUP_ID, accountID);
		res = requestJSON("addMembershipRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		initialRole.add("roleToAdd");
		assertEquals(initialRole, res.getStringMap(Account_Strings.RES_META).get(Account_Strings.PROPERTIES_MEMBERSHIP_ROLE));
	}

	@Test
	public void removeMembershipRoleFromGroup(){
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		Map<String,Object> params = new HashMap<String,Object>();
		// Ensure that there is an existing group
		params.put(Account_Strings.REQ_USERNAME, "removeMembershipRole");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		params.put(Account_Strings.REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		params.put(Account_Strings.REQ_ROLE, initialRole);
		res = requestJSON("new", params);
		assertNull("removeMembershipTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		params.clear();
		res = requestJSON("removeMembershipRole", null);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
		res = requestJSON("removeMembershipRole", params);
		assertEquals(Account_Strings.ERROR_NO_ROLE, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_ROLE, "roleToRemove");
		res = requestJSON("removeMembershipRole", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "removeMembershipRole");
		res = requestJSON("removeMembershipRole", params);
		assertEquals("No such role is found.", res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_ROLE, "dragon");
		initialRole.remove("dragon");
		res = requestJSON("removeMembershipRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(initialRole, res.getStringMap(Account_Strings.RES_META).get("membershipRoles"));
	}

	@Test
	public void multiLevelGroupOwnership(){
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		// Ensure that there is an existing group
		params.put(Account_Strings.REQ_USERNAME, "group1");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		params.put(Account_Strings.REQ_DEFAULT_ROLES, false);
		ArrayList<String> initialRole = new ArrayList<String>();
		initialRole.add("dragon");
		initialRole.add("tiger");
		initialRole.add("lion");
		params.put(Account_Strings.REQ_ROLE, initialRole);
		res = requestJSON("new", params);
		String groupID = res.getString(Account_Strings.RES_ACCOUNT_ID);
		assertNull("MultiLevelGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));

		// Ensure that there is an existing users
		List<String> userID = new ArrayList<String>();
		for ( int idx = 1; idx <= 2; idx++ ) {
			params.clear();
			params.put(Account_Strings.REQ_USERNAME, "user"+idx);
			params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
			res = requestJSON("new", params);
			assertNull("MultiLevelGroupTest: Something wrong in adding user" + idx + ".", res.get(Account_Strings.RES_ERROR));
			userID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
		}
		List<String> expectedResult = new ArrayList<String>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// Adding the user1 to group1
		params.clear();
		String [] userIDList = new String[]{userID.get(0)};
		params.put(Account_Strings.REQ_ADD_LIST, userIDList);
		params.put(Account_Strings.REQ_GROUP_ID, groupID);
		params.put(Account_Strings.REQ_ROLE, "dragon");
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedResult, res.get(Account_Strings.RES_FAIL_ADD));
		// Reaffirm the result
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID);
		params.put(Account_Strings.REQ_USER_ID, userID.get(0));
		res = requestJSON("getMemberRole", params);
		assertEquals("dragon", res.get(Account_Strings.RES_SINGLE_RETURN_VALUE));

		// Adding membershipRoles to user1
		params.clear();
		params.put(Account_Strings.REQ_ROLE, "knight");
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(0));
		res = requestJSON("addMembershipRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		// Adding user2 to user1
		params.clear();
		userIDList = new String[]{userID.get(1)};
		params.put(Account_Strings.REQ_ADD_LIST, userIDList);
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(0));
		params.put(Account_Strings.REQ_ROLE, "knight");
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedResult, res.get(Account_Strings.RES_FAIL_ADD));

		// Reaffirm the result
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(0));
		params.put(Account_Strings.REQ_USER_ID, userID.get(1));
		res = requestJSON("getMemberRole", params);
		assertEquals("knight", res.get(Account_Strings.RES_SINGLE_RETURN_VALUE));
	}

	@Test
	public void getMemberListInfo(){
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
		List<String> addUserList1 = new ArrayList<String>(), addUserList2 = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>();
		// Ensure that there is an existing group
		for( int idx = 1; idx <= 2; idx ++ ) {
			params.put(Account_Strings.REQ_USERNAME, "exampleGrp"+idx);
			params.put(Account_Strings.REQ_IS_GROUP, true);
			res = requestJSON("new", params);
			assertNull("getMemberListInfoTest: Something wrong in creating group " + idx + ".", res.get(Account_Strings.RES_ERROR));
			groupID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
		}
		for( int idx = 1; idx <= 5; idx ++ ) {
			params.clear();
			params.put(Account_Strings.REQ_USERNAME, "member" + idx);
			params.put(Account_Strings.REQ_PASSWORD, "password");
			res = requestJSON("new", params);
			assertNull("getMemberListInfoTest: Something wrong in adding user " + idx + ".", res.get(Account_Strings.RES_ERROR));
			userID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
			if ( idx % 2 == 0 ) {
				addUserList1.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
			} else {
				addUserList2.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
			}
		}
		params.clear();
		params.put(Account_Strings.REQ_ROLE, "member");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList1);
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));

		params.clear();
		params.put(Account_Strings.REQ_ROLE, "member");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList2);
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(1));
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));

		// Make member 5 a group account
		params.clear();
		params.put(Account_Strings.REQ_ROLE, "little-boy");
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(4));
		res = requestJSON("addMembershipRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		res = requestJSON("groupRoles", params);
		// Add some members into member 5
		params.clear();
		params.put(Account_Strings.REQ_ROLE, "little-boy");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList2);
		params.put(Account_Strings.REQ_GROUP_ID, userID.get(4));
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));

		List<List<String>> expectedResult = new ArrayList<List<String>>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// 1st Test: Empty Submission
		res = requestJSON("get_member_list_info", null);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// 2nd Test: Invalid Group ID
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, "smlj group ID");
		res = requestJSON("get_member_list_info", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// 3rd Test: Valid Group ID unknown headers ( group 1 )
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		params.put(Account_Strings.REQ_HEADERS, "['first', 'second', 'third']");
		res = requestJSON("get_member_list_info", params);
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("", "", "")));
		assertEquals(expectedResult, res.get(Account_Strings.RES_DATA));
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_TOTAL) == 2);
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_FILTERED) == 2);

		// 4th Test: Valid Group ID invalid headers ( group 1 )
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		params.put(Account_Strings.REQ_HEADERS, " this is an invalid header");
		res = requestJSON("get_member_list_info", params);
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(1), "member")));
		expectedResult.add(new ArrayList<>(Arrays.asList(userID.get(3), "member")));
		assertThat("Something is wrong with the lists", res.getList(Account_Strings.RES_DATA), containsInAnyOrder(expectedResult.toArray()));
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_TOTAL) == 2);
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_FILTERED) == 2);

		// 5th Test: Valid Group ID default headers ( group 1 )
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("get_member_list_info", params);
		assertThat("Something is wrong with the lists", res.getList(Account_Strings.RES_DATA), containsInAnyOrder(expectedResult.toArray()));
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_TOTAL) == 2);
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_FILTERED) == 2);

		// 6th Test: Valid Group ID custom headers ( group 1 )
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		params.put(Account_Strings.REQ_HEADERS, "['" + Account_Strings.PROPERTIES_ROLE + "', 'group__oid', 'account_email', 'randomHeader']");
		res = requestJSON("get_member_list_info", params);
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0), "member2", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("member", groupID.get(0), "member4", "")));
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_TOTAL) == 2);
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_FILTERED) == 2);
		assertThat("Something is wrong with the lists", res.getList(Account_Strings.RES_DATA), containsInAnyOrder(expectedResult.toArray()));

		// 7th Test: No groupID, user who is not a group is logged in
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "member2");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("login", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_HEADERS, "['" + Account_Strings.PROPERTIES_ROLE + "', 'group__oid', 'account_email', 'randomHeader']");
		res = requestJSON("get_member_list_info", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );

		// 8th Test: No groupID, user is group and is logged in
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "member5");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("login", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_HEADERS, "['" + Account_Strings.PROPERTIES_ROLE + "', 'group__oid', 'account_email', 'randomHeader']");
		res = requestJSON("get_member_list_info", params);
		expectedResult.clear();
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4), "member1", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4), "member3", "")));
		expectedResult.add(new ArrayList<>(Arrays.asList("little-boy", userID.get(4), "member5", "")));
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_TOTAL) == 3);
		assertTrue(res.getInt(Account_Strings.RES_RECORDS_FILTERED) == 3);
		assertThat("Something is wrong with the lists", res.getList(Account_Strings.RES_DATA), containsInAnyOrder(expectedResult.toArray()));
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );

	}

	@Test
	public void singleMemberMeta(){
		GenericConvertMap<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		List<String> userID = new ArrayList<String>(), groupID = new ArrayList<String>();
		List<String> addUserList = new ArrayList<String>();
		List<String> expectedFailResult = new ArrayList<String>();
		// Ensure that there is an existing group
		params.put(Account_Strings.REQ_USERNAME, "exampleGrp");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("getMemberListInfoTest: Something wrong in creating group.", res.get(Account_Strings.RES_ERROR));
		groupID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
		for( int idx = 1; idx <= 3; idx ++ ) {
			params.clear();
			params.put(Account_Strings.REQ_USERNAME, "single" + idx);
			params.put(Account_Strings.REQ_PASSWORD, "password");
			res = requestJSON("new", params);
			assertNull("getMemberListInfoTest: Something wrong in adding user " + idx + ".", res.get(Account_Strings.RES_ERROR));
			userID.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
			if ( idx % 2 == 1 ) {
				addUserList.add(res.getString(Account_Strings.RES_ACCOUNT_ID));
			}
		}
		params.clear();
		params.put(Account_Strings.REQ_ROLE, "member");
		params.put(Account_Strings.REQ_ADD_LIST, addUserList);
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("add_remove_member", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals(expectedFailResult, res.get(Account_Strings.RES_FAIL_ADD));
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// 1st Test: Empty Submission
		res = requestJSON("get_single_member_meta", null);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));
		// 2nd Test: Invalid accountID
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, "randomID");
		res = requestJSON("get_single_member_meta", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));
		// 3rd Test: Valid accountID, account not in group ( single 2 )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(1));
		res = requestJSON("get_single_member_meta", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP_ID, res.get(Account_Strings.RES_ERROR));
		// 4th Test: Valid accountID, account not in group, invalid groupID, no roles ( single 2 )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(1));
		params.put(Account_Strings.REQ_GROUP_ID, "anyhowGroupID");
		res = requestJSON("get_single_member_meta", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));
		// 5th Test: Valid accountID, account not in group, Valid group, no roles ( single 2, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(1));
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("get_single_member_meta", params);
		assertEquals("User is not in group or not in specified role.", res.get(Account_Strings.RES_ERROR));
		// 6th Test: Valid accountID, account in group, Valid group, no roles ( single 1, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("get_single_member_meta", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals("member", res.getStringMap(Account_Strings.RES_META).get(Account_Strings.PROPERTIES_ROLE));
		// 7th Test: Valid accountID, account in group, Valid group, with roles ( single 1, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("get_single_member_meta", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertEquals("member", res.getStringMap(Account_Strings.RES_META).get(Account_Strings.PROPERTIES_ROLE));
		// 8th Test: Valid accountID, account in group, Valid group, with wrong roles ( single 1, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_ACCOUNT_ID, userID.get(0));
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		params.put(Account_Strings.REQ_ROLE, "wrongRole");
		res = requestJSON("get_single_member_meta", params);
		assertEquals("User is not in group or not in specified role.", res.get(Account_Strings.RES_ERROR));
		// 9th Test: No accountID, user logged in, user not group ( single2, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "single2");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("login", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("get_single_member_meta", params);
		assertEquals("User is not in group or not in specified role.", res.get(Account_Strings.RES_ERROR));
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );
		// 10th Test:  No accountID, user logged in, user in group ( single1, exampleGrp )
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "single3");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("login", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		params.clear();
		params.put(Account_Strings.REQ_GROUP_ID, groupID.get(0));
		res = requestJSON("get_single_member_meta", params);
		assertEquals("member", res.getStringMap(Account_Strings.RES_META).get(Account_Strings.PROPERTIES_ROLE));
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Account_Strings.RES_RETURN) );


	}
	//
	// 		// builder.put(path+"getListOfGroupIDOfMember", getListOfGroupIDOfMember);
	// 		// builder.put(path+"getListOfMemberIDInGroup", getListOfMemberIDInGroup);
	// @Test
	// public void getListOfGroupIDOfMember(){
	// 	GenericConvertMap<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "group number 1");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "group number 2");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "userToRetrieve");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userWithNoGroup");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that user is in both group
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "userToRetrieve");
	// 	params.put(Account_Strings.REQ_GROUPNAME, "group number 1");
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "group number 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	res = requestJSON("getListOfGroupIDOfMember", null);
	// 	assertEquals("No oid and username found.", res.get(Account_Strings.RES_ERROR));
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "anyhowuser");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userToRetrieve");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 2);
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userWithNoGroup");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 0);
	//
	// }
	//
	// @Test
	// public void getListOfMemberIDInGroup(){
	// 	GenericConvertMap<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "groupToRetrieve");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "groupWithNoMember");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "user number 1");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "user number 2");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that group has both member
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "user number 1");
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupToRetrieve");
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "user number 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	res = requestJSON("getListOfMemberIDInGroup", null);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_GROUPNAME, "anyhowgroup");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupToRetrieve");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 2);
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupWithNoMember");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 0);
	// }
	//
	// @Test
	// public void getListOfGroupObjectOfMember(){
	// 	GenericConvertMap<String,Object> res = null;
	// 	/// -----------------------------------------
	// 	/// Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	// Ensure that there is an existing user
	// 	Map<String,Object> params = new HashMap<String,Object>();
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "group test 1");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "group test 2");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "userObjToRetrieve");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userObjWithNoGroup");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that user is in both group
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "userObjToRetrieve");
	// 	params.put(Account_Strings.REQ_GROUPNAME, "group test 1");
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "group test 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	// 	res = requestJSON("getListOfGroupIDOfMember", null);
	// 	assertEquals("No oid and username found.", res.get(Account_Strings.RES_ERROR));
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "anyhowuser");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userObjToRetrieve");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 2);
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "userObjWithNoGroup");
	// 	res = requestJSON("getListOfGroupIDOfMember", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 0);
	// }
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
	// 	params.put(Account_Strings.REQ_USERNAME, "groupObjToRetrieve");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	// 	// Ensure that there is an existing group
	// 	params.put(Account_Strings.REQ_USERNAME, "groupObjWithNoMember");
	// 	params.put(Account_Strings.REQ_IS_GROUP, true);
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that there is an existing user
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "user test 1");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "user test 2");
	// 	params.put(Account_Strings.REQ_PASSWORD, "thisismypassword");
	// 	res = requestJSON("new", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
	//
	// 	// Ensure that group has both member
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_USERNAME, "user test 1");
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupObjToRetrieve");
	// 	params.put(Account_Strings.REQ_ROLE, "member");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_USERNAME, "user test 2");
	// 	res = requestJSON("addMember", params);
	// 	assertNull("ListGroupTest: Something wrong in adding user to group", res.get(Account_Strings.RES_ERROR));
	// 	/// -----------------------------------------
	// 	/// End of Preparation before commencement of Test
	// 	/// -----------------------------------------
	//
	// 	res = requestJSON("getListOfMemberIDInGroup", null);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));
	// 	params.clear();
	// 	params.put(Account_Strings.REQ_GROUPNAME, "anyhowgroup");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupObjToRetrieve");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 2);
	//
	// 	params.put(Account_Strings.REQ_GROUPNAME, "groupObjWithNoMember");
	// 	res = requestJSON("getListOfMemberIDInGroup", params);
	// 	assertTrue(res.getList(Account_Strings.RES_LIST).size() == 0);
	// }
}
