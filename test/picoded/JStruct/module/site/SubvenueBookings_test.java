package picoded.JStruct.module.site;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JStruct.JStruct;

public class SubvenueBookings_test {
	
	SubvenueBookings subvenueBookings = null;
	JStruct jStruct = null;
	// Tablename to string
	private String tableName = TestConfig.randomTablePrefix();
	
	@Before
	public void setUp() {
		jStruct = new JStruct();
		subvenueBookings = new SubvenueBookings();
		subvenueBookings = new SubvenueBookings(jStruct, tableName);
		subvenueBookings = new SubvenueBookings(jStruct, tableName, null);
		subvenueBookings.systemSetup();
	}
	
	@After
	public void tearDown() {
		subvenueBookings.systemTeardown();
	}
	
	@Test
	public void createSubvenueBookingTest() {
		assertNotNull(subvenueBookings.createSubvenueBooking("subvenueID", "eventID", "venueID",
			10.0f));
	}
	
	@Test
	public void setupStandardTablesTest() {
		subvenueBookings.setupStandardTables(jStruct, tableName, "test");
		subvenueBookings.setupStandardTables(jStruct, tableName, "");
		subvenueBookings.setupStandardTables(jStruct, tableName, null);
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookings_bySubVenueIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(null));
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(""));
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId("_subvenueID"));
		
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookings_byBookingIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId(null));
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId(""));
		assertNotNull(subvenueBookings.getSubvenueBookings_byBookingId("_bookingID"));
		
	}
	
	@Test(expected = Exception.class)
	public void getSubvenueBookingDates_byBookingIdTest() throws Exception {
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId(null));
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId(""));
		assertNotNull(subvenueBookings.getSubvenueBookingDates_byBookingId("_subvenueID"));
		
	}
	
	@Test(expected = Exception.class)
	public void updateSubvenueBookingStatusTest() throws Exception {
		assertNotNull(subvenueBookings.updateSubvenueBookingStatus(null, null));
		assertNotNull(subvenueBookings.updateSubvenueBookingStatus("", ""));
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
