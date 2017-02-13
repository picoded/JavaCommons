package picoded.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;
import picoded.conv.*;
import picoded.set.*;
import picoded.web.*;
import picoded.RESTBuilder.*;

public class CoreApiPage_test {
	
	//
	// The test folders to use
	//
	File testFolder = new File("./test-files/test-specific/servlet/CoreApiPage/");
	
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
		if(testServlet != null) {
			testServlet.close();
			testServlet = null;
		}
	}
	
	//
	// Simple get test
	//
	public static class SimpleEcho extends CoreApiPage {
		
		// Result fetching
		public static RESTFunction testEcho = (req, res) -> {
			res.put("echo", req.get("echo"));
			return res;
		};
		
		// Message to put
		@Override
		public void restBuilderSetup(RESTBuilder rbObj) {
			rbObj.getNamespace("test").put(HttpRequestType.GET, testEcho);
			rbObj.getNamespace("test").put(HttpRequestType.POST, testEcho);
		}
	}
	
	//
	// Assertion
	//
	@Test
	public void simpleHelloWorldTest() {
		assertNotNull( testServlet = new EmbeddedServlet(testPort, new SimpleEcho()) );
		String testUrl = "http://localhost:"+testPort+"/api/test/";
		
		String msg = "hello";
		
		Map<String,Object> expected = new HashMap<String,Object>();
		expected.put("echo", "hello");
		String testString = ConvertJSON.fromObject(expected, true).trim();
		
		Map<String,String[]> args = new HashMap<String,String[]>();
		args.put("echo", new String[] { msg });
		
		assertEquals( testString, RequestHttp.get(testUrl, args).toString().trim() );
		assertEquals( testString, RequestHttp.post(testUrl, args).toString().trim() );
		
		Map<String,File[]> blankFileMap = new HashMap<String,File[]>();
		assertEquals( testString, RequestHttp.post(testUrl, args, blankFileMap).toString().trim() );
	}
	
	//
	// File upload assertion
	//
	public static class SimpleFile extends CoreApiPage {
		
		// Result fetching
		public static RESTFunction testEcho = (req, res) -> {
			assertEquals( "hello", req.getString("msg") );
			
			Object fileObj = null;
			assertNotNull( fileObj = req.get("file") );
			
			assertTrue( fileObj instanceof RequestFileMap );
			RequestFileMap fileMap = (RequestFileMap)fileObj;
			
			assertEquals( 1, fileMap.size() );
			for(String key : fileMap.keySet()) {
				assertEquals("hello.txt", key);
				assertEquals( "hello world", fileMap.get(key).trim() );
				//assertEquals("hello world")
			}
			return res;
		};
		
		// Message to put
		@Override
		public void restBuilderSetup(RESTBuilder rbObj) {
			rbObj.getNamespace("test").put(HttpRequestType.POST, testEcho);
		}
	}
	
	//
	// Assertion
	//
	@Test
	public void simpleFileTest() {
		assertNotNull( testServlet = new EmbeddedServlet(testPort, new SimpleFile()) );
		String testUrl = "http://localhost:"+testPort+"/api/test/";
		
		Map<String,String[]> args = new HashMap<String,String[]>();
		args.put("msg", new String[] { "hello" });
		
		Map<String,File[]> fileMap = new HashMap<String,File[]>();
		fileMap.put("file", new File[] { new File(testFolder,"hello.txt") });
		
		// Check that is no error
		assertEquals( "{ }", RequestHttp.post(testUrl, args, fileMap).toString().trim() );
	}
	
}
