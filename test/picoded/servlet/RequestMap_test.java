package picoded.servlet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestMap_test {
	
	private RequestMap requestMap;
	
	@Before
	public void setUp() {
		requestMap = new RequestMap();
	}
	
	@After
	public void tearDown() {
		requestMap = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(requestMap);
	}
	
	@Test
	public void constructordefault() {
		requestMap = new RequestMap();
		assertNotNull(requestMap);
		assertEquals(0, requestMap.size());
	}
	
	@Test
	public void constructorParametrized() {
		Map<String, Object> proxy = new HashMap<String, Object>();
		proxy.put("my_key", "val1");
		requestMap = new RequestMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void constructorParametrizedNullParameter() {
		requestMap = new RequestMap((Map<String, Object>) null);
		assertNotNull(requestMap);
		assertEquals(0, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", new String[] { "val1" });
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapNullValueTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", null);
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapEmptyTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", new String[] {});
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapNullElementTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", new String[] { null });
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapOneNullElementTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", new String[] { "val", null });
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
	
	@Test
	public void fromStringArrayValueMapElementsTest() {
		Map<String, String[]> proxy = new HashMap<String, String[]>();
		proxy.put("my_key", new String[] { "oop", "foo" });
		requestMap = RequestMap.fromStringArrayValueMap(proxy);
		assertNotNull(requestMap);
		assertEquals(1, requestMap.size());
	}
}
