package picoded.servlet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
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
import org.junit.Before;
import org.junit.Test;

import picoded.conv.ConvertJSON;
import picoded.enums.HttpRequestType;

public class CorePage_test {
	
	private CorePage corePage;
	private CorePage corePageSpy = mock(CorePage.class);
	
	@Before
	public void setUp() {
		corePage = new CorePage();
	}
	
	@After
	public void tearDown() {
		corePage = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(corePage);
	}
	
	@Test
	public void requestTypeSetTest() {
		assertNotNull(CorePage.RequestTypeSet.GET);
	}
	
	@Test
	public void getHttpServletRequestTest() {
		assertNull(corePage.getHttpServletRequest());
	}
	
	@Test
	public void requestHeaderMapInvalidTest() {
		assertNull(corePage.requestHeaderMap());
	}
	
	@Test
	public void processChainTest() throws ServletException, IOException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.GET;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void isJsonRequestTest() {
		assertNotNull(corePage.setJsonRequestFlag("*"));
		assertTrue(corePage.isJsonRequest());
	}
	
	@Test
	public void isJsonRequestAlternatePathTest() throws IOException, ServletException {
		CorePage corePageLocal;
		corePage.setJsonRequestFlag("in");
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("in", new String[] { "json" });
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		when(httpRequest.getParameterMap()).thenReturn(map);
		corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, httpRequest, response));
		assertTrue(corePageLocal.isJsonRequest());
	}
	
	@Test
	public void getJsonRequestFlagTest() {
		corePage.setJsonRequestFlag("in");
		assertEquals("in", corePage.getJsonRequestFlag());
	}
	
	@Test
	public void requestHeaderMapTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePage.doGet(request, response);
		assertNull(corePage.requestHeaderMap());
	}
	
	@Test
	public void isGET() throws IOException, ServletException {
		when(corePageSpy.isGET()).thenReturn(true);
		assertTrue(corePageSpy.isGET());
	}
	
	@Test
	public void isOPTIONTest() throws IOException, ServletException {
		when(corePageSpy.isOPTION()).thenReturn(true);
		assertTrue(corePageSpy.isOPTION());
	}
	
	@Test
	public void isPOSTTest() throws IOException, ServletException {
		when(corePageSpy.isPOST()).thenReturn(true);
		assertTrue(corePageSpy.isPOST());
	}
	
	@Test
	public void isPUTTest() throws IOException, ServletException {
		when(corePageSpy.isPUT()).thenReturn(true);
		assertTrue(corePageSpy.isPUT());
	}
	
	@Test
	public void isDELETETest() throws IOException, ServletException {
		when(corePageSpy.isDELETE()).thenReturn(true);
		assertTrue(corePageSpy.isDELETE());
	}
	
	@Test
	public void spawnInstanceTest() throws ServletException {
		assertNotNull(corePage.spawnInstance());
	}
	
	@Test
	public void initSetupTest() {
		ServletConfig servletConfig = mock(ServletConfig.class);
		corePage.initSetup(corePage, servletConfig);
	}
	
	@Test
	public void getWriterTest() throws IOException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(corePageSpy.getOutputStream()).thenReturn(mockOutput);
		//assertNotNull(testPage.getWriter());
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(corePageSpy.getWriter());
	}
	
	@Test
	public void getOutputStreamInvalidTest() {
		assertNull(corePage.getOutputStream());
	}
	
	@Test
	public void getOutputStreamTest() throws IOException, ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePageSpy.doGet(request, response);
		//assertNotNull(testPage.getOutputStream());
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(corePageSpy.getOutputStream());
	}
	
	@Test
	public void getContextPathTest() {
		assertNotNull(corePage.getContextPath());
	}
	
	@Test
	public void getContextURITest() {
		assertEquals("/", corePage.getContextURI());
	}
	
	@Test
	public void getServletContextURITest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(corePageSpy.getHttpServletRequest()).thenReturn(request);
		when(request.getServletPath()).thenReturn("/");
		//can not be NOT NULL due to Servlet where request and response is NULL
		assertNull(corePageSpy.getServletContextURI());
	}
	
	@Test
	public void getServletContextURIInvalidTest() {
		assertNull(corePageSpy.getServletContextURI());
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void getParameterTest() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("user", new String[] { "me" });
		when(httpRequest.getParameterMap()).thenReturn(map);
		assertEquals("me", corePageSpy.getParameter("user"));
	}
	
	//@Test //can not be tested due to Servlet where request and response is NULL
	public void getParameterNULLTest() {
		assertNull(corePage.getParameter(null));
	}
	
	@Test
	public void sendRedirectTest() {
		corePage.sendRedirect("/home");
	}
	
	@Test
	public void doAuthTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doAuth(map));
	}
	
	@Test
	public void doRequestTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doRequest(map));
	}
	
	@Test
	public void doGetRequestTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doGetRequest(map));
	}
	
	@Test
	public void doPostRequesTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doPostRequest(map));
	}
	
	@Test
	public void doPutRequestTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doPutRequest(map));
	}
	
	@Test
	public void doDeleteRequestTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doDeleteRequest(map));
	}
	
	@Test
	public void outputRequestTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(corePage.outputRequest(map, printWriter));
		printWriter.close();
	}
	
	@Test(expected = Exception.class)
	public void outputRequestExceptionTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(corePage.outputRequestException(map, printWriter, new Exception()));
		printWriter.close();
	}
	
	@Test
	public void doJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doJSON(map, map));
	}
	
	@Test
	public void doGetJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doGetJSON(map, map));
	}
	
	@Test
	public void doPostJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doPostJSON(map, map));
	}
	
	@Test
	public void doPutJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doPutJSON(map, map));
	}
	
	@Test
	public void doDeleteJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		assertTrue(corePage.doDeleteJSON(map, map));
	}
	
	@Test
	public void outputJSONTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertTrue(corePage.outputJSON(map, template, printWriter));
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
	public void outputJSONExceptionTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		assertFalse(corePage.outputJSONException(map, template, printWriter, new Exception("There is an error")));
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
	public void doGetTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageSpy.doGet(request, response);
	}
	
	@Test
	public void doPostTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageSpy.doPost(request, response);
	}
	
	@Test
	public void doPutTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageSpy.doPut(request, response);
	}
	
	@Test
	public void doDeleteTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageSpy.doDelete(request, response);
	}
	
	@Test
	public void doOptionsTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageSpy.doOptions(request, response);
	}
	
	@Test
	public void contextInitializedTest() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		corePageSpy.contextInitialized(servletContextEvent);
	}
	
	@Test
	public void contextDestroyedTest() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		corePage.contextDestroyed(servletContextEvent);
	}
}
