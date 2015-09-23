package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.*;
import java.util.*;
import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.io.IOUtils;

import com.amazonaws.util.StringUtils;

import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.fileUtils.FileUtils;
import picoded.servletUtils.EmbeddedServlet;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;
import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.servlet.*;
import picoded.JStruct.*;
import picoded.RESTBuilder.templates.*;
import picoded.webUtils.*;

public class MetaTableApiBuilderTomcat_test {
	
	public static class MetaTableApiServlet extends BasePage {
		public boolean isJsonRequest() {
			return true;
		}
		
		/// Process the request, not the authentication layer
		public boolean doJSON(Map<String, Object> outputData, Map<String, Object> templateData) throws Exception {
			return rb.servletCall("", this, outputData);
		}
	}
	
	protected static EmbeddedServlet tomcat = null;
	
	private static List<String> _oids = null;
	private static MetaTable mtObj = null;
	private static MetaTableApiBuilder mtApi = null;
	private static RESTBuilder rb = null;
	
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
	public static void serverSetUp() throws LifecycleException, IOException, JStackException {
		mtObj = implementationConstructor();
		mtObj.systemSetup();
		populateMetaTableDummyData(3, 3);
		
		mtApi = new MetaTableApiBuilder(mtObj);
		
		rb = new RESTBuilder();
		mtApi.setupRESTBuilder(rb, "/meta-test/");
		
		if (tomcat == null) {
			File webInfFile = new File("./test-files/tmp/WEB-INF");
			
			for (File file : webInfFile.listFiles()) {
				file.delete(); //to accomodate certain people who do not use command line
			}
			
			webInfFile.mkdir();
			
			File context = new File("./test-files/tmp");
			tomcat = new EmbeddedServlet("", context)
			
			.withServlet("/api/*", "meta-table-test", new MetaTableApiServlet()).withPort(15000);
			
			tomcat.start();
			//			tomcat.awaitServer();
		}
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if (mtObj != null) {
			mtObj.systemTeardown();
		}
		mtObj = null;
		
		if (tomcat != null) {
			tomcat.stop();
		}
		tomcat = null;
	}
	
	//	@Test
	public void awaitServer() {
		tomcat.awaitServer();
	}
	
	RequestHttp requester;
	ResponseHttp response;
	Map<String, Object> responseMap;
	
	@Test
	@SuppressWarnings("unchecked")
	public void list_POST_test() {
		String path = "http://127.0.0.1:15000/api/meta-test/list";
		
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		Map<String, String[]> headersMap = new HashMap<String, String[]>();
		
		paramsMap.put("headers", new String[] { "[\"_oid\"]" });
		response = RequestHttp.post(path, paramsMap, null, null);
		assertNotNull(response);
		
		Map<String, Object> resMap = response.toMap();
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
		String path = "http://127.0.0.1:15000/api/meta-test/get";
		
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		
		Map<String, String[]> headersMap = new HashMap<String, String[]>();
		response = RequestHttp.get(path, paramsMap, null, null);
		
		if (response.statusCode() == 404) {
			System.out.println(404);
		}
		
		assertNotNull(response);
		Map<String, String[]> resHeaders = response.headersMap();
	}
	
	@Test
	public void meta_POST_test_delta() {
		String path = "http://127.0.0.1:15000/api/meta-test/post";
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
		String path = "http://127.0.0.1:15000/api/meta-test/post";
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
		String path = "http://127.0.0.1:15000/api/meta-test/post";
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
		String path = "http://127.0.0.1:15000/api/meta-test/meta";
		String getPath = "http://127.0.0.1:15000/api/meta-test/meta";
		Map<String, Object> respMap = null;
		
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[] { _oids.get(0) });
		
		//first get user
		response = RequestHttp.get(getPath, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());
		assertNotNull(respMap.get("meta"));
		
		//then delete
		response = RequestHttp.delete(path, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());
		
		//then check again
		response = RequestHttp.get(getPath, paramsMap, null, null);
		assertNotNull(respMap = response.toMap());
		assertNull(respMap.get("meta"));
	}
	
}
