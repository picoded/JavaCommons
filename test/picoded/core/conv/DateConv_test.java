package picoded.core.conv;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.core.conv.ISODateConv.ISODateFormat;

public class DateConv_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new DateConv();
		
	}
	
	@Test
	public void convMilliSecondsToISO() {
		long millisecondsDate = Long.parseLong("1441756800000");
		
		//check case
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisecondsDate);
		String calISODate = "0" + cal.get(Calendar.DATE) + "-0" + (cal.get(Calendar.MONTH) + 1) + "-"
			+ cal.get(Calendar.YEAR);
		
		String isoDate = ISODateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		
		assertEquals(calISODate, isoDate);
	}
	
	@Test
	public void convISOToMilliseconds() {
		String isoDate = "1990-05-20";
		
		String millisecondsDate = ISODateConv.toMillisecondsFormat(isoDate, ISODateFormat.YYYYMMDD, "-");
		
		String isoDateReconstructed = ISODateConv.toISOFormat(Long.parseLong(millisecondsDate),
			ISODateFormat.YYYYMMDD, "-");
		
		assertEquals(isoDate, isoDateReconstructed);
		isoDate = "2016-10-25";
		millisecondsDate = ISODateConv.toMillisecondsFormat(isoDate, ISODateFormat.YYYYMMDD, "-");
		isoDateReconstructed = ISODateConv.toISOFormat(Long.parseLong(millisecondsDate),
			ISODateFormat.YYYYMMDD, "-");
		assertEquals(isoDate, isoDateReconstructed);
	}
	
	@Test
	public void changeISOFormat() {
		
		long millisecondsDate = Long.parseLong("1431756800000"); //16-5-2015
		String isoDate_dmy = ISODateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		
		assertNull(ISODateConv.changeISODateFormat(null, null, null, null));
		assertNull(ISODateConv.changeISODateFormat("", ISODateFormat.DDMMYYYY, ISODateFormat.YYYYMMDD,
			null));
		
		isoDate_dmy = ISODateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		assertEquals("16-05-2015", isoDate_dmy);
		
		isoDate_dmy = ISODateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, null);
		assertEquals("16-05-2015", isoDate_dmy);
		
		String isoDate_ymd = ISODateConv.changeISODateFormat(isoDate_dmy, ISODateFormat.DDMMYYYY,
			ISODateFormat.YYYYMMDD, "-");
		assertEquals("2015-05-16", isoDate_ymd);
		
		String isoDate_mdy = ISODateConv.changeISODateFormat(isoDate_ymd, ISODateFormat.YYYYMMDD,
			ISODateFormat.MMDDYYYY, "-");
		assertEquals("05-16-2015", isoDate_mdy);
		
		String isoDate_ydm = ISODateConv.changeISODateFormat(isoDate_mdy, ISODateFormat.MMDDYYYY,
			ISODateFormat.YYYYDDMM, "-");
		assertEquals("2015-16-05", isoDate_ydm);
		
	}
	
	@Test
	public void toISODateFormat() {
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat(null));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat(""));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat("ddmmyyyy"));
		assertEquals(ISODateFormat.MMDDYYYY, ISODateConv.toISODateFormat("mmddyyyy"));
		assertEquals(ISODateFormat.YYYYMMDD, ISODateConv.toISODateFormat("yyyymmdd"));
		assertEquals(ISODateFormat.YYYYDDMM, ISODateConv.toISODateFormat("yyyyddmm"));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat("abc"));
	}
	
	@Test
	public void toMillisecondsFormat() {
		assertNull(ISODateConv.toMillisecondsFormat(null, null, null));
		assertNull(ISODateConv.toMillisecondsFormat("", null, null));
		assertNull(ISODateConv.toMillisecondsFormat("2016-10-25-12", null, "-"));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat(""));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat("ddmmyyyy"));
		assertEquals(ISODateFormat.MMDDYYYY, ISODateConv.toISODateFormat("mmddyyyy"));
		assertEquals(ISODateFormat.YYYYMMDD, ISODateConv.toISODateFormat("yyyymmdd"));
		assertEquals(ISODateFormat.YYYYDDMM, ISODateConv.toISODateFormat("yyyyddmm"));
		assertEquals(ISODateFormat.DDMMYYYY, ISODateConv.toISODateFormat("abc"));
	}
	
	@Test
	public void isInISOFormat() {
		assertTrue(ISODateConv.isInISOFormat("-str-"));
		assertFalse(ISODateConv.isInISOFormat("str"));
	}
	
	@Test
	public void isInMillisecondsFormat() {
		assertTrue(ISODateConv.isInMillisecondsFormat("-str"));
		assertFalse(ISODateConv.isInMillisecondsFormat("str-"));
		assertTrue(ISODateConv.isInMillisecondsFormat("str"));
	}
	
	@Test
	public void getCurrentDateISO() {
		assertNotNull(ISODateConv.getCurrentDateISO(ISODateFormat.DDMMYYYY, null));
		assertNotNull(ISODateConv.getCurrentDateISO(ISODateFormat.DDMMYYYY, "-"));
	}
}