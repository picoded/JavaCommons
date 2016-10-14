package picoded.servlet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.conv.ConvertJSON;
import picoded.enums.HttpRequestType;

public class CorePage_test {
	
	private CorePage corePage;
	private CorePage corePageMock; // = mock(CorePage.class);
	
	@Before
	public void setUp() {
		corePage = new CorePage();
		corePageMock = mock(CorePage.class);
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
	public void requestHeaderMapValidTest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		List<String> headerList = new ArrayList<String>();
		headerList.add("header_1");
		headerList.add("header_2");
		when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerList));
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		assertNotNull(corePageLocal.requestHeaderMap());
	}
	
	@Test
	public void requestHeaderMapAlternatePathTest() {
		Map<String, String[]> map = new HashMap<>();
		map.put("arg", new String[] { "a", "b" });
		corePage._requestHeaderMap = map;
		assertNotNull(corePage.requestHeaderMap());
	}
	
	@Test
	public void processChainTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.GET;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainJSONPathTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.GET;
		corePage.responseOutputStream = mockStream;
		corePage.setJsonRequestFlag("*");
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainJSONPOSTTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.POST;
		corePage.responseOutputStream = mockStream;
		corePage.setJsonRequestFlag("*");
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainJSONPUTTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.PUT;
		corePage.responseOutputStream = mockStream;
		corePage.setJsonRequestFlag("*");
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainJSONDELETETest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.DELETE;
		corePage.responseOutputStream = mockStream;
		corePage.setJsonRequestFlag("*");
		assertTrue(corePage.processChain());
	}
	
	@Test(expected = ServletException.class)
	public void processChainExceptionTest() throws Exception {
		assertFalse(corePage.processChain());
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
	public void isGET() {
		corePage.requestType = HttpRequestType.GET;
		assertTrue(corePage.isGET());
	}
	
	@Test
	public void isOPTIONTest() {
		corePage.requestType = HttpRequestType.OPTION;
		assertTrue(corePage.isOPTION());
	}
	
	@Test
	public void isPOSTTest() {
		corePage.requestType = HttpRequestType.POST;
		assertTrue(corePage.isPOST());
	}
	
	@Test
	public void isPUTTest() {
		corePage.requestType = HttpRequestType.PUT;
		assertTrue(corePage.isPUT());
	}
	
	@Test
	public void isDELETETest() {
		corePage.requestType = HttpRequestType.DELETE;
		assertTrue(corePage.isDELETE());
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
	public void initSetupexceptionTest() throws ServletException {
		ServletConfig servletConfig = mock(ServletConfig.class);
		doThrow(Exception.class).when(corePageMock).init(servletConfig);
		corePageMock.initSetup(corePageMock, servletConfig);
	}
	
	@Test
	public void getWriterTest() throws IOException, ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		HttpServletRequest request = mock(HttpServletRequest.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		assertNotNull(corePageLocal.getWriter());
	}
	
	@Test
	public void getWriterNullTest() {
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(corePageMock.getOutputStream()).thenReturn(mockOutput);
		assertNull(corePageMock.getWriter());
	}
	
	@Test
	public void getOutputStreamInvalidTest() {
		assertNull(corePage.getOutputStream());
	}
	
	@Test
	public void getOutputStreamValidTest() throws IOException, ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		HttpServletRequest request = mock(HttpServletRequest.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		assertNotNull(corePageLocal.getOutputStream());
	}
	
	@Test
	public void getOutputStreamNullTest() throws IOException, ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePageMock.doGet(request, response);
		assertNull(corePageMock.getOutputStream());
	}
	
	@Test
	public void getContextPathTest() {
		corePage._contextPath = "/root";
		assertEquals("/root", corePage.getContextPath());
	}
	
	@Test
	public void getContextPathRequestObjectNotNullTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getServletContext()).thenReturn(mock(ServletContext.class));
		when(request.getServletContext().getRealPath("/")).thenReturn("/root");
		corePage.httpRequest = request;
		assertEquals("/root/", corePage.getContextPath());
	}
	
	@Test
	public void getContextPathThroughServletContextEventTest() {
		ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContextEvent.getServletContext()).thenReturn(servletContext);
		when(servletContext.getRealPath("/")).thenReturn("/home");
		corePage._servletContextEvent = servletContextEvent;
		assertEquals("/home/", corePage.getContextPath());
	}
	
	@Test
	public void getContextPathExceptionTest() {
		assertNotNull(corePage.getContextPath());
	}
	
	@Test
	public void getContextURITest() {
		assertEquals("/", corePage.getContextURI());
	}
	
	@Test
	public void getContextURIThroughContextPathTest() {
		corePage._contextURI = "/root";
		assertEquals("/root", corePage.getContextURI());
	}
	
	@Test
	public void getContextURIThroughHTTPRequestTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getContextPath()).thenReturn("/root");
		corePage.httpRequest = request;
		assertEquals("/root", corePage.getContextURI());
	}
	
	@Test
	public void getContextURIThroughServletContextEventTest() {
		ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContextEvent.getServletContext()).thenReturn(servletContext);
		when(servletContext.getContextPath()).thenReturn("/home");
		corePage._servletContextEvent = servletContextEvent;
		assertEquals("/home/", corePage.getContextURI());
	}
	
	@Test
	public void getServletContextURITest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(corePageMock.getHttpServletRequest()).thenReturn(request);
		when(request.getServletPath()).thenReturn("/");
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		assertNotNull(corePageLocal.getServletContextURI());
	}
	
	@Test
	public void getServletContextURIInvalidTest() {
		assertNull(corePageMock.getServletContextURI());
	}
	
	@Test(expected = RuntimeException.class)
	public void getServletContextURIExceptionTest() {
		assertNull(corePage.getServletContextURI());
	}
	
	public void getParameterTest() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("user", new String[] { "me" });
		when(httpRequest.getParameterMap()).thenReturn(map);
		assertEquals("me", corePageMock.getParameter("user"));
	}
	
	@Test
	public void getParameterNULLTest() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		Map<String, String[]> map = new HashMap<String, String[]>();
		when(httpRequest.getParameterMap()).thenReturn(map);
		assertNull(corePageMock.getParameter(""));
	}
	
	@Test
	public void sendRedirectTest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = corePage.setupInstance(HttpRequestType.GET, request, response);
		corePageLocal.sendRedirect("/home");
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
	public void outputJSONHTTPResponseNotNullTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		HttpServletResponse response = mock(HttpServletResponse.class);
		corePage.httpResponse = response;
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
	public void outputJSONExceptionHTTPResponseNotNullTest() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> template = new HashMap<String, Object>();
		map.put("user", new String[] { "me" });
		File file = new File("me.txt");
		PrintWriter printWriter = new PrintWriter(file);
		HttpServletResponse response = mock(HttpServletResponse.class);
		corePage.httpResponse = response;
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
		corePageMock.doGet(request, response);
	}
	
	@Test
	public void doPostTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageMock.doPost(request, response);
	}
	
	@Test
	public void doPutTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageMock.doPut(request, response);
	}
	
	@Test
	public void doDeleteTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageMock.doDelete(request, response);
	}
	
	@Test
	public void doOptionsTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream mockOutput = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(mockOutput);
		corePageMock.doOptions(request, response);
	}
	
	@Test(expected = ServletException.class)
	public void doOptionsExceptionTest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		corePageMock.doOptions(request, response);
	}
	
	@Test
	public void contextInitializedTest() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		corePage.contextInitialized(servletContextEvent);
	}
	
	@Test
	public void contextDestroyedTest() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		corePage.contextDestroyed(servletContextEvent);
	}
	
	@Test(expected = Exception.class)
	public void doExceptionTest() throws Exception {
		corePage.doException(new Exception("CorePage Exception"));
	}
	
	@Test
	public void getHttpServletResponseTest() {
		assertNull(corePage.getHttpServletResponse());
	}
	
	@Test
	public void requestCookieMapTest() {
		assertNull(corePage.requestCookieMap());
	}
	
	@Test
	public void requestCookieMapCookieNotNullTest() {
		Map<String, String[]> cookieMap = new HashMap<String, String[]>();
		corePage._requestCookieMap = cookieMap;
		assertNotNull(corePage.requestCookieMap());
	}
	
	@Test
	public void requestCookieMapHTTPRequestNotNullTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie cookie = mock(Cookie.class);
		when(request.getCookies()).thenReturn(new Cookie[] { cookie });
		corePage.httpRequest = request;
		assertNotNull(corePage.requestCookieMap());
	}
	
	@Test
	public void requestServletPathTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		assertNull(corePage.requestServletPath());
	}
	
	@Test
	public void requestTypeTest() {
		assertNull(corePage.requestType());
	}
	
	@Test
	public void requestTypeNotNullTest() {
		corePage.requestType = HttpRequestType.GET;
		assertEquals(HttpRequestType.GET, corePage.requestType());
	}
	
	@Test
	public void requestURITest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		assertNull(corePage.requestURI());
	}
	
	@Test
	public void requestWildcardUriTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		assertNull(corePage.requestWildcardUri());
	}
	
	@Test
	public void requestWildcardUriPathNotNullTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		when(request.getPathInfo()).thenReturn("/home");
		assertEquals("\\home", corePage.requestWildcardUri());
	}
}
