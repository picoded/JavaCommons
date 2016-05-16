package picodedTests.servlet;

// Target test class
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.servlet.CorePage;











// Test Case include
import org.junit.*;
import org.mockito.Mockito;

import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class CorePage_test extends Mockito {
	
	public CorePage testPage = null;
	
	
	
	@Before
	public void setUp() throws ServletException {
		testPage = new CorePage();
		//testPage.spawnInstance();
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
	public void requestHeaderMap_invalid() {
		assertNull(testPage.requestHeaderMap());
	}
	
	@Test
	public void requestHeaderMap() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream mockOutput = mock(ServletOutputStream.class);
        Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doGet(request, response);
		assertNotNull(testPage.requestHeaderMap());
	}
	
	@Test
	public void requestCookieMap() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream mockOutput = mock(ServletOutputStream.class);
        Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doGet(request, response);
		assertNotNull(testPage.requestCookieMap());
	}
	
	@Test
	public void requestCookieMap_invalid() {
		assertNull(testPage.requestCookieMap());
	}
	
	@Test
	public void setJsonRequestFlag() {
		assertNotNull(testPage.setJsonRequestFlag("in"));
	}
	
	@Test
	public void getJsonRequestFlag() {
		testPage.setJsonRequestFlag("in");
		assertEquals("in", testPage.getJsonRequestFlag());
	}
	
	@Test
	public void isJsonRequest() {
		assertNotNull(testPage.setJsonRequestFlag("*"));
		assertTrue(testPage.isJsonRequest());
	}
	
	@Test
	public void isJsonRequest1() throws Exception {
		CorePage corePage;
		//testPage = mock(CorePage.class);
		assertNotNull(corePage = testPage.setJsonRequestFlag("in"));
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		Mockito.when(httpRequest.getParameterMap()).thenReturn(map);
		Map<String, Object> outputData = new HashMap<String, Object>();
		corePage.doGetJSON(outputData, outputData);
		assertTrue(corePage.isJsonRequest());
	}
	
	@Test
	public void isGET() throws IOException, ServletException {
		testPage = mock(CorePage.class);
		Mockito.when(testPage.isGET()).thenReturn(true);
		assertTrue(testPage.isGET());
	}
	
	@Test
	public void isOPTION() throws IOException, ServletException {
		testPage = mock(CorePage.class);
		Mockito.when(testPage.isOPTION()).thenReturn(true);
		assertTrue(testPage.isOPTION());
	}
	
	@Test
	public void isPOST() throws IOException, ServletException {
		testPage = mock(CorePage.class);
		Mockito.when(testPage.isPOST()).thenReturn(true);
		assertTrue(testPage.isPOST());
	}
	
	@Test
	public void isPUT() throws IOException, ServletException {
		testPage = mock(CorePage.class);
		Mockito.when(testPage.isPUT()).thenReturn(true);
		assertTrue(testPage.isPUT());
	}
	
	@Test
	public void isDELETE() throws IOException, ServletException {
		testPage = mock(CorePage.class);
		Mockito.when(testPage.isDELETE()).thenReturn(true);
		assertTrue(testPage.isDELETE());
	}
	
	@Test
	public void spawnInstance() throws ServletException {
		assertNotNull(testPage.spawnInstance());
	}
	
	@Test
	public void initSetup() {
		ServletConfig servletConfig = mock(ServletConfig.class);
		testPage.initSetup(testPage, servletConfig);
	}
	
	@Test
	public void getWriter() throws IOException {
		testPage = mock(CorePage.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream mockOutput = mock(ServletOutputStream.class);
        Mockito.when(testPage.getOutputStream()).thenReturn(mockOutput);
        assertNotNull(testPage.getWriter());
	}
	
	@Test
	public void getOutputStream_invalid() {
		assertNull(testPage.getOutputStream());
	}
	
	@Test
	public void getOutputStream() throws IOException, ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		HttpServletRequest request = mock(HttpServletRequest.class);
		testPage.doGet(request, response);
		assertNotNull(testPage.getOutputStream());
	}
	
	@Test
	public void getContextPath() {
		assertNotNull(testPage.getContextPath());
	}
	
	@Test
	public void getContextURI() {
		assertEquals("/", testPage.getContextURI());
	}
	
	
}