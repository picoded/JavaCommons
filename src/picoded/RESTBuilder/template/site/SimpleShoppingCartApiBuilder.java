package picoded.RESTBuilder.template.site;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.JStruct.*;
import picoded.JStruct.module.site.*;
import picoded.servlet.*;
import picoded.struct.*;
import picoded.conv.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
///
/// Provides a shopping cart and product API
/// All in a single API package.
///
public class SimpleShoppingCartApiBuilder {

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Class variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public SimpleShoppingCart core = null;

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor options
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	/// Empty constructor
	public SimpleShoppingCartApiBuilder(SimpleShoppingCart inCore) {
		core = inCore;
	}

	public SimpleShoppingCartApiBuilder(JStruct inStruct, String prefix) {
		core = new SimpleShoppingCart(inStruct, prefix);
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
	/// Cart storage format will be as JSON : [[ID,count],...]
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | list            | [[ID,count], ... ]      | [optional] Shopping item ID, and count to                       |
	/// |                 |                         |            add/edit/delete (when total count=0)                 |
	/// | mode            | "update", "replace"     | [default=update] Indicate if list is an update or replacement   |
	/// | simple          | boolean                 | [default=false] Ignore cart content meta, and its checks        |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | list            | [[ID,count,meta], ... ] | Shopping cart content with item ID, count, and content          |
	/// | itemCount       | Integer                 | Number of unique item ID inside the cart                        |
	/// | quantityCount   | Integer                 | Total quantity of items                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction cart_GET_and_POST = (req, res) -> {
		
		//
		// Get req params
		//
		String mode = req.getString("mode","update");
		boolean simple = req.getBoolean("simple", false);
		String listStr = req.getString("list", null);
		
		//
		// Function reuse vars
		//
		CorePage requestPage= req.requestPage();
		GenericConvertList<List<Object>> cart = null;
		
		//
		// Assumes an update is needed if not pure get
		//
		if( listStr != null ) {
			// Prepare update list
			GenericConvertList<List<Object>> updateList = core.cartCookieJSONToList(listStr);
			
			// Check for replacement mode
			if( mode != null && mode.equalsIgnoreCase("replace") ) {
				// Run replacement mode
				cart = core.replaceCartList(requestPage, updateList, simple);
			} else {
				// Run update mode
				cart = core.updateCartList(requestPage, updateList, simple);
			}
		} else {
			//
			// Simply just get the list instead
			//
			cart = core.getCartList(requestPage, simple); 
		}
		
		res.put("list", cart);
		res.put("itemCount", cart.size());
		res.put("quantityCount", core.cartListQuantityCount(cart));
		
		return res;
	};

	///
	/// # product (GET/POST)
	///
	/// Gets / Updates the current shopping cart stored inside the cookie "cart" parameter
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The product list owner ID                                       |
	/// | list            | [{meta}]                | Product Details metaobject. "_oid" is needed                    |
	/// | mode            | "update", "replace"     | [default=update] Indicate if list is an update or replacement   |
	/// |                 |                         | @TODO : Support "replace"                                       |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## Details of the update object
	///
	///	"_oid":owner id MUST BE GIVEN,
	/// "update":[
	///	    {
	///		    "_oid":if oid is given, its an update. If null or "new", its a create,
	///		    ... product key/values here ...
	///	    }
	/// ]
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The product list owner ID                                       |
	/// | list            | [{meta}]                | Shopping cart content with item ID, count, and content          |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction product_GET_and_POST = (req, res) -> {
		
		//
		// _oid sanity check
		//
		String oid = req.getString("_oid", "");
		res.put("_oid", oid);
		if (oid.isEmpty()) {
			res.put("error", "Request object did not contain an oid");
			return res;
		}
		
		//
		// Function reuse vars
		//
		List<MetaObject> prodList = null;
		
		//
		// Get req params
		//
		String mode = req.getString("mode","update");
		String listStr = req.getString("list", null);
		
		//
		// Update / GET
		//
		if( listStr != null ) {
			//
			// Assumes an update 
			//
			List<Object> updateList = ConvertJSON.toList(listStr);
			
			// Check for replacement mode
			if( mode != null && mode.equalsIgnoreCase("replace") ) {
				throw new RuntimeException("mode=replace not supported");
			} else {
				prodList = core.updateProductList( oid, updateList );
			}
		} else {
			//
			// Simply get the list instead
			//
			prodList = core.getProductList( oid );
		}
		res.put("list", prodList);
		
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

		rb.getNamespace(setPrefix + "product").put(HttpRequestType.GET, product_GET_and_POST);
		rb.getNamespace(setPrefix + "product").put(HttpRequestType.POST, product_GET_and_POST);

		return rb;
	}

}