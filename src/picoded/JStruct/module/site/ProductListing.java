package picoded.JStruct.module.site;

/// Java imports
import java.util.List;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.struct.GenericConvertList;
/// Picoded imports

///
/// A product listing system, which is built ontop of MetaTable, and AtomicLongMap
///
public class ProductListing {
	
	/// Inventory owner metatable
	protected MetaTable productOwner = null;
	
	/// Inventory listing
	public MetaTable productItem = null;
	
	/// Product list max size
	protected int productMax = 250;
	
	protected String ownerID = "_ownerID";
	
	public SimpleShoppingCart shoppingCart = null;
	
	/// Empty constructor
	public ProductListing() {
		//Does nothing : manual setup
	}
	
	public ProductListing(JStruct inStruct, String prefix, String listing) {
		setupStandardTables(inStruct, prefix, listing);
	}
	
	public ProductListing(JStruct inStruct, String prefix) {
		setupStandardTables(inStruct, prefix, null);
	}
	
	///
	/// Setup the standard tables, with the given JStruct
	///
	/// @param   The JStruct object to build ontop of
	/// @param   The table name prefix to generate the various meta table
	///
	public void setupStandardTables(JStruct inStruct, String prefix, String listing) {
		productOwner = inStruct.getMetaTable(prefix);
		if (listing == null || listing.length() < 4) {
			productItem = inStruct.getMetaTable(prefix + "_productList");
		} else {
			productItem = inStruct.getMetaTable(prefix + "_" + listing);
		}
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemSetup() {
		productOwner.systemSetup();
		productItem.systemSetup();
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemTeardown() {
		productOwner.systemTeardown();
		productItem.systemTeardown();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Product listing
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Gets a list of products assigned under an id
	///
	/// @param  The ownerID/eventID/ID assigned
	///
	/// @return List of meta objects representing the owner
	///
	public GenericConvertList<MetaObject> getList(String ownerID) {
		return new SimpleShoppingCart().getProductList(ownerID);
	}
	
	///
	/// Updates the product listing under an id
	///
	/// @param  The id assigned
	/// @param  List of product objects to insert / update
	///
	/// @return List of meta objects representing the owner
	///
	public List<MetaObject> updateList(String ownerID, List<Object> inUpdateList) {
		return new SimpleShoppingCart().updateProductList(ownerID, inUpdateList);
	}
	
	//	/////////////////////////////////////////////////////////////////////////////////////////
	//	//
	//	// Sales purchase order [utils]
	//	//
	//	/////////////////////////////////////////////////////////////////////////////////////////
	//
	//	///
	//	/// Sanatizes a map data from protected purchase order data
	//	///
	//	/// @param Map to sanatize and return
	//	///
	//	/// @return The parameter
	//	///
	//	protected GenericConvertMap<String, Object> sanatizePurchaseData(GenericConvertMap<String, Object> inMap) {
	//		// Sanatize the item info
	//		inMap.remove("_oid");
	//		inMap.remove("_orderID");
	//		inMap.remove("_sellerID");
	//
	//		inMap.remove(ownerID);
	//		inMap.remove("_ownerMeta");
	//
	//		inMap.remove("_productID");
	//		inMap.remove("_productMeta");
	//
	//		inMap.remove("_orderStatus");
	//
	//		// Other systems reserved vars
	//		inMap.remove("_createTime");
	//		inMap.remove("_updateTime");
	//
	//		// Reserved and not in use?
	//		inMap.remove("_purchaserID");
	//		return inMap;
	//	}
}
