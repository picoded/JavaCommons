package picodedTests.fileUtils;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.File;
import java.util.*;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.fileUtils.*;

///
/// Test Case for picoded.fileUtils.ConfigFileSet
///
public class FileUtils_test {
	
	// Test directories and setup
	//----------------------------------------------------------------------------------------------------
	public String testDirStr = "./test-files/test-specific/fileUtils/FileUtils/";
	public File testDir = new File(testDirStr);
	
	public String outputDirStr = "./test-files/tmp/fileUtils/FileUtils/";
	public File outputDir = new File(outputDirStr);
	
	@Before
	public void setUp() {
		outputDir.mkdirs();
		test_res = null;
	}
	
	// Test variables
	//----------------------------------------------------------------------------------------------------
	String test_doubleSlash = "\\\\";
	String test_jsRegex = "pathname = pathname.replace(/\\\\/g, '/');";
	
	String test_res = null; //tmp testing variable
	
	// Read only test cases
	//----------------------------------------------------------------------------------------------------
	
	/// Test for double slash safely taken
	@Test
	public void readDoubleSlash() throws IOException {
		assertNotNull(test_res = FileUtils.readFileToString(new File(testDir, "doubleSlash.txt")));
		assertEquals(test_doubleSlash, test_res.trim());
		
		assertNotNull(test_res = FileUtils.readFileToString_withFallback(new File(testDir, "doubleSlash.txt"), null));
		assertEquals(test_doubleSlash, test_res.trim());
	}
	
	/// Test for double slash safely taken
	@Test
	public void readJSRegex() throws IOException {
		assertNotNull(test_res = FileUtils.readFileToString(new File(testDir, "jsRegex.js")));
		assertEquals(test_jsRegex, test_res.trim());
		
		assertNotNull(test_res = FileUtils.readFileToString_withFallback(new File(testDir, "jsRegex.js"), null));
		assertEquals(test_jsRegex, test_res.trim());
	}
	
	// Write read test cases
	//----------------------------------------------------------------------------------------------------
	
	@Test
	public void writeReadDoubleSlash() throws IOException {
		File outFile = new File(outputDir, "jsRegex.js");
		FileUtils.writeStringToFile(outFile, test_jsRegex);
		
		assertNotNull(test_res = FileUtils.readFileToString(outFile));
		assertEquals(test_jsRegex, test_res.trim());
		
		assertNotNull(test_res = FileUtils.readFileToString_withFallback(outFile, null));
		assertEquals(test_jsRegex, test_res.trim());
	}
	
}
