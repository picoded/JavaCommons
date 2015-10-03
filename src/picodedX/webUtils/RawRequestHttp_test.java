package picodedTests.webUtils;

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
import picoded.webUtils.*;
import picoded.FunctionalInterface.*;

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

public class RawRequestHttp_test {
	
	public String httpBinURL() {
		return "http://httpbin.org";
	}
	
	public String echoWebSocketURL() {
		return "wss://echo.websocket.org";
	}
	
	RawRequestHttp reqObj = null;
	
	@Before
	public void setUp() {
		reqObj = null;
		//wsObj = new RequestHttp();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void GET_basicTest() throws IOException {
		
		// ResponseHttp res = null;
		Map<String, Object> resMap = null;
		
		assertNotNull(reqObj = (new RawRequestHttp(httpBinURL() + "/get?hello=world")).connect());
		assertNotNull(resMap = reqObj.toMap());
		
		assertEquals(200, reqObj.statusCode());
		assertEquals("world", ((Map<String, Object>) (resMap.get("args"))).get("hello"));
		
		reqObj.disconnect();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void GET_headerTest() throws IOException {
		
		Map<String, List<String>> headerMap = null;
		List<String> subList = new ArrayList<String>();
		
		subList.add("testValue");
		
		headerMap = new HashMap<String, List<String>>();
		headerMap.put("Testkey", subList);
		
		assertNotNull(reqObj = (new RawRequestHttp(httpBinURL() + "/get?hello=world")).setHeaderMap(headerMap).connect());
		
		assertEquals("testValue", ((Map<String, String>) (reqObj.toMap().get("headers"))).get("Testkey"));
		//assertEquals( "testValue", resWithHeaders.getHeaders().get("testKey") );
	}
	
	@Test
	public void GET_statusTest() throws IOException {
		assertNotNull(reqObj = (new RawRequestHttp(httpBinURL() + "/status/418")).connect());
		assertEquals(418, reqObj.statusCode());
	}
	
	// 	// @Test @SuppressWarnings("unchecked")
	// 	// public void GET_stream100() throws IOException {
	// 	// 	System.out.println("Runnin GET_stream20");
	// 	// 	ResponseHttp res = null;
	// 	// 	
	// 	// 	assertNotNull( res = RequestHttp.get(httpBinURL()+"/stream/100") );
	// 	// 	assertEquals( 200, res.statusCode() );
	// 	// 	
	// 	// 	System.out.println(res.toString());
	// 	// 	
	// 	// 	res.waitForCompletedRequest();
	// 	// }
	// 	
	// 	@Test @SuppressWarnings("unchecked")
	// 	public void POST_basicTest() throws IOException {
	// 		ResponseHttp res = null;
	// 		Map<String,Object> resMap = null;
	// 		Map<String,String[]> postData = new HashMap<String,String[]>();
	// 		postData.put("hello", new String[] { "world" });
	// 		
	// 		assertNotNull( res = RequestHttp.post(httpBinURL()+"/post", postData) );
	// 		assertEquals( 200, res.statusCode() );
	// 		assertNotNull( resMap = res.toMap() );
	// 		assertEquals( "world", ((Map<String,Object>)(resMap.get("form"))).get("hello") );
	// 		
	// 		res.waitForCompletedRequest();
	// 	}
	// 	
	// 	@Test
	// 	public void WEBSOCKET_echoTest() {
	// 		
	// 		assertNotNull( reqObj = new RequestHttp( echoWebSocketURL() ) );
	// 		reqObj.websocketConnect();
	// 		
	// 		assertEquals( "hello", reqObj.sendAndWait("hello") );
	// 		assertEquals( "new", reqObj.sendAndWait("new") );
	// 		assertEquals( "world", reqObj.sendAndWait("world") );
	// 	}
	
}
