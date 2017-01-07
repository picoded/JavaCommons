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
	File helloWorldHtml = new File(testCollection, "helloWorldHtml");
	File helloWorldJava = new File(testCollection, "helloWorldJava");
	File helloWorldJWar = new File(testCollection, "helloWorldJWar/test.war");
	
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
	// Sanity testing other modes?
	//
	
	@Test
	public void helloWorldHtml() {
		assertTrue( helloWorldHtml.isDirectory() );
		assertNotNull(testServlet = new EmbeddedServlet(testPort, helloWorldHtml));
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/index.html").toString().trim() );
	}
	
	@Test
	public void helloWorldJava() {
		assertTrue( helloWorldJava.isDirectory() );
		assertNotNull(testServlet = new EmbeddedServlet(testPort, helloWorldJava));
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-html.html").toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-java").toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-jsp.jsp").toString().trim() );
	}
	
	@Test
	public void helloWorldJWar() {
		assertTrue( helloWorldJWar.isFile() );
		assertNotNull(testServlet = new EmbeddedServlet(testPort, helloWorldJWar));
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-html.html").toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-java").toString().trim() );
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:"+testPort+"/test-jsp.jsp").toString().trim() );
	}
	
}
