package picodedTests.fileUtils;

// Target test class
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Other imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.fileUtils.DeleteFilesByAge;

///
/// Test Case for picoded.fileUtils.DeleteFilesByAge
///
public class DeleteFilesByAge_test {
	
	public String testDir = "./test-files/tmp/fileUtils/DeleteFilesByAge";
	
	@Before
	public void setUp() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
		(new File(testDir)).mkdirs();
		
		PrintWriter pWriter;
		
		pWriter = new PrintWriter(testDir + "/olderFile.txt", "UTF-8");
		pWriter.println("olderFile");
		pWriter.close();
		
		// Pause for 2 seconds
		Thread.sleep(2000);
		
		pWriter = new PrintWriter(testDir + "/newerFile.txt", "UTF-8");
		pWriter.println("newerFile");
		pWriter.close();
	}
	
	@After
	public void tearDown() {
		// (new File("./test-files/tmp/olderFile.txt")).delete();
		// (new File("./test-files/tmp/newerFile.txt")).delete();
	}
	
	// / Test the following functions
	// /
	// / + DeleteFilesByAge.olderThenGivenAgeInSeconds
	// / + DeleteFilesByAge.olderThenUnixTimestamp (via
	// olderThenGivenAgeInSeconds)
	@Test
	public void basicFileAgeTesting() {
		assertTrue((new File(testDir + "/olderFile.txt")).isFile());
		assertTrue((new File(testDir + "/newerFile.txt")).isFile());
		
		DeleteFilesByAge.olderThenGivenAgeInSeconds(testDir, 1);
		
		assertFalse((new File(testDir + "/olderFile.txt")).isFile());
		assertTrue((new File(testDir + "/newerFile.txt")).isFile());
	}
	
}