package picoded.dstack;

import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/// SerlvetLogging, is a utility class meant to facilitate the logging of server sideded application events, and errors
///
/// Note that this is the MINIMAL implementation used.
///
/// Servlet logging levels are as outlined
///
/// + error  : Exception or major violations
/// + warn   : Dangerous config changes, or user violations
/// + info   : Informative context, such as server startup / shutdown
/// + log    : Page request, the spammy stuff
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // Logs the error, with the additional parameters
/// logger.log("page error code: %i error performed by user %s", 500, "cats");
/// 
/// // Logs the error, with an exception
/// logger.log(caughtException, "page error code: %i error performed by user %s", 500, "dogs");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public interface EventLogger extends DStackCommon<String,String> {
	
	/// Core logWithLevel function
	default void logWithLevel(Level l, Exception e, String format, Object... args) {
		// Build the string and format
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		// Logs it in standrad log
		formatter.close();
		if (e != null) {
			Logger.getLogger(EventLogger.class.getName()).log(l, sb.toString(), e);
		} else {
			Logger.getLogger(EventLogger.class.getName()).log(l, sb.toString());
		}
	}
	
	/// logWithLevel 
	default void logWithLevel(Level l, String format, Object... args) {
		logWithLevel(l, (Exception) null, format, args);
	}
	
	//----------------------------------------------------------------
	//
	// Convinence functions
	//
	//----------------------------------------------------------------
	
	/// Log with exception
	default void log(Exception e, String format, Object... args) {
		logWithLevel(Level.FINE, e, format, args);
	}
	
	/// Log 
	default void log(String format, Object... args) {
		logWithLevel(Level.FINE, (Exception) null, format, args);
	}
	
	/// Info with exception
	default void info(Exception e, String format, Object... args) {
		logWithLevel(Level.INFO, e, format, args);
	}
	
	/// Info 
	default void info(String format, Object... args) {
		logWithLevel(Level.INFO, (Exception) null, format, args);
	}
	
	/// Info with exception
	default void warn(Exception e, String format, Object... args) {
		logWithLevel(Level.WARNING, e, format, args);
	}
	
	/// Info 
	default void warn(String format, Object... args) {
		logWithLevel(Level.WARNING, (Exception) null, format, args);
	}
	
	/// Error with exception
	default void error(Exception e, String format, Object... args) {
		logWithLevel(Level.SEVERE, e, format, args);
	}
	
	/// Error 
	default void error(String format, Object... args) {
		logWithLevel(Level.SEVERE, (Exception) null, format, args);
	}
	
}
