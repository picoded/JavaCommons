package picodedTests.webTemplateEngines.PagesBuilder;

import java.io.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;
import picoded.conv.*;
import picoded.fileUtils.*;
import picoded.webTemplateEngines.PagesBuilder.*;

public class PagesBuilder_test {
	
	////////////////////////////////////////////////////////////
	//
	// Test vars
	//
	////////////////////////////////////////////////////////////
	
	// Test directory define
	//------------------------------------------------------------------------
	
	protected String templateTestDir = "test-files/test-specific/webTemplateEngines/PagesBuilder/";
	protected String outputTestDir = "test-files/tmp/webTemplateEngines/PagesBuilder/";
	protected String basicTemplateDir = templateTestDir + "basic-test-pages/";
	
	// Test vars
	//------------------------------------------------------------------------
	
	protected PagesBuilder pages = null;
	protected PagesBuilderCore html = null;
	
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
		pages = null;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Constructor test
	//
	////////////////////////////////////////////////////////////
	
	@Test
	public void constructorTest() {
		assertNotNull(pages = new PagesBuilder(basicTemplateDir, outputTestDir));
		assertNotNull(html = new PagesBuilderCore(basicTemplateDir));
	}
	
	////////////////////////////////////////////////////////////
	//
	// basic tests
	//
	////////////////////////////////////////////////////////////
	
	@Test
	public void basicPagesBuilderCoreTest() {
		constructorTest();
		String buffer = null;
		
		assertNotNull(buffer = html.prefixHTML("index"));
		assertTrue(buffer, buffer.indexOf("html") >= 0);
		
		assertNotNull(buffer = html.suffixHTML("index"));
		assertTrue(buffer, buffer.indexOf("/html") >= 0);
		
		assertNotNull(buffer = html.buildPageFrame("index"));
		assertTrue(buffer, buffer.indexOf("hello world") >= 0);
		assertTrue(buffer, buffer.indexOf("pageFrame") >= 0);
		assertTrue(buffer, buffer.indexOf("pageFrame_index") >= 0);
	}
	
	@Test
	public void buildPageBasicTest() throws IOException {
		constructorTest();
		pages.buildAndOutputPage("index");
		
		// Check asset folder
		assertEquals("is a COPYCAT: Meow!", FileUtils.readFileToString(new File(outputTestDir + "index/assets/bob.txt")));
	}
	
	@Test
	public void nestedPageTest() throws IOException {
		constructorTest();
		pages.buildAndOutputPage("nested/page");
		
		// Check asset folder
		assertTrue( FileUtils.readFileToString(new File(outputTestDir + "nested/page/index.html")).indexOf("Hello pageFrame_nested_page") > 0 );
	}
	
	@Test
	public void nestedPageAutoTest() throws IOException {
		constructorTest();
		pages.buildAllPages();
		
		// Check asset folder
		assertTrue( FileUtils.readFileToString(new File(outputTestDir + "nested/page/index.html")).indexOf("Hello pageFrame_nested_page") > 0 );
		assertTrue( FileUtils.readFileToString(new File(outputTestDir + "nested/two/page/index.html")).indexOf("Hello pageFrame_nested_two_page") > 0 );
	}
}
