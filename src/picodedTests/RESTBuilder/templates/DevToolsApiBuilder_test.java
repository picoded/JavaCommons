package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.catalina.LifecycleException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import picoded.conv.*;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.templates.*;
import picoded.conv.GUID;
import picoded.servlet.BasePage;
import picoded.servletUtils.EmbeddedServlet;
import picoded.webUtils.RequestHttp;
import picoded.webUtils.ResponseHttp;

public class DevToolsApiBuilder_test {
	protected static EmbeddedServlet tomcat = null;
	protected static String testAddress = "http://127.0.0.1:";
	protected static int port = 15000;
	protected static boolean portAvailableCalled = false;
	// The testing RESTBuilder object
	public static RESTBuilder rbObj = null;
	private static DevToolsApiBuilder builder;
	private ResponseHttp response;
	private Map<String, Object> responseMap;
	
	@SuppressWarnings("serial")
	public static class DevToolsApiBuilderServlet extends BasePage {
		public boolean isJsonRequest() {
			return true;
		}
		
		// / Process the request, not the authentication layer
		public boolean doJSON(Map<String, Object> outputData, Map<String, Object> templateData) throws Exception {
			return rbObj.servletCall("", this, outputData);
		}
	}
	
	@BeforeClass
	public static void setup() throws LifecycleException {
		if (!portAvailableCalled) {
			while (!portAvailableCalled) {
				available(port);
				if (!portAvailableCalled) {
					port += 100;
				}
			}
		}
		testAddress = testAddress + port;
		rbObj = new RESTBuilder();
		builder = new DevToolsApiBuilder(rbObj);
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
			.withServlet("/api/*", "meta-table-test", new DevToolsApiBuilderServlet()).withPort(port);
			tomcat.start();
			// tomcat.awaitServer();
		}
	}
	
	@AfterClass
	public static void tearDown() throws LifecycleException {
		if (tomcat != null) {
			// tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		builder = null;
		tomcat = null;
		rbObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(rbObj);
	}
	
	@Test
	public void mapTesting() {
		assertEquals("{}", ConvertJSON.fromMap(rbObj.namespaceTree()));
		builder.setupRESTBuilder(rbObj, "dev.");
		assertNotEquals("{}", ConvertJSON.fromMap(rbObj.namespaceTree()));
	}
	
	//@Test
	public void api_tree_GETTest() {
		builder.setupRESTBuilder(rbObj, builder, "/meta-test/");
		assertNotEquals("{}", ConvertJSON.fromMap(rbObj.namespaceTree()));
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		response = RequestHttp.get(testAddress + "/api/tree/api_tree_GET", paramsMap);
		assertNotNull(response);
		assertNotNull(responseMap = response.toMap());
		assertNull("No key was supplied", responseMap.get("error"));
		assertNotNull("No key was supplied", responseMap.get("data"));
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
