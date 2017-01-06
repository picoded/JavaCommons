package picoded.servlet.util;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import picoded.TestConfig;
import picoded.conv.*;
import picoded.set.*;
import picoded.web.*;

///
/// Test the EmbeddedServlet implmentation
///
public class EmbeddedServlet_test {
	
	//
	// The test folders to use
	//
	File testCollection = new File("test-files/test-specific/servlet/util/EmbeddedServlet/");
	File helloWorldDir = new File(testCollection, "HelloWorld");
	
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
	}
	
	@After
	public void tearDown() {
		if(testServlet != null) {
			testServlet.close();
			testServlet = null;
		}
	}
	
	//
	// Test them all !
	//
	
	@Test
	public void helloWorld() {
		assertTrue( helloWorldDir.isDirectory() );
		assertNotNull(testServlet = new EmbeddedServlet(testPort, helloWorldDir));
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/index.html").toString().trim() );
	}
	
}
