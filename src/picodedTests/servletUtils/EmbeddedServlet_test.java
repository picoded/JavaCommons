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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class EmbeddedServlet_test
{
	EmbeddedServlet tomcat = null;
	
	@Before
	public void setUp(){
		File context = new File("./test-files/test-specific/embeddedTomcat");
		
		tomcat = new EmbeddedServlet("/app", context)
		.withPort(15000)
		.withServlet("/date", "datePrintServlet", 
		new HttpServlet() {
			private static final long serialVersionUID = 1L;
		 
			@Override
			protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
				String getValue = req.getParameter("getValue");
				//System.out.println("Get Request param : " +getValue);
				resp.getWriter().append(getValue);
			}

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
				String postValue = req.getParameter("postValue");
				//System.out.println("Post Request param : " +postValue);
				resp.getWriter().append(postValue);
			}
		});
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException, IOException{
		
		tomcat.start();
		
//		String getResult = doGetTest();
//		assertEquals(getResult, "true");
//		
//		String postResult = doPostTest();
//		assertEquals(postResult, "true");
		
		String newGetResult = doGetTestWithHttpRequesterClass();
		assertEquals(newGetResult, "true");
	}
	
	private String doGetTestWithHttpRequesterClass() {
//		HttpClient client = HttpClients.createDefault();
//		HttpGet httpGet = new HttpGet("http://localhost:15000/app/date?getValue=true");
		
		HashMap<String, String> cookies = new HashMap<String, String>();
		cookies.put("cookie1", "cookie1Value");
		
		PiHttpRequester requester = new PiHttpRequester();
		PiHttpResponse resp = requester.sendRequest("http://localhost:15000/app/date?getValue=true", HttpRequestType.TYPE_GET, null, cookies, "");
		System.out.println("Requester returned with response");
		try {
//			HttpResponse resp = client.execute(httpGet);
//			
//			
//			HttpEntity entity = resp.getEntity();
//			InputStream respStream = entity.getContent();
//			InputStreamReader reader = new InputStreamReader(respStream);
//			char[] buffer = new char[10];
//			reader.read(buffer);
//			String returnVal = String.valueOf(buffer).trim();
//			return returnVal;
			
			InputStream respStream = resp.getResponseBody();
			InputStreamReader reader = new InputStreamReader(respStream);
			char[] buffer = new char[10];
			reader.read(buffer);
			String returnVal = String.valueOf(buffer).trim();
			System.out.println("New get test returned with result: " +returnVal);
			return returnVal;
		} catch (ClientProtocolException ex) {
			
		} catch (IOException ex) {
			
		}
		
		return null;
	}
	
	private String doGetTest() throws IOException{
		URL testURL = null;
		URLConnection conn = null;
		InputStream response = null;
		String urlString = "http://localhost:"+ tomcat.getPort() +"/app/date?getValue=true";
		
		try{
			
			testURL = new URL(urlString);
			conn = testURL.openConnection();
			response = conn.getInputStream();
			
		} catch(MalformedURLException ex){
			System.out.println("MalformedURL: " +ex.getMessage());
		} catch (IOException ex){
			System.out.println("IOException: " +ex.getMessage());
		}
		
		InputStreamReader inputReader = new InputStreamReader(response);
		char[] buffer = new char[10];
		inputReader.read(buffer);
		String returnVal = String.valueOf(buffer).trim();
		
		return returnVal;
	}
	
	private String doPostTest() throws IOException{
		URL testURL = null;
		URLConnection conn = null;
		InputStream response = null;
		String urlString = "http://localhost:"+ tomcat.getPort() +"/app/date";
		
		try{
			
			testURL = new URL(urlString); //create url
			conn = testURL.openConnection(); //open connection
			conn.setDoOutput(true);
			
			String query = "postValue=true";
			OutputStream output = conn.getOutputStream();
			output.write(query.getBytes());
			
			response = conn.getInputStream(); //
			
		} catch(MalformedURLException ex){
			System.out.println("MalformedURL: " +ex.getMessage());
		} catch (IOException ex){
			System.out.println("IOException: " +ex.getMessage());
		}
		
		InputStreamReader inputReader = new InputStreamReader(response);
		char[] buffer = new char[10];
		inputReader.read(buffer);
		
		String returnVal = String.valueOf(buffer).trim();
		
		return returnVal;
	}
}