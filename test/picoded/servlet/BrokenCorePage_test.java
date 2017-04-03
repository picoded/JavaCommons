package picoded.servlet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import picoded.set.EmptyArray;
import picoded.set.HttpRequestType;

public class BrokenCorePage_test {
	
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
	
	// @Test
	// public void processChainJSONPathTest() throws ServletException {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.GET;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	assertTrue(corePage.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPathRequestTypeOtherTest() throws ServletException {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.HEAD;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	assertTrue(corePage.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPOSTTest() throws ServletException {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.POST;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	assertTrue(corePage.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPOSTDoAuthTest() throws Exception {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.POST;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	Map<String, Object> templateDataObj = new HashMap<String, Object>();
	// 	corePage.templateDataObj = templateDataObj;
	// 	CorePage corePageLocal = spy(corePage);
	// 	when(corePageLocal.doAuth(templateDataObj)).thenReturn(false);
	// 	assertFalse(corePageLocal.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPOSTDoJSONTest() throws Exception {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.POST;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	Map<String, Object> templateDataObj = new HashMap<String, Object>();
	// 	corePage.templateDataObj = templateDataObj;
	// 	corePage.jsonDataObj = templateDataObj;
	// 	CorePage corePageLocal = spy(corePage);
	// 	when(corePageLocal.doJSON(templateDataObj, templateDataObj)).thenReturn(false);
	// 	assertFalse(corePageLocal.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPUTTest() throws ServletException {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.PUT;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	assertTrue(corePage.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONDELETETest() throws ServletException {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.DELETE;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	assertTrue(corePage.processChain());
	// }
	// 
	// @Test
	// public void processChainJSONPOSTExceptionTest() throws Exception {
	// 	ServletOutputStream mockStream = mock(ServletOutputStream.class);
	// 	corePage.requestType = HttpRequestType.POST;
	// 	corePage.responseOutputStream = mockStream;
	// 	corePage.setJsonRequestFlag("*");
	// 	Map<String, Object> templateDataObj = new HashMap<String, Object>();
	// 	corePage.templateDataObj = templateDataObj;
	// 	corePage.jsonDataObj = templateDataObj;
	// 	CorePage corePageLocal = spy(corePage);
	// 	when(corePageLocal.doPostJSON(templateDataObj, templateDataObj)).thenThrow(Exception.class);
	// 	assertFalse(corePageLocal.processChain());
	// }
	
	@Test
	public void processChainNormalPathTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.GET;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainNormalPathRequestTypeOtherTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.HEAD;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainNormalPathRequestTypePUTFalseTest() throws Exception {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.PUT;
		Map<String, Object> templateDataObj = new HashMap<String, Object>();
		corePage.templateDataObj = templateDataObj;
		corePage.jsonDataObj = templateDataObj;
		CorePage corePageLocal = spy(corePage);
		when(corePageLocal.doPutRequest(templateDataObj)).thenReturn(false);
		corePageLocal.responseOutputStream = mockStream;
		assertFalse(corePageLocal.processChain());
	}
	
	@Test
	public void processChainNormalPOSTTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.POST;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainNormalPOSTDoAuthTest() throws Exception {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.POST;
		corePage.responseOutputStream = mockStream;
		Map<String, Object> templateDataObj = new HashMap<String, Object>();
		corePage.templateDataObj = templateDataObj;
		CorePage corePageLocal = spy(corePage);
		when(corePageLocal.doAuth(templateDataObj)).thenReturn(false);
		assertFalse(corePageLocal.processChain());
	}
	
	@Test
	public void processChainNormalPOSTDoJSONTest() throws Exception {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.POST;
		corePage.responseOutputStream = mockStream;
		Map<String, Object> templateDataObj = new HashMap<String, Object>();
		corePage.templateDataObj = templateDataObj;
		CorePage corePageLocal = spy(corePage);
		when(corePageLocal.doRequest(templateDataObj)).thenReturn(false);
		assertFalse(corePageLocal.processChain());
	}
	
	@Test
	public void processChainNormalPUTTest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.PUT;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test
	public void processChainNormalDELETETest() throws ServletException {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.DELETE;
		corePage.responseOutputStream = mockStream;
		assertTrue(corePage.processChain());
	}
	
