package picoded.JStruct.module.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.conv.ConvertJSON;
import picoded.conv.GenericConvert;
import picoded.struct.GenericConvertHashMap;
import picoded.struct.GenericConvertList;
import picoded.struct.GenericConvertMap;

public class ProductListing_test {
	
	/// Test object
	public ProductListing productListing = null;
	
	/// The product owner to currently use for testing
	public MetaObject productOwnerObject = null;
	
	/// To override for implementation
	///------------------------------------------------------
	public JStruct jstructConstructor() {
		return new JStruct();
	}
	
	public ProductListing implementationConstructor() {
		return new ProductListing(jstructConstructor(), "cart");
	}
	
	public MetaTable implementationConstructor1() {
		return (new JStruct()).getMetaTable("test");
	}
	
	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		productListing = implementationConstructor();
		productListing.systemSetup();
	}
	
	@After
	public void tearDown() {
		if (productListing != null) {
			productListing.systemTeardown();
		}
		productListing = null;
		productOwnerObject = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		new ProductListing(jstructConstructor(), "shopping", "cart");
		assertNotNull(productListing);
		new ProductListing();
	}
	
	// Test cases
	//-----------------------------------------------
	@Test
	public void productSetup() {
		productOwnerObject = productListing.productOwner.newObject();
		productOwnerObject.put("name", "Scrooge Mcduck");
		productOwnerObject.saveDelta();
		
		assertEquals(0, productListing.getList(productOwnerObject._oid()).size());
		
		List<Object> prodList = ConvertJSON.toList("[" + "{" + "\"name\" : \"product_01\"" + "},"
			+ "{" + "\"name\" : \"product_02\"" + "}" + "]");
		
		assertEquals(2, productListing.updateList(productOwnerObject._oid(), prodList).size());
	}
	
	@Test(expected = Exception.class)
	public void getListTest() throws Exception {
		assertNotNull(productListing.getList(""));
	}
	
	@Test(expected = Exception.class)
	public void getListTest1() throws Exception {
		assertNotNull(productListing.updateList(null, null));
	}
	
	@Test(expected = Exception.class)
	public void updateList() throws Exception {
		assertNotNull(productListing.updateList("", null));
	}
	
	@Test(expected = Exception.class)
	public void updateList1() throws Exception {
		assertNotNull(productListing.updateList("test", null));
	}
	
	@Test(expected = Exception.class)
	public void updateList2() throws Exception {
		MetaTable productOwner = implementationConstructor1();
		productOwnerObject = productListing.productOwner.newObject();
		productOwnerObject.put("id-1", "Scrooge Mcduck");
		productOwnerObject.put("_oid", "new");
		productOwnerObject.saveDelta();
		productOwner.append("id-1", productOwnerObject);
		productListing.productOwner = productOwner;
		assertNotNull(productListing.updateList(
			productListing.productOwner.getFromKeyName("_oid")[0]._oid(), null));
	}
	
	@Test(expected = Exception.class)
	public void updateList3() throws Exception {
		MetaTable productOwner = implementationConstructor1();
		productOwnerObject = productListing.productOwner.newObject();
		productOwnerObject.put("id-1", "Scrooge Mcduck");
		productOwnerObject.put("_oid", "new");
		productOwnerObject.saveDelta();
		productOwner.append("id-1", productOwnerObject);
		productListing.productOwner = productOwner;
		productListing.productItem = productListing.productOwner;
		String testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"testCart\"] ]";
		List<Object> inUpdateList = new ArrayList<Object>();
		inUpdateList.add(GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>()));
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
		testJSON = "[[\"id-1\",10],[\"id-2\",0],[\"id-3\",-5],[\"id-4\",10,{\"someMeta\":100}], null, [\"id-7\"], [\""
			+ productOwner.getFromKeyName("_oid")[0]._oid() + "\", 0] ]";
		GenericConvertList<List<Object>> cartList = GenericConvert.toGenericConvertList(testJSON,
			new ArrayList<Object>());
		GenericConvertMap<String, Object> itemObj = new GenericConvertHashMap<String, Object>();
		itemObj.put("product_01", ConvertJSON.toList("[{\"name\":\"product_01\"}]"));
		itemObj.put("testCart", cartList);
		itemObj.put("_oid", "new");
		inUpdateList.add(itemObj);
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
		itemObj.put("_oid", productOwner.getFromKeyName("_oid")[0]._oid());
		inUpdateList.add(itemObj);
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
		
	}
	
	@Test(expected = Exception.class)
	public void updateProductList7() throws Exception {
		MetaTable productOwner = implementationConstructor1();
		productOwnerObject = productListing.productOwner.newObject();
		productOwnerObject.put("id-1", "Scrooge Mcduck");
		productOwnerObject.put("_oid", "new");
		productOwnerObject.saveDelta();
		productOwner.append("id-1", productOwnerObject);
		productListing.productOwner = productOwner;
		String testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"testCart\"] ]";
		List<Object> inUpdateList = new ArrayList<Object>();
		
		inUpdateList.add(GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>()));
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
		
		inUpdateList = new ArrayList<Object>();
		GenericConvertList<List<Object>> cartList = GenericConvert.toGenericConvertList(testJSON,
			new ArrayList<Object>());
		GenericConvertMap<String, Object> itemObj = new GenericConvertHashMap<String, Object>();
		itemObj.put("product_01", ConvertJSON.toList("[{\"name\":\"product_01\"}]"));
		itemObj.put("testCart", cartList);
		itemObj.put("_oid", "new");
		inUpdateList.add(itemObj);
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
		
		itemObj.put("_oid", productListing.productItem.getFromKeyName("_ownerID")[0].get("_ownerID")
			.toString());
		inUpdateList.add(itemObj);
		assertNotNull(productListing.updateList(productOwner.getFromKeyName("_oid")[0]._oid(),
			inUpdateList));
	}
}
