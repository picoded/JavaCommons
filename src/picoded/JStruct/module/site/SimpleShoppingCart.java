package picoded.JStruct.module.site;

/// Java imports
import java.util.*;

/// Picoded imports
import picoded.conv.*;
import picoded.struct.*;
import picoded.servlet.*;
import picoded.JStruct.*;
import picoded.struct.query.*;

/// 
/// A simple shopping cart system, which is built ontop of MetaTable, and AtomicLongMap
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
	
	/// Cart maximum size
	public int cart_max = 50;
	
	/// Product list max size
	public int product_max = 250;
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor options
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/// Empty constructor
	public SimpleShoppingCart() {
		//Does nothing : manual setup
	}
	
	public SimpleShoppingCart(JStruct inStruct, String prefix) {
		setupStandardTables(inStruct, prefix);
	}
	
	///
	/// Setup the standard tables, with the given JStruct
	///
	/// @param   The JStruct object to build ontop of
	/// @param   The table name prefix to generate the various meta table
	///
	public void setupStandardTables(JStruct inStruct, String prefix) {
		productOwner = inStruct.getMetaTable(prefix);
		productItem = inStruct.getMetaTable(prefix + "_item");
		productCount = inStruct.getAtomicLongMap(prefix + "_count");
	
		salesOrder = inStruct.getMetaTable(prefix + "_sale"); //Formalised shopping cart?
		salesReceipt = inStruct.getMetaTable(prefix + "_receipt");
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemSetup() {
		productOwner.systemSetup();
		productItem.systemSetup();
		productCount.systemSetup();
		salesOrder.systemSetup();
		salesReceipt.systemSetup();
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemTeardown() {
		productOwner.systemTeardown();
		productItem.systemTeardown();
		productCount.systemTeardown();
		salesOrder.systemTeardown();
		salesReceipt.systemTeardown();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Shopping cart functions (Low level)
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Gets the current shopping cart, from the current CorePage request cookie
	/// This fetches the JSON stored in the cart, as defined by "shoppingCartCookieName"
	///
	/// @param  Request page used, this is used to get the cookie value
	///
	/// @return Returns the cart cookie JSON string
	///
	public String getCartCookieJSON(CorePage requestPage) {
		//
		// Safety checks
		//
		if( requestPage == null ) {
			throw new RuntimeException("Missing requestPage parameter");
		}
		
		//
		// Get existing cookie values
		//
		String cartJsonStr = null;
		Map<String,String[]> cookieMap = requestPage.requestCookieMap();
		String[] cookieSet = null;
		if( cookieMap != null ) {
			cookieSet = cookieMap.get(shoppingCartCookieName);
		}
		
		if( cookieSet != null && cookieSet.length > 0 ) {
			cartJsonStr = cookieSet[0];
		} else {
			cartJsonStr = "";
		}
		return cartJsonStr;
	}
	
	///
	/// Updates the current CorePage request cookie as defined by "shoppingCartCookieName"
	///
	/// @param  Request page used, this is used to get the cookie value
	/// @param  The cookie cart JSON string
	///
	public void setCartCookieJSON(CorePage requestPage, String updateJson) {
		// Original cookie
		String ori = getCartCookieJSON(requestPage);
		
		// Update the cookie value only if needed
		if( !updateJson.equals(ori) ) {
			javax.servlet.http.Cookie theOneCookie = new javax.servlet.http.Cookie(shoppingCartCookieName, updateJson);
			requestPage.getHttpServletResponse().addCookie(theOneCookie);
		}
	}
	
	///
	/// Converts the cart cookie json string, to the cart listing object
	///
	/// @param  Cart cookie string
	///
	/// @return Returns the cart cookie string
	///
	public GenericConvertList<List<Object>> cartCookieJSONToList(String cartJsonStr) {
		return GenericConvert.toGenericConvertList( cartJsonStr, new ArrayList<Object>() );
	}
	
	///
	/// Converts the cart listing object to JSON string
	///
	/// @param  Cart cookie string
	///
	/// @return Returns the cart cookie string
	///
	public String cartListToCookieJSON(List<List<Object>> inList) {
		GenericConvertList<List<Object>> cartList = GenericConvert.toGenericConvertList( inList, new ArrayList<Object>() );
		
		// Iterate and ensure valid cart size
		for(int i=0;i<cartList.size();i++){
			List<Object> cartLine = cartList.getGenericConvertList(i);
			if(cartLine == null) {
				continue;
			}
			
			int lineSize = cartLine.size();
			if(lineSize <= 1) {
				// Not enough info
				cartList.set(i, null);
			} else if(lineSize > 2) {
				// Too much info, trim it
				cartLine = cartLine.subList(0, 2);
				cartList.set(i, cartLine);
			}
		}
		
		// Null out items less then zero
		cartList = nullOutZeroCount(cartList);
		
		// Remove null from list
		cartList.removeAll(Collections.singleton(null));
		
		return ConvertJSON.fromList( cartList );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Shopping cart functions (Higher level)
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Gets the current shopping cart, from the current CorePage request cookie
	/// This fetches the JSON stored in the cart, as defined by "shoppingCartCookieName"
	///
	/// @param  Request page used, this is used to get the cookie value
	/// @param  Simple mode indicator, if its false, it runs the validation step for more information 
	///
	/// @return Returns a list as `[[ID,count], ... ]` in simple mode, else returns `[[ID,count,meta], ... ]`
	///
	public GenericConvertList<List<Object>> getCartList(CorePage requestPage, boolean simple) {
		//
		// Return var
		//
		GenericConvertList<List<Object>> ret = cartCookieJSONToList(getCartCookieJSON(requestPage));
		if( !simple ) {
			return fetchAndValidateCartList(ret);
		}
		return ret;
	}
	
	///
	/// Takes in a `[[ID,count], ... ]` cart listing, fetch its meta object and validates it. 
	/// Converting it into a `[[ID,count,meta], ... ]` in the process.
	///
	/// Removes items with count 0 or less, will be removed from list
	///
	/// @TODO The actual validation, beyond just fetching the meta object
	///
	/// @param  The `[[ID,count], ... ]` list to filter
	///
	/// @return The processed and validated `[[ID,count,meta], ... ]`
	///
	public GenericConvertList<List<Object>> fetchAndValidateCartList(List<List<Object>> inList) {
		
		// Ensure valid input list
		GenericConvertList<List<Object>> cartList = GenericConvertList.build(inList);
		
		// Iterate and get meta objects
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
			cartList.set(i, cartLine);
		}
		
		// Null out zero counts
		cartList = nullOutZeroCount(cartList);
		
		// Remove null from list
		cartList.removeAll(Collections.singleton(null));
		
		// Return final list
		return cartList;
	}
	
	///
	/// Merges in two `[[ID,count], ... ]` cart listing into a single listing
	///
	/// @param  The `[[ID,count], ... ]` list to use as base
	/// @param  The `[[ID,count], ... ]` list to add
	/// @param  Indicator if items 0 or less, will be removed from list
	///
	/// @return The processed and validated `[[ID,count,meta], ... ]`
	///
	public GenericConvertList<List<Object>> mergeCartList(List<List<Object>> inBaseList, List<List<Object>> addList, boolean removeZeroCount) {
		
		// Ensure valid input list
		GenericConvertList<List<Object>> baseList = GenericConvertList.build(inBaseList);
		GenericConvertList<List<Object>> updateList = GenericConvertList.build(addList);
		
		// Iterate the add list
		if( updateList != null ) {
			int iLen = updateList.size();
			for( int i=0; i<iLen; ++i) {
				// Line record of an update
				GenericConvertList<Object> updateLine = updateList.getGenericConvertList(i, null);
				
				// Skip blank / invalid lines
				if(updateLine == null || updateLine.size() < 2) {
					continue;
				}

				// ID and count
				String id = updateLine.getString(0);
				int count = updateLine.getInt(1);
				// Note this maybe null
				Object meta = updateLine.get(2);

				//
				// Find the ID in existing cartList, update count
				//
				boolean found = false;
				for(int j=0;j<baseList.size();j++){
					// Iterate, and ignore blank lines
					GenericConvertList<Object> baseLine = baseList.getGenericConvertList(j);
					if(baseLine == null) {
						continue;
					}
					// ID is valid
					if(baseLine.getString(0).equals(id)) {
						
						// Base count
						int baseCount = baseLine.getInt(1);
						
						// Sum up the count
						baseLine.set(1, baseCount+count);
						
						// Check if meta object needs to be transfered over
						if( meta != null ) {
							// Check for base meta object, this takes priority in merge
							Object baseMeta = baseLine.get(2);
							// Merge if baseMeta is null
							if(baseMeta == null) {
								baseLine.set(2, meta);
							}
						}
						
						// Replace and update
						baseList.set(j, baseLine);
						
						// Indicate item was found, validate
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
					
					if(meta != null) {
						newList.add(meta);
					}
					
					baseList.add(newList);
				}
			}
		}
		
		// Remove zero count or less rows
		if(removeZeroCount) {
			// Null out  zero count
			baseList = nullOutZeroCount(baseList);
		}
		
		// Remove null from list
		baseList.removeAll(Collections.singleton(null));
		
		// Return final list
		return baseList;
	}
	
	///
	/// Update and add in the additional  `[[ID,count], ... ]` cart listing into a existing cart
	///
	/// @param  Request page used, this is used to get the cookie value
	/// @param  The `[[ID,count], ... ]` list to add
	/// @param  Simple mode indicator, if its false, it runs the validation step for more information
	///
	/// @return Returns a list as `[[ID,count], ... ]` in simple mode, else returns `[[ID,count,meta], ... ]`
	///
	public GenericConvertList<List<Object>> updateCartList(CorePage requestPage, List<List<Object>> addList, boolean simple) {
		// Get existing cart
		GenericConvertList<List<Object>> cart = getCartList(requestPage, false);
		
		// Merge in addList
		if( addList != null ) {
			cart = mergeCartList( cart, addList, true );
		}
		
		// Update, validate, return the cart
		return replaceCartList(requestPage, cart, simple);
	}
	
	///
	/// Full replacement varient of updateCartList
	///
	/// @param  Request page used, this is used to get the cookie value
	/// @param  The `[[ID,count], ... ]` list to add
	/// @param  Simple mode indicator, if its false, it runs the validation step for more information
	///
	/// @return Returns a list as `[[ID,count], ... ]` in simple mode, else returns `[[ID,count,meta], ... ]`
	///
	public GenericConvertList<List<Object>> replaceCartList(CorePage requestPage, List<List<Object>> cartList, boolean simple) {
		// Get existing cart
		GenericConvertList<List<Object>> cart = GenericConvert.toGenericConvertList( cartList, new ArrayList<Object>() );
		
		// Validate
		if(!simple) {
			cart = fetchAndValidateCartList(cart);
		}
		
		// Max cart size handling
		if( cart.size() > cart_max ) {
			throw new RuntimeException("Cart maximum size exceeded : "+cart.size()+"/"+cart_max);
		}
		
		// Update the JSON cookie with cart
		setCartCookieJSON( requestPage, cartListToCookieJSON(cart) );
		
		// Return the cart
		return cart;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// shopping cart [util functions]
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Nulls items with count of 0 or less
	///
	/// @param  The `[[ID,count], ... ]` list to use 
	///
	/// @return The processed and validated `[[ID,count], ... ]`
	///
	protected GenericConvertList<List<Object>> nullOutZeroCount(GenericConvertList<List<Object>> cartList) {
		// Iterate and get meta objects
		for(int i=0;i<cartList.size();i++){
			GenericConvertList<Object> cartLine = cartList.getGenericConvertList(i);
			// Ignore blank lines
			if(cartLine == null) {
				continue;
			}
			// Invalid count check
			int count = cartLine.getInt(1, 0);
			if( count <= 0 ) {
				// Removing invalid count
				cartList.set(i, null);
			}
		}
		return cartList;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Product listing 
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Gets a list of products assigned under an ownerID
	///
	/// @param  The ownerID assigned
	///
	/// @return List of meta objects representing the owner
	///
	public GenericConvertList<MetaObject> getProductList(String ownerID) {
		// Sanity check
		if( ownerID == null || ownerID.isEmpty() ) {
			throw new RuntimeException("Missing ownerID");
		}
		
		// Return object
		GenericConvertList<MetaObject> ret = new GenericConvertArrayList<MetaObject>();
		
		// Fetch and populate
		MetaObject[] queryRet = productItem.query("_ownerID=?", new String[] { ownerID }, "_createdTime", 0, product_max);
		if (queryRet != null && queryRet.length > 0) {
			for (int i = 0; i < queryRet.length; ++i) {
				ret.add(queryRet[i]);
			}
		}
		
		// Return
		return ret;
	}
	
	///
	/// Updates the product listing under an ownerID
	///
	/// @param  The ownerID assigned
	/// @param  List of product objects to insert / update
	///
	/// @return List of meta objects representing the owner
	///
	public List<MetaObject> updateProductList(String ownerID, List<Object> inUpdateList) {
		//
		// Sanity check
		//
		if( ownerID == null || ownerID.isEmpty() ) {
			throw new RuntimeException("Missing ownerID");
		}
		
		MetaObject ownerObj = productOwner.get(ownerID);
		if (ownerObj == null) {
			throw new RuntimeException("Missing product owner object for : "+ownerID);
		}
		
		if (inUpdateList == null) {
			throw new RuntimeException("Missing update list");
		}
		
		// Existing product list from ownerID
		GenericConvertList<MetaObject> prodList = getProductList(ownerID);
		
		// Update list to use
		GenericConvertList<Object> updateList = GenericConvertList.build(inUpdateList);
		
		//
		// Iterate the update list, updating if need be. La, la la
		//
		int iLen = updateList.size();
		for (int i = 0; i < iLen; ++i) {
			// Ensure it is a new object, avoid meta object changes bugs
			GenericConvertMap<String, Object> updateProduct = updateList.getGenericConvertStringMap(i, null);
			
			// Skip null rows
			if (updateProduct == null) {
				continue;
			}
			
			// Make new object, clone the values
			updateProduct = new GenericConvertHashMap<String,Object>(updateProduct);
			
			// Product _oid
			String update_oid = updateProduct.getString("_oid", null);
			if( update_oid != null && update_oid.equalsIgnoreCase("new") ) {
				update_oid = null;
			}
			
			// The meta object to create / update
			MetaObject updateMetaObject = null;
			
			if( update_oid != null ) {
				// Old meta object
				updateMetaObject = productItem.get( update_oid );
				
				// Security validation of owner ID
				if( !ownerID.equals(updateMetaObject.get("_ownerID")) ) {
					throw new SecurityException("Unauthorized update call to object "+update_oid+" with invalid ownerID "+ownerID);
				}
				
			} else {
				// New meta object
				updateMetaObject = productItem.newObject();
				prodList.add(updateMetaObject);
			}
			
			// MetaObject productEntryObj = null;
			// String productID = GenericConvert.toString(singleProduct.get("_oid"), "");
			// if (productID == null || productID.isEmpty() || productID.equalsIgnoreCase("new")) {
			// 	productEntryObj = productItem.append(null, productDetails);
			// 
			// 	productDetails.put("_createTime", (System.currentTimeMillis() / 1000L));
			// 
			// 	productEntryObj.saveAll();
			// } else {
			// 	productEntryObj = productItem.get(productID);
			// 	productEntryObj.putAll(productDetails);
			// 	productEntryObj.saveDelta();
			// }
		}
		
		return prodList;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// products [util functions]
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	///
	/// Converts the cart listing object to JSON string
	///
	/// @param  List of metaobject to find
	/// @param  _oid to find for
	///
	/// @return The metaobject found (if found)
	///
	protected MetaObject findMetaObjectInList(List<MetaObject> list, String _oid) {
		for(MetaObject o : list) {
			if(o._oid().equalsIgnoreCase(_oid)) {
				return o;
			}
		}
		return null;
	}
	
	// ///
	// /// # product (GET/POST)
	// ///
	// /// Gets / Updates the current shopping cart stored inside the cookie "cart" parameter
	// ///
	// /// ## HTTP Request Parameters (Optional)
	// ///
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// /// | Parameter Name  | Variable Type	        | Description                                                     |
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// /// | _oid            | String                  | The product list owner ID                                       |
	// /// | update          | [{meta}]                | Product Details metaobject. "_oid" is needed, and count to      |
	// ///	|				  |							| add/edit/delete (when count=0)                                  |
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// ///
	// /// ## Details of the update object
	// ///
	// ///	"_oid":owner id MUST BE GIVEN,
	// /// "update":[
	// ///	    {
	// ///		    "_oid":if oid is given, its an update. If null or "new", its a create,
	// ///		    "meta":{
	// ///			    product key/values here
	// ///		    }
	// ///	    }
	// /// ]
	// ///
	// /// ## JSON Object Output Parameters
	// ///
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// /// | Parameter Name  | Variable Type	        | Description                                                     |
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// /// | list            | [{meta}]                | Shopping cart content with item ID, count, and content          |
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// /// | error           | String (Optional)       | Errors encounted if any                                         |
	// /// +-----------------+-------------------------+-----------------------------------------------------------------+
	// ///
	// public RESTFunction product_GET_and_POST = (req, res) -> {
	// 	try {
	
	// 		//if update is not null, do an update
	// 		String[] updateArray = null;
	// 		List<String> updateErrors = new ArrayList<String>();
	// 		updateArray = req.getStringArray("update", null);
	// 		if (updateArray != null) {
	// 			for (int i = 0; i < updateArray.length; ++i) {
	// 				Map<String, Object> singleProduct = GenericConvert.toStringMap(updateArray[i], null);
	// 				if (singleProduct == null) {
	// 					updateErrors.add("Single Product for element number " + i + " in updateArray is null");
	// 				}
	// 
	// 				Map<String, Object> productDetails = GenericConvert.toStringMap(singleProduct.get("meta"), null);
	// 				if (productDetails == null) {
	// 					updateErrors.add("Product details for element number " + i + " in updateArray is null or empty");
	// 					continue;
	// 				}
	// 				productDetails.put("_ownerID", oid);
	// 
	// 				MetaObject productEntryObj = null;
	// 				String productID = GenericConvert.toString(singleProduct.get("_oid"), "");
	// 				if (productID == null || productID.isEmpty() || productID.equalsIgnoreCase("new")) {
	// 					productEntryObj = productItem.append(null, productDetails);
	// 
	// 					productDetails.put("_createTime", (System.currentTimeMillis() / 1000L));
	// 
	// 					productEntryObj.saveAll();
	// 				} else {
	// 					productEntryObj = productItem.get(productID);
	// 					productEntryObj.putAll(productDetails);
	// 					productEntryObj.saveDelta();
	// 				}
	// 			}
	// 		} else {
	// 			updateErrors.add("updateArray is null, no insertion to be done");
	// 		}
	// 
	// 		res.put("queryLog", updateErrors);
	// 
	// 	} catch (Exception e) {
	// 		throw new RuntimeException(e);
	// 	}
	// 
	// 	return res;
	// };
	// 
	// /////////////////////////////////////////////////////////////////////////////////////////
	// //
	// // RestBuilder template builder
	// //
	// /////////////////////////////////////////////////////////////////////////////////////////
	// 
	// ///
	// /// Takes the restbuilder and implements its respective default API
	// ///
	// public RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix) {
	// 	rb.getNamespace(setPrefix + "cart").put(HttpRequestType.GET, cart_GET_and_POST);
	// 	rb.getNamespace(setPrefix + "cart").put(HttpRequestType.POST, cart_GET_and_POST);
	// 
	// 	rb.getNamespace(setPrefix + "product").put(HttpRequestType.GET, product_GET_and_POST);
	// 	rb.getNamespace(setPrefix + "product").put(HttpRequestType.POST, product_GET_and_POST);
	// 
	// 	return rb;
	// }

}
