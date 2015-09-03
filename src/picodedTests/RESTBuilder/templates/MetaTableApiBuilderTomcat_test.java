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
		public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
			return rb.servletCall( "", this, outputData );
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
	
	private static void populateMetaTableDummyData(int min, int max){
		Random rnd = new Random();
		int _max = rnd.nextInt(max);
		_max = _max > min ? _max : min;
		
		_oids = new ArrayList<String>();
		
		for(int i = 0; i < _max; ++i){
			String oid = GUID.base58();
			_oids.add(oid);
			
			Map<String, Object> innerObj = new HashMap<String, Object>();
			innerObj.put("_oid", oid);
			innerObj.put("_name", "name"+i);
			innerObj.put("_age", "age"+i);
			
			mtObj.append(oid,  innerObj).saveAll();
		}
	}
	
	@BeforeClass
	public static void serverSetUp() throws LifecycleException, IOException, JStackException {
		mtObj = implementationConstructor();
		mtObj.systemSetup();
		populateMetaTableDummyData(3, 3);
		
		mtApi = new MetaTableApiBuilder(mtObj);
		
		rb = new RESTBuilder();
		mtApi.setupRESTBuilder(rb,  "meta-test.");
		
		if( tomcat == null ) {
			File webInfFile = new File("./test-files/tmp/WEB-INF");
			
			for(File file : webInfFile.listFiles()){
				file.delete(); //to accomodate certain people who do not use command line
			}
			
			webInfFile.mkdir();
			
			File context = new File("./test-files/tmp");
			tomcat = new EmbeddedServlet("", context)
			
			.withServlet("/api/*", "meta-table-test", new MetaTableApiServlet())
			.withPort(15000);
			
			tomcat.start();
//			tomcat.awaitServer();
		}
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if( mtObj != null ) {
			mtObj.systemTeardown();
		}
		mtObj = null;
		
		if(tomcat != null) {
			tomcat.stop();
		}
		tomcat = null;
	}
	
	RequestHttp requester;
	ResponseHttp response;
	Map<String,Object> responseMap;
	
//	@Test
	public void list_GET_and_POST_test(){
		String path = "./test-files/tmp/WEB-INF/list";
		
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		Map<String, String[]> headersMap = new HashMap<String, String[]>();
		response = RequestHttp.get(path, paramsMap, null, headersMap);
		
		assertNotNull(response);
	}
	
	@Test
	public void meta_GET_test(){
		String path = "http://127.0.0.1:15000/api/meta-test/list";
		
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("_oid", new String[]{_oids.get(0)});
		
		
		Map<String, String[]> headersMap = new HashMap<String, String[]>();
		response = RequestHttp.get(path, paramsMap, null, null);
		
		if(response.statusCode() == 404){
			System.out.println(404);
		}
		
		assertNotNull(response);
	}
	
//	@Test
	public void meta_POST_test(){
		
	}
	
}
