package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.util.concurrent.AtomicLongMap;

import picoded.JStack.JStackException;
import picoded.JStruct.internal.JStruct_AtomicLongMap;
import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.templates.AtomicLongMapApiBuilder;
import picoded.servlet.BasePage;
import picoded.servletUtils.EmbeddedServlet;
import picoded.webUtils.RequestHttp;
import picoded.webUtils.ResponseHttp;

public class AtomicLongMapApiBuilder_test {
	
	// Servlet setup
	// --------------------------------------------------------------------------
	protected static EmbeddedServlet tomcat = null;
	protected static String testAddress = "http://127.0.0.1:";
	protected static int port = 15000;
	protected static boolean portAvailableCalled = false;
	//private RequestHttp requester;
	private ResponseHttp response;
	private Map<String, Object> responseMap;
	private static AtomicLongMapApiBuilder builder;
	private static RESTBuilder rb = null;
	
	@SuppressWarnings("serial")
	public static class AtomicLongMapApiBuilderServlet extends BasePage {
		public boolean isJsonRequest() {
			return true;
		}
		
		// / Process the request, not the authentication layer
		public boolean doJSON(Map<String, Object> outputData, Map<String, Object> templateData) throws Exception {
			return rb.servletCall("", this, outputData);
		}
	}
	
	@BeforeClass
	public static void serverSetUp() throws LifecycleException, IOException, JStackException {
		if (!portAvailableCalled) {
			while (!portAvailableCalled) {
				available(port);
				if (!portAvailableCalled) {
					port += 100;
				}
			}
		}
		testAddress = testAddress + port;
		builder = new AtomicLongMapApiBuilder(new JStruct_AtomicLongMap());
		
		rb = new RESTBuilder();
		builder.setupRESTBuilder(rb, "/meta-test/");
		
		if (tomcat == null) {
			File webInfFile = new File("./test-files/tmp/WEB-INF");
			
			if (webInfFile.exists()) {
				for (File file : webInfFile.listFiles()) {
					file.delete(); // to accomodate certain people who do not
					// use command line
				}
			}
			
			webInfFile.mkdir();
			
			File context = new File("./test-files/tmp");
			tomcat = new EmbeddedServlet("", context)
			
			.withServlet("/api/*", "meta-table-test", new AtomicLongMapApiBuilderServlet()).withPort(port);
			
			tomcat.start();
			// tomcat.awaitServer();
		}
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if (tomcat != null) {
			// tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		builder = null;
		tomcat = null;
	}
	
	// Tests
	// --------------------------------------------------------------------------
	@Test
	public void getValueInvalidTest() {
		response = RequestHttp.get(testAddress + "/api/meta-test/value");
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void getValueTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		response = RequestHttp.get(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("No key was supplied", responseMap.get("error"));
		//assertNull(responseMap.get("value"));
	}
	
	@Test
	public void getValuesTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { "foo", "oop" });
		response = RequestHttp.get(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		assertNotNull(responseMap.get("values"));
	}
	
	@Test
	public void getValuesInvalidEmptyKeysTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { });
		response = RequestHttp.get(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("Key array supplied was empty", responseMap.get("error"));
		
	}
	
	@Test
	public void getValuesInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.get(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void setValueInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { });
		paramsMap.put("allowEmptyValue", new String[] {"false" });
		response = RequestHttp.post(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
	}
	
	@Test
	public void setValueTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		paramsMap.put("allowEmptyValue", new String[] {"true" });
		paramsMap.put("value", new String[] { });
		response = RequestHttp.post(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
	}
	
