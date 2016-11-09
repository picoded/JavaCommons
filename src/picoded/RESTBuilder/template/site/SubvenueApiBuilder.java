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
// import java.lang.System.*;
import com.google.common.io.BaseEncoding;
///
/// Provides a product listings API
/// All in a single API package.
///
public class SubvenueApiBuilder {

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Class variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	public SubvenueBookings core = null;

	public MetaTableApiBuilder bookingsApi = null;

	public MetaTableApiBuilder subvenueBookingsApi = null;

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor options
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	/// Empty constructor
	public SubvenueApiBuilder(SubvenueBookings inCore) {
		core = inCore;
	}

	public SubvenueApiBuilder(JStruct inStruct, String prefix) {
		core = new SubvenueBookings(inStruct, prefix);
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
	public RESTFunction subvenueDates_GET_and_POST = (req, res) -> {

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
		List<MetaObject> bookingDatesList = null;
		bookingDatesList = core.getSubvenueBookingDates_byBookingId(oid);
		res.put("list", bookingDatesList);

		return res;
	};

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Update status API
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	///
	/// # subvenueBookings.status (GET/POST)
	///
	/// Gets or update the subvenue booking status
	///
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
	public RESTFunction subvenueBookingStatus_GET_and_POST = (req, res) -> {
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
		GenericConvertList<MetaObject>   bookingOrder;

		if(oid.isEmpty()){
			res.put("error","There needs to be a purchase order ID!");
			return res;
		}

		if(status == ""){
			//Fetches the status of the purchase order. User will need to sieve through to find the status
			bookingOrder = core.getSubvenueBookings_bySubVenueId(oid);
		}else{
			//Updates the status of the purchase order
			bookingOrder = core.updateSubvenueBookingStatus( oid, status);
		}

		res.put("_oid",oid);
		res.put("data", bookingOrder);

		return res;
	};


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
	public RESTFunction subvenueBookings_POST = (req, res) -> {


		String subvenueID = req.getString("subvenueID","");
		String eventID = req.getString("eventID","");
		String venueID = req.getString("venueID","");
		Float paymentAmount = req.getFloat("paymentAmount",0);
		//
		// sanity check
		//
		if (subvenueID.isEmpty()) {
			res.put("error", "Request object did not contain an subvenueID");
			return res;
		}
		if (eventID.isEmpty()) {
			res.put("error", "Request object did not contain an eventID");
			return res;
		}

		//
		// Function reuse vars
		//
		GenericConvertMap<String,Object>  booking = null;
		booking = core.createSubvenueBooking(subvenueID , eventID , venueID , paymentAmount);

		// Creating the date items
		@SuppressWarnings("unchecked")
		// List<List<Object>> datesList = (List<List<Object>>)(Object)req.getObjectList("list", null);
		Map<String, Object> datesList_obj_raw = req.getStringMap("list", null);

		if(datesList_obj_raw == null){
			res.put("error", "datesList_obj_raw is null");
			return res;
		}

		// System.out.println("bookings "+ booking);
		// System.out.println("datesList "+ datesList_obj_raw);
		//With a subvenue booking,store the respective date and timeslots
		GenericConvertMap<String,Object> bookingSlots = null;
		try{
			bookingSlots = core.createBookingSlots(booking.getString("_oid") , datesList_obj_raw);
		}catch(Exception ex){
			ex.printStackTrace();
			res.put("error", ex.getMessage());
			return res;
		}
		res.put("list", bookingSlots);

		return res;
	};

	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	        | Description                                                      |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | _oid            | String                  | The booking ID                                                  |
	/// | _subvenueID     | String                  | The subvenue ID                                                 |
	/// | list            | [{meta}]                | openings content with positions name and content                |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	/// | error           | String (Optional)       | Errors encounted if any                                         |
	/// +-----------------+-------------------------+-----------------------------------------------------------------+
	///
	public RESTFunction subvenueBookings_GET = (req, res) -> {
		//
		// _oid sanity check
		//
		String oid = req.getString("_oid", "");
		res.put("_oid", oid);

		String subvenueID = req.getString("_subvenueID","");
		res.put("_subvenueID",subvenueID);

		if (oid.isEmpty() && subvenueID.isEmpty()) {
			res.put("error", "Request object did not contain a booking ID or subvenue ID");
			return res;
		}
		//
		// Gets all booking keys for that specific subvenue
		//
		List<MetaObject> bookingsList = null;
		if(oid.length() != 0){
			bookingsList = core.getSubvenueBookings_byBookingId(oid);
		}
		if(subvenueID.length() != 0){
			bookingsList = core.getSubvenueBookings_bySubVenueId(subvenueID);
		}
		System.out.println("bookingsList "+bookingsList);
		if(bookingsList.size() == 0){
			res.put("isEmpty",true);
		}

		// List<List<MetaObject>> allbookingsForSubvenue = new ArrayList<List<MetaObject>>();
		// //for each key, get all dates associated with the keys
		// for(MetaObject meta : bookingsList){
		//
		// 	List<MetaObject> bookingDatesList = null;
		//
		// 	bookingDatesList = core.getSubvenueBookings_byBookingId(meta.getString("_oid", ""));
		// 	System.out.println("Booking dates list " + bookingDatesList);
		//
		// 	allbookingsForSubvenue.add(bookingDatesList);
		// }

		res.put("list", bookingsList);

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
		rb.getNamespace(setPrefix + "subvenuebookingDates").put(HttpRequestType.GET, subvenueDates_GET_and_POST);
		rb.getNamespace(setPrefix + "subvenuebookingDates").put(HttpRequestType.POST, subvenueDates_GET_and_POST);

		rb.getNamespace(setPrefix + "subvenuebookings").put(HttpRequestType.GET, subvenueBookings_GET);
		rb.getNamespace(setPrefix + "subvenuebookings").put(HttpRequestType.POST, subvenueBookings_POST);

		rb.getNamespace(setPrefix + "subvenuebookings.status").put(HttpRequestType.GET, subvenueBookingStatus_GET_and_POST);
		rb.getNamespace(setPrefix + "subvenuebookings.status").put(HttpRequestType.POST, subvenueBookingStatus_GET_and_POST);

		bookingsApi = new MetaTableApiBuilder(core.subvenueBookingDates);
		subvenueBookingsApi = new MetaTableApiBuilder(core.subvenueBookings);
		//ownerApi.setupRESTBuilder( rb, setPrefix + "owner" );
		bookingsApi.setupRESTBuilder( rb, setPrefix + "subvenuebookingDates." );
		subvenueBookingsApi.setupRESTBuilder( rb, setPrefix + "subvenuebookings." );

		return rb;
	}

}
