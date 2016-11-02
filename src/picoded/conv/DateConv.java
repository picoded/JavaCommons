package picoded.conv;

import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;

import com.mysql.jdbc.StringUtils;

///
/// Convenience class to convert between date types
/// Month is 1-indexed
/// Default dateformat is DD-MM-YYYY like the rest of the civilised world uses
///
public class DateConv {
	
	/// Invalid constructor (throws exception)
	protected DateConv() {
		throw new IllegalAccessError("Utility class");
	}
	
	public enum ISODateFormat {
		DDMMYYYY, MMDDYYYY, YYYYMMDD, YYYYDDMM
	}
	
	public static ISODateFormat toISODateFormat(String format) {
		if (format == null || format.isEmpty()) {
			return ISODateFormat.DDMMYYYY;
		}
		
		String format_cleaned = RegexUtil.removeAllNonAlphaNumeric(format);
		
		if ("mmddyyyy".equalsIgnoreCase(format_cleaned)) {
			return ISODateFormat.MMDDYYYY;
		} else if ("yyyymmdd".equalsIgnoreCase(format_cleaned)) {
			return ISODateFormat.YYYYMMDD;
		} else if ("yyyyddmm".equalsIgnoreCase(format_cleaned)) {
			return ISODateFormat.YYYYDDMM;
		} else {
			return ISODateFormat.DDMMYYYY;
		}
	}
	
	public static String toISOFormat(long inDate, ISODateFormat dateFormat, String separator) {
		if (separator == null) {
			separator = "-";
		} else {
			//TODO sanitise separator string?
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(inDate);
		
		String date = String.valueOf(cal.get(Calendar.DATE));
		if (date.length() == 1) {
			date = "0" + date;
		}
		
		String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		
		String isoDate = String
			.valueOf(date + separator + month + separator + cal.get(Calendar.YEAR));
		isoDate = changeISODateFormat(isoDate, ISODateFormat.DDMMYYYY, dateFormat, separator);
		
		return isoDate;
	}
	
	///
	/// I return string to that i can return null if an error happened during conversion
	///
	public static String toMillisecondsFormat(String inDate, ISODateFormat currentDateFormat,
		String separator) {
		if (StringUtils.isNullOrEmpty(inDate)) {
			return null;
		}
		
		String newDate = changeISODateFormat(inDate, currentDateFormat, ISODateFormat.YYYYMMDD,
			separator);
		String[] newDateSplit = null;
		if (newDate != null) {
			newDateSplit = newDate.split(separator);
		}
		if (newDateSplit == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(newDateSplit[0]), Integer.parseInt(newDateSplit[1]) - 1,
			Integer.parseInt(newDateSplit[2]));
		return String.valueOf(cal.getTimeInMillis());
		
	}
	
	///
	/// Util functions
	///
	public static boolean isInISOFormat(String inDateString) {
		if (inDateString.indexOf('-') != inDateString.lastIndexOf('-')) {
			return true;
		}
		return false;
	}
	
	public static boolean isInMillisecondsFormat(String inDateString) {
		if (inDateString.startsWith("-") || !inDateString.contains("-")) {
			return true;
		}
		return false;
	}
	
	public static String getCurrentDateISO(ISODateFormat dateFormat, String separator) {
		if (separator == null) {
			separator = "-";
		}
		
		Calendar cal = Calendar.getInstance();
		int date = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		
		String newDate = String.valueOf(date + separator + month + separator + year); //ddmmyyyy
		newDate = changeISODateFormat(newDate, ISODateFormat.DDMMYYYY, dateFormat, separator);
		
		return newDate;
	}
	
	///
	/// Convert from one ISO date format to another format
	///
	public static String changeISODateFormat(String inDateISO, ISODateFormat currentDateFormat,
		ISODateFormat newDateFormat, String separator) {
		if (inDateISO == null || (currentDateFormat == null && newDateFormat == null)) {
			return null;
		}
		
		if (separator == null) {
			separator = "-";
		} else {
			//TODO sanitise separator string?
		}
		
		String[] dateSplit = inDateISO.split(separator);
		if (dateSplit.length != 3) {
			return null;
		}
		
		dateSplit = resortDateArray(dateSplit, currentDateFormat, newDateFormat);
		StringBuilder sb = new StringBuilder();
		for (byte i = 0; i < dateSplit.length; ++i) {
			sb.append(dateSplit[i]);
			
			if (i < dateSplit.length - 1) {
				sb.append(separator);
			}
		}
		
		return sb.toString();
	}
	
	private static String[] resortDateArray(String[] inDateSplit, ISODateFormat currentDateFormat,
		ISODateFormat newDateFormat) {
		byte[] currentDateSorting = getISODateSorting(currentDateFormat);
		byte[] newDateSorting = getISODateSorting(newDateFormat);
		String[] dateSplit = new String[3];
		for (byte i = 0; i < dateSplit.length; ++i) {
			dateSplit[i] = inDateSplit[ArrayUtils.indexOf(currentDateSorting, newDateSorting[i])];
		}
		
		return dateSplit;
	}
	
	private static byte[] getISODateSorting(ISODateFormat dateFormat) {
		switch (dateFormat) {
		case DDMMYYYY:
			return new byte[] { 0, 1, 2 };
		case MMDDYYYY:
			return new byte[] { 1, 0, 2 };
		case YYYYMMDD:
			return new byte[] { 2, 1, 0 };
		case YYYYDDMM:
			return new byte[] { 2, 0, 1 };
		default:
			return null;
		}
	}
}
