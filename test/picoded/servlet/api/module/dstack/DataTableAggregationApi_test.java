package picoded.servlet.api.module.dstack;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import org.junit.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.math.BigDecimal;

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
public class DataTableAggregationApi_test extends ApiModule_test {
	
	//----------------------------------------------------------------------------------------
	//
	//  Test setup
	//
	//----------------------------------------------------------------------------------------
	
	/// The test servlet to use
	public static class DataTableAggregationApiTestServlet extends ApiModuleTestServlet {
		@Override
		public ApiModule moduleSetup(CommonStack stack) {
			// Setup the test table and api
			DataObjectMap table = stack.dataObjectMap("test-data");
			DataTableAggregationApi tableApi = new DataTableAggregationApi(table);
			apiTestPath = "data";
			
			// Returning this api, will automatically do the setup needed
			return tableApi;
		}
	}
	
	public CorePage setupServlet() {
		return new DataTableAggregationApiTestServlet();
	}
	
	//----------------------------------------------------------------------------------------
	//
	//  Test running
	//
	//----------------------------------------------------------------------------------------
	@Test
	public void newAggTest() {
		requestJSON("data/new", "{ \"data\" : { \"doubleValue\" : 1 } }");
		requestJSON("data/new", "{ \"data\" : { \"doubleValue\" : 5 } }");
		requestJSON("data/new", "{ \"data\" : { \"doubleValue\" : 10 } }");
		
		// get it boyo
		GenericConvertMap<String, Object> res = requestJSON("data/aggregatedlist",
			"{ \"aggregations\" : [ \"count(doubleValue)\", \"max(doubleValue)\", \"avg(doubleValue)\" ] }");
		Map<String, Object> aggMap = res.getStringMap("aggregationResult");
		
		assertEquals(3, GenericConvert.toInt(aggMap.get("count(doubleValue)")));
		assertEquals(10.0, GenericConvert.toDouble(aggMap.get("max(doubleValue)")), 0);
		assertEquals(5.33, GenericConvert.toDouble(aggMap.get("avg(doubleValue)")), 0);
	}
}
