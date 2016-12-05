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
		assertNotNull(productListing);
		new SimpleShoppingCart();
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
	public void updateProductList5() throws Exception {
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
		
		inUpdateList = new ArrayList<Object>();
		itemObj.put("_oid", productListing.productItem.getFromKeyName("_oid")[0]._oid());
		inUpdateList.add(itemObj);
		productListing.productOwner = productListing.productItem;
		assertNotNull(productListing.getList(productListing.productItem.getFromKeyName("_oid")[0]
			._oid()));
		itemObj = new GenericConvertHashMap<String, Object>();
		assertNotNull(productListing
			.getList(productListing.productOwner.getFromKeyName("_ownerID")[0].get("_ownerID")
				.toString()));
		testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"_ownerID\", \""
			+ productListing.productItem.getFromKeyName("_oid")[0]._oid()
			+ "\"], [\""
			+ productListing.productItem.getFromKeyName("_oid")[0]._oid() + "\", 9] ]";
		cartList = GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>());
		itemObj.put("_oid", productListing.productItem.getFromKeyName("_ownerID")[0].get("_ownerID")
			.toString());
		itemObj.put("testCart", cartList);
		inUpdateList.add(itemObj);
		assertNotNull(productListing.updateList(
			productListing.productItem.getFromKeyName("_ownerID")[0].get("_ownerID").toString(),
			inUpdateList));
	}
	
	@Test(expected = Exception.class)
	public void updateProductList6() throws Exception {
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
		
		inUpdateList = new ArrayList<Object>();
		itemObj.put("_oid", productListing.productItem.getFromKeyName("_oid")[0]._oid());
		inUpdateList.add(itemObj);
		productListing.productOwner = productListing.productItem;
		itemObj = new GenericConvertHashMap<String, Object>();
		assertNotNull(productListing
			.getList(productListing.productOwner.getFromKeyName("_ownerID")[0].get("_ownerID")
				.toString()));
		testJSON = "[[\"id-1\",11],[\"id-4\",-1],[\"id-3\",-6],[\"id-5\",11,{\"someMeta\":130}], null, [\"_ownerID\", \""
			+ productListing.productItem.getFromKeyName("_oid")[0]._oid()
			+ "\"], [\""
			+ productListing.productItem.getFromKeyName("_oid")[0]._oid() + "\", 9] ]";
		cartList = GenericConvert.toGenericConvertList(testJSON, new ArrayList<Object>());
		itemObj.put("_oid", productListing.productItem.getFromKeyName("_oid")[0]._oid());
		assertNotNull(productListing.updateList(
			productListing.productItem.getFromKeyName("_oid")[0]._oid(), inUpdateList));
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