	@Test(expected = ServletException.class)
	public void processChainExceptionTest() throws Exception {
		assertFalse(corePage.processChain());
	}
	
	@Test(expected = ServletException.class)
	public void processChainNormalPOSTExceptionTest() throws Exception {
		ServletOutputStream mockStream = mock(ServletOutputStream.class);
		corePage.requestType = HttpRequestType.POST;
		corePage.responseOutputStream = mockStream;
		Map<String, Object> templateDataObj = new HashMap<String, Object>();
		corePage.templateDataObj = templateDataObj;
		CorePage corePageLocal = spy(corePage);
		when(corePageLocal.doPostRequest(templateDataObj)).thenThrow(Exception.class);
		assertFalse(corePageLocal.processChain());
	}
	
	// @Test
	// public void isJsonRequestTest() {
	// 	assertNotNull(corePage.setJsonRequestFlag("*"));
	// 	assertTrue(corePage.isJsonRequest());
	// }
	// 
	// @Test
	// public void isJsonRequestAlternatePathTest() throws IOException, ServletException {
	// 	CorePage corePageLocal;
	// 	corePage.setJsonRequestFlag("in");
	// 	HttpServletRequest httpRequest = mock(HttpServletRequest.class);
	// 	Map<String, String[]> map = new HashMap<String, String[]>();
	// 	map.put("in", new String[] { "json" });
	// 	HttpServletResponse response = mock(HttpServletResponse.class);
	// 	ServletOutputStream mockOutput = mock(ServletOutputStream.class);
	// 	when(response.getOutputStream()).thenReturn(mockOutput);
	// 	when(httpRequest.getParameterMap()).thenReturn(map);
	// 	corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, httpRequest, response));
	// 	assertTrue(corePageLocal.isJsonRequest());
	// }
	// 
	// @Test
	// public void isJsonRequestAlternatePathEmptyStringTest() throws IOException, ServletException {
	// 	CorePage corePageLocal;
	// 	corePage.setJsonRequestFlag("in");
	// 	HttpServletRequest httpRequest = mock(HttpServletRequest.class);
	// 	Map<String, String[]> map = new HashMap<String, String[]>();
	// 	map.put("in", new String[] { "" });
	// 	HttpServletResponse response = mock(HttpServletResponse.class);
	// 	ServletOutputStream mockOutput = mock(ServletOutputStream.class);
	// 	when(response.getOutputStream()).thenReturn(mockOutput);
	// 	when(httpRequest.getParameterMap()).thenReturn(map);
	// 	corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, httpRequest, response));
	// 	assertFalse(corePageLocal.isJsonRequest());
	// }
	// 
	// @Test
	// public void getJsonRequestFlagTest() {
	// 	corePage.setJsonRequestFlag("in");
	// 	assertEquals("in", corePage.getJsonRequestFlag());
	// }
	
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
	public void isGETTest() {
		corePage.requestType = HttpRequestType.PUT;
		assertFalse(corePage.isGET());
	}
	
	@Test
	public void isOPTIONFalseTest() {
		corePage.requestType = HttpRequestType.PUT;
		assertFalse(corePage.isOPTION());
	}
	
	@Test
	public void isPOSTFalseTest() {
		corePage.requestType = HttpRequestType.PUT;
		assertFalse(corePage.isPOST());
	}
	
	@Test
	public void isPUTFalseTest() {
		corePage.requestType = HttpRequestType.GET;
		assertFalse(corePage.isPUT());
	}
	
