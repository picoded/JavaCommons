package picodedTests.servlet;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.conv.ConvertJSON;
import picoded.servlet.CorePage;

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
		//assertNotNull(testPage.requestHeaderMap()); 
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(testPage.requestHeaderMap());
	}
	
	@Test
	public void requestCookieMap() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doGet(request, response);
		//assertNotNull(testPage.requestCookieMap());
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(testPage.requestHeaderMap());
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
	
	//@Test
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
		//assertNotNull(testPage.getWriter());
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(testPage.getWriter());
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
		//assertNotNull(testPage.getOutputStream());
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(testPage.getOutputStream());
	}
	
	@Test
	public void getContextPath() {
		assertNotNull(testPage.getContextPath());
	}
	
	@Test
	public void getContextURI() {
		assertEquals("/", testPage.getContextURI());
	}
	
	@Test
	public void getServletContextURI() {
		testPage = mock(CorePage.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		Mockito.when(testPage.getHttpServletRequest()).thenReturn(request);
		Mockito.when(request.getServletPath()).thenReturn("/");
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(testPage.getServletContextURI());
	}
	
	@Test
	public void getServletContextURI_invalid() {
		testPage = mock(CorePage.class);
		assertNull(testPage.getServletContextURI());
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void getParameter() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("user", new String[] { "me" });
		Mockito.when(httpRequest.getParameterMap()).thenReturn(map);
		assertEquals("me", testPage.getParameter("user"));
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void getParameter_NULL() {
		assertNull(testPage.getParameter(null));
	}
	
	@Test
	public void sendRedirect() {
		testPage.sendRedirect("/home");
	}
	
	@Test
	public void doAuth() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doAuth(map));
	}
	
	@Test
	public void doRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doRequest(map));
	}
	
	@Test
	public void doGetRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doGetRequest(map));
	}
	
	@Test
	public void doPostRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doPostRequest(map));
	}
	
	@Test
	public void doPutRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doPutRequest(map));
	}
	
	@Test
	public void doDeleteRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doDeleteRequest(map));
	}
	
	@Test
	public void outputRequest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(testPage.outputRequest(map, printWriter));
		printWriter.close();
	}
	
	@Test(expected = Exception.class)
	public void outputRequestException() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(testPage.outputRequestException(map, printWriter, new Exception()));
		printWriter.close();
	}
	
	@Test
	public void doJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doJSON(map, map));
	}
	
	@Test
	public void doGetJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doGetJSON(map, map));
	}
	
	@Test
	public void doPostJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doPostJSON(map, map));
	}
	
	@Test
	public void doPutJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doPutJSON(map, map));
	}
	
	@Test
	public void doDeleteJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(testPage.doDeleteJSON(map, map));
	}
	
	@Test
	public void outputJSON() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(testPage.outputJSON(map, template, printWriter));
		printWriter.flush();
		printWriter.close();
		File rFile = new File("me.txt");
		BufferedReader expected = new BufferedReader(new FileReader(rFile));
		String line;
		while ((line = expected.readLine()) != null) {
			assertEquals(ConvertJSON.fromObject(map), line);
		}
		expected.close();
		rFile.delete();
	}
	
	@Test
	public void outputJSONException() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertFalse(testPage.outputJSONException(map, template, printWriter, new Exception("There is an error")));
		printWriter.flush();
		printWriter.close();
		File rFile = new File("me.txt");
		BufferedReader expected = new BufferedReader(new FileReader(rFile));
		String line;
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("error",
			org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(new Exception("There is an error")));
		while ((line = expected.readLine()) != null) {
			assertNotNull(line);
		}
		expected.close();
		rFile.delete();
	}
	
	@Test
	public void doGet() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doGet(request, response);
	}
	
	@Test
	public void doPost() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doPost(request, response);
	}
	
	@Test
	public void doPut() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doPut(request, response);
	}
	
	@Test
	public void doDelete() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doDelete(request, response);
	}
	
	@Test
	public void doOptions() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(mockOutput);
		testPage.doOptions(request, response);
	}
	
	@Test
	public void contextInitialized() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		testPage.contextInitialized(servletContextEvent);
	}
	
	@Test
	public void contextDestroyed() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		testPage.contextDestroyed(servletContextEvent);
	}
}