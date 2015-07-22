package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.*;
import java.util.*;
import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.io.IOUtils;

import com.amazonaws.util.StringUtils;

import picoded.conv.ConvertJSON;
import picoded.servletUtils.EmbeddedServlet;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

import picoded.RESTBuilder.*;
import picoded.RESTBuilder.templates.*;
import picoded.webUtils.*;

public class AccountLogin_test {
	
	EmbeddedServlet tomcat = null;
	AccountLogin loginServlet;
	String testAddress = "http://127.0.0.1:5000";
	
	RequestHttp requester;
	ResponseHttp response;
	Map<String,Object> responseMap;
	
	@Before
	public void setUp() throws Exception {
		if( tomcat == null ) {
			File webInfFile = new File("./test-files/tmp/WEB-INF");
			webInfFile.mkdir();
			
			File context = new File("./test-files/tmp");
			loginServlet = new AccountLogin();
			
			tomcat = new EmbeddedServlet("", context)
			.withPort(15000)
			.withServlet("/api/account", "loginServlet", loginServlet);
			
			tomcat.start();
		}
	}
	
	@Test
	public void noLoginTest() throws IOException {
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login" ) );
		assertNotNull( responseMap = response.toMap() );
	}
	
	// 
	// @Test
	// public void TestLoginServletProcess()throws LifecycleException, IOException{
	// 	tomcat.start();
	// 	httpRequester = new PiHttpRequester();
	// 	
	// 	File WEBINFFile = new File("./test-files/tmp/WEB-INF");
	// 	WEBINFFile.mkdir();
	// 	
	// 	boolean initialLoginCheckShouldBeFalse = amILoggedIn();
	// 	assertEquals(false, initialLoginCheckShouldBeFalse);
	// 	
	// 	boolean didLoginSucceed = sendLoginParams();
	// 	assertEquals(true, didLoginSucceed);
	// 	
	// 	boolean secondLoginCheckShouldBeTrue = amILoggedIn();
	// 	assertEquals(true, secondLoginCheckShouldBeTrue);
	// }
	// 
	// private boolean amILoggedIn(){
	// 	HashMap<String, String> getParams = new HashMap<String, String>();
	// 	getParams.put("user", "testUser");
	// 	
	// 	PiHttpResponse piResp = httpRequester.sendGetRequest("http://localhost:15000", "login", getParams, null, cookies);
	// 	
	// 	try
	// 	{
	// 		String response = IOUtils.toString(piResp.getResponseBody());
	// 		Map<String,Object> rm = ConvertJSON.toMap(response);
	// 		GenericConvertMap<String, Object> gm = ProxyGenericConvertMap.ensureGenericConvertMap(rm);
	// 		
	// 		if(StringUtils.isNullOrEmpty(gm.getString("user"))){
	// 			return false;
	// 		} else {
	// 			if(gm.getString("user").equals("testUser")){
	// 				return true;
	// 			}
	// 			return false;
	// 		}
	// 	} catch (IOException ex){
	// 		System.out.println(ex.getMessage());
	// 	}
	// 	return false;
	// }
	// 
	// private boolean sendLoginParams(){
	// 	HashMap<String, String> postParams = new HashMap<String, String>();
	// 	
	// 	postParams.put("user", "testUser");
	// 	postParams.put("password", "1234"); 
	// 	
	// 	PiHttpResponse piResp = httpRequester.sendPostRequest("http://localhost:15000", "login", postParams, null, cookies);
	// 	
	// 	try
	// 	{
	// 		cookies = piResp.getCookies();
	// 		
	// 		String response = IOUtils.toString(piResp.getResponseBody());
	// 		
	// 		Map<String,Object> rm = ConvertJSON.toMap(response);
	// 		return (boolean)rm.get("login-status");
	// 	} catch (IOException ex){
	// 		System.out.println(ex.getMessage());
	// 		throw new RuntimeException(ex);
	// 	}
	// }
}
