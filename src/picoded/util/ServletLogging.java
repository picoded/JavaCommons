
package picoded.util;

import picoded.jSql.*

/// SerlvetLogging, is a utility class meant to facilitate the logging of server sideded application events, and errors
/// This is meant to push the logging into a central SQL database, and fallbacks onto its local SQLite file, in event the
/// JSql connection fails.
///
/// !Note this is a conceptual draft. You may suggest better naming of variables / collumns / suggest changes to better facilitate the class
///
/// Core features
/// - DB failure resistent. Fallsback onto a local sqlite db
/// - SQLite local DB syncing data upwards to remote DB
/// - Centralized remote DB for all logs, to do useful stuff if live system does goes down (or do funny stuff)
/// - Possible extension of analytics options with SQL db data
/// - Querable logs, with their respective formats
/// - Java exception handling, with deduplication of stack trace data (since recurring exceptions, can easily pile up gigs of logs)
///
/// Notes
/// - Hash refers to MD5 of the string to a byte[16] array, then base58 convert them to a string. Produces a compact 22 character hash
/// -
///
/// Tricky bits (to settle later):
/// - how to set the JSql connection to low timeout? or must it be part of the JDBC constructor options parameters
///   - if so, can this be changed?. For example, can the JSql connection be replicated with the parameters, with the low timeout options
///   - you may need to settle issue #36 for this to work
///
public class ServletLogging {
	
	public ServletLogging(JSql sqlite, JSql db) {
		
	}
	
	public ServletLogging(String sqlitePath, JSql db) {
		
	}
	
	public int longIndexed = 04;
	public int stringIndexed = 04;
	
	public int longNotIndexed = 04;
	public int stringNotIndexed = 04;
	
	public int exceptionRootTraceIndexed = 02;
	public int exceptionThrownTraceIndexed = 02;
	
	/// Performs the needed table setup, required for the class. Do note that this includes the setup of logging formats and its respective tables
	/// Note that this table exists on both the sqlite, and the actual DB, all tables keep their data locally on sqlite, except logTable,
	/// which pushes to the central DB, and clears locally.
	///
	/// # PREFIX_logFormat
	/// This table represents the raw table formats, used by the logging framework, and has the following collumns
	///
	/// - hash
	/// - format (indexed, unique)
	/// - name (indexed, unique)
	///
	/// # PREFIX_logStrHashes
	/// Stores strings larger then certain size (21?) and hash them, this is so that duplicate strings can be found and reused
	///
	/// - hash (indexed)
	/// - sVal (indexed, unique TEXT string)
	///
	/// # PREFIX_logTable
	/// The actual log storage, note that all string values that are larger then a defined size (21?) is stored in PREFIX_logStrHashes instead,
	/// where its hash value is used. Note that the process to check for existing hashes, is done locally on the sqlite, before the central DB.
	///
	/// - instID  (indexed)        // instance ID, used to trace the hashing source
	/// - creTime (indexed)        // created unix timestamp
	/// - fmtHash (indexed)        // format string hash, see PREFIX_logStrHashes
	/// - logType (indexed)        // log data type
	/// - expHash (indexed)        // exception hash, see PREFIX_expHash
	/// - offSync (indexed)        // offline sync boolean indicator
	/// - reqID   (indexed)        // request ID, generated on each request session
	/// - l01-XX  (indexed)        // long values used in the format, %IP4 is converted to long values
	/// - s01-XX  (indexed)        // varchar string value storage, varchar(22)
	/// - lXX-YY  (not indexed)    // long values used in the format that is NOT indexed
	/// - sXX-YY  (not indexed)    // varchar string value storage, varchar(22)
	///
	/// # PREFIX_excTraceHash
	/// Contains the hash values of the stack traces. This function similarly to logStrHashes
	///
	/// - hash (indexed)
	/// - sVal (indexed, unique TEXT string)
	///
	/// # PREFIX_exception
	/// Table consisting of exception records, split in according to stack trace. The idea here is to prevent huge exception stack traces
	/// from flooding the database storage system, when they are essentially, the same messages.
	///
	/// - expHash    (indexed)     // Full exception message hash
	/// - creTime    (indexed)     // Exception created timestamp
	///
	/// (note the stack messages is filled in in the following order)
	/// - excRoot    (indexed)     // Exception root cause message
	/// - excTrace   (indexed)     // Exception thrown by message
	/// - excR01-XX  (indexed)     // Exception after root cause tracing messages
	/// - excT01-XX  (indexed)     // Exception thrown by tracing messages
	/// - excMid     (not indexed) // The remaining exception message in a JSON array
	///
	/// - stkRoot    (indexed)     // Stack tracing corresponding to the exception message
	/// - stkTrace   (indexed)     // Stack tracing corresponding to the exception message
	/// - stkR01-XX  (indexed)     // Stack tracing corresponding to the exception message
	/// - stkT01-XX  (indexed)     // Stack tracing corresponding to the exception message
	/// - stkMid     (not indexed) // Stack tracing corresponding to the exception message, in a JSON array
	///
	/// The following is sqlite only, used to cache configs, such as instanceID
	///
	/// # PREFIX_config
	/// - key (unique, indexed)
	/// - sVal
	///
	public ServletLogging tableSetup() {
		return this;
	}
	
	//public
}