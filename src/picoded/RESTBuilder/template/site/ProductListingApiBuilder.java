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
/// Provides a product listings API
/// All in a single API package.
///
public class ProductListingApiBuilder {

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Class variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	public ProductListing core = null;

	public MetaTableApiBuilder listingsApi = null;

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor options
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	/// Empty constructor
	public ProductListingApiBuilder(ProductListing inCore) {
		core = inCore;
	}

	public ProductListingApiBuilder(JStruct inStruct, String prefix) {
		core = new ProductListing(inStruct, prefix);
	}

	///
	/// # listings (GET/POST)
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
	/// | Parameter Name  | Variable Type	        | Description                                                      |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The event ID                                                    |
	/// | list            | [{meta}]                | openings content with positions name and content                |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction productListings_GET_and_POST = (req, res) -> {

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
				prodList = core.updateList( oid, updateList );
			}
		} else {
			//
			// Simply get the list instead
			//
			prodList = core.getList( oid );
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
		rb.getNamespace(setPrefix + "productList").put(HttpRequestType.GET, productListings_GET_and_POST);
		rb.getNamespace(setPrefix + "productList").put(HttpRequestType.POST, productListings_GET_and_POST);

		listingsApi = new MetaTableApiBuilder(core.productItem);

		//ownerApi.setupRESTBuilder( rb, setPrefix + "owner" );
		listingsApi.setupRESTBuilder( rb, setPrefix + "productList." );

		return rb;
	}

}
