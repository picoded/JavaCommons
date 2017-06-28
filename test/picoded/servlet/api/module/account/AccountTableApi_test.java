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
			System.out.println("ModuleSetup was ran.");
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
		assertNull( requestJSON("isLogin", null).get("ERROR") );
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get("return") );
	}

	@Test
	public void loginProcessFlow() {
		// reuse result map
		Map<String,Object> res = null;

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get("return") );

		// Does a failed login
		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put("loginID", "laughing-man");
		loginParams.put("loginPass", "Is the enemy");
		res = requestJSON("login", loginParams);

		assertEquals( Boolean.FALSE, res.get("isLogin") );
		assertEquals( "Failed login (wrong password or invalid user?)", res.get("ERROR") );

		// Check for invalid login
		assertEquals( Boolean.FALSE, requestJSON("isLogin", null).get("return") );

		// Does the actual login
		loginParams.put("loginID", "laughing-man");
		loginParams.put("loginPass", "The Catcher in the Rye");

		// Request and check result
		res = requestJSON("login", loginParams);
		assertNull( res.get("ERROR") );
		assertNull( res.get("INFO") );
		assertEquals( Boolean.TRUE, res.get("isLogin") );
		// Validate that login is valid
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.TRUE, res.get("return") );
	}

	@Test
	public void loginLockingIncrement() {
		// reuse result map
		Map<String,Object> res = null;
		// Checks that the test begins with the user not logged in
		res = requestJSON("isLogin", null);
		assertEquals( Boolean.FALSE, res.get("return") );

		Map<String,Object> loginParams = new HashMap<String,Object>();
		loginParams.put("loginID", "laughing-man");
		loginParams.put("loginPass", "Is the enemy");
		int doTestLoop = 3, currentLoop = 0;
		while(currentLoop < doTestLoop){
			res = requestJSON("login", loginParams);
			// System.out.println(res.get("ERROR")+" <<<<<<<<<<");
			Map<String, Object> params = new HashMap<String,Object>();
			params.put("accountName", "laughing-man");
			int waitTime = (int) requestJSON("lockTime", params).get("lockTime");
			if(currentLoop%2==0){
				assertEquals( Boolean.FALSE, res.get("isLogin") );
				assertEquals("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n"+
											"Likely failure is due to insufficient Thread.sleep()\n",
											"Failed login (wrong password or invalid user?)", res.get("ERROR") );
			}else{
				assertThat("It failed on the Loop Number: "+ (currentLoop+1) +" with waitTime: "+waitTime+"\n",
				 						res.get("ERROR").toString(), containsString("user locked out") );
				try{
					Thread.sleep(waitTime*1000);
				}catch(InterruptedException ie){}
			}
			currentLoop++;
		}
	}

	@Test
	public void groupFunctionalities(){
		// reuse result map
		Map<String,Object> res = null;
		res = requestJSON("membershipRoles", null);
		assertNotNull(res.get("list"));
	}
}
