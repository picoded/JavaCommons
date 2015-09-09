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
			
			if(webInfFile.listFiles() != null) {
				for(File file : webInfFile.listFiles()){
					file.delete(); //to accomodate certain people who do not use command line
				}
			}
			
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
	
//	@Test
	public void noLoginTest() {
		assertNotNull( response = RequestHttp.get( testAddress+"/api/account/login" ) );
		assertNotNull( responseMap = response.toMap() );
		assertNull( "Full map string of error: "+responseMap.toString()+" -> ", responseMap.get("error") );
		assertNull( responseMap.get("accountID") );
		assertNull( responseMap.get("accountNames") );
	}
	
//	@Test 
	@SuppressWarnings("unchecked")
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
		cred.put("accountPass", new String[] { "is-sudo" } ); //correct password
		
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
	
//	@Test 
	@SuppressWarnings("unchecked")
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
	
//	@Test
	public void infoByNameTest(){
		//try to get info without logging in
		response = RequestHttp.get(testAddress+"/api/account/info/name");
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("accountID"));
		
		//do login now
		AccountObject testUser = getAndLoginUser("the-root", "is-sudo");
//		testUser.setPassword("is-sudo");
//		
//		HashMap<String,String[]> cred = new HashMap<String,String[]>();
//		cred.put("accountName", new String[] { "the-root" } );
//		cred.put("accountPass", new String[]{ "is-sudo" });
//		
//		response = RequestHttp.post(testAddress+"/api/account/login/", cred);
//		assertNotNull(responseMap = response.toMap());
//		assertNotNull( responseMap.get("accountID") );
//		assertNotNull( responseMap.get("accountNames") );
		
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		//reattempt data retrieval
		HashMap<String,String[]> getParams = new HashMap<String,String[]>();
		getParams.put("accountName", new String[]{ "the-root" });
		response = RequestHttp.get(testAddress+"/api/account/info/name/the-root", getParams, cookieJar, null);
		assertNotNull(responseMap = response.toMap());
		assertNotNull(responseMap.get("accountID"));
	}
	
