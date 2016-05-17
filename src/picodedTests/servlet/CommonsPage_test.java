package picodedTests.servlet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import oracle.net.aso.p;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.servlet.CommonsPage;
import picoded.servlet.CorePage;

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
	
	@Test
	public void doAuth() throws Exception {
		Map<String, Object> templateData = new HashMap<String, Object>();
		assertTrue(testPage.doAuth(templateData));
	}
	
	@Test
	public void isJsonRequest() throws Exception {
		CorePage corePage = mock(CorePage.class);
		Mockito.when(corePage.requestWildcardUriArray()).thenReturn(new String[] {"/"});
		Mockito.when(corePage.requestWildcardUri()).thenReturn("/");
		assertTrue(testPage.isJsonRequest());
	}
	
	@Test
	public void isJsonRequest_False() throws Exception {
		assertFalse(testPage.isJsonRequest());
	}
	
	@Test
	public void outputRequest() throws Exception {
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] {"me"});
		assertTrue(testPage.outputRequest(map, printWriter));
		printWriter.close();
	}
}
