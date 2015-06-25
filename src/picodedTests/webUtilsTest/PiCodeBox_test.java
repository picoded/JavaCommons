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
import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.servletUtils.EmbeddedServlet;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;
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
	
	Map<String, String> receivedCookies = null;
	
	@Before
	public void setUp(){
		File context = new File("./test-files/tmp");
		
		piCodeBox = new PiCodeBox();
		piCodeBox._contextPath = "./text-files/tmp/";
		
		tomcat = new EmbeddedServlet("", context)
		.withPort(15000)
		.withServlet("/public", "piCodeBoxProxy", piCodeBox);
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException, IOException{
		
		tomcat.start();
		
		httpRequester = new PiHttpRequester();
		
		File WEBINFFile = new File("./test-files/tmp/WEB-INF");
		WEBINFFile.mkdir();
		
		DoLoginGet();
		DoPostTest();
		DoFinalGet();
		
//		String result = DoPostTest();
//		assertEquals("true", result);
		
//		DoGetTest();
	}
	
	private void DoLoginGet(){
		HashMap<String, String> getParams = new HashMap<String, String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		
		getParams.put("user", "Sam");
		
		PiHttpResponse piResp = httpRequester.sendGetRequest("http://localhost:15000", "public", getParams, null, cookies);
		
		try
		{
			String response = IOUtils.toString(piResp.getResponseBody());
			System.out.println("Login Get Resp: "+response);
		} catch (IOException ex){
			System.out.println(ex.getMessage());
		}
	}
	
	private String DoPostTest(){
		HashMap<String, String> postParams = new HashMap<String, String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		
		postParams.put("user", "Sam");
		postParams.put("password", "1234"); 
		
		PiHttpResponse piResp = httpRequester.sendPostRequest("http://localhost:15000", "public", postParams, null, cookies);
		
		try
		{
			receivedCookies = piResp.getCookies();
			
			
			
			
			String response = IOUtils.toString(piResp.getResponseBody());
			
			Map<String,Object> rm = ConvertJSON.toMap(response);
			GenericConvertMap<String, Object> gm = ProxyGenericConvertMap.ensureGenericConvertMap(rm);
			//String[] responseSplit = response.split(":");
			
			System.out.println(gm.getString("login-status"));
			
			return (gm.getString("login-status"));
		} catch (IOException ex){
			System.out.println(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}
	
	private void DoFinalGet(){
		HashMap<String, String> getParams = new HashMap<String, String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		
		getParams.put("user", "Sam");
		
		PiHttpResponse piResp = httpRequester.sendGetRequest("http://localhost:15000", "public", getParams, null, receivedCookies);
		
		try
		{
			String response = IOUtils.toString(piResp.getResponseBody());
			System.out.println("Final Get Resp: "+response);
		} catch (IOException ex){
			System.out.println(ex.getMessage());
		}
	}

	private void DoGetTest(){
		HashMap<String, String> getParams = new HashMap<String, String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		
		getParams.put("user", "Sam");
		
		PiHttpResponse piResp = httpRequester.sendGetRequest("http://localhost:15000", "public", getParams, null, cookies);
		
		try
		{
			Map<String, String> respCookies = piResp.getCookies();
			
			for(String key : respCookies.keySet()){
				System.out.println("Got cookie name: "+key+ " and value: "+respCookies.get(key));
			}

			//do second get test
			HashMap<String, String> getParamsNew = new HashMap<String, String>();
			HashMap<String, String> newCookies = new HashMap<String, String>();
			getParams.put("user", "Sam");

			PiHttpResponse secondGetResp = httpRequester.sendGetRequest("http://localhost:15000", "public", getParamsNew, null, respCookies);
			String secondGetRespBody = IOUtils.toString(secondGetResp.getResponseBody());
			System.out.println("SECOND GET RESP BODY: "+secondGetRespBody);
			
			System.out.println(picoded.conv.ConvertJSON.fromMap(secondGetResp.getCookies()));
			
			
			String response = IOUtils.toString(piResp.getResponseBody());
			System.out.println("Get Resp: "+response);
		} catch (IOException ex){
			System.out.println(ex.getMessage());
		}
	}
}