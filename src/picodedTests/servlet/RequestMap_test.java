package picodedTests.servlet;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.servlet.RequestMap;

public class RequestMap_test {
	
	private RequestMap requestMap = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void constructor() {
		requestMap = new RequestMap();
		assertNotNull(requestMap);
	}
	
	@Test
	public void constructorWithParameter() {
		Map<String, String> proxy = new HashMap<String, String>();
		requestMap = new RequestMap(proxy);
		assertNotNull(requestMap);
	}
	
}
