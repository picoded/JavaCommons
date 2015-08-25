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
import picoded.JStruct.*;
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
			accTable.systemSetup();
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
	public void noLoginTest() {
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login" ) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNames") );
	}
	
	@Test @SuppressWarnings("unchecked")
	public void loginTest() {
		
		// Setup user
		AccountObject testUser = accTable.newObject("the-root");
		testUser.setPassword("is-sudo");
		
		// Credentials in request
		HashMap<String,String[]> cred = new HashMap<String,String[]>();
		cred.put("accountName", new String[] { "the-root" } );
		
		// Wrong login attempt
		//---------------------------------------
		cred.put("accountPass", new String[] { "is-not-square" } ); //wrong password
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNames") );
		
		// Correct login attempt
		//---------------------------------------
		cred.put("accountPass", new String[] { "is-sudo" } ); //wrong password
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap = response.toMap() );
		assertNotNull( responseMap.get("accountID") );
		assertNotNull( responseMap.get("accountNames") );
		assertEquals( "the-root", ((List<String>)(responseMap.get("accountNames"))).get(0) );
		
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
		assertNotNull( responseMap.toString(), responseMap.get("accountNames") );
		assertEquals( "the-root", ((List<String>)(responseMap.get("accountNames"))).get(0) );
		
		// Logout attempt
		//---------------------------------------
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/logout", null, cookieJar, null ) );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap = response.toMap() );
		assertEquals( "true", responseMap.get("logout") );
		assertNotNull( cookieJar = response.cookiesMap() );
		
		// Logout validation
		//---------------------------------------
		assertNotNull( cookieJar = response.cookiesMap() );
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login", null, cookieJar, null ) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNames") );
		
	}
	
	@Test @SuppressWarnings("unchecked")
	public void passwordChange() {
		
		// Setup user
		//---------------------------------------
		AccountObject testUser = accTable.newObject("the-changing-user");
		testUser.setPassword("is-old");
		Map<String,String[]> cookieJar = null;
		
		HashMap<String,String[]> cred = new HashMap<String,String[]>();
		cred.put("accountName", new String[] { "the-changing-user" } );
		
		// Login before change
		//---------------------------------------
		cred.put("accountPass", new String[] { "is-old" } );
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNotNull( cookieJar = response.cookiesMap() );
		assertNotNull( responseMap = response.toMap() );
		
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap.toString(), responseMap.get("accountID") );
		assertNotNull( responseMap.toString(), responseMap.get("accountNames") );
		assertEquals( "the-changing-user", ((List<String>)(responseMap.get("accountNames"))).get(0) );
		
		String accountID = (String)(responseMap.get("accountID"));
		
		// Password change failed
		//---------------------------------------
		HashMap<String,String[]> passwordChange = new HashMap<String,String[]>();
		passwordChange.put( "oldPassword", new String[] {"is-old-WRONG"} );
		passwordChange.put( "newPassword", new String[] {"is-brand-NEW-world-1"} );
		passwordChange.put( "accountID", new String[] {accountID} );
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/password", passwordChange, cookieJar, null) );
		assertNotNull( responseMap = response.toMap() );
		assertNotNull( responseMap.toString(), responseMap.get("success") );
		assertFalse( responseMap.toString(), (Boolean)(responseMap.get("success")) );
		assertNotNull( responseMap.toString(), responseMap.get("error") );
		
		// Password change works
		//---------------------------------------
		passwordChange.put( "oldPassword", new String[] {"is-old"} );
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/password", passwordChange, cookieJar, null) );
		assertNotNull( responseMap = response.toMap() );
		assertNotNull( responseMap.toString(), responseMap.get("success") );
		assertTrue( responseMap.toString(), (Boolean)(responseMap.get("success")) );
		assertNull( responseMap.toString(), responseMap.get("error") );
		assertEquals( responseMap.toString(), accountID, responseMap.get("accountID") );
		
		// Login after change, old password invalid
		//---------------------------------------
		cred.put("accountPass", new String[] { "is-old" } ); //wrong password
		cred.put("accountName", new String[] { "the-changing-user" } );
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNotNull( response.cookiesMap() );
		assertNotNull( responseMap = response.toMap() );
		
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.toString(), responseMap.get("accountID") );
		assertNull( responseMap.toString(), responseMap.get("accountNames") );
		
		// Login after change, new password
		//---------------------------------------
		cred.put("accountPass", new String[] { "is-brand-NEW-world-1" } );
		cred.put("accountName", new String[] { "the-changing-user" } );
		
		assertNotNull( response = RequestHttp.post( testAddress+"/api/account/login", cred) );
		assertNotNull( response.cookiesMap() );
		assertNotNull( responseMap = response.toMap() );
		
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNotNull( responseMap.toString(), responseMap.get("accountID") );
		assertNotNull( responseMap.toString(), responseMap.get("accountNames") );
		assertEquals( "the-changing-user", ((List<String>)(responseMap.get("accountNames"))).get(0) );
		
	}
}
