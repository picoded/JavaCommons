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

import picoded.core.conv.DateConv.ISODateFormat;

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
		
		String isoDate = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		
		assertEquals(calISODate, isoDate);
	}
	
	@Test
	public void convISOToMilliseconds() {
		String isoDate = "1990-05-20";
		
		String millisecondsDate = DateConv.toMillisecondsFormat(isoDate, ISODateFormat.YYYYMMDD, "-");
		
		String isoDateReconstructed = DateConv.toISOFormat(Long.parseLong(millisecondsDate),
			ISODateFormat.YYYYMMDD, "-");
		
		assertEquals(isoDate, isoDateReconstructed);
		isoDate = "2016-10-25";
		millisecondsDate = DateConv.toMillisecondsFormat(isoDate, ISODateFormat.YYYYMMDD, "-");
		isoDateReconstructed = DateConv.toISOFormat(Long.parseLong(millisecondsDate),
			ISODateFormat.YYYYMMDD, "-");
		assertEquals(isoDate, isoDateReconstructed);
	}
	
	@Test
	public void changeISOFormat() {
		
		long millisecondsDate = Long.parseLong("1431756800000"); //16-5-2015
		String isoDate_dmy = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		
		assertNull(DateConv.changeISODateFormat(null, null, null, null));
		assertNull(DateConv.changeISODateFormat("", ISODateFormat.DDMMYYYY, ISODateFormat.YYYYMMDD,
			null));
		
		isoDate_dmy = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, "-");
		assertEquals("16-05-2015", isoDate_dmy);
		
		isoDate_dmy = DateConv.toISOFormat(millisecondsDate, ISODateFormat.DDMMYYYY, null);
		assertEquals("16-05-2015", isoDate_dmy);
		
		String isoDate_ymd = DateConv.changeISODateFormat(isoDate_dmy, ISODateFormat.DDMMYYYY,
			ISODateFormat.YYYYMMDD, "-");
		assertEquals("2015-05-16", isoDate_ymd);
		
		String isoDate_mdy = DateConv.changeISODateFormat(isoDate_ymd, ISODateFormat.YYYYMMDD,
			ISODateFormat.MMDDYYYY, "-");
		assertEquals("05-16-2015", isoDate_mdy);
		
		String isoDate_ydm = DateConv.changeISODateFormat(isoDate_mdy, ISODateFormat.MMDDYYYY,
			ISODateFormat.YYYYDDMM, "-");
		assertEquals("2015-16-05", isoDate_ydm);
		
	}
	
	@Test
	public void toISODateFormat() {
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat(null));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat(""));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat("ddmmyyyy"));
		assertEquals(ISODateFormat.MMDDYYYY, DateConv.toISODateFormat("mmddyyyy"));
		assertEquals(ISODateFormat.YYYYMMDD, DateConv.toISODateFormat("yyyymmdd"));
		assertEquals(ISODateFormat.YYYYDDMM, DateConv.toISODateFormat("yyyyddmm"));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat("abc"));
	}
	
	@Test
	public void toMillisecondsFormat() {
		assertNull(DateConv.toMillisecondsFormat(null, null, null));
		assertNull(DateConv.toMillisecondsFormat("", null, null));
		assertNull(DateConv.toMillisecondsFormat("2016-10-25-12", null, "-"));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat(""));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat("ddmmyyyy"));
		assertEquals(ISODateFormat.MMDDYYYY, DateConv.toISODateFormat("mmddyyyy"));
		assertEquals(ISODateFormat.YYYYMMDD, DateConv.toISODateFormat("yyyymmdd"));
		assertEquals(ISODateFormat.YYYYDDMM, DateConv.toISODateFormat("yyyyddmm"));
		assertEquals(ISODateFormat.DDMMYYYY, DateConv.toISODateFormat("abc"));
	}
	
	@Test
	public void isInISOFormat() {
		assertTrue(DateConv.isInISOFormat("-str-"));
		assertFalse(DateConv.isInISOFormat("str"));
	}
	
	@Test
	public void isInMillisecondsFormat() {
		assertTrue(DateConv.isInMillisecondsFormat("-str"));
		assertFalse(DateConv.isInMillisecondsFormat("str-"));
		assertTrue(DateConv.isInMillisecondsFormat("str"));
	}
	
	@Test
	public void getCurrentDateISO() {
		assertNotNull(DateConv.getCurrentDateISO(ISODateFormat.DDMMYYYY, null));
		assertNotNull(DateConv.getCurrentDateISO(ISODateFormat.DDMMYYYY, "-"));
	}
}