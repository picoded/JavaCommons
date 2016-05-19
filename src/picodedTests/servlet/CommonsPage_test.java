package picodedTests.servlet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.RESTBuilder.*;
import picoded.ServletLogging.ServletLogging;
import picoded.servlet.CommonsPage;
import picoded.servlet.CorePage;
import picoded.webUtils.EmailBroadcaster;

public class CommonsPage_test extends Mockito {

	private CommonsPage testPage = null;
	
	@Before
	public void setUp() {
		testPage = new CommonsPage();
	}
	
	@After
	public void tearDown() {
		testPage = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(testPage);
	}
	
	@Test
	public void enableCommonWildcardAuthRedirection() {
		assertTrue(testPage.enableCommonWildcardAuthRedirection());
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void doAuth() throws Exception {
		Map<String, Object> templateData = new HashMap<String, Object>();
		assertTrue(testPage.doAuth(templateData));
	}
	
	@Test
	public void isJsonRequest() throws Exception {
		CorePage corePage = mock(CorePage.class);
		Mockito.when(corePage.requestWildcardUriArray()).thenReturn(new String[] {"/"});
		Mockito.when(corePage.requestWildcardUri()).thenReturn("/");
		assertFalse(testPage.isJsonRequest()); //as overridden method already tested
	}
	
	@Test
	public void isJsonRequest_False() throws Exception {
		assertFalse(testPage.isJsonRequest());
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void outputRequest() throws Exception {
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] {"me"});
		assertTrue(testPage.outputRequest(map, printWriter));
		printWriter.close();
	}
	
	@Test
	public void buildApiScript() throws IOException {
		assertNotNull(testPage.buildApiScript());
	}
	
	@Test
	public void restBuilderSetup()  {
		testPage.restBuilderSetup(new RESTBuilder());
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void outputJSON() throws Exception  {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] {"me"});
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		CorePage corePage = mock(CorePage.class);
		Mockito.when(corePage.requestWildcardUriArray()).thenReturn(new String[] {"/"});
		Mockito.when(corePage.requestWildcardUri()).thenReturn("/");
		assertTrue(testPage.outputJSON(map, template, printWriter));
		printWriter.close();
	}
	
	@Test
	public void initializeContext() throws Exception  {
		testPage.initializeContext();
	}
	
	@Test
	public void systemEmail() throws Exception  {
		EmailBroadcaster emailBroadcaster = testPage.systemEmail();
		assertNotNull(emailBroadcaster);
	}
	
	@Test
	public void systemLogging() throws Exception  {
		ServletLogging log = testPage.systemLogging();
		assertNotNull(log);
	}
}
