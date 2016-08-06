package picodedTests.fileUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.*;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.fileUtils.ConfigFile;

///
/// Test Case for picoded.fileUtils.ConfigFile
///
public class ConfigFile_test {
	
	public String testDir = "./test-files/test-specific/fileUtils/ConfigFile";
	protected ConfigFile configObj = null;
	protected ConfigFile jsConfigObj = null;
	
	@Before
	public void setUp() {
		File tFile = new File(testDir + "/iniTestFile.ini");
		boolean canRead = tFile.canRead();
		assertTrue(canRead);
		
		File jsFile = new File(testDir + "/iniTestFileJSON.js");
		boolean canReadJSON = jsFile.canRead();
		assertTrue(canReadJSON);
		
		configObj = new ConfigFile(testDir + "/iniTestFile.ini");
		
		jsConfigObj = new ConfigFile(testDir + "/iniTestFileJSON.js");
	}
	
	@After
	public void tearDown() {
		configObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(configObj);
	}
	
	@Test
	public void samtest() {
		//String helloVal = (String) configObj.get("main.hello");
		configObj.get("main.hello");
	}
	
	@Test
	public void jsonTest() {
		String jsonHelloVal = (String) jsConfigObj.get("hello");
		assertEquals("world", jsonHelloVal);
		
		String jsonArrayVal = (String) jsConfigObj.get("testArray.hello");
		assertEquals("world", jsonArrayVal);
		
		String jsonInnerArrayVal = (String) jsConfigObj.get("testArray.innerArray.hi");
		assertEquals("hello", jsonInnerArrayVal);
		
		Object jsonMap = jsConfigObj.get("testArray");
		if (jsonMap instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> innerMap = (Map<String, Object>) jsonMap;
			assertNotNull(innerMap);
			assertTrue(innerMap instanceof Map);
			assertTrue(innerMap.size() > 0);
			assertEquals("world", innerMap.get("hello"));
		}
	}
	
	@Test
	public void getString_stringArr() {
		assertEquals("testing expected value", "servlet-commons", configObj.getString("jSql_connection.database", null));
		assertEquals("default value test", null, configObj.getString("dunexists.ever", null));
		assertEquals("invalid key length (ini) test", "Hello World", configObj.getString("dunExists", "Hello World"));
	}
	
	@Test
	public void getString_string() {
		// assertEquals( new String[] { "jSql_connection", "database" }, (new
		// String("jSql_connection.database").split("\\.")) );
		assertEquals("testing expected value", "servlet-commons", configObj.getString("jSql_connection.database", null));
		assertEquals("default value test", null, configObj.getString("dunExists.never", null));
		assertEquals("invalid key test", "Hello World", configObj.getString("dunExists", "Hello World"));
	}
	
	@Test
	public void getInt() {
		assertEquals("testing expected value", "3600", configObj.getString("userAuthCookieConfig.loginLifetime", null));
		assertEquals("testing expected value", 3600, configObj.getInt("userAuthCookieConfig.loginLifetime", -1));
		
		assertEquals("default value test", Integer.MIN_VALUE, configObj.getInt("dunExists.never", Integer.MIN_VALUE));
		assertEquals("default value for non valid string", Integer.MIN_VALUE,
			configObj.getInt("jSql_connection.database", Integer.MIN_VALUE));
	}
	
	@Test
	public void getBoolean() {
		assertTrue("testing expected value", configObj.getBoolean("userAuthCookieConfig.isHttpOnly", false));
	}
	
}
