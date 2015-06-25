package picodedTests.webUtilsTest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
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
import picoded.webUtils.PiHttpRequester;
import picoded.webUtils.PiHttpResponse;
import picoded.webUtils.HttpRequestType;

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
import javax.servlet.ServletInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.aspectj.weaver.ast.Var;

import picoded.webUtils.PiCodeBox;

public class PiCodeBox_test
{
	EmbeddedServlet tomcat = null;
	PiHttpRequester httpRequester;
	PiCodeBox piCodeBox;
	
	@Before
	public void setUp(){
		File context = new File("./test-files/test-specific/embeddedTomcat");
		
		piCodeBox = new PiCodeBox();
		
		tomcat = new EmbeddedServlet("/app", context)
		.withPort(15000)
		.withServlet("/public", "piCodeBoxProxy", piCodeBox);
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException, IOException{
		
		tomcat.start();
		
		httpRequester = new PiHttpRequester();
		
		DoGetTest();

	}
	
	private void DoGetTest(){
		HashMap<String, String> getParams = new HashMap<String, String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		
		getParams.put("user", "Sam");
		cookies.put("cookieName", "cookieValue");
		
		PiHttpResponse piResp = httpRequester.sendGetRequest("http://localhost:15000/app", "public", getParams, null, cookies);
		
		try
		{
			String response = IOUtils.toString(piResp.getResponseBody());
			System.out.println("Resp: "+response);
		} catch (IOException ex){
			System.out.println(ex.getMessage());
		}
		
	}
}