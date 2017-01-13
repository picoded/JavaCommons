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
	}
	
}