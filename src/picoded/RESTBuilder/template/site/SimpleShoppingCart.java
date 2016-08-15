package picoded.RESTBuilder.template.site;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.JStruct.*;

///
/// Provides a shopping cart and inventory API
/// All in a single API package.
///
public class SimpleShoppingCart {
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Class variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/// Inventory owner metatable
	public MetaTable inventoryOwner = null;
	
	/// Inventory listing
	public MetaTable inventoryItems = null;
	
	/// Atomic inventory counting
	public AtomicLongMap inventoryCount = null;
	
	/// Sales order
	public MetaTable salesOrder = null;
	
	/// Sales reciept
	public MetaTable salesReceipt = null;
	
	/// Shopping cart cookie name
	public String shoppingCartCookieName = "cart";
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor options
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/// Empty constructor
	public SimpleShoppingCart() {
		//Does nothing
	}
	
	public SimpleShoppingCart(JStruct inStruct, String prefix) {
		inventoryOwner = inStruct.getMetaTable(prefix);
		inventoryItems = inStruct.getMetaTable(prefix+"_item");
		inventoryCount = inStruct.getAtomicLongMap(prefix+"_count");
		
		salesOrder = inStruct.getMetaTable(prefix+"_sale"); //Formalised shopping cart?
		salesOrder = inStruct.getMetaTable(prefix+"_receipt");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Shopping cart API
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//--------------------------------------------
	//
	// Simple shopping cart handling
	//
	//--------------------------------------------
	
	///
	/// # cart (GET/POST) 
	///
	/// Gets / Updates the current shopping cart stored inside the cookie "cart" parameter
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | update          | String[[ID,count]]      | Shopping item ID, and count to add/edit/delete (when count=0)   |
	/// | simple          | boolean                 | Ignore cart content meta, and its checks                        |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | cart            | String[[ID,count,meta]] | Shopping cart content with item ID, count, and content          |
	/// | itemCount       | Integer                 | Number of unique item ID inside the cart                        |
	/// | quantityCount   | Integer                 | Total quantity of items                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction cart_GET_and_POST = (req, res) -> {
		try {
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	};
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// RestBuilder template builder
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Takes the restbuilder and implements its respective default API
	///
	public RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix) {
		rb.getNamespace(setPrefix + "cart").put(HttpRequestType.GET, cart_GET_and_POST);
		rb.getNamespace(setPrefix + "cart").put(HttpRequestType.POST, cart_GET_and_POST);
		
		return rb;
	}
	
}
