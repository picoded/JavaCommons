package picodedTests.fileUtils;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.fileUtils.ConfigFileSet;

///
/// Test Case for picoded.fileUtils.ConfigFileSet
///
public class ConfigFileSet_simple_test {
	
	public String testDir = "./test-files/test-specific/fileUtils/ConfigFileSet";
	
	protected ConfigFileSet configObj = null;
	
	@Before
	public void setUp() {
		File tFile = new File(testDir);
		assertTrue(tFile.canRead());
		
		configObj = new ConfigFileSet(tFile);// .addConfigSet(testDir);
		
		// File fileSetFolder = new File(testDir);
		// configObj.addConfigSet(fileSetFolder, "", ".");
	}
	
	@After
	public void tearDown() {
		configObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(configObj);
	}
	
	// @Test
	// wrote a function to dump out all files in a folder, with a
	// separator...unused at the moment, but might be useful in a utils lib one
	// day
	public void testKeyGen() {
		// File fileSetFolder = new File(testDir);
		// List<String> keys = configObj.getFileNamesFromFolder(fileSetFolder,
		// "", ".");
	}
	
	@Test
	public void testRecursiveFunc() {
		// main.ini
		assertEquals("hello", configObj.get("main.main-include.test"));
		assertEquals("headerHello", configObj.get("main.header.test"));
		
		// related.ini
		assertEquals("hi", configObj.get("related.main-include.test"));
		assertEquals("relatedHeaderHi", configObj.get("related.relatedHeader.test"));
		
		// firstInnerFolder.firstInner
		assertEquals("hello", configObj.get("firstInnerFolder.firstInner.main-include.test"));
		assertEquals("headerHello", configObj.get("firstInnerFolder.firstInner.header.test"));
		
		// firstInnerFolder.secondInnerFolder.secondInner
		assertEquals("hello", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.main-include.test"));
		assertEquals("12", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.main-include.number"));
		assertEquals("headerHello", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.header.test"));
		assertEquals("13", configObj.get("firstInnerFolder.secondInnerFolder.secondInner.header.number"));
		
		//		// test json
		assertEquals("hello", configObj.get("iniTestFileJSON.test"));
		assertEquals("123", configObj.get("iniTestFileJSON.number"));
	}
	
	@Test
	public void subMapTest() {
		assertNotNull(configObj.createSubMap(null));
		assertNotNull(configObj.get("firstInnerFolder"));
		assertNotNull(configObj.createSubMap(null, "firstInnerFolder"));
		
		assertNotEquals("hello",
			configObj.createSubMap(null, "firstInnerFolder").get("firstInnerFolder.main-include.test"));
		assertNull(configObj.createSubMap(null, "firstInnerFolder").get("firstInnerFolder"));
	}
	
	@Test
	public void multiMap_firstLayerTest() {
		assertNotNull(configObj.get("firstInnerFolder")); // should be a map
		assertNull(configObj.get("ThisShouldNotExists")); // should be a map
	}
	
	///
	/// I blame yiwei for not making this readable
	///
	/// #_# : ROARS!
	///
	@Test
	public void multiMapFetch() {
		assertNotNull(configObj.getGenericConvertStringMap("firstInnerFolder")); // should be a map
		assertNotNull(configObj.getGenericConvertStringMap("firstInnerFolder").get("secon")); // should be a map
		
		assertEquals("2+1", configObj.get("firstInnerFolder.second.3"));
		assertNotNull(configObj.getGenericConvertStringMap("firstInnerFolder"));
		assertEquals("2+1", configObj.getGenericConvertStringMap("firstInnerFolder").get("second.3"));
		
		assertNotNull(configObj.getGenericConvertStringMap("firstInnerFolder.second"));
		assertEquals("2+1", configObj.getGenericConvertStringMap("firstInnerFolder.second").get("3"));
		
		assertNotNull(configObj.getGenericConvertStringMap("firstInnerFolder").getGenericConvertStringMap("second"));
		assertEquals("2+1", configObj.getGenericConvertStringMap("firstInnerFolder").getGenericConvertStringMap("second")
			.get("3"));
		
		assertNotNull("Quarter", configObj.get("firstInnerFolder.secondLayerInnerFolder.2"));
		assertEquals("Quarter", configObj.get("firstInnerFolder.secondLayerInnerFolder.2.test.test.test.testing"));
		assertEquals("Quarter",
			configObj.getGenericConvertStringMap("firstInnerFolder.secondLayerInnerFolder")
				.get("2.test.test.test.testing"));
		
		assertEquals(
			"final",
			configObj.getGenericConvertStringMap("firstInnerFolder.secondLayerInnerFolder").get(
				"2.test.test.test.test.test.test"));
		assertEquals("foo", configObj.getGenericConvertStringMap("firstInnerFolder.second").get("main.int"));
		assertEquals("testing",
			configObj.getGenericConvertStringMap("firstInnerFolder").get("secondLayerInnerFolder.2.test.subtest"));
		assertNotNull(configObj.get("firstInnerFolder.second.include"));
		assertNull(configObj
			.getGenericConvertStringMap("firstInnerFolder.secondLayerInnerFolder.2.test.test.test.test.test.nulltesT"));
		assertNotNull(configObj.get("firstInnerFolder.second"));
		assertEquals("result", configObj.get("firstInnerFolder.second.function1.return"));
		assertEquals("InnerFolder",
			configObj.getGenericConvertStringMap("firstInnerFolder").getGenericConvertStringMap("second").get("Layer"));
		assertEquals("2+1", configObj.get("firstInnerFolder.second.3"));
	}
	
	@Test
	public void getKeySetTest() {
		Set<String> set = configObj.getGenericConvertStringMap("keySetTest").keySet();
		assertTrue(set.contains("one"));
		assertTrue(set.contains("two"));
	}
	
	///
	/// Get the config file set for sys.JStack.stack bug
	///
	@Test
	public void jstackConfig() {
		assertNotNull(configObj.getGenericConvertStringMap("sys.JStack"));
		assertNotNull(configObj.get("sys.JStack.stack"));
		
	}
}