//	@Test
	public void infoByID_test(){
		//try to get info without logging in
		response = RequestHttp.get(testAddress+"/api/account/info/id");
		assertNotNull(responseMap = response.toMap());
		assertNull(responseMap.get("accountID"));
		
		//do login now
		AccountObject testUser = getAndLoginUser("the-root", "is-sudo");
//		testUser.setPassword("is-sudo");
//		
//		HashMap<String,String[]> cred = new HashMap<String,String[]>();
//		cred.put("accountName", new String[] { "the-root" } );
//		cred.put("accountPass", new String[]{ "is-sudo" });
//		
//		response = RequestHttp.post(testAddress+"/api/account/login/", cred);
//		assertNotNull(responseMap = response.toMap());
//		assertNotNull( responseMap.get("accountID") );
//		assertNotNull( responseMap.get("accountNames") );
		
		String userID = (String)responseMap.get("accountID");
		
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		//reattempt data retrieval
		HashMap<String,String[]> getParams = new HashMap<String,String[]>();
		getParams.put("accountName", new String[]{ "the-root" });
		getParams.put("accountID", new String[]{userID});
		response = RequestHttp.get(testAddress+"/api/account/info/id", getParams, cookieJar, null);
		assertNotNull(responseMap = response.toMap());
		assertNotNull(responseMap.get("accountID"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void members_list_GET(){ //group get function
		//do login now
		AccountObject testUser = getAndLoginUser("the-root", "is-sudo");
		String userID = (String)responseMap.get("accountID");
		
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		//create group and add testUser
		AccountObject groupAObj = getOrCreateGroup("GroupA");
		groupAObj.addMember(testUser, "guest");
		groupAObj.saveDelta();
		
		//reattempt data retrieval
		HashMap<String,String[]> getParams = new HashMap<String,String[]>();
		getParams.put("accountName", new String[]{ "the-root" });
		getParams.put("accountID", new String[]{userID});
		getParams.put("headers", new String[]{"name", "_oid", "role"});
		
		response = RequestHttp.get(testAddress+"/api/account/members/list/"+groupAObj._oid(), getParams, cookieJar, null);
		assertNotNull(responseMap = response.toMap());
		Object obj = responseMap.get("data");
		assertNotNull(obj);
		
		List<List<Object>> groupData = (List<List<Object>>)obj;
		assertNotNull(groupData);
		assertNotNull(groupData.get(0));
		assertEquals("guest", groupData.get(0).get(2));
	}
	
	@Test
	public void members_list_POST(){
		//do login now
		AccountObject testUser = getAndLoginUser("the-root", "is-sudo");
		String userID = (String)responseMap.get("accountID");
		
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		//create group and add testUser
		AccountObject groupAObj = getOrCreateGroup("GroupB");
		groupAObj.addMember(testUser, "admin");
		groupAObj.saveDelta();
		
		List<Map<String, Object>> groupData = getGroupData("the-root", testUser._oid(), groupAObj._oid());
		Map<String, Object> testUserData = getUserFromGroupData("the-root", groupData);
		assertNotNull(testUserData);
		assertEquals("the-root", testUserData.get("name"));
		assertEquals("admin", testUserData.get("role"));
		
		//now set users
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		Map<String, Object> setMemberMap = new HashMap<String, Object>();
		setMemberMap.put("NewUserA", "guest");
		setMemberMap.put("NewUserB", "admin");
		String setMemberMapJSON = ConvertJSON.fromMap(setMemberMap);
		paramsMap.put("setMembers", new String[]{setMemberMapJSON});
		
		response = RequestHttp.post(testAddress+"/api/account/members/list/"+groupAObj._oid(), paramsMap);
		assertNotNull(responseMap = response.toMap());
	}
	
	private AccountObject getAndLoginUser(String userName, String password){
		AccountObject user = accTable.getFromName(userName);
		if(user == null){
			user = accTable.newObject(userName);
			user.setPassword(password);
		}
		
		HashMap<String,String[]> cred = new HashMap<String,String[]>();
		cred.put("accountName", new String[] {userName } );
		cred.put("accountPass", new String[]{ password });
		
		response = RequestHttp.post(testAddress+"/api/account/login/", cred);
		assertNotNull(responseMap = response.toMap());
		assertNotNull( responseMap.get("accountID") );
		assertNotNull( responseMap.get("accountNames") );
		
		return user;
	}
	
	private AccountObject getOrCreateGroup(String groupName){
		AccountObject group = accTable.getFromName(groupName);
		if(group == null){
			accTable.newObject(groupName).saveAll();
			group = accTable.getFromName(groupName);
		}
		
		assertNotNull(group);
		
		return group;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getGroupData(String userName, String userID, String groupOID){
		HashMap<String,String[]> getParams = new HashMap<String,String[]>();
		getParams.put("accountName", new String[]{ userName });
		getParams.put("accountID", new String[]{userID});
		String[] headers = new String[]{"name", "_oid", "role"};
		getParams.put("headers", headers);
		
		Map<String,String[]> cookieJar = null;
		assertNotNull( cookieJar = response.cookiesMap() );
		
		response = RequestHttp.get(testAddress+"/api/account/members/list/"+groupOID, getParams, cookieJar, null);
		assertNotNull(responseMap = response.toMap());
		
		Object obj = responseMap.get("data");
		assertNotNull(obj);
		
		List<Map<String, Object>> ret = new  ArrayList<Map<String, Object>>();
		List<List<Object>> groupData = (List<List<Object>>)obj;
		for(int i = 0; i < groupData.size(); ++i){
			List<Object> userData = groupData.get(i);
			ret.add(new HashMap<String, Object>());
			for(int x = 0; x < headers.length; ++x){
				ret.get(i).put(headers[x], userData.get(x));
			}
		}
		return ret;
	}
	
	private Map<String, Object> getUserFromGroupData(String userName, List<Map<String, Object>> groupData){
		for(Map<String, Object> userData : groupData){
			if(userData.containsValue(userName)){
				return userData;
			}
		}
		
		return null;
	}

}
