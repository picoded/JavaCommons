package picodedTests.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.servlet.JStackPage;
import picoded.JStack.*;

public class JStackPage_test extends Mockito {
	
	private JStackPage testPage = null;
	
	@Before
	public void setUp() {
		testPage = new JStackPage();
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
	public void getWebInfPath() {
		assertEquals("./WEB-INF/", testPage.getWebInfPath());
	}
	
	@Test
	public void getClassesPath() {
		assertEquals("./WEB-INF/classes/", testPage.getClassesPath());
	}
	
	@Test
	public void getLibraryPath() {
		assertEquals("./WEB-INF/lib/", testPage.getLibraryPath());
	}
	
	@Test
	public void getConfigsPath() {
		assertEquals("./WEB-INF/config/", testPage.getConfigsPath());
	}
	
	@Test
	public void getPagesTemplatePath() {
		assertEquals("./WEB-INF/pages/", testPage.getPagesTemplatePath());
	}
	
	@Test
	public void getPagesOutputPath() {
		assertEquals("./", testPage.getPagesOutputPath());
	}
	
	@Test
	public void getJsmlTemplatePath() {
		assertEquals("./WEB-INF/jsml/", testPage.getJsmlTemplatePath());
	}
	
	@Test
	public void JConfig() {
		JConfig j = testPage.JConfig();
		assertNotNull(j);
	}
	
	@Test
	public void JStack() {
		JStack j = testPage.JStack();
		assertNotNull(j);
	}
	
	@Test
	public void JStack_disposeStackLayers() throws JStackException {
		testPage.JStack_disposeStackLayers();
	}
	
	@Test
	public void doSharedTeardown() throws Exception {
		testPage.doSharedTeardown();
	}
}
