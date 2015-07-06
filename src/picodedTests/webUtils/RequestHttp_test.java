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


public class RequestHttp_test {
	
	public String httpBinURL() {
		return "http://httpbin.org";
	}
	
	public String echoWebSocketURL() {
		return "wss://echo.websocket.org";
	}
	
	RequestHttp reqObj = null;
	
	@Before
	public void setUp(){
		reqObj = null;
		//wsObj = new RequestHttp();
	}
	
	//@Test @SuppressWarnings("unchecked")
//	public void GET_basicTest() throws IOException {
//		System.out.println("Runnin GET_basicTest");
//		ResponseHttp res = null;
//		Map<String,Object> resMap = null;
//		
//		assertNotNull( res = RequestHttp.get(httpBinURL()+"/get?hello=world") );
//		assertNotNull( resMap = res.toMap() );
//		
//		assertEquals( 200, res.statusCode() );
//		assertEquals( "world", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
//	}
	
//	@Test @SuppressWarnings("unchecked")
//	public void GET_headerTest() throws IOException {
//		ResponseHttp resWithHeaders = null;
//		Map<String, String> headerMap = null;
//		
//		headerMap = new HashMap<String, String>();
//		headerMap.put("Testkey", "testValue");
//		
//		assertNotNull(resWithHeaders = RequestHttp.get(httpBinURL()+"/headers", headerMap));
//		System.out.println(resWithHeaders.getHeaders());
//		System.out.println(resWithHeaders.toString());
//
//		assertEquals( "testValue", ((Map<String,Object>)(resWithHeaders.toMap().get("headers"))).get("Testkey") );
//		//assertEquals( "testValue", resWithHeaders.getHeaders().get("testKey") );
//	}
	
//	@Test 
	public void GET_statusTest() throws IOException {
		System.out.println("Running GET_statusTest");
		ResponseHttp res = null;
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/status/418") );
		assertEquals( 418, res.statusCode() );
	}

	@Test @SuppressWarnings("unchecked")
	public void GET_stream20() throws IOException {
		System.out.println("Runnin GET_stream20");
		ResponseHttp res = null;
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/stream/100") );
		assertEquals( 200, res.statusCode() );
		
		res.waitForCompletedRequest();

		//System.out.println(res.toString());
		//assertEquals( "world", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
	}
	
//	@Test @SuppressWarnings("unchecked")
//	public void POST_basicTest() throws IOException {
//		System.out.println("Running POST_basicTest");
//		ResponseHttp res = null;
//		Map<String,Object> resMap = null;
//		
//		Map<String,String[]> postData = new HashMap<String,String[]>();
//		postData.put("hello", new String[] { "world" });
//		
//		assertNotNull( res = RequestHttp.post(httpBinURL()+"/post", postData) );
//		System.out.println("sent post Request");
//		assertNotNull( resMap = res.toMap() );
//		System.out.println("first check");
//		assertEquals( 200, res.statusCode() );
//		System.out.println("second Check");
//		//assertEquals( "", res.toString() );
//		assertEquals( "world", ((Map<String,Object>)(resMap.get("form"))).get("hello") );
//		System.out.println("last Check");
//	}
	
//	@Test
	public void WEBSOCKET_echoTest() {
		System.out.println("Running WEBSOCKET_echoTest");
		assertNotNull( reqObj = new RequestHttp( echoWebSocketURL() ) );
		reqObj.websocketConnect();
		
		assertEquals( "hello", reqObj.sendAndWait("hello") );
		assertEquals( "new", reqObj.sendAndWait("new") );
		assertEquals( "world", reqObj.sendAndWait("world") );
	}
	
}
