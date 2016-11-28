package picoded.JStruct.module.site;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaTable;
import picoded.struct.GenericConvertHashMap;

public class SubvenueBookings_test {
	
	SubvenueBookings subvenueBookings = null;
	JStruct jStruct = null;
	// Tablename to string
	private String tableName = TestConfig.randomTablePrefix();
	GenericConvertHashMap<String, Object> resMap = null;
	
	@Before
	public void setUp() {
		jStruct = new JStruct();
		subvenueBookings = new SubvenueBookings();
		subvenueBookings = new SubvenueBookings(jStruct, tableName);
		subvenueBookings = new SubvenueBookings(jStruct, tableName, null);
		subvenueBookings.systemSetup();
		resMap = new GenericConvertHashMap<String, Object>();
	}
	
	@After
	public void tearDown() {
		subvenueBookings.systemTeardown();
	}
	
	// / To override for implementation
	// /------------------------------------------------------
	public MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable(tableName);
	}
	
	@Test
	public void createSubvenueBookingTest() {
		assertNotNull(resMap = (GenericConvertHashMap<String, Object>) subvenueBookings
			.createSubvenueBooking("subvenueID", "eventID", "venueID", 10.0f));
	}
	
	@Test
	public void setupStandardTablesTest() {
		subvenueBookings.setupStandardTables(jStruct, tableName, "test");
		subvenueBookings.setupStandardTables(jStruct, tableName, "");
		subvenueBookings.setupStandardTables(jStruct, tableName, null);
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookings_bySubVenueIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(resMap.get("_subvenueID")
			.toString()));
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(null));
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(""));
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookings_byBookingIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId("_bookingID"));
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId(null));
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId(""));
		
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookingDates_byBookingIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId(resMap.get("_subvenueID")
			.toString()));
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId(null));
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId(""));
		
	}
	
	@Test(expected = Exception.class)
	public void updateSubvenueBookingStatusTest() throws Exception {
		subvenueBookings.subvenueBooking = implementationConstructor();
		assertNotNull(subvenueBookings.updateSubvenueBookingStatus(null, null));
		assertNotNull(subvenueBookings.updateSubvenueBookingStatus(resMap.get("_subvenueID")
			.toString(), ""));
		assertNotNull(subvenueBookings.updateSubvenueBookingStatus("_subvenueID", "newStatus"));
	}
	
	@Test(expected = Exception.class)
	public void createBookingSlotsTest() throws Exception {
		Map<String, Object> datesList = new HashMap<String, Object>();
		assertNotNull(subvenueBookings.createBookingSlots(null, datesList));
		List<Object> list = new ArrayList<Object>();
		list.add("2016-11-28");
		list.add("2016-11-28");
		datesList.put("date", list);
		assertNotNull(subvenueBookings.createBookingSlots("date", datesList));
		datesList = new HashMap<String, Object>();
		datesList.put("date", "2016-11-28");
		assertNotNull(subvenueBookings.createBookingSlots("date", datesList));
	}
}
