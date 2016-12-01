package picoded.JStruct.module.site;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.conv.ConvertJSON;
import picoded.conv.GenericConvert;
import picoded.enums.HttpRequestType;
import picoded.servlet.CorePage;
import picoded.struct.GenericConvertList;
// Test depends

// MetaTable base test class
public class SimpleShoppingCart_test {
	
	/// Test object
	public SimpleShoppingCart simpleShoppingCart = null;
	
	/// The product owner to currently use for testing
	public MetaObject productOwnerObject = null;
	
	private CorePage corePage;
	
	/// To override for implementation
	///------------------------------------------------------
	public JStruct jstructConstructor() {
		return new JStruct();
	}
	
	public SimpleShoppingCart implementationConstructor() {
		return new SimpleShoppingCart(jstructConstructor(), "cart");
	}
	
	public MetaTable implementationConstructor1() {
		return (new JStruct()).getMetaTable("test");
	}
	
	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		simpleShoppingCart = implementationConstructor();
		simpleShoppingCart.systemSetup();
		corePage = new CorePage();
	}
	
	@After
	public void tearDown() {
		if (simpleShoppingCart != null) {
			simpleShoppingCart.systemTeardown();
		}
		simpleShoppingCart = null;
		productOwnerObject = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(simpleShoppingCart);
		new SimpleShoppingCart();
	}
	
	// Test cases
	//-----------------------------------------------
	
	@Test
	public void productSetup() {
		productOwnerObject = simpleShoppingCart.productOwner.newObject();
		productOwnerObject.put("name", "Scrooge Mcduck");
		productOwnerObject.saveDelta();
		
		assertEquals(0, simpleShoppingCart.getProductList(productOwnerObject._oid()).size());
		
		List<Object> prodList = ConvertJSON.toList("[" + "{" + "\"name\" : \"product_01\"" + "},"
			+ "{" + "\"name\" : \"product_02\"" + "}" + "]");
		
		assertEquals(2, simpleShoppingCart.updateProductList(productOwnerObject._oid(), prodList)
			.size());
		assertEquals(2, simpleShoppingCart.getProductList(productOwnerObject._oid()).size());
	}
	
	@Test(expected = Exception.class)
	public void getCartCookieJSONTest() throws Exception {
		assertNotNull(simpleShoppingCart.getCartCookieJSON(null));
	}
	
	@Test
	public void getCartCookieJSONTest1() throws ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		List<String> headerList = new ArrayList<String>();
		headerList.add("header_1");
		headerList.add("header_2");
		when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerList));
		CorePage corePageLocal = spy(corePage.setupInstance(HttpRequestType.GET, request, response));
		assertNotNull(simpleShoppingCart.getCartCookieJSON(corePageLocal));
		String[] cookiesArr = new String[] {};
		Map<String, String[]> _requestCookieMap = new HashMap<String, String[]>();
		_requestCookieMap.put("shopping-cart", cookiesArr);
		corePageLocal._requestCookieMap = _requestCookieMap;
		assertNotNull(simpleShoppingCart.getCartCookieJSON(corePageLocal));
		cookiesArr = new String[] { "item1", "item2", "item2", "item4" };
		_requestCookieMap.put("shopping-cart", cookiesArr);
		corePageLocal._requestCookieMap = _requestCookieMap;
		String updateJson = null;
		assertNotNull(updateJson = simpleShoppingCart.getCartCookieJSON(corePageLocal));
		simpleShoppingCart.setCartCookieJSON(corePageLocal, updateJson);
		simpleShoppingCart.setCartCookieJSON(corePageLocal, "item5");
		assertNotNull(simpleShoppingCart.getCartList(corePageLocal, true));
		assertNotNull(simpleShoppingCart.getCartList(corePageLocal, false));
		String testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"id-9\"] ]";
		GenericConvertList<List<Object>> testCart = GenericConvert.toGenericConvertList(testJSON,
			new ArrayList<Object>());
		assertNotNull(simpleShoppingCart.updateCartList(corePageLocal, testCart, true));
		assertNotNull(simpleShoppingCart.updateCartList(corePageLocal, null, false));
		
	}
	
	@Test
	public void cartListToCookieJSON() {
		String testJSON = "[[\"id-1\",10],[\"id-2\",0],[\"id-3\",-5],[\"id-4\",10,{\"someMeta\":100}], null, [\"id-7\"] ]";
		
		String testJSON1 = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"id-9\"] ]";
		GenericConvertList<List<Object>> testCart = GenericConvert.toGenericConvertList(testJSON,
			new ArrayList<Object>());
		GenericConvertList<List<Object>> testCart1 = GenericConvert.toGenericConvertList(testJSON1,
			new ArrayList<Object>());
		
		String validJSON = "[[\"id-1\",10],[\"id-4\",10]]";
		assertEquals(validJSON, simpleShoppingCart.cartListToCookieJSON(testCart));
		assertEquals(validJSON, simpleShoppingCart.cartListToCookieJSON(simpleShoppingCart
			.cartCookieJSONToList(validJSON)));
		testCart = GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>());
		assertNotNull(simpleShoppingCart.cartListQuantityCount(testCart));
		MetaTable productItem = implementationConstructor1();
		productOwnerObject = simpleShoppingCart.productOwner.newObject();
		productOwnerObject.put("id-1", "Scrooge Mcduck");
		productOwnerObject.saveDelta();
		productItem.append("id-1", productOwnerObject);
		simpleShoppingCart.productItem = productItem;
		testJSON = "[[\"id-1\",10],[\"id-2\",0],[\"id-3\",-5],[\"id-4\",10,{\"someMeta\":100}], null, [\"id-7\"], [\""
			+ productItem.getFromKeyName("_oid")[0]._oid() + "\", 0] ]";
		testCart = GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>());
		assertNotNull(simpleShoppingCart.fetchAndValidateCartList(testCart));
		
		testJSON = "[[\"id-1\",10],[\"id-2\",0],[\"id-3\",-5, 3],[\"id-4\",10,{\"someMeta\":100}], null, [\"id-7\"] ]";
		testJSON1 = "[[\"id-1\",11],[\"id-4\",-1, null],[\"id-3\",-6, 4],[\"id-5\",11,{\"someMeta\":130}], null, [\"id-9\"] ]";
		testCart = GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>());
		testCart1 = GenericConvert.toGenericConvertList(testJSON1, new ArrayList<Object>());
		
		assertNotNull(simpleShoppingCart.mergeCartList(testCart, testCart1, true));
	}
}
