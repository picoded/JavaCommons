package picoded.servlet.api.module.account;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.*;

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
			AccountTable table = new AccountTable(stack, "account");
			AccountTableApi ret = new AccountTableApi(table);

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
		// assertEquals( Boolean.TRUE, requestJSON("isLogin", null).get("return") );
	}
}