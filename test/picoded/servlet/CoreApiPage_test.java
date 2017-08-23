package picoded.servlet;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.BiFunction;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;
import picoded.core.conv.*;
import picoded.core.common.*;
import picoded.web.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.util.EchoApi;

public class CoreApiPage_test {
	
	//
	// The test folders to use
	//
	File testFolder = new File("./test/files/servlet/CoreApiPage/");
	
	//
	// The test vars to use
	//
	int testPort = 0; //Test port to use
	EmbeddedServlet testServlet = null; //Test servlet to use
	
	//
	// Standard setup and teardown
	//
	@Before
	public void setUp() {
		testPort = TestConfig.issuePortNumber();
		testServlet = null;
	}
	
	@After
	public void tearDown() throws Exception {
		if (testServlet != null) {
			testServlet.close();
			testServlet = null;
		}
	}
	
	//
	// Simple hello world servlet
	//
	public static class SimpleEcho extends CoreApiPage {
		
		// Message to put
		@Override
		public void apiBuilderSetup(ApiBuilder api) {
			api.put("echo", EchoApi.SimpleEcho);
		}
	}
	
	//
	// Hello world echo assertion assertion !
	//
	@Test
	public void simpleHelloWorldTest() {
		assertNotNull(testServlet = new EmbeddedServlet(testPort, new SimpleEcho()));
		String testUrl = "http://localhost:" + testPort + "/api/echo/";
		
		String msg = "hello world"; // The test message
		
		// The expected result
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("echo", msg); 
		String testString = ConvertJSON.fromObject(expected, true).trim();
		
		// Request parmeters
		Map<String, String[]> args = new HashMap<String, String[]>();
		args.put("echo", new String[] { msg });
		
		// Actual API calls
		assertEquals(testString, RequestHttp.get(testUrl, args).toString().trim());
		assertEquals(testString, RequestHttp.post(testUrl, args).toString().trim());
		Map<String, File[]> blankFileMap = new HashMap<String, File[]>();
		assertEquals(testString, RequestHttp.post(testUrl, args, blankFileMap).toString().trim());
	}
	
	//
	// File upload assertion
	//
	public static class SimpleFile extends CoreApiPage {
		
		// Result fetching
		public static BiFunction<ApiRequest, ApiResponse, ApiResponse> testFileEcho = (req, res) -> {
			assertEquals("hello", req.getString("msg"));
			
			Object fileObj = null;
			assertNotNull(fileObj = req.get("file"));
			
			assertTrue(fileObj instanceof RequestFileArray);
			RequestFileArray fileArr = (RequestFileArray) fileObj;
			
			assertEquals(1, fileArr.size());
			assertEquals("hello world", fileArr.get(0).trim());
			
			return res;
		};
		
		// Message to put
		@Override
		public void apiBuilderSetup(ApiBuilder api) {
			api.put("file", testFileEcho);
		}
	}
	
	//
	// File upload, with server side assertion
	//
	@Test
	public void simpleFileTest() {
		assertNotNull(testServlet = new EmbeddedServlet(testPort, new SimpleFile()));
		String testUrl = "http://localhost:" + testPort + "/api/file/";
		
		Map<String, String[]> args = new HashMap<String, String[]>();
		args.put("msg", new String[] { "hello" });
		
		Map<String, File[]> fileMap = new HashMap<String, File[]>();
		fileMap.put("file", new File[] { new File(testFolder, "hello.txt") });
		
		// Check that is no error
		assertEquals("{ }", RequestHttp.post(testUrl, args, fileMap).toString().trim());
	}
	
	//
	// File upload with content client side assertion
	//
	@Test
	public void simpleEchoTest() {
		// Server
		assertNotNull(testServlet = new EmbeddedServlet(testPort, new SimpleEcho()));
		String testUrl = "http://localhost:" + testPort + "/api/echo/";
		
		// Arguments
		Map<String, String[]> args = new HashMap<String, String[]>();
		args.put("msg", new String[] { "hello" });
		Map<String, File[]> fileMap = new HashMap<String, File[]>();
		fileMap.put("file", new File[] { new File(testFolder, "hello.txt") });
		
		// Expected
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("msg", "hello");
		List<String> expectedFile = new ArrayList<String>();
		expectedFile.add("hello world\n");
		expected.put("file", expectedFile);
		
		// Check that is no error
		assertEquals(expected,
			ConvertJSON.toMap(RequestHttp.post(testUrl, args, fileMap).toString().trim()));
	}
	
	//
	// Invalid endpoint testing
	//
	@Test
	public void envalidEndpointTest() {
		// Server
		assertNotNull(testServlet = new EmbeddedServlet(testPort, new SimpleEcho()));
		String testUrl = "http://localhost:" + testPort + "/api/this-does-not-exist/";
		
		// Check that there IS an error
		assertNotNull(	ConvertJSON.toMap(RequestHttp.get(testUrl).toString().trim()).get("ERROR") );
	}
	
}
