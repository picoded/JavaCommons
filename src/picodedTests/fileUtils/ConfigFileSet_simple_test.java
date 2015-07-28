package picodedTests.fileUtils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;

// Test Case include
import org.junit.*;

import static org.junit.Assert.*;
import picoded.fileUtils.*;

///
/// Test Case for picoded.fileUtils.ConfigFileSet
///
public class ConfigFileSet_simple_test {
	
	public String testDir = "./test-files/test-specific/fileUtils/ConfigFileSet";
	
	protected ConfigFileSet configObj = null;
	
	@Before
	public void setUp() {
		File tFile = new File(testDir);
		assertTrue( tFile.canRead() );
		
		configObj = new ConfigFileSet();//.addConfigSet(testDir);
	}
	
	@After
	public void tearDown() {
		configObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull( configObj );
	}
	
	//@Test
	//wrote a function to dump out all files in a folder, with a separator...unused at the moment, but might be useful in a utils lib one day
	public void testKeyGen(){
		File fileSetFolder = new File(testDir);
		List<String> keys = configObj.getFileNamesFromFolder(fileSetFolder, "", ".");
	}
	
	@Test
	public void testRecursiveFunc(){
		File fileSetFolder = new File(testDir);
		configObj.addConfigSet(fileSetFolder, "", ".");
		
		//main.ini
		assertEquals("hello", configObj.get("main.main-include.test"));
		assertEquals("headerHello", configObj.get("main.header.test"));
		
		//related.ini
		assertEquals("hi", configObj.get("related.main-include.test"));
		assertEquals("relatedHeaderHi", configObj.get("related.relatedHeader.test"));
		
		//firstInnerFolder.firstInner
		assertEquals("hello", configObj.get("firstInnerFolder.firstInner.main-include.test"));
		assertEquals("headerHello", configObj.get("firstInnerFolder.firstInner.header.test"));
		
		//firstInnerFolder.secondInnerFolder.secondInner
		assertEquals("hello", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.main-include.test"));
		assertEquals("12", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.main-include.number"));
		assertEquals("headerHello", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.header.test"));
		assertEquals("13", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.header.number"));
		
		//test json
		assertEquals("hello", configObj.get("iniTestFileJSON.test"));
		assertEquals("123", configObj.get("iniTestFileJSON.number"));
	}
}
