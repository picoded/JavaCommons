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
			DataObjectMap table = stack.getDataTable("test-data");
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
	public void newGetSetGetTest() {
		// Create a new object
		GenericConvertMap<String, Object> res = requestJSON("data/new",
			"{ \"data\" : { \"zero\" : 1 } }");
		checkResultForError(res);
		assertNotNull(res.getString("result"));
		
		// Get the _oid value
		String oid = res.getString("result");
		
		// Get the data
		assertNotNull(res = requestJSON("data/get", "{ \"_oid\" : \"" + oid + "\"}"));
		checkResultForError(res);
		
		// Validate the data
		assertEquals(oid, res.getString("_oid"));
		assertNotNull(res.getStringMap("result"));
		assertEquals(1, res.getStringMap("result").get("zero"));
		
		// Set and update the data
		assertNotNull(res = requestJSON("data/set", "{ \"_oid\" : \"" + oid
			+ "\", \"data\": { \"zero\" : 2 } }"));
		checkResultForError(res);
		
		// Get the updated data
		assertNotNull(res = requestJSON("data/get", "{ \"_oid\" : \"" + oid + "\"}"));
		checkResultForError(res);
		
		// Validate the updated data
		assertEquals(oid, res.getString("_oid"));
		assertNotNull(res.getStringMap("result"));
		assertEquals(2, res.getStringMap("result").get("zero"));
	}
	
	@Test
	public void basicListTest() {
		// Get blank list
		GenericConvertMap<String, Object> res = requestJSON("data/list", null);
		checkResultForError(res);
		assertNotNull(res.getObjectArray("result"));
		assertTrue(res.getObjectArray("result").length == 0);
		
		// Create a single object
		newGetSetGetTest();
		
		// Get a list of 1
		res = requestJSON("data/list", null);
		checkResultForError(res);
		assertNotNull(res.getObjectArray("result"));
		assertTrue(res.getObjectArray("result").length == 1);
	}
	
	@Test
	public void keyNamesTest() {
		// Generate an object
		newGetSetGetTest();
		
		// Get keyname
		GenericConvertMap<String, Object> res = requestJSON("data/keyNames", null);
		checkResultForError(res);
		assertNotNull(res.getObjectArray("result"));
		
		// Validate "zero" is in the keyname
		assertTrue(res.getObjectArray("result").length >= 1);
		assertTrue(Arrays.asList(res.getObjectArray("result")[0]).contains("zero"));
	}
	
}
