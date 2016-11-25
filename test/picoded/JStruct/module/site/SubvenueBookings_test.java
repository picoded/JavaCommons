package picoded.JStruct.module.site;

import static org.junit.Assert.*;

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
		subvenueBookings = new SubvenueBookings(jStruct, tableName);
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void createSubvenueBookingTest() {
		assertNotNull(subvenueBookings.createSubvenueBooking("subvenueID", "eventID", "venueID",
			10.0f));
	}
	
	@Test
	public void setupStandardTablesTest() {
		subvenueBookings.setupStandardTables(jStruct, tableName, "test");
		
	}
	
	@Test
	public void getSubvenueBookings_bySubVenueIdTest() {
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId(tableName));
		assertNotNull(subvenueBookings.getSubvenueBookings_bySubVenueId("_subvenueID"));
		
	}
	
}
