package picodedTests.codeBoxTest;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.junit.Before;
import org.junit.Test;

import picoded.codeBox.CodeBoxLoginServlet;
import picoded.servletUtils.EmbeddedServlet;
import picoded.webUtils.PiHttpRequester;

public class CodeBoxLoginServlet_test {

	EmbeddedServlet tomcat = null;
	PiHttpRequester httpRequester;
	CodeBoxLoginServlet loginServlet;
	
	Map<String, String> cookies = null;
	
	@Before
	public void setUp(){
		//File context = new File("./test-files/tmp");
		File context = new File("./test-files/picodebox");
		
		loginServlet = new CodeBoxLoginServlet();
		
		tomcat = new EmbeddedServlet("", context)
		.withPort(15000)
		.withServlet("/login", "loginServlet", loginServlet);
	}
	
	@Test
	public void TestLoginServletProcess()throws LifecycleException, IOException{
		tomcat.start();
		
		httpRequester = new PiHttpRequester();
		
		File WEBINFFile = new File("./test-files/tmp/WEB-INF");
		WEBINFFile.mkdir();
		
		tomcat.awaitServer();
	}
}