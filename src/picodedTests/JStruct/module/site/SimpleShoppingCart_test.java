package picodedTests.JStruct.module.site;

// Target test class
import picoded.JStruct.*;
import picoded.JStruct.module.site.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.commons.lang3.RandomUtils;

import picoded.conv.*;
import picoded.struct.*;

import picodedTests.TestConfig;

// MetaTable base test class
public class SimpleShoppingCart_test {
	
	/// Test object
	public SimpleShoppingCart testObj = null;
	
	/// To override for implementation
	///------------------------------------------------------
	public JStruct jstructConstructor() {
		return new JStruct();
	}
	
	public SimpleShoppingCart implementationConstructor() {
		return new SimpleShoppingCart(jstructConstructor(), "cart");
	}
	
	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		testObj = implementationConstructor();
		testObj.systemSetup();
	}
	
	@After
	public void tearDown() {
		if(testObj != null) {
			testObj.systemTeardown();
		}
		testObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(testObj);
		
		//run maintaince, no exception?
		// mtObj.maintenance();
	}
	
	// Test cases
	//-----------------------------------------------
	
	// // Test utility used to generate random maps
	// protected HashMap<String, Object> randomObjMap() {
	// 	HashMap<String, Object> objMap = new CaseInsensitiveHashMap<String, Object>();
	// 	objMap.put(GUID.base58(), RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
	// 	objMap.put(GUID.base58(), -(RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3))));
	// 	objMap.put(GUID.base58(), GUID.base58());
	// 	objMap.put(GUID.base58(), GUID.base58());
	// 	
	// 	objMap.put("num", RandomUtils.nextInt(0, (Integer.MAX_VALUE - 3)));
	// 	objMap.put("str_val", GUID.base58());
	// 	
	// 	return objMap;
	// }
	// 
	// @Test
	// public void newObjectTest() {
	// 	MetaObject moObj = null;
	// 	
	// 	assertNotNull(moObj = mtObj.newObject());
	// 	moObj.put("be", "happy");
	// 	moObj.saveDelta();
	// 	
	// 	String guid = null;
	// 	assertNotNull(guid = moObj._oid());
	// 	
	// 	assertNotNull(moObj = mtObj.get(guid));
	// 	assertEquals("happy", moObj.get("be"));
	// }
	// 
}
