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


public class CorePage_test {
	
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
	public static class SimpleHelloWorld extends CorePage {
		// Message to put
		@Override
		public boolean doRequest(Map<String, Object> templateData) throws Exception {
			templateData.put("OutputString", "<h1>Hello World</h1>");
			return true;
		}
		
		/// Does the output processing, this is after do(Post/Get/Put/Delete)Request
		@Override
		public boolean outputRequest(Map<String, Object> templateData, PrintWriter output)
			throws Exception {
				
			output.println("hello");
			
			// /// Does string output if parameter is set
			// Object outputString = templateData.get("OutputString");
			// output.print(outputString);
			return true;
		}
	}
	
	// Setup for resuse
	public void helloWorldAssert(String testUrl) {
		assertNotNull(testServlet);
		//testServlet.await();
		
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get(testUrl, null).toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.post(testUrl, null).toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.put(testUrl, null).toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.delete(testUrl, null).toString().trim() );
	}
	
	@Test
	public void simpleHelloWorldTest() {
		assertNotNull( testServlet = new EmbeddedServlet(testPort, new SimpleHelloWorld()) );
		helloWorldAssert( "http://localhost:"+testPort+"/test/" );
	}
	
}
