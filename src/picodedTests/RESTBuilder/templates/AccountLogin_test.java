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
import picoded.JStack.*;
import picoded.RESTBuilder.templates.*;
import picoded.webUtils.*;

public class AccountLogin_test {
	
	// Servlet setup
	//--------------------------------------------------------------------------
	protected static AccountLogin loginServlet;
	protected static EmbeddedServlet tomcat = null;
	protected static String testAddress = "http://127.0.0.1:15000";
	protected static AccountTable accTable = null;
	
	@BeforeClass
	public static void serverSetUp() throws LifecycleException, IOException, JStackException {
		if( tomcat == null ) {
			File webInfFile = new File("./test-files/tmp/WEB-INF");
			webInfFile.mkdir();
			
			File context = new File("./test-files/tmp");
			loginServlet = new AccountLogin();
			
			tomcat = new EmbeddedServlet("", context)
			.withPort(15000)
			.withServlet("/api/account",  "loginServlet1", loginServlet)
			.withServlet("/api/account/*", "loginServlet2", loginServlet);
			tomcat.start();
			
			// Setup servlet context path. This is required for the servlet dependencies
			// to work through a test case. Before the actual request is called
			loginServlet._contextPath = "./test-files/tmp/";
			
			// Does the table and account setup
			accTable = loginServlet.accountAuthTable();
			accTable.stackSetup();
		}
	}
	
	@AfterClass
	public static void serverTearDown() throws LifecycleException, IOException {
		if(tomcat != null) {
			//tomcat.awaitServer(); //manual
			tomcat.stop();
		}
		tomcat = null;
	}
	
	// Tests 
	//--------------------------------------------------------------------------
	RequestHttp requester;
	ResponseHttp response;
	Map<String,Object> responseMap;
	
	@Test
	public void noLoginTest() throws IOException {
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login" ) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNAME") );
	}
	
	@Test @SuppressWarnings("unchecked")
	public void loginTest() throws IOException {
		
		// Setup user
		AccountObject testUser = accTable.newObject("the-root");
		testUser.setPassword("is-sudo");
		
		// Credentials in request
		HashMap<String,String[]> cred = new HashMap<String,String[]>();
		cred.put("accountNAME", new String[] { "the-root" } );
		
		// Wrong login attempt
		//---------------------------------------
		cred.put("accountPASS", new String[] { "is-not-square" } ); //wrong password
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", null) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNAME") );
		
		// Correct login attempt
		//---------------------------------------
		cred.put("accountPASS", new String[] { "is-sudo" } ); //wrong password
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap = response.toMap() );
		assertNotNull( responseMap.get("accountID") );
		assertNotNull( responseMap.get("accountNAME") );
		assertEquals( "the-root", ((List<String>)(responseMap.get("accountNAME"))).get(0) );
		
		// Login validation
		//---------------------------------------
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		assertNotNull( cookieJar.get("Account_Puid") );
		assertNotNull( cookieJar.get("Account_Nonc") );
		assertNotNull( cookieJar.get("Account_Hash") );
		//assertNotNull( cookieJar.get("Account_Rmbr") );
		
		assertNotNull( cookieJar.get("Account_Puid")[0] );
		assertNotNull( cookieJar.get("Account_Nonc")[0] );
		assertNotNull( cookieJar.get("Account_Hash")[0] );
		//assertNotNull( cookieJar.get("Account_Rmbr")[0] );
		
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login", null, cookieJar, null ) );
		
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap = response.toMap() );
		assertNotNull( responseMap.toString(), responseMap.get("accountID") );
		assertNotNull( responseMap.toString(), responseMap.get("accountNAME") );
		assertEquals( "the-root", ((List<String>)(responseMap.get("accountNAME"))).get(0) );
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
