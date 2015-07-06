package picodedTests.servletUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;
import picoded.servletUtils.EmbeddedServlet;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.servletUtils.*;
import picoded.webUtils.*;

import java.util.Random;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;

import picodedTests.webUtils.RequestHttp_test;

public class ProxyServlet_test extends RequestHttp_test {
	static EmbeddedServlet tomcat = null;
	static EmbeddedServlet ws_tomcat = null;
	
	@Override
	public String httpBinURL() {
		return "http://127.0.0.1:16000";
	}
	
	@Override
	public String echoWebSocketURL() {
		return "ws://127.0.0.1:16001";
	}
	
	@BeforeClass
	public static void serverSetUp() throws LifecycleException, IOException {
		File context = new File("./test-files/test-specific/embeddedTomcat");
		
		ProxyServlet proxy = new ProxyServlet();
		proxy.setProxyHost("httpbin.org");
		
		tomcat = new EmbeddedServlet("", context)
		.withPort(16000)
		.withServlet("/*", "publicProxyServlet", proxy);
		
		tomcat.start();
		
		ProxyServlet ws_proxy = new ProxyServlet();
		ws_proxy.setProxyHost("echo.websocket.org");
		
		ws_tomcat = new EmbeddedServlet("", context)
		.withPort(16001)
		.withServlet("/*", "publicProxyServlet", ws_proxy);
		
		ws_tomcat.start();
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if(tomcat != null) {
			//tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		if(ws_tomcat != null) {
			//tomcat.awaitServer(); //manual
			ws_tomcat.stop();
		}
		tomcat = null;
		ws_tomcat = null;
	}
	
	@Test
	public void TestServerStartup() throws LifecycleException, IOException{
		assertNotNull(tomcat);
	}
	
}
