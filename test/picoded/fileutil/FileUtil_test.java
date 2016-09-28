package picoded.fileutil;

import static org.junit.Assert.assertEquals;
// Test Case include
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

///
/// Test Case for picoded.FileUtil.ConfigFileSet
///
public class FileUtil_test {
	
	// Test directories and setup
	//----------------------------------------------------------------------------------------------------
	public static String testDirStr = "./test-files/test-specific/fileutils/FileUtils/";
	public File testDir = new File(testDirStr);
	public String outputDirStr = "./test-files/tmp/fileutils/FileUtils/";
	public File outputDir = new File(outputDirStr);
	
	/// Invalid constructor test
		@Test(expected = IllegalAccessError.class)
		public void invalidConstructor() throws Exception {
			new FileUtil();
	}
		
	@Before
	public void setUp() {
		outputDir.mkdirs();
		test_res = null;
		fileCollection = new ArrayList<File>();
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
		
		// encoding null test
		String str = null;
		assertNotNull(FileUtil.readFileToString(new File(testDir, "doubleSlash.txt"), str));
		// encoding empty test
		str = "";
		assertNotNull(FileUtil.readFileToString(new File(testDir, "doubleSlash.txt"), str));
		// encoding not empty
		str = "US-ASCII";
		assertNotNull(FileUtil.readFileToString(new File(testDir, "doubleSlash.txt"), str));
	}
	
