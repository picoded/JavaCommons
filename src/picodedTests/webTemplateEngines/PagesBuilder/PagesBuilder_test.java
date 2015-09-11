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
	protected String basicTemplateDir = templateTestDir+"basic-test-pages/";
	
	// Test vars
	//------------------------------------------------------------------------
	
	protected PagesBuilder pages = null;
	protected PagesHTML html = null;
	
	////////////////////////////////////////////////////////////
	//
	// Test setup / teardown
	//
	////////////////////////////////////////////////////////////
	
	@Before
	public void setUp() {
		// Ensure output directory is set
		File outputDir = new File(outputTestDir);
		
		if( outputDir.exists() ) {
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
		assertNotNull(html = new PagesHTML(basicTemplateDir));
	}
	
	////////////////////////////////////////////////////////////
	//
	// basic tests
	//
	////////////////////////////////////////////////////////////
	
	@Test 
	public void basicPagesHTMLTest() {
		constructorTest();
		String buffer = null;
		
		assertNotNull(buffer = html.prefixHTML());
		assertTrue( buffer, buffer.indexOf("html") >= 0 );
		
		assertNotNull(buffer = html.suffixHTML());
		assertTrue( buffer, buffer.indexOf("/html") >= 0 );
		
		assertNotNull(buffer = html.buildPageFrame("index"));
		assertTrue( buffer, buffer.indexOf("hello world") >= 0 );
		assertTrue( buffer, buffer.indexOf("pageFrame") >= 0 );
		assertTrue( buffer, buffer.indexOf("pageFrame_index") >= 0 );
	}
	
	@Test
	public void buildPageBasicTest() throws IOException {
		constructorTest();
		pages.buildPage("index");
		
		// Check asset folder
		assertEquals("is a COPYCAT: Meow!", FileUtils.readFileToString(new File(outputTestDir+"index/assets/bob.txt")) );
	}
}
