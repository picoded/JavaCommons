package picoded.RESTBuilder.template.site;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.JStruct.*;
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
public class SimpleShoppingCart {

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Class variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	/// Inventory owner metatable
	public MetaTable productOwner = null;

	/// Inventory listing
	public MetaTable productItem = null;

	/// Atomic product counting
	public AtomicLongMap productCount = null;

	/// Sales order
	public MetaTable salesOrder = null;

	/// Sales reciept
	public MetaTable salesReceipt = null;

	/// Shopping cart cookie name
	public String shoppingCartCookieName = "shopping-cart";

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
		setupTables(inStruct, prefix);
	}

	public void setupTables(JStruct inStruct, String prefix) {
		productOwner = inStruct.getMetaTable(prefix);
		productItem = inStruct.getMetaTable(prefix + "_item");
		productCount = inStruct.getAtomicLongMap(prefix + "_count");

		salesOrder = inStruct.getMetaTable(prefix + "_sale"); //Formalised shopping cart?
		salesOrder = inStruct.getMetaTable(prefix + "_receipt");
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
	/// | update          | [[ID,count], ... ]      | [optional] Shopping item ID, and count to                       |
	/// |                 |                         |            add/edit/delete (when count=0)                       |
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
		try {
			//
			// Get existing cookie values
			//
			String[] cookieSet = req.requestPage().requestCookieMap().get(shoppingCartCookieName);
			String cartJsonStr = null;

			if( cookieSet != null && cookieSet.length > 0 ) {
				cartJsonStr = cookieSet[0];
			} else {
				cartJsonStr = "";
			}
			GenericConvertList<Object> cartList = GenericConvert.toGenericConvertList( cartJsonStr, new ArrayList<Object>() );

			//
			// Get the update list, apply update
			//
			GenericConvertList<Object> updateList = req.getGenericConvertList("update");
			if( updateList != null ) {
				for( int i=0; i<updateList.size(); ++i) {
					// Line record of an update
					GenericConvertList<Object> updateLine = updateList.getGenericConvertList(i, null);
					// Skip blank lines
					if(updateLine == null || updateLine.size() < 2) {
						continue;
					}

					String id = updateLine.getString(0);
					int count = updateLine.getInt(1);

					//
					// Find the ID in existing cartList, update count
					//
					boolean found = false;
					for(int j=0;j<cartList.size();j++){
						GenericConvertList<Object> cartLine = cartList.getGenericConvertList(j);
						if(cartLine == null) {
							continue;
						}
						if(cartLine.getString(0).equals(id)) {
							cartLine.add(1,count);
							found = true;
							break;
						}
					}
					//
					// No ID found, append to cartList
					//
					if(!found) {
						List<Object> newList = new ArrayList<Object>();
						newList.add(id);
						newList.add(count);
						cartList.add(newList);
					}

				}
			}

			//
			// Update the cookie value if needed
			//
			String cartJsonStr_new = ConvertJSON.fromList( cartList );
			if( !cartJsonStr_new.equals(cartJsonStr) ) {
				javax.servlet.http.Cookie theOneCookie = new javax.servlet.http.Cookie(shoppingCartCookieName, cartJsonStr_new);
				req.requestPage().getHttpServletResponse().addCookie(theOneCookie);
			}

			//
			// For non simple mode, iterate the list and append the meta
			//
			if( req.getBoolean("simple") != true ) {
				for(int i=0;i<cartList.size();i++){
					GenericConvertList<Object> cartLine = cartList.getGenericConvertList(i);
					if(cartLine == null) {
						continue;
					}
					String itemID = cartLine.getString(0);
					Map<String,Object> itemMeta = productItem.get(itemID);

					//
					// @TODO SAFETY CHECK? : when meta object is invalid
					//

					cartLine.add(2, itemMeta);
				}
			}

			//
			// Return values
			//
			res.put("list", cartList);
			res.put("itemCount", cartList.size());

			//
			// Iterate the items for total quantity
			//
			int quantityCount = 0;
			for(int i=0;i<cartList.size();i++){
				GenericConvertList<Object> cartLine = cartList.getGenericConvertList(i);
				if(cartLine == null) {
					continue;
				}
				quantityCount += cartLine.getInt(1,0);
			}
			res.put("quantityCount", quantityCount);


		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	/// | update          | [{meta}]                | Product Details metaobject. "_oid" is needed, and count to      |
	///	|				  |							| add/edit/delete (when count=0)                                  |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## Details of the update object
	///
	///	"_oid":owner id MUST BE GIVEN,
	/// "update":[
	///	    {
	///		    "_oid":if oid is given, its an update. If null or "new", its a create,
	///		    "meta":{
	///			    product key/values here
	///		    }
	///	    }
	/// ]
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | list            | [{meta}]                | Shopping cart content with item ID, count, and content          |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction product_GET_and_POST = (req, res) -> {
		try {
			res.put("error", "");
			MetaObject ret = null;

			String oid = req.getString("_oid", "");
			if (oid.isEmpty()) {
				res.put("error", "Request object did not contain an oid");
				return res;
			}

			MetaObject productOwnerObj = productOwner.get(oid);
			if (productOwnerObj == null) {
				res.put("error", "Product Owner table does not contain an object for this oid");
				return res;
			}

			//if update is not null, do an update
			String[] updateArray = null;
			List<String> updateErrors = new ArrayList<String>();
			updateArray = req.getStringArray("update", null);
			if (updateArray != null) {
				for (int i = 0; i < updateArray.length; ++i) {
					Map<String, Object> singleProduct = GenericConvert.toStringMap(updateArray[i], null);
					if (singleProduct == null) {
						updateErrors.add("Single Product for element number " + i + " in updateArray is null");
					}

					Map<String, Object> productDetails = GenericConvert.toStringMap(singleProduct.get("meta"), null);
					if (productDetails == null) {
						updateErrors.add("Product details for element number " + i + " in updateArray is null or empty");
						continue;
					}
					productDetails.put("_ownerID", oid);

					MetaObject productEntryObj = null;
					String productID = GenericConvert.toString(singleProduct.get("_oid"), "");
					if (productID == null || productID.isEmpty() || productID.equalsIgnoreCase("new")) {
						productEntryObj = productItem.append(null, productDetails);

						productDetails.put("_createTime", (System.currentTimeMillis() / 1000L));

						productEntryObj.saveAll();
					} else {
						productEntryObj = productItem.get(productID);
						productEntryObj.putAll(productDetails);
						productEntryObj.saveDelta();
					}
				}
			} else {
				updateErrors.add("updateArray is null, no insertion to be done");
			}

			res.put("queryLog", updateErrors);

			//then do a get
			MetaObject[] queryRet = productItem.query("_ownerID=?", new String[] { oid }, "_createdTime", 0, 50);
			List<Object> retMeta = new ArrayList<Object>();
			if (queryRet != null && queryRet.length > 0) {
				for (int i = 0; i < queryRet.length; ++i) {
					retMeta.add(queryRet[i]);
				}
			}
			res.put("list", retMeta);
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

		rb.getNamespace(setPrefix + "product").put(HttpRequestType.GET, product_GET_and_POST);
		rb.getNamespace(setPrefix + "product").put(HttpRequestType.POST, product_GET_and_POST);

		return rb;
	}

}
