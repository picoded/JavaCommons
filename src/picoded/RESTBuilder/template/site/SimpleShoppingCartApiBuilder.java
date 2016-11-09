package picoded.RESTBuilder.template.site;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.RESTBuilder.template.core.*;
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

//For HMAC key generation
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.BaseEncoding;
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

	public MetaTableApiBuilder ownerApi = null;
	public MetaTableApiBuilder productApi = null;
	public MetaTableApiBuilder salesOrderApi = null;
	public MetaTableApiBuilder salesItemApi = null;

	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
	private static final Charset C_UTF8 = Charset.forName("UTF8");
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
	// Sales order API
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	///
	/// # sale.order (GET/POST)
	///
	/// Gets or create a sales order, not this is supposingly immutable, beyond change of status.
	/// Use sale.order.status, to do a change in status.
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | [optional] The purchase order ID                                |
	/// | status          | String                  | [optional] The order status                                     |
	/// | list            | [[ID,count,total cost], | [optional] Shopping item ID, and count to                       |
	/// |                 | ... ]                   |            add/edit/delete (when total count=0)                 |
	/// | useShoppingCart | boolean                 | [default=false] Indicates to use  the cookie shopping cart      |
	/// | orderMeta       | {meta}                  | [default={}] Order specific meta details                        |
	/// | itemMeta        | {meta}                  | [default={}] Item specific meta details                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The purchase order request OID                                  |
	/// | data            | {meta}                  | Purchase order details                                          |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	@SuppressWarnings("unchecked")
	public RESTFunction saleOrder_GET_and_POST = (req, res) -> {

		//
		// User safety check
		//
		MetaObject currentUser = ((BasePage)(req.requestPage())).currentAccount();
		if(currentUser == null){
			res.put("error","User is not login");
			return res;
		}

		//
		// Get params
		//
		String oid = req.getString("_oid", "");
		//"approved", "paid", "rejected", "failed"
		String status = req.getString("status", "");

		Boolean useShoppingCart = req.getBoolean("useShoppingCart", false);
		List<List<Object>> cartList = (List<List<Object>>)(Object)req.getObjectList("list", null);

		GenericConvertMap<String,Object> orderMeta = req.getGenericConvertStringMap("orderMeta", null);
		GenericConvertMap<String,Object> itemMeta = req.getGenericConvertStringMap("itemMeta", null);
		GenericConvertMap<String,Object> purchaseOrder = null;
		if( useShoppingCart ) {
			cartList = core.getCartList(req.requestPage(), true);
		}

		if(oid.isEmpty()){
			//Makes sure that there are items in the list first
			if(cartList == null || cartList.isEmpty()){
				res.put("error","Cannot create order with empty cart list!");
			}else{
				//Create new purchase order
				purchaseOrder = core.createPurchaseOrder(currentUser._oid(), cartList , orderMeta, itemMeta, status);
			}
		}else{
			//Reuse old purchase order oid
			res.put("_oid", oid);
			if(cartList != null){
				res.put("error","You cannot update a list / with a purchase order");
			}

			//Gets the respective purchase order
			purchaseOrder = core.fetchPurchaseOrder(oid);
		}
		res.put("data",purchaseOrder);

		return res;
	};

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Update status API
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	///
	/// # sale.order.status (GET/POST)
	///
	/// Gets or update the sales status
	/// Use sale.order.status, to do a change in status.
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	         | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | [optional] The purchase order ID                                |
	/// | status          | String                  | [optional] The order status                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	         | Description                                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The purchase order request OID                                  |
	/// | data            | {meta}                  | Purchase order details                                          |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction saleOrderstatus_GET_and_POST = (req, res) -> {
		//
		// User safety check
		//
		MetaObject currentUser = ((BasePage)(req.requestPage())).currentAccount();
		if(currentUser == null){
			res.put("error","User is not login");
			return res;
		}
		//
		// Get params
		//
		String oid = req.getString("_oid", "");
		//"approved", "paid", "rejected", "failed"
		String status = req.getString("status", "");
		GenericConvertMap<String,Object>  purchaseOrder;

		if(oid.isEmpty()){
			res.put("error","There needs to be a purchase order ID!");
			return res;
		}

		if(status == ""){
			//Fetches the status of the purchase order. User will need to sieve through to find the status
			purchaseOrder = core.fetchPurchaseOrder( oid );
		}else{
			//Updates the status of the purchase order
			purchaseOrder = core.updatePurchaseOrderStatus( oid, status);
		}

		res.put("_oid",oid);
		res.put("data", purchaseOrder);

		return res;
	};

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Adyen HMAC key generator(aka Merchant Signature) API
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	///
	/// # adyen.hmac (GET/POST)
	///
	/// Gets or create a sales order, not this is supposingly immutable, beyond change of status.
	/// Use sale.order.status, to do a change in status.
	///
	/// ## HTTP Request Parameters (Optional)
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	         | Description                                                    |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | hmacMeta        |{meta}                   | Contains the following:                                         |
	/// |                 |                         |  Key                                                            |
	/// |                 |                         |  shopperEmail                                                   |
	/// |                 |                         |  merchantReference                                              |
	/// |                 |                         |  merchantAccount                                                |
	/// |                 |                         |  currencyCode                                                   |
	/// |                 |                         |  paymentAmount                                                  |
	/// |                 |                         |  sessionValidity                                                |
	/// |                 |                         |  shipBeforeDate                                                 |
	/// |                 |                         |  skinCode                                                       |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	         | Description                                                    |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | merchantSig     | String                  | The merchant signature/hmac                                     |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction adyenHmac_POST = (req, res) -> {
		//Adyen test site HMAC key
		byte[] hmacKey = BaseEncoding.base16().decode("6F2A54A3885DBDD0A1AF22E31C23F5D0673A2C497851DF63689612A85179EC92");
		//Adyen live site HMAC key confirmed
		// byte[] hmacKey = BaseEncoding.base16().decode("B59E1106A3AD13B38BC6F4C6F3A4FF948DDC53FDA08D6849B2027D7780FC1148");


		String hmacSig = null;

		GenericConvertMap<String,Object> hmacMeta = req.getGenericConvertStringMap("hmacMeta", null);

		if(hmacMeta == null){
			res.put("error","Cannot create HMAC Key without any details!");
		}else{
			// Sort order is important (using natural ordering)
			SortedMap<String, String> params = new TreeMap<>();

			// Get all the params, and sort / filter it
			for( String keyName : hmacMeta.keySet() ) {
				if( keyName.equalsIgnoreCase("sig") || keyName.equalsIgnoreCase("merchantSig") || keyName.startsWith("ignore.") ) {
					continue;
				}

				if( hmacMeta.getString(keyName, "").trim().length() > 0 ) {
					params.put(keyName, hmacMeta.getString(keyName).trim());
				}
			}

			// Calculate the data to sign
			String signingData = Stream.concat(params.keySet().stream(), params.values().stream())
					  .map(SimpleShoppingCartApiBuilder::escapeVal)
					  .collect(Collectors.joining(":"));

			// Create the signature and add it to the parameter map
			try {
				 hmacSig = calculateHMAC(signingData, hmacKey);
				res.put("merchantSig" , hmacSig);
			} catch (SignatureException e) {
				 res.put("error" , e);
			}
			// Expected sig: GJ1asjR5VmkvihDJxCd8yE2DGYOKwWwJCBiV3R51NFg=
			System.out.println(hmacSig);

		}

		return res;
	};

	private static String escapeVal(String val) {
		 if(val == null) { return ""; }
		 return val.replace("\\", "\\\\").replace(":", "\\:");
	};

	private static String calculateHMAC(String data, byte[] key)  throws java.security.SignatureException {
		try {
			// Create an hmac_sha256 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);

			// Get an hmac_sha256 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
			mac.init(signingKey);

			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes(C_UTF8));

			// Base64-encode the hmac
			return  BaseEncoding.base64().encode(rawHmac);
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
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

		ownerApi = new MetaTableApiBuilder(core.productOwner);
		productApi = new MetaTableApiBuilder(core.productItem);
		salesOrderApi = new MetaTableApiBuilder(core.salesOrder);
		salesItemApi = new MetaTableApiBuilder(core.salesItem);

		//ownerApi.setupRESTBuilder( rb, setPrefix + "owner" );
		productApi.setupRESTBuilder( rb, setPrefix + "product." );
		salesOrderApi.setupRESTBuilder( rb, setPrefix + "sale.order." );
		salesItemApi.setupRESTBuilder( rb, setPrefix + "sale.item." );

		rb.getNamespace(setPrefix + "sale.order.create").put(HttpRequestType.GET, saleOrder_GET_and_POST);
		rb.getNamespace(setPrefix + "sale.order.create").put(HttpRequestType.POST, saleOrder_GET_and_POST);

		rb.getNamespace(setPrefix + "sale.order.status").put(HttpRequestType.GET, saleOrderstatus_GET_and_POST);
		rb.getNamespace(setPrefix + "sale.order.status").put(HttpRequestType.POST, saleOrderstatus_GET_and_POST);

		rb.getNamespace(setPrefix + "sale.order.adyen").put(HttpRequestType.POST, adyenHmac_POST);

		return rb;
	}

}
