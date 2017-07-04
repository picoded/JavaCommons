package picoded.servlet.api.module.account;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
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

		createDetails.clear();
		createDetails.put(Account_Strings.REQ_GROUPNAME, "girl band");
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

		createDetails.clear();
		createDetails.put(Account_Strings.REQ_GROUPNAME, "bbbbb");
		res = requestJSON("groupRoles", createDetails);
		assertEquals(expectedRoles, res.get(Account_Strings.RES_LIST));
	}

	@Test
	public void groupRoles(){
		// reuse result map
		Map<String,Object> res = null;
		res = requestJSON("groupRoles", null);
		assertEquals("No groupname is found.", res.get(Account_Strings.RES_ERROR));
		Map<String,Object> params = new HashMap<String,Object>();

		params.put(Account_Strings.REQ_GROUPNAME, "girly");
		res = requestJSON("groupRoles", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Add in a group specifically for this test
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "Macho");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull(res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "Macho");
		res = requestJSON("groupRoles", params);
		// assertEquals("No group is found.", res.get(Account_Strings.RES_ERROR));
		assertNull(res.get(Account_Strings.RES_ERROR));
		ArrayList<String> expectedRoles = new ArrayList<String>();
		expectedRoles.add("member");
		expectedRoles.add("admin");
		assertEquals(expectedRoles, res.get(Account_Strings.RES_LIST));
	}

	@Test
	public void addMemberToGroup() {
		Map<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(Account_Strings.REQ_USERNAME, "memberToAdd");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("AddMemberTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		// Ensure that there is an existing group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "newGroup");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("AddMemberTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// Invalid user, group and role
		res = requestJSON("addMember", null);
		assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "wrong member");
		res = requestJSON("addMember", params);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
		res = requestJSON("addMember", params);
		assertEquals(Account_Strings.ERROR_NO_ROLE, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_ROLE, "unknown role");
		res = requestJSON("addMember", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Valid group
		params.put(Account_Strings.REQ_GROUPNAME, "newGroup");
		res = requestJSON("addMember", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));

		// Valid user
		params.put(Account_Strings.REQ_USERNAME, "memberToAdd");
		res = requestJSON("addMember", params);
		assertEquals("User is already in group or role is not found.", res.get(Account_Strings.RES_ERROR));

		// Valid role
		params.put(Account_Strings.REQ_ROLE, "admin");
		res = requestJSON("addMember", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_META));

		// Add the user again
		res = requestJSON("addMember", params);
		assertEquals("User is already in group or role is not found.", res.get(Account_Strings.RES_ERROR));
	}

	@Test
	public void getMemberMeta(){
		Map<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(Account_Strings.REQ_USERNAME, "membermeta");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		// Ensure that there is an existing group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "memberMetaGroup");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		// Ensure that the user is added to the group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "membermeta");
		params.put(Account_Strings.REQ_GROUPNAME, "memberMetaGroup");
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("addMember", params);
		assertNull("MemberMetaTest: Something wrong in adding member to group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// Invalid user, group and role
		res = requestJSON("getMemberMeta", null);
		assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "wrong member");
		res = requestJSON("getMemberMeta", params);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
		params.put(Account_Strings.REQ_ROLE, "unknown role");
		res = requestJSON("getMemberMeta", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Valid group
		params.put(Account_Strings.REQ_GROUPNAME, "memberMetaGroup");
		res = requestJSON("getMemberMeta", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));

		// Valid user
		params.put(Account_Strings.REQ_USERNAME, "membermeta");
		res = requestJSON("getMemberMeta", params);
		assertEquals("User is not in group or not in specified role.", res.get(Account_Strings.RES_ERROR));

		// Valid role
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("getMemberMeta", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_META));

		// No role at all
		params.remove(Account_Strings.REQ_ROLE);
		res = requestJSON("getMemberMeta", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_META));
	}


	@Test
	public void getMemberRole(){
		Map<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(Account_Strings.REQ_USERNAME, "memberrole");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_USERNAME, "memberNotInGroup");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		// Ensure that there is an existing group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "memberRoleGroup");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		// Ensure that the user is added to the group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "memberrole");
		params.put(Account_Strings.REQ_GROUPNAME, "memberRoleGroup");
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("addMember", params);
		assertNull("MemberMetaTest: Something wrong in adding member to group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// Invalid user, group and role
		res = requestJSON("getMemberRole", null);
		assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "wrong member");
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Valid group
		params.put(Account_Strings.REQ_GROUPNAME, "memberRoleGroup");
		res = requestJSON("getMemberRole", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));

		// Invalid user
		params.put(Account_Strings.REQ_USERNAME, "memberNotInGroup");
		res = requestJSON("getMemberRole", params);
		assertEquals("No role for user is found.", res.get(Account_Strings.RES_ERROR));

		// Valid User
		params.put(Account_Strings.REQ_USERNAME, "memberrole");
		res = requestJSON("getMemberRole", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_SINGLE_RETURN_VALUE));
	}

	@Test
	public void removeMemberFromGroup() {
		Map<String,Object> res = null;
		/// -----------------------------------------
		/// Preparation before commencement of Test
		/// -----------------------------------------
		// Ensure that there is an existing user
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(Account_Strings.REQ_USERNAME, "removemember");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_USERNAME, "wrongmemberNotInGroup");
		params.put(Account_Strings.REQ_PASSWORD, "password");
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding user.", res.get(Account_Strings.RES_ERROR));
		// Ensure that there is an existing group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "memberRemoveGroup");
		params.put(Account_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", params);
		assertNull("MemberMetaTest: Something wrong in adding group.", res.get(Account_Strings.RES_ERROR));
		// Ensure that the user is added to the group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "removemember");
		params.put(Account_Strings.REQ_GROUPNAME, "memberRemoveGroup");
		params.put(Account_Strings.REQ_ROLE, "member");
		res = requestJSON("addMember", params);
		assertNull("MemberMetaTest: Something wrong in adding member to group.", res.get(Account_Strings.RES_ERROR));
		/// -----------------------------------------
		/// End of Preparation before commencement of Test
		/// -----------------------------------------

		// Invalid user, group and role
		res = requestJSON("removeMember", null);
		assertEquals(Account_Strings.ERROR_NO_USERNAME, res.get(Account_Strings.RES_ERROR));

		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "wrong member");
		res = requestJSON("removeMember", params);
		assertEquals(Account_Strings.ERROR_NO_GROUPNAME, res.get(Account_Strings.RES_ERROR));

		params.put(Account_Strings.REQ_GROUPNAME, "wrong group");
		res = requestJSON("removeMember", params);
		assertEquals(Account_Strings.ERROR_NO_GROUP, res.get(Account_Strings.RES_ERROR));

		// Valid group
		params.put(Account_Strings.REQ_GROUPNAME, "memberRemoveGroup");
		res = requestJSON("removeMember", params);
		assertEquals(Account_Strings.ERROR_NO_USER, res.get(Account_Strings.RES_ERROR));

		// Invalid user
		params.put(Account_Strings.REQ_USERNAME, "wrongmemberNotInGroup");
		res = requestJSON("removeMember", params);
		assertEquals("User is not in group.", res.get(Account_Strings.RES_ERROR));

		// Legit Account but not Group Account
		params.put(Account_Strings.REQ_GROUPNAME, "removemember");
		System.out.println("This is thaw plarrr");
		res = requestJSON("removeMember", params);
		assertEquals("This is not a group.", res.get(Account_Strings.RES_ERROR));
		System.out.println("This is thaw plarrrqwewqeqwewqewqewq");

		// Valid User and Group
		params.clear();
		params.put(Account_Strings.REQ_USERNAME, "removemember");
		params.put(Account_Strings.REQ_GROUPNAME, "memberRemoveGroup");
		res = requestJSON("removeMember", params);
		assertNull(res.get(Account_Strings.RES_ERROR));
		assertNotNull(res.get(Account_Strings.RES_META));
	}
}