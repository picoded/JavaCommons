package picoded.servlet.util;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

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
	// The test servlet
	//
	EmbeddedServlet testServlet = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		testServlet = null;
	}
	
	@Test
	public void helloWorld() {
		assertTrue( helloWorldDir.isDirectory() );
		assertNotNull(testServlet = new EmbeddedServlet(helloWorldDir));
		//testServlet.await();
		
		assertEquals( "<h1>Hello World</h1>", RequestHttp.get("http://localhost:8080/index.html").toString().trim() );
	}
	
}
