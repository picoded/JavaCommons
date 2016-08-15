package picoded.JStruct;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

import picoded.conv.Base58;
import picoded.conv.GUID;
import picoded.utils.systemInfo;

import java.math.BigInteger;
import java.security.MessageDigest;

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
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // Logs the error, with the additional parameters
/// logger.log("page error code: %i error performed by user %s", 500, "cats");
/// 
/// // Logs the error, with an exception
/// logger.log(caughtException, "page error code: %i error performed by user %s", 500, "dogs");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public interface EventLogger {
	
	// // Minimal implementation logger
	// private static Logger logger = Logger.getLogger(EventLogger.class.getName());
	// 
	// /// Converts from LogLevel, to minimal implementation
	// protected static Level stdLevelFromLogLevel(LogLevel l) {
	// 	if (l == LogLevel.ERROR) {
	// 		return Level.SEVERE;
	// 	} else if (l == LogLevel.WARN) {
	// 		return Level.WARNING;
	// 	} else if (l == LogLevel.INFO) {
	// 		return Level.INFO;
	// 	}
	// 	return Level.INFO;
	// }
	
	// ///
	// /// Centralized logging, which the other functions call
	// ///
	// protected void logWithLevel(LogLevel level, Exception e, String format, Object... args) {
	// 	
	// 	// // Build the string and format
	// 	// StringBuilder sb = new StringBuilder();
	// 	// Formatter formatter = new Formatter(sb, Locale.US);
	// 	// formatter.format(format, args);
	// 	// 
	// 	// // Logs it in standrad log
	// 	// if (e != null) {
	// 	// 	logger.log(stdLevelFromLogLevel(level), sb.toString(), e);
	// 	// } else {
	// 	// 	logger.log(stdLevelFromLogLevel(level), sb.toString());
	// 	// }
	// }
	
	/// Core logging function
	public void log(Level l, Exception e, String format, Object... args);
	
	//----------------------------------------------------------------
	//
	// Convinence functions
	//
	//----------------------------------------------------------------
	
	/// Log 
	public default void log(Level l, String format, Object... args) {
		log(l, (Exception)null, format, args);
	}
	
	/// Info with exception
	public default void info(Exception e, String format, Object... args) {
		log(Level.INFO, e, format, args);
	}
	
	/// Info 
	public default void info(String format, Object... args) {
		log(Level.INFO, (Exception)null, format, args);
	}
	
	/// Info with exception
	public default void warn(Exception e, String format, Object... args) {
		log(Level.WARNING, e, format, args);
	}
	
	/// Info 
	public default void warn(String format, Object... args) {
		log(Level.WARNING, (Exception)null, format, args);
	}
	
	/// Error with exception
	public default void error(Exception e, String format, Object... args) {
		log(Level.SEVERE, e, format, args);
	}
	
	/// Error 
	public default void error(String format, Object... args) {
		log(Level.SEVERE, (Exception)null, format, args);
	}
	
}
