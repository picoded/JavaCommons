package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.JStack.JStackException;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaTable;
import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.templates.MetaTableApiBuilder;
import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.servlet.BasePage;
import picoded.servletUtils.EmbeddedServlet;
import picoded.webUtils.RequestHttp;
import picoded.webUtils.ResponseHttp;

@SuppressWarnings({ "rawtypes", "serial" , "try"})
public class MetaTableApiBuilderTomcat_test {

	public static class MetaTableApiServlet extends BasePage {
		public boolean isJsonRequest() {
			return true;
		}

		// / Process the request, not the authentication layer
		public boolean doJSON(Map<String, Object> outputData,
				Map<String, Object> templateData) throws Exception {
			return rb.servletCall("", this, outputData);
		}
	}

	protected static EmbeddedServlet tomcat = null;

	private static List<String> _oids = null;
	private static MetaTable mtObj = null;
	private static MetaTableApiBuilder mtApi = null;
	private static RESTBuilder rb = null;
	protected static int port = 15000;
	protected static boolean portAvailableCalled = false;

	private static MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}

	private static void populateMetaTableDummyData(int min, int max) {
		Random rnd = new Random();
		int _max = rnd.nextInt(max);
		_max = _max > min ? _max : min;

		_oids = new ArrayList<String>();

		for (int i = 0; i < _max; ++i) {
			String oid = GUID.base58();
			_oids.add(oid);

			Map<String, Object> innerObj = new HashMap<String, Object>();
			innerObj.put("_oid", oid);
			innerObj.put("_name", "name" + i);
			innerObj.put("_age", "age" + i);

			mtObj.append(oid, innerObj).saveAll();
		}
	}

	@BeforeClass
	public static void serverSetUp() throws LifecycleException, JStackException {
		if (!portAvailableCalled) {
			while (!portAvailableCalled) {
				available(port);
				if (!portAvailableCalled) {
					port += 100;
				}
			}
		}
		mtObj = implementationConstructor();
		mtObj.systemSetup();
		populateMetaTableDummyData(3, 3);

		mtApi = new MetaTableApiBuilder(mtObj);

		rb = new RESTBuilder();
		mtApi.setupRESTBuilder(rb, "/meta-test/");

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

					.withServlet("/api/*", "meta-table-test",
							new MetaTableApiServlet()).withPort(port);

			tomcat.start();
			// tomcat.awaitServer();
		}
	}

	@AfterClass
	public static void serverTearDown() throws LifecycleException {
		if (mtObj != null) {
			mtObj.systemTeardown();
		}
		mtObj = null;

		if (tomcat != null) {
			tomcat.stop();
		}
		tomcat = null;
	}

	// @Test
	// public void awaitServer() {
	// tomcat.awaitServer();
	// }

	RequestHttp requester;
	ResponseHttp response;
	Map<String, Object> responseMap;

	@Test
	public void list_POST_test() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/list";

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		// Map<String, String[]> headersMap = new HashMap<String, String[]>();

		paramsMap.put("headers", new String[] { "[\"_oid\"]" });
		response = RequestHttp.post(path, paramsMap, null, null);
		assertNotNull(response);

		Map<String, Object> resMap = response.toMap();

		@SuppressWarnings("unchecked")
		List<List<String>> dataList = (List<List<String>>) resMap.get("data");
		assertNotNull(dataList);

		List<String> convList = new ArrayList<String>();
		for (List<String> innerList : dataList) {
			convList.addAll(innerList);
		}
		boolean contains = convList.containsAll(_oids);
		assertTrue(contains);
	}

	@Test
	public void meta_GET_test() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/get";

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });

		// Map<String, String[]> headersMap = new HashMap<String, String[]>();
		response = RequestHttp.get(path, paramsMap, null, null);

		if (response.statusCode() == 404) {
			System.out.println("RESPONSE STATUS: " + 404);
		}

		assertNotNull(response);
		// Map<String, String[]> resHeaders = response.headersMap();
	}

	@Test
	public void meta_POST_test_delta() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/post";
		String jsonString = "";

		Map<String, Object> deltaObj = new HashMap<String, Object>();
		deltaObj.put("_name", "DeltaReplacedName");
		deltaObj.put("_age", "DeltaReplacedAge");
		jsonString = ConvertJSON.fromMap(deltaObj);

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		paramsMap.put("updateMode", new String[] { "delta" });
		paramsMap.put("meta", new String[] { jsonString });

		response = RequestHttp.post(path, paramsMap, null, null);
		assertNotNull(response);

		Map<String, Object> newMetaObj = new HashMap<String, Object>();
		newMetaObj.put("_name", "NewMetaObjectName");
		newMetaObj.put("_age", "NewMetaObjectAge");
	}

	@Test
	public void meta_POST_test_full() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/post";
		String jsonString = "";

		Map<String, Object> fullObj = new HashMap<String, Object>();
		fullObj.put("_name", "FullReplacedName");
		jsonString = ConvertJSON.fromMap(fullObj);

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(1) });
		paramsMap.put("updateMode", new String[] { "full" });
		paramsMap.put("meta", new String[] { jsonString });

		response = RequestHttp.post(path, paramsMap, null, null);
		assertNotNull(response);
	}

	@Test
	public void meta_POST_test_new() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/post";
		String jsonString = "";

		Map<String, Object> newMetaObj = new HashMap<String, Object>();
		newMetaObj.put("_name", "NewMetaObjectName");
		newMetaObj.put("_age", "NewMetaObjectAge");
		jsonString = ConvertJSON.fromMap(newMetaObj);

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { "new" });
		paramsMap.put("meta", new String[] { jsonString });

		response = RequestHttp.post(path, paramsMap, null, null);
		assertNotNull(response);
	}

	@Test
	public void meta_DELETE_test() {
		String path = "http://127.0.0.1:" + port + "/api/meta-test/meta";
		String getPath = "http://127.0.0.1:" + port + "/api/meta-test/meta";
		Map<String, Object> respMap = null;

		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });

		// first get user
		response = RequestHttp.get(getPath, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());
		assertNotNull(respMap.get("meta"));

		// then delete
		response = RequestHttp.delete(path, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());

		// then check again
		response = RequestHttp.get(getPath, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());
		assertNull(respMap.get("meta"));
	}

	private static boolean available(int port) {
		if (!portAvailableCalled) {
			try (Socket ignored = new Socket("localhost", port)) {
				return false;
			} catch (IOException ignored) {
				portAvailableCalled = true;
				// System.out.println(" PORT : " + port);
				return true;
			}
		}
		return true;
	}

	@Test
	public void list_GET_and_POST_AllBlankTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.get("http://127.0.0.1:" + port
				+ "/api/meta-test/list", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		assertNotNull(responseMap.get("data"));
	}

	@Test
	public void list_GET_and_POSTTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("caseSensitive", new String[] { "true" });
		paramsMap.put("queryColumns", new String[] { "_name", "_age" });
		paramsMap.put("searchValue", new String[] { "abc", "def", "xyz" });
		response = RequestHttp.get("http://127.0.0.1:" + port
				+ "/api/meta-test/list", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		assertNotNull(responseMap.get("data"));
	}

	@Test
	public void list_GET_and_POSTOrderByTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("caseSensitive", new String[] { "true" });
		paramsMap.put("queryColumns", new String[] { "_name", "_age" });
		paramsMap.put("searchValue", new String[] { "xyz" });
		paramsMap.put("orderBy", new String[] { "_name" });
		response = RequestHttp.get("http://127.0.0.1:" + port
				+ "/api/meta-test/list", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		assertNotNull(responseMap.get("data"));
		assertNotNull(responseMap.get("draw"));
		assertNotNull(responseMap.get("headers"));
	}

	@Test
	public void list_GET_and_POSTQueryTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("caseSensitive", new String[] { "true" });
		paramsMap.put("queryColumns", new String[] { "_name", "_oid" });
		paramsMap.put("searchValue", new String[] { "xyz", "123456" });
		paramsMap.put("queryArgs", new String[] { "25", "65" });
		paramsMap.put("orderBy", new String[] { "_name" });
		paramsMap.put("query", new String[] { "_age > ? AND _age < ? " });
		response = RequestHttp.post("http://127.0.0.1:" + port
				+ "/api/meta-test/list", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("error"));
		assertNotNull(responseMap.get("data"));
		assertNotNull(responseMap.get("draw"));
		assertNotNull(responseMap.get("headers"));
	}

	@Test
	public void csv_exportAllParamest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("caseSensitive", new String[] { "true" });
		paramsMap.put("queryColumns", new String[] { "_name", "_oid" });
		paramsMap.put("searchValue", new String[] { "xyz", "123456" });
		paramsMap.put("queryArgs", new String[] { "25", "65" });
		paramsMap.put("orderBy", new String[] { "_name" });
		paramsMap.put("query", new String[] { "_age > ? AND _age < ? " });
		response = RequestHttp.get("http://127.0.0.1:" + port
				+ "/api/meta-test/csv", paramsMap);
		assertNotNull(response);
	}

	@Test
	public void csv_exportRESTTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.post("http://127.0.0.1:" + port
				+ "/api/meta-test/csv", paramsMap);
		assertNotNull(response);
	}

	@Test
	public void meta_POSTInvalidTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		response = RequestHttp.post("http://127.0.0.1:" + port
				+ "/api/meta-test/meta", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertEquals("No meta object was found in the request",
				responseMap.get("error"));
	}

	@Test
	public void meta_POSTTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		paramsMap.put("meta",
				new String[] { "{\"oop\":\"java\",\"foo\":\"class\"}" });
		response = RequestHttp.post("http://127.0.0.1:" + port
				+ "/api/meta-test/meta", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("No meta object was found in the request",
				responseMap.get("error"));
		LinkedHashMap updatedMObj = (LinkedHashMap) responseMap
				.get("updateMeta");
		assertEquals("java", updatedMObj.get("oop"));
	}

	@Test
	public void meta_POSTAllParamTest() {
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		paramsMap.put("meta",
				new String[] { "{\"oop\":\"java\",\"foo\":\"class\"}" });
		paramsMap.put("updateMode", new String[] { "updateMode" });
		response = RequestHttp.post("http://127.0.0.1:" + port
				+ "/api/meta-test/meta", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("No meta object was found in the request",
				responseMap.get("error"));
		LinkedHashMap updatedMObj = (LinkedHashMap) responseMap
				.get("updateMeta");
		assertEquals("java", updatedMObj.get("oop"));
	}

}
