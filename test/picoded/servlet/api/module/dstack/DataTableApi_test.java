package picoded.servlet.api.module.dstack;

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
public class DataTableApi_test extends ApiModule_test {
	
	//----------------------------------------------------------------------------------------
	//
	//  Test setup
	//
	//----------------------------------------------------------------------------------------
	
	/// The test servlet to use
	public static class DataTableApiTestServlet extends ApiModuleTestServlet {
		@Override
		public ApiModule moduleSetup(CommonStack stack) {
			// Setup the test table and api
			DataTable table = stack.getDataTable("test-data");
			DataTableApi tableApi = new DataTableApi(table);
			apiTestPath = "data";

			// Returning this api, will automatically do the setup needed
			return tableApi;
		}
	}
	
	public CorePage setupServlet() {
		return new DataTableApiTestServlet();
	}
	
	//----------------------------------------------------------------------------------------
	//
	//  Test running
	//
	//----------------------------------------------------------------------------------------
	
	@Test
	public void newGetUpdateGetTest() {
		// Create a new object
		GenericConvertMap<String, Object> res = requestJSON("data/new", "{ data : { zero : 1 } }");
		assertNotNull(res);
		assertNotNull(res.getString("result"));

		// Get the _oid value
		String oid = res.getString("result");
	}
	
}
