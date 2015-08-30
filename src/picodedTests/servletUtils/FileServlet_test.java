package picodedTests.servletUtils;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.*;
import java.util.*;

import org.apache.catalina.LifecycleException;
import org.apache.commons.io.IOUtils;

import picoded.conv.*;
import picoded.struct.*;
import picoded.servletUtils.*;
import picoded.webUtils.*;

public class FileServlet_test {
	
	// Servlet setup
	//--------------------------------------------------------------------------
	protected static FileServlet fileServlet = null;
	protected static EmbeddedServlet tomcat = null;
	protected static String testAddress = "http://127.0.0.1:15000";
	
	@BeforeClass
	public static void serverSetUp() throws LifecycleException, IOException {
		if( tomcat == null ) {
			File rootFolder = new File("./test-files/test-specific/servletUtils/FileServlet");
			File fileFolder = new File("./test-files/test-specific/servletUtils/FileServlet/WEB-INF/innerFiles");
			
			fileServlet = new FileServlet(fileFolder);
			
			tomcat = new EmbeddedServlet("", rootFolder)
				.withPort(15000)
				.withServlet("/files/*",  "fileServer", fileServlet);
				
			tomcat.start();
		}
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if(tomcat != null) {
			//tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		tomcat = null;
	}
	
	// Tests 
	//--------------------------------------------------------------------------
	RequestHttp requester;
	ResponseHttp response;
	String responseString;
	
	@Test
	public void helloTest() {
		assertNotNull( response = RequestHttp.get( testAddress+"/files/hello.html" ) );
		assertEquals("world", response.toString());
	}
	
}
