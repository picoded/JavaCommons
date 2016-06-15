package picodedTests.servletUtils;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.*;
import java.net.Socket;
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
	protected FileServlet fileServlet = null;
	protected EmbeddedServlet tomcat = null;
	protected String testAddress = "http://127.0.0.1:";
	protected static int port = 15000;
	protected static boolean portAvailableCalled = false;
	
	@Before
	public void serverSetUp() throws LifecycleException, IOException {
		if (tomcat == null) {
			if (!portAvailableCalled) {
				while (!portAvailableCalled) {
					available(port);
					if (!portAvailableCalled) {
						port += 100;
					}
				}
				testAddress = "http://127.0.0.1:" + port;
			}
			File rootFolder = new File("./test-files/test-specific/servletUtils/FileServlet");
			File fileFolder = new File("./test-files/test-specific/servletUtils/FileServlet/WEB-INF/innerFiles");
			
			fileServlet = new FileServlet(fileFolder);
			
			tomcat = new EmbeddedServlet("", rootFolder).withPort(port).withServlet("/files/*", "fileServer", fileServlet);
			
			tomcat.start();
		}
	}
	
	@After
	public void serverTearDown() throws LifecycleException, IOException {
		if (tomcat != null) {
			//tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		tomcat = null;
		
		requester = null;
		response = null;
		responseString = null;
	}
	
	// Tests 
	//--------------------------------------------------------------------------
	RequestHttp requester = null;
	ResponseHttp response = null;
	String responseString = null;
	
	@Test
	public void helloTest() {
		assertNotNull(response = RequestHttp.get(testAddress + "/files/hello.html"));
		assertEquals("world", response.toString());
	}
	
	@SuppressWarnings("try")
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
