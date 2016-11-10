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
/// A product listing system, which is built ontop of MetaTable, and AtomicLongMap
///
public class SubvenueBookings {
	
	/// Subvenue Bookings metatable
	public MetaTable subvenueBookings = null;
	
	///Subvenue Booking Dates metatable
	public MetaTable subvenueBookingDates = null;
	
	private int bookings_max = 50;
	
	/// Empty constructor
	public SubvenueBookings() {
		//Does nothing : manual setup
	}
	
	public SubvenueBookings(JStruct inStruct, String prefix, String listing) {
		setupStandardTables(inStruct, prefix, listing);
	}
	
	public SubvenueBookings(JStruct inStruct, String prefix) {
		setupStandardTables(inStruct, prefix, null);
	}
	
	///
	/// Setup the standard tables, with the given JStruct
	///
	/// @param   The JStruct object to build ontop of
	/// @param   The table name prefix to generate the various meta table
	///
	public void setupStandardTables(JStruct inStruct, String prefix, String listing) {
		if (listing == null || listing.length() < 4) {
			subvenueBookings = inStruct.getMetaTable(prefix + "_subvenueBooking");
			subvenueBookingDates = inStruct.getMetaTable(prefix + "_subvenueBookingDates");
		} else {
			subvenueBookings = inStruct.getMetaTable(prefix + "_" + listing);
			subvenueBookingDates = inStruct.getMetaTable(prefix + "_" + listing);
		}
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemSetup() {
		subvenueBookings.systemSetup();
		subvenueBookingDates.systemSetup();
	}
	
	///
	/// Calls the systemSetup for the underlying MetaTable / AtomicLongMap
	///
	public void systemTeardown() {
		subvenueBookings.systemTeardown();
		subvenueBookingDates.systemTeardown();
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
	public GenericConvertList<MetaObject> getSubvenueBookings_bySubVenueId(String in_subvenueId) {
		// Sanity check
		if (in_subvenueId == null || in_subvenueId.isEmpty()) {
			throw new RuntimeException("Missing in_subvenueId");
		}
		
		// Return object
		GenericConvertList<MetaObject> ret = new GenericConvertArrayList<MetaObject>();
		
		// Fetch and populate
		MetaObject[] queryRet = subvenueBookings.query("_subvenueID=? AND (_status=? OR _status=?)",
			new String[] { in_subvenueId, "Paid", "Approved" }, "_createdTime", 0, bookings_max);
		if (queryRet != null && queryRet.length > 0) {
			for (int i = 0; i < queryRet.length; ++i) {
				ret.add(queryRet[i]);
			}
		}
		
		// Return
		return ret;
	}
	
	public GenericConvertList<MetaObject> getSubvenueBookings_byBookingId(String in_bookingID) {
		// Sanity check
		if (in_bookingID == null || in_bookingID.isEmpty()) {
			throw new RuntimeException("Missing in_bookingID");
		}
		
		// Return object
		GenericConvertList<MetaObject> ret = new GenericConvertArrayList<MetaObject>();
		
		// Fetch and populate
		MetaObject[] queryRet = subvenueBookings.query("_oid=?", new String[] { in_bookingID },
			"_createdTime", 0, bookings_max);
		if (queryRet != null && queryRet.length > 0) {
			for (int i = 0; i < queryRet.length; ++i) {
				ret.add(queryRet[i]);
			}
		}
		
		// Return
		return ret;
	}
	
	public GenericConvertList<MetaObject> getSubvenueBookingDates_byBookingId(String in_subvenueId) {
		// Sanity check
		if (in_subvenueId == null || in_subvenueId.isEmpty()) {
			throw new RuntimeException("Missing in_subvenueId");
		}
		
		// Return object
		GenericConvertList<MetaObject> ret = new GenericConvertArrayList<MetaObject>();
		
		// Fetch and populate
		MetaObject[] queryRet = subvenueBookingDates.query("_bookingID=?",
			new String[] { in_subvenueId }, "_createdTime", 0, bookings_max);
		if (queryRet != null && queryRet.length > 0) {
			for (int i = 0; i < queryRet.length; ++i) {
				ret.add(queryRet[i]);
			}
		}
		
		// Return
		return ret;
	}
	
	///
	/// Creates a new subvenue booking
	///
	/// @param  The subvenueID assigned
	/// @param  List of product objects to insert / update
	///
	/// @return a booking MetaObject
	///
	public GenericConvertMap<String, Object> createSubvenueBooking(String subvenueID,
		String eventID, String venueID, Float paymentAmount) {
		
		// Prepare the actual return object
		GenericConvertHashMap<String, Object> resMap = new GenericConvertHashMap<String, Object>();
		
		// The meta object to create / update
		MetaObject updateMetaObject = null;
		// New meta object in subvenue bookings table
		updateMetaObject = subvenueBookings.newObject();
		
		updateMetaObject.put("_subvenueID", subvenueID);
		updateMetaObject.put("_eventID", eventID);
		updateMetaObject.put("_status", "Pending");
		updateMetaObject.put("_venueID", venueID);
		updateMetaObject.put("_paymentAmount", paymentAmount);
		updateMetaObject.saveDelta();
		
		resMap.putAll(updateMetaObject);
		
		return resMap;
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
	public GenericConvertList<MetaObject> updateSubvenueBookingStatus(String in_subvenueId,
		String newStatus) {
		
		// Prepare the actual return object
		GenericConvertList<MetaObject> res = new GenericConvertArrayList<MetaObject>();
		
		// Sanity check
		if (in_subvenueId == null || in_subvenueId.isEmpty()) {
			throw new RuntimeException("Missing in_subvenueId");
		}
		
		// Sales object
		MetaObject bookingObject = subvenueBookings.get(in_subvenueId);
		
		// Update sales object status
		bookingObject.put("_status", newStatus);
		bookingObject.saveDelta();
		
		res.add(bookingObject);
		// Return
		return res;
	}
	
	///
	/// Creates booking slots for a given booking
	///
	/// @param  The bookingID assigned
	/// @param  List of product objects to insert / update
	///
	/// @return List of meta objects representing the owner
	///
	@SuppressWarnings("unchecked")
	public GenericConvertMap<String, Object> createBookingSlots(String bookingID,
		Map<String, Object> datesList) throws Exception {
		// Prepare the actual return object
		GenericConvertHashMap<String, Object> resMap = new GenericConvertHashMap<String, Object>();
		
		// The item order list
		ArrayList<MetaObject> itemList = new ArrayList<MetaObject>();
		
		// Assuming datesList is a Map<String, Object> which actually is a Map<String, List<Object>>
		for (String dateKey : datesList.keySet()) {
			Object timeslot_list_raw = datesList.get(dateKey);
			if (timeslot_list_raw instanceof List) {
				// list = [0500-0600, 0600-0700]
				List<Object> timeslot_list = (List<Object>) timeslot_list_raw;
				
				//create MetaObject
				// dateKey will be _date
				// timeslot_list will be _timeSlots
				
				// Single Date object
				MetaObject dateItem = subvenueBookingDates.newObject();
				
				// Link it all up
				dateItem.put("_bookingID", bookingID);
				dateItem.put("_date", dateKey);
				dateItem.put("_timeSlots", timeslot_list);
				
				dateItem.saveDelta();
				
				itemList.add(dateItem);
			} else {
				throw new Exception("timeslot_list_raw was not a List<Object>, was a "
					+ timeslot_list_raw.getClass());
			}
		}
		
		resMap.put("_booking", itemList);
		
		return resMap;
	}
}
