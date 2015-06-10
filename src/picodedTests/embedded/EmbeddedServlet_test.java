package picodedTests.embedded;

import org.junit.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;
import picoded.embedded.EmbeddedServlet;
import picoded.struct.CaseInsensitiveHashMap;

import java.util.Random;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.RandomUtils;

public class EmbeddedServlet_test
{
	@Before
	public void setUp(){
		
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException{
		File context = new File("./test-files/test-specific/embeddedTomcat");
		EmbeddedServlet servy = new EmbeddedServlet("/app", context)
									.withPort(15000)
									.withServlet("/date", "datePrintServlet", "picoded.embedded.DatePrintServlet");
		
		servy.start();
		servy.awaitServer();
	}
}