	@Test
	public void setValueNonEmptyValueTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		//paramsMap.put("allowEmptyValue", new String[] {"false" });
		paramsMap.put("value", new String[] { "1" });
		response = RequestHttp.post(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
		
		paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		response = RequestHttp.get(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertEquals(1, responseMap.get("value"));
	}
	
	@Test
	public void setValueInvalidWithoutValueTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post(testAddress + "/api/meta-test/value", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void setValuesInvalidWithoutValueTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void setValuesInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keyValues", new String[] { });
		paramsMap.put("allowEmptyValue", new String[] {"false" });
		response = RequestHttp.post(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
	}
	
	@Test
	public void setValuesTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keyValues", new String[] { "oop", "foo" });
		paramsMap.put("allowEmptyValue", new String[] {"true" });
		paramsMap.put("values", new String[] { "1", "2" });
		response = RequestHttp.post(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		
		paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { "foo", "oop" });
		response = RequestHttp.get(testAddress + "/api/meta-test/values", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		@SuppressWarnings("unchecked")
		Map<String, Long> valueMap = (Map<String, Long>)responseMap.get("values");
		assertNotNull(valueMap);
		assertEquals(2, valueMap.size());
	}
	
	@Test
	public void deleteValueInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValue", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An empty key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void deleteValueExceptionTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValue", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An error occured while trying to delete a value", responseMap.get("error"));
	}
	
	@Test
	public void deleteValueTest() {
		setValueNonEmptyValueTest();
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValue", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("An empty key was supplied", responseMap.get("error"));
		@SuppressWarnings("unchecked")
		Map<String, Long> valueMap = (Map<String, Long>)responseMap.get("values");
		assertEquals(0, valueMap.get("my_key").longValue());	
	}
	
	@Test
	public void deleteValuesInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValues", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key array was supplied", responseMap.get("error"));
	}
	
	@Test
	public void deleteValuesInvalidKeysTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { });
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValues", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("No key array was supplied", responseMap.get("error"));
	}
	
	@Test
	public void deleteValuesExceptionTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { "oop", "foo" });
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValues", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An error occured while trying to delete a value", responseMap.get("error"));
	}
	
	
	@Test
	public void deleteValuesTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("keys", new String[] { "oop", "foo" });
		response = RequestHttp.post(testAddress + "/api/meta-test/deleteValues", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("An empty key was supplied", responseMap.get("error"));
		@SuppressWarnings("unchecked")
		Map<String, Long> valueMap = (Map<String, Long>)responseMap.get("values");
		assertEquals(0, valueMap.get("oop").longValue());	
		assertEquals(0, valueMap.get("foo").longValue());	
	}
	
	//@Test
	public void getMapTest() {
		setValueNonEmptyValueTest();
		response = RequestHttp.get(testAddress + "/api/meta-test/map", null);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("error", responseMap.get("error"));
		@SuppressWarnings("rawtypes")
		AtomicLongMap valueMap = (AtomicLongMap)responseMap.get("map");
		assertNotNull(valueMap);
		assertEquals(1, valueMap.size());
	}
	
	@Test
	public void weakCompareAndSetTest() {
		setValueNonEmptyValueTest();
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		paramsMap.put("expect", new String[] { "1" });
		paramsMap.put("update", new String[] { "5" });
		response = RequestHttp.post(testAddress + "/api/meta-test/weakCompareAndSet", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("An empty key was supplied", responseMap.get("error"));
		assertNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
		assertEquals(true, responseMap.get("map"));
	}
	
	@Test
	public void weakCompareAndSetInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post(testAddress + "/api/meta-test/weakCompareAndSet", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An empty key was supplied", responseMap.get("error"));
	}
	
	@Test
	public void weakCompareAndSetInvalidAllwedTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("key", new String[] { "my_key" });
		paramsMap.put("allowed", new String[] { });
		response = RequestHttp.post(testAddress + "/api/meta-test/weakCompareAndSet", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNotNull("An empty value was supplied, and allowEmptyValue is false", responseMap.get("error"));
	}
	
	private static boolean available(int port) {
		if (!portAvailableCalled) {
			try (Socket ignored = new Socket("localhost", port)) {
				return false;
			} catch (IOException ignored) {
				portAvailableCalled = true;
				return true;
			}
		}
		return true;
	}
	
}
