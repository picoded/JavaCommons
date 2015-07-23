package picodedTests.fileUtils;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		File tFile = new File(testDir+"/iniTestFile.ini");
		assertTrue( tFile.canRead() );
		
		configObj = new ConfigFileSet().addConfigSet(testDir+"/iniTestFile.ini");
	}
	
	@After
	public void tearDown() {
		configObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull( configObj );
	}
	
	@Test
	public void testString(){
		String helloVal = (String)configObj.get("iniTestFile.main.hello");
		System.out.println(helloVal);
	}
	
}
