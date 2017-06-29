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

import picoded.servlet.api.module.account.Naming_Strings;

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
		assertNull( requestJSON("isLogin", null).get(Naming_Strings.RES_ERROR) );
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Naming_Strings.RES_RETURN) );
	}

	@Test
	public void loginProcessFlow() {
		// reuse result map
		Map<String,Object> res = null;

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Naming_Strings.RES_RETURN) );

		// Does a failed login
		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put(Naming_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Naming_Strings.REQ_PASSWORD, "Is the enemy");
		res = requestJSON("login", loginParams);

		assertEquals( Boolean.FALSE, res.get("isLogin") );
		assertEquals( "Failed login (wrong password or invalid user?)", res.get(Naming_Strings.RES_ERROR) );

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get(Naming_Strings.RES_RETURN) );

		// Does the actual login
		loginParams.put(Naming_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Naming_Strings.REQ_PASSWORD, "The Catcher in the Rye");

		// Request and check result
		res = requestJSON("login", loginParams);
		assertNull( res.get(Naming_Strings.RES_ERROR) );
		assertNull( res.get("INFO") );
		assertEquals( Boolean.TRUE, res.get("isLogin") );
		// Validate that login is valid
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.TRUE, res.get(Naming_Strings.RES_RETURN) );

		// Log out current user
		res = requestJSON("logout", null);
		assertEquals( Boolean.TRUE, res.get(Naming_Strings.RES_RETURN) );

		// Validate that logout is valid
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.FALSE, res.get(Naming_Strings.RES_RETURN) );
	}

	@Test
	public void loginLockingIncrement() {
		// reuse result map
		Map<String,Object> res = null;
		// Checks that the test begins with the user not logged in
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.FALSE, res.get(Naming_Strings.RES_RETURN) );

		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put(Naming_Strings.REQ_USERNAME, "laughing-man");
		loginParams.put(Naming_Strings.REQ_PASSWORD, "Is the enemy");
		int doTestLoop = 3, currentLoop = 0;
		while(currentLoop < doTestLoop){
			res = requestJSON("login", loginParams);
			// System.out.println(res.get(Naming_Strings.RES_ERROR)+" <<<<<<<<<<");
			Map<String, Object> params = new HashMap<String,Object>();
			params.put(Naming_Strings.REQ_ACCOUNT_NAME, "laughing-man");
			int waitTime = (int) requestJSON("lockTime", params).get("lockTime");
			if(currentLoop%2==0){
				assertEquals( Boolean.FALSE, res.get(Naming_Strings.RES_IS_LOGIN) );
				assertEquals("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n"+
											"Likely failure is due to insufficient Thread.sleep()\n",
											"Failed login (wrong password or invalid user?)", res.get(Naming_Strings.RES_ERROR) );
			}else{
				assertThat("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n",
				 						res.get(Naming_Strings.RES_ERROR).toString(), containsString("user locked out") );
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
		assertEquals("No username was supplied", res.get(Naming_Strings.RES_ERROR));

		createDetails.put(Naming_Strings.REQ_USERNAME, "little-boy");
		res = requestJSON("new", createDetails);
		assertEquals("No password was supplied", res.get(Naming_Strings.RES_ERROR));
		// Successfully created account
		createDetails.put(Naming_Strings.REQ_PASSWORD, "sooo smallll");
		res = requestJSON("new", createDetails);
		assertNotNull( res.get(Naming_Strings.RES_META));
		assertNull(res.get(Naming_Strings.RES_ERROR));

		String accountID = res.get(Naming_Strings.RES_ACCOUNT_ID).toString();

		//Create same user again
		res = requestJSON("new", createDetails);
		assertEquals("Object already exists in account Table", res.get(Naming_Strings.RES_ERROR));
		assertEquals(accountID, res.get(Naming_Strings.RES_ACCOUNT_ID));
	}

	@Test
	public void createNewGroup() {
		Map<String,Object> res = null;
		Map<String,Object> createDetails = new HashMap<String,Object>();
		createDetails.put(Naming_Strings.REQ_USERNAME, "boy band");
		createDetails.put(Naming_Strings.REQ_IS_GROUP, true);
		res = requestJSON("new", createDetails);
		assertNull(res.get(Naming_Strings.RES_ERROR));

		// Creates the same group
		res = requestJSON("new", createDetails);
		assertEquals("Object already exists in account Table", res.get(Naming_Strings.RES_ERROR));

		// Not using the correct value to create group
		createDetails.put(Naming_Strings.REQ_USERNAME, "girl band");
		createDetails.put(Naming_Strings.REQ_IS_GROUP, "random Words");
		res = requestJSON("new", createDetails);
		assertNotNull(res.get(Naming_Strings.RES_ERROR));


		createDetails.put(Naming_Strings.REQ_IS_GROUP, "1");
		res = requestJSON("new", createDetails);
		assertNull(res.get(Naming_Strings.RES_ERROR));
	}

	@Test
	public void getGroups() {
		Map<String,Object> res = null;

	}

	@Test
	public void groupFunctionalities(){
		// reuse result map
		Map<String,Object> res = null;
		res = requestJSON("membershipRoles", null);
		assertNotNull(res.get(Naming_Strings.RES_LIST));
	}
}
