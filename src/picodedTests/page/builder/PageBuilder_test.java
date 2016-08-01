package picodedTests.page.builder;

import java.io.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;
import picoded.conv.*;
import picoded.fileUtils.*;
import picoded.page.builder.*;

public class PageBuilder_test {
	
	////////////////////////////////////////////////////////////
	//
	// Test vars
	//
	////////////////////////////////////////////////////////////
	
	// Test directory define
	//------------------------------------------------------------------------
	
	protected String templateTestDir = "test-files/test-specific/page/builder/";
	protected String outputTestDir = "test-files/tmp/page/builder/";
	protected String basicTemplateDir = templateTestDir + "basic-test-page/";
	
	// Test vars
	//------------------------------------------------------------------------
	
	protected PageBuilder page = null;
	protected PageBuilderCore html = null;
	
	////////////////////////////////////////////////////////////
	//
	// Test setup / teardown
	//
	////////////////////////////////////////////////////////////
	
	@Before
	public void setUp() {
		// Ensure output directory is set
		File outputDir = new File(outputTestDir);
		
		if (outputDir.exists()) {
			outputDir.delete();
		}
		outputDir.mkdirs();
	}
	
	@After
	public void tearDown() {
		page = null;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Constructor test
	//
	////////////////////////////////////////////////////////////
	
	@Test
	public void constructorTest() {
		assertNotNull(page = new PageBuilder(basicTemplateDir, outputTestDir));
		assertNotNull(html = new PageBuilderCore(basicTemplateDir));
	}
	
	////////////////////////////////////////////////////////////
	//
	// basic tests
	//
	////////////////////////////////////////////////////////////
	
	@Test
	public void basicPageBuilderCoreTest() {
		constructorTest();
		String buffer = null;
		
		assertNotNull(buffer = html.prefixHTML("index"));
		assertTrue(buffer, buffer.indexOf("html") >= 0);
		
		assertNotNull(buffer = html.suffixHTML("index"));
		assertTrue(buffer, buffer.indexOf("/html") >= 0);
		
		assertNotNull(buffer = html.buildPageFrame("index"));
		assertTrue(buffer, buffer.indexOf("hello world") >= 0);
		assertTrue(buffer, buffer.indexOf("pageFrame") >= 0);
		assertTrue(buffer, buffer.indexOf("page-index") >= 0);
	}
	
	@Test
	public void buildPageBasicTest() throws IOException {
		constructorTest();
		page.buildAndOutputPage("index");
		
		// Check asset folder
		assertEquals("is a COPYCAT: Meow!", FileUtils.readFileToString(new File(outputTestDir + "index/assets/bob.txt")));
	}
	
	@Test
	public void nestedPageTest() throws IOException {
		constructorTest();
		page.buildAndOutputPage("nested/page");
		
		// Check asset folder
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "nested/page/index.html")).indexOf(
			"Hello page-nested-page") > 0);
	}
	
	@Test
	public void nestedPageAutoTest() throws IOException {
		constructorTest();
		page.buildAllPages();
		
		// Check asset folder
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "nested/page/index.html")).indexOf(
			"Hello page-nested-page") > 0);
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "nested/two/page/index.html")).indexOf(
			"Hello page-nested-two-page") > 0);
	}
	
	@Test 
	public void componentsMapTest() {
		constructorTest();
		
		assertEquals("Hello ${PageClass}", page.buildPageComponentMap().getSubMap("nested").getSubMap("page").get("html") );
		assertEquals("Hello ${PageClass}", page.buildPageComponentMap().getSubMap("nested").getSubMap("two").getSubMap("page").get("html") );
		
		assertNotNull( page.buildPageComponentMap().getSubMap("components").getSubMap("utils").getSubMap("IEWarning").get("html") );
	}
	
	@Test
	public void componentsSubstitute() throws IOException {
		constructorTest();
		page.buildAllPages();
		
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "helloComponents/msg/index.html")).indexOf(
			"<h1>Hello World</h1>") >= 0);
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "helloComponents/world/index.html")).indexOf(
			"<page-helloComponents-msg/>") < 0);
		assertTrue(FileUtils.readFileToString(new File(outputTestDir + "helloComponents/world/index.html")).indexOf(
			"<h1>Hello World</h1>") >= 0);
	}
}