	// Write read test cases
	//----------------------------------------------------------------------------------------------------
	@Test
	public void readFileToStringWithFallback() throws IOException {
		assertEquals(null, FileUtil.readFileToString_withFallback(null, null, null));
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "jsRegex.js"), null, null));
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(null, "test", "US-ASCII"));
		assertEquals("test", FileUtil.readFileToString_withFallback(null, "test", "US-ASCII"));
		assertEquals(null, FileUtil.readFileToString_withFallback(null, null, "US-ASCII"));
		assertEquals("test", FileUtil.readFileToString_withFallback(null, "test", null));
		assertEquals("", FileUtil.readFileToString_withFallback(new File(""), "", ""));
		assertEquals("", FileUtil.readFileToString_withFallback(new File(""), "", ""));
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "jsRegex.js"), "", ""));
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "doubleSlash.txt"), "", ""));
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(new File(testDir, "doubleSlash.txt"), "", "US-ASCII"));
		assertEquals("test", FileUtil.readFileToString_withFallback(new File(""), "test", "US-ASCII"));
		assertEquals("test", FileUtil.readFileToString_withFallback(new File(""), "test", "US-ASCII"));
	}
	
	@Test
	public void writeReadDoubleSlash() throws IOException {
		File outFile = new File(outputDir, "jsRegex.js");
		FileUtil.writeStringToFile(outFile, test_jsRegex);
		assertNotNull(test_res = FileUtil.readFileToString(outFile));
		assertEquals(test_jsRegex, test_res.trim());
		
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(outFile, null));
		assertEquals(null, FileUtil.readFileToString_withFallback(null, null));
		assertEquals(test_res, FileUtil.readFileToString_withFallback(outFile, "test"));
		assertEquals("", FileUtil.readFileToString_withFallback(new File(""), ""));
		
		String str = null;
		assertEquals(null, FileUtil.readFileToString_withFallback(null, null, str));
		str="";
		assertEquals("", FileUtil.readFileToString_withFallback(new File(""), "", str));
		// encoding not empty
		str = "US-ASCII";
		assertNotNull(test_res = FileUtil.readFileToString_withFallback(outFile, null, str));
		assertEquals(test_res, FileUtil.readFileToString_withFallback(outFile, "test", str));
		assertEquals(test_res, FileUtil.readFileToString_withFallback(outFile, "", str));
		
		
		assertEquals(test_jsRegex, test_res.trim());
		str = null;
		FileUtil.writeStringToFile(outFile, test_jsRegex, str);
		str="";
		FileUtil.writeStringToFile(outFile, test_jsRegex, str);
		// encoding not empty
		str = "US-ASCII";
		
		FileUtil.writeStringToFile(outFile, test_jsRegex, str);
	}
	
	@Test
	public void writeStringToFileIfDifferant() throws IOException {
		File outFile = new File(outputDir, "jsRegex.js");
		FileUtil.writeStringToFile_ifDifferant(outFile, test_jsRegex);
		outFile = new File(outputDir, "doubleSlash.txt");
		FileUtil.writeStringToFile_ifDifferant(outFile, test_jsRegex);
		outFile = new File(outputDir, "test.js");
		FileUtil.writeStringToFile_ifDifferant(outFile, test_jsRegex);
	}
	
	/// Test for list Dirs
	@Test
	public void testListDirs() throws IOException {
		assertNotNull(FileUtil.listDirs(new File("./test-files/test-specific/fileutils/")));
		assertNotNull(FileUtil.listDirs(testDir));
		assertNotNull(FileUtil.listDirs(outputDir));
		assertEquals(new ArrayList<File>(), FileUtil.listDirs(null));
	}
	
	/// Test for Copy Directory If Different
	@Test
	public void testCopyDirectoryIfDifferent() throws IOException {
		FileUtil.copyDirectory_ifDifferent(testDir, outputDir);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/"), new File("./test-files/tmp/"));
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File("./test-files/tmp/"));
		
		FileUtil.copyDirectory_ifDifferent(testDir, outputDir, true);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/"), new File(
			"./test-files/tmp/"), true);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File("./test-files/tmp/"), true);
	
		FileUtil.copyDirectory_ifDifferent(testDir, outputDir, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/"), new File(
			"./test-files/tmp/"), false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File("./test-files/tmp/"), false);
		
		FileUtil.copyDirectory_ifDifferent(testDir, outputDir, true, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/"), new File(
			"./test-files/tmp/"), true, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File("./test-files/tmp/"), true, false);
		
		FileUtil.copyDirectory_ifDifferent(testDir, outputDir, false, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/"), new File(
			"./test-files/tmp/"), false, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File("./test-files/tmp/"), false, false);
		FileUtil.copyDirectory_ifDifferent(new File("./test-files/test-specific/fileutils/ConfigFile/"),
				new File(""), false, false);
	}
	
	/// Test for Copy Directory If Different
	@Test
	public void testCopyFileIfDifferentCreateSymbolicLink() throws IOException {
		Path existingFilePath = Paths.get(testDirStr + "jsRegex.js");
		Path symLinkPath = Paths.get(outputDirStr+ "jsRegexLink.js");
		Files.createSymbolicLink(symLinkPath, existingFilePath);
		FileUtil.copyFile_ifDifferent(existingFilePath.toFile(), symLinkPath.toFile(), false, false);
		symLinkPath.toFile().delete();
	}
	 
	@Test
	public void testCopyFileIfDifferent() throws IOException {
		Path existingFilePath = Paths.get(testDirStr + "jsRegex.js");
		Path symLinkPath = Paths.get(outputDirStr+ "jsRegexLink.js");
		FileUtil.copyFile_ifDifferent(existingFilePath.toFile(), symLinkPath.toFile());
		FileUtil.copyFile_ifDifferent(existingFilePath.toFile(), symLinkPath.toFile(), true);
		symLinkPath.toFile().delete();
		existingFilePath = Paths.get(testDirStr + "doubleSlash.txt");
		symLinkPath = Paths.get(outputDirStr+ "doubleSlashLink.txt");
		Files.createSymbolicLink(symLinkPath, existingFilePath);
		FileUtil.copyFile_ifDifferent(existingFilePath.toFile(), symLinkPath.toFile(), false, true);
		symLinkPath.toFile().delete();
	}
	/// Test for Newest File Timestamp
	@Test
	public void testNewestFileTimestamp() throws IOException {
		assertEquals(0L, FileUtil.newestFileTimestamp(null));
		assertEquals(0L, FileUtil.newestFileTimestamp(new File("")));
		assertNotNull(FileUtil.newestFileTimestamp(new File(testDirStr + "jsRegex.js")));
		assertNotNull(FileUtil.newestFileTimestamp(new File(testDirStr + "doubleSlash.txt")));
		assertNotNull(FileUtil.newestFileTimestamp(new File("./test-files/test-specific/fileutils/")));
		List<String> excludeNames = new ArrayList<String>();
		excludeNames.add("jsRegex.js");
		excludeNames.add("doubleSlash.txt");
		assertEquals(0L, FileUtil.newestFileTimestamp(null, null));
		assertEquals(0L, FileUtil.newestFileTimestamp(new File(""), new ArrayList<String>()));
		assertNotNull(FileUtil.newestFileTimestamp(new File("./test-files/test-specific/fileutils/"), excludeNames));
		assertNotNull(FileUtil.newestFileTimestamp(new File("./test-files/test-specific/fileutils/ConfigFile"), null));
		
	}
	
	/// Test for Get Base Name
	@Test
	public void testGetBaseName() throws IOException {
		assertEquals(null, FileUtil.getBaseName(null));
		assertEquals("", FileUtil.getBaseName(""));
		assertEquals("jsRegex", FileUtil.getBaseName("jsRegex.js"));
		assertEquals("doubleSlash", FileUtil.getBaseName("doubleSlash.txt"));
	}
	
	/// Test for Get Base Name
	@Test
	public void testGetExtension() throws IOException {
		assertEquals(null, FileUtil.getExtension(null));
		assertEquals("", FileUtil.getExtension(""));
		assertEquals("js", FileUtil.getExtension("jsRegex.js"));
		assertEquals("txt", FileUtil.getExtension("doubleSlash.txt"));
	}
	
	/// Test for Get Full Path
	@Test
	public void testGetFullPath() throws IOException {
		assertEquals(null, FileUtil.getFullPath(null));
		assertEquals("", FileUtil.getFullPath(""));
		assertEquals(testDirStr, FileUtil.getFullPath(testDirStr + "jsRegex.js"));
		assertEquals(testDirStr, FileUtil.getFullPath(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Get Full Path No End Separator
	@Test
	public void testGetFullPathNoEndSeparator() throws IOException {
		assertEquals(null, FileUtil.getFullPathNoEndSeparator(null));
		assertEquals("", FileUtil.getFullPathNoEndSeparator(""));
		String path = testDirStr.substring(0, testDirStr.length() - 1);
		assertEquals(path, FileUtil.getFullPathNoEndSeparator(testDirStr + "jsRegex.js"));
		assertEquals(path, FileUtil.getFullPathNoEndSeparator(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Get Name
	@Test
	public void testGetName() throws IOException {
		assertEquals(null, FileUtil.getName(null));
		assertEquals("", FileUtil.getName(""));
		assertEquals("jsRegex.js", FileUtil.getName(testDirStr + "jsRegex.js"));
		assertEquals("doubleSlash.txt", FileUtil.getName(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Get Name
	@Test
	public void testGetPath() throws IOException {
		assertEquals(null, FileUtil.getName(null));
		assertEquals("", FileUtil.getName(""));
		assertEquals(testDirStr, FileUtil.getPath(testDirStr + "jsRegex.js"));
		assertEquals(testDirStr, FileUtil.getPath(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Get Path No End Separator
	@Test
	public void testGetPathNoEndSeparator() throws IOException {
		assertEquals(null, FileUtil.getPathNoEndSeparator(null));
		assertEquals("", FileUtil.getPathNoEndSeparator(""));
		String path = testDirStr.substring(0, testDirStr.length() - 1);
		assertEquals(path, FileUtil.getPathNoEndSeparator(testDirStr + "jsRegex.js"));
		assertEquals(path, FileUtil.getPathNoEndSeparator(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Normalize
	@Test
	public void testNormalize() throws IOException {
		assertEquals(null, FileUtil.normalize(null));
		assertEquals("", FileUtil.normalize(""));
		String path = testDirStr.substring(2);
		assertEquals(path + "jsRegex.js", FileUtil.normalize(testDirStr + "jsRegex.js"));
		assertEquals(path + "doubleSlash.txt", FileUtil.normalize(testDirStr + "doubleSlash.txt"));
	}
	
	/// Test for Normalize
	@Test
	public void testGetFilePaths() throws IOException {
		List<String> filePathsList = new ArrayList<String>();
		assertEquals(filePathsList, FileUtil.getFilePaths(null));
		assertEquals(filePathsList, FileUtil.getFilePaths(new File("")));
		assertNotNull(FileUtil.getFilePaths(new File(testDirStr)));
		assertNotNull(FileUtil.getFilePaths(new File("./test-files/test-specific/fileutils/")));
		filePathsList.add("jsRegex");
		assertEquals(filePathsList, FileUtil.getFilePaths(new File(testDirStr + "jsRegex.js")));
		assertEquals(filePathsList, FileUtil.getFilePaths(new File(testDirStr + "jsRegex.js"), "/"));
		filePathsList = new ArrayList<String>();
		filePathsList.add("doubleSlash");
		assertEquals(filePathsList, FileUtil.getFilePaths(new File(testDirStr + "doubleSlash.txt"), "/"));
		assertEquals(filePathsList, FileUtil.getFilePaths(new File(testDirStr + "doubleSlash.txt")));
	}
}
