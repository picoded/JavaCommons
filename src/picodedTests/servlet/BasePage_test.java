package picodedTests.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.JStruct.AccountObject;
import picoded.JStruct.AccountTable;
import picoded.servlet.BasePage;

public class BasePage_test extends Mockito {
	
	private BasePage basePage = null;
	
	@BeforeClass
	public static void setEnv() {
		//BasePage_test.class
		new File("./WEB-INF/").mkdir();
	}
	
	@Before
	public void setUp() {
		basePage = new BasePage();
		//basePage._
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void constructor() {
		assertNotNull(basePage);
	}
	
	@Test
	public void restBuilder() {
		assertNotNull(basePage.restBuilder());
	}
	
	@Test
	public void accountAuthTableSetup() throws Exception {
		basePage.initializeContext();
		basePage.accountAuthTableSetup();
	}
	
	@Test
	public void getSuperUserGroupName() {
		assertEquals(basePage.getSuperUserGroupName(), "SuperUsers");
	}
	
	@Test
	public void accountAuthTable() throws Exception {
		basePage.initializeContext();
		AccountTable accountTable = basePage.accountAuthTable();
		assertNotNull(accountTable);
		assertEquals("SuperUsers", accountTable.getSuperUserGroupName());
	}
	
	//@Test
	public void currentAccount() throws Exception {
		basePage.initializeContext();
		assertNotNull(basePage.currentAccount());
	}
	
	//@Test
	public void divertInvalidUser_Valid() throws Exception {
		basePage.initializeContext();
		AccountTable accountTable = basePage.accountAuthTable();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		accountTable.loginAccount(request, response, "admin", "p@ssw0rd!", false);
		basePage.currentAccount();
		assertFalse(basePage.divertInvalidUser(""));
	}
	
	//@Test
	public void divertInvalidUser_Invalid() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Cookie cookie1 = new Cookie("user", "me");
		Mockito.when(request.getCookies()).thenReturn(new Cookie[] { cookie1 });
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		HttpSession session = request.getSession(true);
		AccountTable accountTable = basePage.accountAuthTable();
		AccountObject accountObject = accountTable.loginAccount(request, response, "admin", "P@ssw0rd!", false);
		assertNotNull(accountObject);
		assertTrue(basePage.divertInvalidUser("/"));
	}
	
	//@Test
	public void currentAccountMetaInfo_Valid() throws Exception {
		basePage.initializeContext();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		AccountTable accountTable = basePage.accountAuthTable();
		AccountObject accountObject = accountTable.loginAccount(request, response, "admin", "P@ssw0rd!", false);
		assertNotNull(accountObject);
		basePage.doPost(request, response);
		assertEquals(basePage.currentAccountMetaInfo("admin"), "");
	}
	
	//@Test
	public void currentAccountMetaInfo_Invalid() throws Exception {
		basePage.initializeContext();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		basePage.doPost(request, response);
		assertEquals(basePage.currentAccountMetaInfo(""), "");
	}
	
	@Test
	public void JMTE() {
		assertNotNull(basePage.JMTE());
	}
	
	@Test
	public void PageBuilder() throws Exception {
		new File("./WEB-INF/").mkdir();
		new File("./WEB-INF/page/").mkdir();
		basePage.initializeContext();
		assertNotNull(basePage.PageBuilder());
	}
	
	@Test
	public void JSMLFormSet() throws Exception {
		new File("./WEB-INF/").mkdir();
		new File("./WEB-INF/jsml/").mkdir();
		basePage.initializeContext();
		assertNotNull(basePage.JSMLFormSet());
	}
}
