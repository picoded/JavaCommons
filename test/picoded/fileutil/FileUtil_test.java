package picoded.fileutil;

import static org.junit.Assert.assertEquals;
// Test Case include
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

///
/// Test Case for picoded.FileUtil.ConfigFileSet
///
public class FileUtil_test {

	// Test directories and setup
	//----------------------------------------------------------------------------------------------------
	public String testDirStr = "./test-files/test-specific/fileutils/FileUtils/";
	public File testDir = new File(testDirStr);
	
	public String outputDirStr = "./test-files/tmp/fileutils/FileUtils/";
	public File outputDir = new File(outputDirStr);
	
	@Before
	public void setUp() {
		outputDir.mkdirs();
		test_res = null;
		fileCollection=new ArrayList<File>();
	}
	
	// Test variables
	//----------------------------------------------------------------------------------------------------
	String test_doubleSlash = "\\\\";
	String test_jsRegex = "pathname = pathname.replace(/\\\\/g, '/');";
	
	String test_res = null; //tmp testing variable
	Collection<File> fileCollection = null;
	// Read only test cases
	//----------------------------------------------------------------------------------------------------
	
	/// Test for double slash safely taken
	@Test
	public void readDoubleSlash() throws IOException {
		assertNotNull(test_res = FileUtil.readFileToString(new File(testDir, "doubleSlash.txt")));
		assertEquals(test_doubleSlash, test_res.trim());
		
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "doubleSlash.txt"), null));
		assertEquals(test_doubleSlash, test_res.trim());
	}
	
	/// Test for double slash safely taken
	@Test
	public void readJSRegex() throws IOException {
		assertNotNull(test_res = FileUtil.readFileToString(new File(testDir, "jsRegex.js")));
		assertEquals(test_jsRegex, test_res.trim());
		
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "jsRegex.js"), null));
		assertEquals(test_jsRegex, test_res.trim());
	}
	
	// Write read test cases
	//----------------------------------------------------------------------------------------------------
	
	@Test
	public void writeReadDoubleSlash() throws IOException {
		File outFile = new File(outputDir, "jsRegex.js");
		FileUtil.writeStringToFile(outFile, test_jsRegex);
		
		assertNotNull(test_res = FileUtil.readFileToString(outFile));
		assertEquals(test_jsRegex, test_res.trim());
		
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(outFile, null));
		assertEquals(test_jsRegex, test_res.trim());
	}
}
