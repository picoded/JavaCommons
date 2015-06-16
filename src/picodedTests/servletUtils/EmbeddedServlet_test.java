package picodedTests.servletUtils;

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

import java.util.Random;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.RandomUtils;

public class EmbeddedServlet_test
{
	EmbeddedServlet tomcat = null;
	
	@Before
	public void setUp(){
		File context = new File("./test-files/test-specific/embeddedTomcat");
		
		tomcat = new EmbeddedServlet("/app", context)
		.withPort(15000)
		.withServlet("/date", "datePrintServlet", "picodedTests.servletUtils.EmbeddedTestServlet");
	}
	
	@Test
	public void TestServerStartup()throws LifecycleException, IOException{
		
		tomcat.start();
		
		String getResult = doGetTest();
		assertEquals(getResult, "true");
		
		String postResult = doPostTest();
		assertEquals(postResult, "true");
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