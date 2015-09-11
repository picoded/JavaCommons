package picodedTests.webUtils;

import org.junit.*;

import static org.junit.Assert.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import picoded.conv.GUID;
import picoded.conv.StringEscape;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.webUtils.*;
import picoded.FunctionalInterface.*;

import java.util.Random;
import java.io.IOException;
import java.util.Date;

public class RequestHttp_test {
	
	public String httpBinURL() {
		return "http://httptest.picoded-dev.com:15001/";
	}
	
	public String echoWebSocketURL() {
		return "wss://echo.websocket.org";
	}
	
	ResponseHttp res = null;
	RequestHttp reqObj = null;
	
	@Before
	public void setUp(){
		reqObj = null;
		res = null;
		//wsObj = new RequestHttp();
	}
	
	@Test
	public void nullTest() {
		// Does nothing, but allow the test to be valid while everything else is 'silenced'
		assertNull(null);
	}
	
	@Test @SuppressWarnings("unchecked")
	public void GET_basicTest() throws IOException {
		
		Map<String,Object> resMap = null;
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/get?hello=world") );
		assertNotNull( resMap = res.toMap() );
		
		assertEquals( 200, res.statusCode() );
		assertEquals( "world", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
	}
	
	@Test @SuppressWarnings("unchecked")
	public void GET_parametersTest() throws IOException {
		
		Map<String,Object> resMap = null;
		
		Map<String,String[]> reqMap = new HashMap<String,String[]>();
		
		// Testing standard
		//------------------------
		reqMap.put( "hello", new String[] { "world" } );
		assertNotNull( res = RequestHttp.get( httpBinURL()+"/get", reqMap ) );
		assertNotNull( resMap = res.toMap() );
		assertEquals( 200, res.statusCode() );
		assertEquals( "world", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
		
		// With existing '?'
		//------------------------
		reqMap.put( "hello", new String[] { "who" } );
		assertNotNull( res = RequestHttp.get( httpBinURL()+"/get?", reqMap ) );
		assertNotNull( resMap = res.toMap() );
		assertEquals( 200, res.statusCode() );
		assertEquals( "who", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
		
		// With existing '?a=b'
		//------------------------
		reqMap.put( "hello", new String[] { "motto" } );
		assertNotNull( res = RequestHttp.get( httpBinURL()+"/get?no=way", reqMap ) );
		assertNotNull( resMap = res.toMap() );
		assertEquals( 200, res.statusCode() );
		assertEquals( "motto", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
		assertEquals( "way", ((Map<String,Object>)(resMap.get("args"))).get("no") );
	}
	
	
	////////////////////////////////////////////////////////////////
	//
	// There is a bug in StringEscape.encodeURI()
	// Passing in a json array will not be cneoded properly
	//
	/////////////////////////////////////////////////////////////////
	@Test
	public void GET_parameters_URIEncoding_test() throws IOException {	
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("headers", new String[]{"[\"_oid\", \"_name\"]"});
		ResponseHttp response = null;
		boolean failed = false;
		try{
			response = RequestHttp.get(httpBinURL(), paramsMap, null, null);
		}catch(Exception e){
			failed = true;
		}
		
		assertNull(response);
		assertTrue(failed);
	}
	
	@Test @SuppressWarnings("unchecked")
	public void GET_headerTest() throws IOException {
		ResponseHttp resWithHeaders = null;
		Map<String, String[]> headerMap = null;
		
		headerMap = new HashMap<String, String[]>();
		headerMap.put("Testkey", new String[] { "testValue"} );
		
		assertNotNull(resWithHeaders = RequestHttp.get(httpBinURL()+"/headers", null, null, headerMap));
		
		assertEquals( "testValue", ((Map<String,Object>)(resWithHeaders.toMap().get("headers"))).get("Testkey") );
		//assertEquals( "testValue", resWithHeaders.getHeaders().get("testKey") );
	}
	
	@Test 
	public void GET_statusTest() throws IOException {
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/status/418") );
		assertEquals( 418, res.statusCode() );
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/status/500") );
		assertEquals( 500, res.statusCode() );
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/status/250") );
		assertEquals( 250, res.statusCode() );
	}
	
	@Test @SuppressWarnings("unchecked")
	public void GET_cookiesTest() throws IOException {
		Map<String,Object> resMap = null;
		Map<String,String[]> reqMap = new HashMap<String,String[]>();
		
		reqMap.put("cookie", new String[] {"in the jar"} );
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/cookies", null, reqMap, null) );
		assertEquals( 200, res.statusCode() );
		assertNotNull( resMap = res.toMap() );
		assertEquals( resMap.toString(), "in the jar", ((Map<String,Object>)(resMap.get("cookies"))).get("cookie") );
	}
	
	@Test @SuppressWarnings("unchecked")
	public void POST_basicTest() throws IOException {
		
		Map<String,Object> resMap = null;
		Map<String,String[]> postData = new HashMap<String,String[]>();
		postData.put("hello", new String[] { "world" });
		
		assertNotNull( res = RequestHttp.post(httpBinURL()+"/post", postData) );
		assertNotNull( resMap = res.toMap() );
		assertEquals( "world", ((Map<String,Object>)(resMap.get("form"))).get("hello") );
		assertEquals( 200, res.statusCode() );
		
		res.waitForCompletedRequest();
	}
	
	@Test
	public void WEBSOCKET_echoTest() {
		assertNotNull( res = RequestHttp.websocket( echoWebSocketURL() ) );
		
		assertEquals( "hello", res.sendAndWait("hello") );
		assertEquals( "new", res.sendAndWait("new") );
		assertEquals( "world", res.sendAndWait("world") );
	}
	
//	@Test @SuppressWarnings("unchecked")
//	public void GET_stream100() throws IOException {
//		System.out.println("Runnin GET_stream20");
//		
//		
//		assertNotNull( res = RequestHttp.get(httpBinURL()+"/stream/100") );
//		assertEquals( 200, res.statusCode() );
//		
//		System.out.println(res.toString());
//		
//		res.waitForCompletedRequest();
//	}

}
