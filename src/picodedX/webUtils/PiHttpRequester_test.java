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
import picoded.webUtils.PiHttpRequester;
import picoded.webUtils.PiHttpResponse;
import picoded.enums.HttpRequestType;

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

public class PiHttpRequester_test
{
	EmbeddedServlet tomcat = null;
	
	@Before
	public void setUp(){
		File context = new File("./test-files/test-specific/embeddedTomcat");
		
		tomcat = new EmbeddedServlet("/app", context)
		.withPort(15000)
		.withServlet("/public/*", "publicProxyServlet", 
		new HttpServlet() {
			private static final long serialVersionUID = 1L;
			 
			@Override
			protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
				String getParam = req.getParameter("getValue");
				System.out.println("Saw get parameter : "+getParam);
				resp.getWriter().append("GET");
			}

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
				javax.servlet.http.Cookie[] javaCookies = req.getCookies();
				if(javaCookies != null && javaCookies.length > 0){
					if(javaCookies[0].getValue().equals("postCookieValue")){
						//respond with a cookie
						resp.addCookie(new javax.servlet.http.Cookie("returnCookie", "returnCookieValue"));
					}
				} else {
					System.out.println("Java Cookies are null");
				}
				
				String postParam = req.getParameter("postValue");
				System.out.println("Saw post parameter : "+postParam);
				resp.getWriter().append("POST");
			}
			
			@Override
			protected void doPut(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
				String putParam = "";
				try
				{
					ServletInputStream putStream = req.getInputStream();
					String putStreamString = IOUtils.toString(putStream);
					String[] putStreamSplit = putStreamString.split("=");
					putParam = putStreamSplit[1];
				}catch(IOException ex) {
					System.out.println("Exception: " +ex.getMessage());
				}
				
				System.out.println("Saw put parameter : "+putParam);
				
				resp.getWriter().append("PUT");
			}
			
			@Override
			protected void doDelete(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
				resp.getWriter().append("DELETE");
			}
			});
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException, IOException{
		
		tomcat.start();
		
//		String newGetResult = doGetTest();
//		System.out.println("Finished new get test with result: " +newGetResult);
//		assertEquals(newGetResult, "GET");
		
		String newPostResult = doPostTest();
		System.out.println("Finished new post test with result: " +newPostResult);
		assertEquals(newPostResult, "POST");
		
//		String newPutResult = doPutTest();
//		System.out.println("Finished new put test with result: " +newPutResult);
//		assertEquals(newPutResult, "PUT");
//		
//		String newDeleteResult = doDeleteTest();
//		System.out.println("Finished new delete test with result: " +newDeleteResult);
//		assertEquals(newDeleteResult, "DELETE");
	}
	
	private String doGetTest() {
		HashMap<String, String> cookies = new HashMap<String, String>();
		cookies.put("cookie1", "cookie1Value");
		HashMap<String, String> getParams = new HashMap<String, String>();
		getParams.put("getValue", "true");
		
		PiHttpRequester requester = new PiHttpRequester();
		PiHttpResponse resp = requester.sendGetRequest("http://localhost:15000/app", "public", getParams, null, cookies);
		
		String returnVal = "";
		try{
			InputStream respStream = resp.getResponseBody();
			String value = IOUtils.toString(respStream);
			returnVal = String.valueOf(value).trim();
		} catch (Exception ex) {
			System.out.println("Exception reading inputstream: "+ex.getMessage());
		}
		
		return returnVal;
	}
	
	private String doPostTest() {
		HashMap<String, String> cookies = new HashMap<String, String>();
		cookies.put("postCookie", "postCookieValue");
		
		HashMap<String, String> postParams = new HashMap<String, String>();
		postParams.put("postValue", "true");
		
		PiHttpRequester requester = new PiHttpRequester();
		PiHttpResponse resp = requester.sendPostRequest("http://localhost:15000/app", "public", postParams, null, cookies);
		String returnVal = "";
		try {
			InputStream respStream = resp.getResponseBody();
			String value = IOUtils.toString(respStream);
			returnVal = String.valueOf(value).trim();
			
			Map<String, String> respCookies = resp.getCookies();
			if(respCookies != null){
				String cookieVal = respCookies.get("returnCookie");
				if(cookieVal.equals("returnCookieValue")){
					System.out.println("Cookie from post response found with value: "+cookieVal);
				} else {
					System.out.println("Cookie from post response NOT FOUND");
				}
			} else {
				System.out.println("Cookies from post response is null");
			}
			
		} catch (IOException ex) {
			System.out.println("Exception reading inputstream: "+ex.getMessage());
		}
		
		return returnVal;
	}
	
	private String doPutTest() {
		HashMap<String, String> putParams = new HashMap<String, String>();
		putParams.put("putValue", "true");
		
		PiHttpRequester requester = new PiHttpRequester();
		PiHttpResponse resp = requester.sendPutRequest("http://localhost:15000/app", "public/hi", putParams, null, null);
		String returnVal = "";
		try {
			InputStream respStream = resp.getResponseBody();
			String value = IOUtils.toString(respStream);
			returnVal = String.valueOf(value).trim();
		} catch (IOException ex) {
			System.out.println("Exception reading inputstream: "+ex.getMessage());
		}
		
		return returnVal;
	}
	
	private String doDeleteTest() {
		PiHttpRequester requester = new PiHttpRequester();
		PiHttpResponse resp = requester.sendDeleteRequest("http://localhost:15000/app", "public", null, null);
		String returnVal = "";
		try {
			InputStream respStream = resp.getResponseBody();
			String value = IOUtils.toString(respStream);
			returnVal = String.valueOf(value).trim();
		} catch (IOException ex) {
			System.out.println("Exception reading inputstream: "+ex.getMessage());
		}
		
		return returnVal;
	}
}
