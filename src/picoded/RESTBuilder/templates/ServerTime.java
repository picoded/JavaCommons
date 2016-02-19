package picoded.RESTBuilder.templates;

import picoded.RESTBuilder.*;
import picoded.conv.DateConv;
import picoded.conv.DateConv.ISODateFormat;

/// ServerTime API utility library. Small collection of server timestamp based RESTFunction's
public class ServerTime {
	
	///
	/// Time stamp from server
	///
	/// Default returns a String ISO formatted date in DD-MM-YYYY format, and the date in milliseconds format
	///
	/// # Params
	/// ------------------------------------------------------------------------------------------------------
	/// ISOFormat (Optional,String) | ISO date format. Example "DDMMYYYY", "MMDDYYYY". Default is "DDMMYYYY"  
	/// ------------------------------------------------------------------------------------------------------
	///
	/// # Returns
	/// ---------------------------------------------------------------------------
	/// ISODate          (String) | Current date in ISO format
	/// unixMilliseconds (Long)   | Current date in unixtime milliseconds format
	/// unixSeconds      (Long)   | Current date in unixtime seconds format
	/// --------------------------------------------------------------------------- 
	///
	public static RESTFunction now = (req, res) -> {
		//fetch current time as ISO
		String ISOFormat = req.getString("ISOFormat", "DDMMYYYY");
		ISODateFormat dateFormat = DateConv.toISODateFormat(ISOFormat);
		
		String currentDateISO = "";
		try {
			currentDateISO = DateConv.getCurrentDateISO(dateFormat, "-");
		} catch (Exception ex) {
			res.put("error", "Unable to get ISO Date from ISOFormat " + ISOFormat + ".");
			return res;
		}
		
		//return ISO date
		res.put("ISODate", currentDateISO);
		
		//convert to milliseconds
		String currentDateMilli = DateConv.toMillisecondsFormat(currentDateISO, dateFormat, "-");
		long currentDateMilliAsLong = 0;
		
		try {
			currentDateMilliAsLong = Long.parseLong(currentDateMilli);
		} catch (Exception ex) {
			res.put("error", "Unable to convert ISO Date " + currentDateMilli + " into a long. ");
			return res;
		}
		
		//return milliseconds date
		res.put("unixMilliseconds", currentDateMilliAsLong);
		res.put("unixSeconds", currentDateMilliAsLong / 1000);
		
		return res;
	};
	
}
