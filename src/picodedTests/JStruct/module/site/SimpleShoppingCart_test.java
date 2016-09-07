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
	
	/// The product owner to currently use for testing
	public MetaObject productOwnerObject = null;
	
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
		if (testObj != null) {
			testObj.systemTeardown();
		}
		testObj = null;
		productOwnerObject = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(testObj);
		
		//run maintaince, no exception?
		// testObj.maintenance();
	}
	
	// Test cases
	//-----------------------------------------------
	
	@Test
	public void cartListToCookieJSON() {
		String testJSON = "[[\"id-1\",10],[\"id-2\",0],[\"id-3\",-5],[\"id-4\",10,{\"someMeta\":100}]]";
		GenericConvertList<List<Object>> testCart = GenericConvert
			.toGenericConvertList(testJSON, new ArrayList<Object>());
		
		String validJSON = "[[\"id-1\",10],[\"id-4\",10]]";
		assertEquals(validJSON, testObj.cartListToCookieJSON(testCart));
		assertEquals(validJSON, testObj.cartListToCookieJSON(testObj.cartCookieJSONToList(validJSON)));
	}
	
	@Test
	public void productSetup() {
		productOwnerObject = testObj.productOwner.newObject();
		productOwnerObject.put("name", "Scrooge Mcduck");
		productOwnerObject.saveDelta();
		
		assertEquals(0, testObj.getProductList(productOwnerObject._oid()).size());
		
		List<Object> prodList = ConvertJSON.toList("[" + "{" + "\"name\" : \"product_01\"" + "}," + "{"
			+ "\"name\" : \"product_02\"" + "}" + "]");
		
		assertEquals(2, testObj.updateProductList(productOwnerObject._oid(), prodList).size());
		assertEquals(2, testObj.getProductList(productOwnerObject._oid()).size());
	}
	
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
