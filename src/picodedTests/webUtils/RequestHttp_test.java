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
	
	RequestHttp reqObj = null;
	
	@Before
	public void setUp(){
		reqObj = new RequestHttp();
	}
	
	@Test @SuppressWarnings("unchecked")
	public void GET_basicTest() throws IOException {
		ResponseHttp res = null;
		Map<String,Object> resMap = null;
		
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/get?hello=world") );
		assertNotNull( resMap = res.toMap() );
		
		assertEquals( 200, res.statusCode() );
		assertEquals( "world", ((Map<String,Object>)(resMap.get("args"))).get("hello") );
	}
	
	@Test 
	public void GET_statusTest() throws IOException {
		ResponseHttp res = null;
		assertNotNull( res = RequestHttp.get(httpBinURL()+"/status/418") );
		assertEquals( 418, res.statusCode() );
	}
}