	@Test
	public void isDELETEFalseTest() {
		corePage.requestType = HttpRequestType.PUT;
		assertFalse(corePage.isDELETE());
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
	
	@Test(expected = RuntimeException.class)
	public void initSetupexceptionTest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletConfig servletConfig = mock(ServletConfig.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		doThrow(Exception.class).when(corePageLocal).init(servletConfig);
		corePageLocal.initSetup(corePageLocal, servletConfig);
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
	
	//@Test //testing inside try block
	public void getContextPathDefaultTest() throws ServletException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		when(corePageLocal.getServletContext()).thenReturn(request.getServletContext());
		when(corePageLocal.getServletContext().getRealPath("/")).thenReturn("/home");
		assertEquals("/home/", corePageLocal.getContextPath());
	}
	
	@Test
	public void getContextPathExceptionTest() {
		assertNotNull(corePage.getContextPath());
	}
	
	@Test
	public void getContextURITest() {
		corePage._contextURI = "/";
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
	
	//@Test 
	public void getContextURIExceptionTest() {
		doThrow(UnsupportedEncodingException.class).when(mock(URLDecoder.class, "decode"));
		assertEquals("../", corePage.getContextURI());
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
		corePage.httpRequest = httpRequest;
		assertNull(corePage.getParameter(""));
	}
	
	@Test
	public void sendRedirectTest() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = corePage.setupInstance(HttpRequestType.GET, request, response);
		corePageLocal.sendRedirect("/home");
	}
	
	@Test(expected = RuntimeException.class)
	public void sendRedirectExceptionTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = corePage.setupInstance(HttpRequestType.GET, request, response);
		doThrow(IOException.class).when(response).sendRedirect("/home");
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
	
	// @Test
	// public void outputJSONTest() throws Exception {
	// 	Map<String, Object> map = new HashMap<String, Object>();
	// 	Map<String, Object> template = new HashMap<String, Object>();
	// 	map.put("user", new String[] { "me" });
	// 	File file = new File("test-files/tmp/servlet/me.txt");
	// 	PrintWriter printWriter = new PrintWriter(file);
	// 	assertTrue(corePage.outputJSON(map, template, printWriter));
	// 	printWriter.flush();
	// 	printWriter.close();
	// 	File rFile = new File("test-files/tmp/servlet/me.txt");
	// 	BufferedReader expected = new BufferedReader(new FileReader(rFile));
	// 	String line;
	// 	while ((line = expected.readLine()) != null) {
	// 		assertEquals(ConvertJSON.fromObject(map), line);
	// 	}
	// 	expected.close();
	// 	rFile.delete();
	// }
	// 
	// @Test
	// public void outputJSONHTTPResponseNotNullTest() throws Exception {
	// 	Map<String, Object> map = new HashMap<String, Object>();
	// 	Map<String, Object> template = new HashMap<String, Object>();
	// 	map.put("user", new String[] { "me" });
	// 	File file = new File("test-files/tmp/servlet/me.txt");
	// 	PrintWriter printWriter = new PrintWriter(file);
	// 	HttpServletResponse response = mock(HttpServletResponse.class);
	// 	corePage.httpResponse = response;
	// 	assertTrue(corePage.outputJSON(map, template, printWriter));
	// 	printWriter.flush();
	// 	printWriter.close();
	// 	File rFile = new File("test-files/tmp/servlet/me.txt");
	// 	BufferedReader expected = new BufferedReader(new FileReader(rFile));
	// 	String line;
	// 	while ((line = expected.readLine()) != null) {
	// 		assertEquals(ConvertJSON.fromObject(map), line);
	// 	}
	// 	expected.close();
	// 	rFile.delete();
	// }
	// 
	// @Test
	// public void outputJSONExceptionTest() throws Exception {
	// 	Map<String, Object> map = new HashMap<String, Object>();
	// 	Map<String, Object> template = new HashMap<String, Object>();
	// 	map.put("user", new String[] { "me" });
	// 	File file = new File("test-files/tmp/servlet/me.txt");
	// 	PrintWriter printWriter = new PrintWriter(file);
	// 	assertFalse(corePage.outputJSONException(map, template, printWriter, new Exception(
	// 		"There is an error")));
	// 	printWriter.flush();
	// 	printWriter.close();
	// 	File rFile = new File("test-files/tmp/servlet/me.txt");
	// 	BufferedReader expected = new BufferedReader(new FileReader(rFile));
	// 	String line;
	// 	Map<String, String> ret = new HashMap<String, String>();
	// 	ret.put("error", org.apache.commons.lang3.exception.ExceptionUtils
	// 		.getStackTrace(new Exception("There is an error")));
	// 	while ((line = expected.readLine()) != null) {
	// 		assertNotNull(line);
	// 	}
	// 	expected.close();
	// 	rFile.delete();
	// }
	// 
	// @Test
	// public void outputJSONExceptionHTTPResponseNotNullTest() throws Exception {
	// 	Map<String, Object> map = new HashMap<String, Object>();
	// 	Map<String, Object> template = new HashMap<String, Object>();
	// 	map.put("user", new String[] { "me" });
	// 	File file = new File("test-files/tmp/servlet/me.txt");
	// 	PrintWriter printWriter = new PrintWriter(file);
	// 	HttpServletResponse response = mock(HttpServletResponse.class);
	// 	corePage.httpResponse = response;
	// 	assertFalse(corePage.outputJSONException(map, template, printWriter, new Exception(
	// 		"There is an error")));
	// 	printWriter.flush();
	// 	printWriter.close();
	// 	File rFile = new File("test-files/tmp/servlet/me.txt");
	// 	BufferedReader expected = new BufferedReader(new FileReader(rFile));
	// 	String line;
	// 	Map<String, String> ret = new HashMap<String, String>();
	// 	ret.put("error", org.apache.commons.lang3.exception.ExceptionUtils
	// 		.getStackTrace(new Exception("There is an error")));
	// 	while ((line = expected.readLine()) != null) {
	// 		assertNotNull(line);
	// 	}
	// 	expected.close();
	// 	rFile.delete();
	// }
	
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
	
	@Test(expected = RuntimeException.class)
	public void contextInitializedExceptionTest() throws Exception {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		doThrow(Exception.class).when(corePageLocal).doSharedSetup();
		corePageLocal.contextInitialized(servletContextEvent);
	}
	
	@Test
	public void contextDestroyedTest() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		corePage.contextDestroyed(servletContextEvent);
	}
	
	@Test(expected = RuntimeException.class)
	public void contextDestroyedExceptionTest() throws Exception {
		ServletContextEvent servletContextEvent = new ServletContextEvent(mock(ServletContext.class));
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		doThrow(Exception.class).when(corePageLocal).destroyContext();
		corePageLocal.contextDestroyed(servletContextEvent);
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
		assertEquals("/home", corePage.requestWildcardUri());
	}
	
	@Test
	public void requestWildcardUriExceptionTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		when(request.getPathInfo()).thenThrow(Exception.class);
		assertNull(corePage.requestWildcardUri());
	}
	
	@Test
	public void requestWildcardUriArrayTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		corePage.httpRequest = request;
		assertArrayEquals(EmptyArray.STRING, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayAlternateTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("");
		corePage.httpRequest = request;
		assertArrayEquals(EmptyArray.STRING, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayNotEmptyTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/");
		corePage.httpRequest = request;
		assertArrayEquals(new String[] { "" }, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayNotEmptyAlternateTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("\\");
		corePage.httpRequest = request;
		assertArrayEquals(new String[] { "" }, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayValidPathTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/home/");
		corePage.httpRequest = request;
		assertArrayEquals(new String[] { "home" }, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayValidAlternatePathTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/home\\");
		corePage.httpRequest = request;
		assertArrayEquals(new String[] { "home" }, corePage.requestWildcardUriArray());
	}
	
	@Test
	public void requestWildcardUriArrayInvalidPathTest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("home");
		corePage.httpRequest = request;
		assertArrayEquals(new String[] { "home" }, corePage.requestWildcardUriArray());
	}
	
	@Test(expected = ServletException.class)
	public void setupInstanceExceptionTest() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		List<String> headerList = new ArrayList<String>();
		headerList.add("header_1");
		headerList.add("header_2");
		when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerList));
		when(response.getOutputStream()).thenThrow(Exception.class);
		corePage.setupInstance(HttpRequestType.GET, request, response);
	}
	
	@Test
	public void setupInstance2paramsTest() throws ServletException {
		Map<String, String[]> reqParam = new HashMap<String, String[]>();
		assertNotNull(corePage.setupInstance(HttpRequestType.GET, reqParam));
	}
	
	@Test
	public void setupInstance3paramsTest() throws ServletException {
		Map<String, String[]> reqParam = new HashMap<String, String[]>();
		Map<String, Cookie[]> reqCookieMap = new HashMap<String, Cookie[]>();
		assertNotNull(corePage.setupInstance(HttpRequestType.GET, reqParam, reqCookieMap));
	}
}
