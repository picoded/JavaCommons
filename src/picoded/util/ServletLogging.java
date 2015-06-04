package picoded.util;

import java.util.logging.*;

import picoded.conv.Base58;
import picoded.conv.GUID;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlType;
import picoded.util.systemInfo;
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
/// - Hash collisions are ignored. Really, what are the odds for unintentional hash collision.
///
/// Tricky bits (to settle later):
/// - how to set the JSql connection to low timeout? or must it be part of the JDBC constructor options parameters
///   - if so, can this be changed?. For example, can the JSql connection be replicated with the parameters, with the low timeout options
///   - you may need to settle issue #36 for this to work
///
public class ServletLogging {
	
	private static Logger logger = Logger.getLogger(ServletLogging.class.getName());

   /// Object byte space default as 260
	protected int objColumnLength = 260;
	
	/// Key byte space default as 260
	protected int keyColumnLength = 260;
	
	/// Value byte space default as 4000
	protected int valColumnLength = 4000;
	
	protected JSql sqliteObj;
	protected JSql jSqlObj;
	
	public ServletLogging(JSql sqlite, JSql db) {
		this.sqliteObj = sqlite;
		this.jSqlObj = db;
	}
	
	public ServletLogging(String sqlitePath, JSql db) {
		this.sqliteObj = JSql.sqlite(sqlitePath);
		this.jSqlObj = db;
	}
	
	public int longIndexed = 04;
	public int stringIndexed = 04;
	
	public int longNotIndexed = 04;
	public int stringNotIndexed = 04;
	
	public int exceptionRootTraceIndexed = 02;
	public int exceptionThrownTraceIndexed = 02;
	
	// / Performs the needed table setup, required for the class. Do note that
	// this includes the setup of logging formats and its respective tables
	// / Note that this table exists on both the sqlite, and the actual DB, all
	// tables keep their data locally on sqlite, except logTable,
	// / which pushes to the central DB, and clears locally.
	// /
	// / # PREFIX_logFormat
	// / This table represents the raw table formats, used by the logging
	// framework, and has the following columns
	// /
	// / - hash
	// / - format (indexed, unique)
	// / - name (indexed, unique)
	// /
	// / # PREFIX_logStrHashes
	// / Stores strings larger then certain size (21?) and hash them, this is so
	// that duplicate strings can be found and reused
	// /
	// / - hash (indexed)
	// / - sVal (indexed, unique TEXT string)
	// /
	// / # PREFIX_logTable
	// / The actual log storage, note that all string values that are larger
	// then a defined size (21?) is stored in PREFIX_logStrHashes instead,
	// / where its hash value is used. Note that the process to check for
	// existing hashes, is done locally on the sqlite, before the central DB.
	// /
	// / - systemHash (indexed) // systemHash, used to trace the logging source
	// / - reqsID (indexed) // request ID, used to trace the logging source
	// / - creTime (indexed) // created unix timestamp
	// / - fmtHash (indexed) // format string hash, see PREFIX_logStrHashes
	// / - logType (indexed) // log data type
	// / - expHash (indexed) // exception hash, see PREFIX_expHash
	// / - offSync (indexed) // offline sync boolean indicator
	// / - reqID (indexed) // request ID, generated on each request session
	// / - l01-XX (indexed) // long values used in the format, %IP4 is converted
	// to long values
	// / - s01-XX (indexed) // varchar string value storage, varchar(22)
	// / - lXX-YY (not indexed) // long values used in the format that is NOT
	// indexed
	// / - sXX-YY (not indexed) // varchar string value storage, varchar(22)
	// /
	// / # PREFIX_excStrHash
	// / Contains the hash values of the stack traces. This function similarly
	// to logStrHashes
	// /
	// / - hash (indexed)
	// / - sVal (indexed, unique TEXT string)
	// /
	// / # PREFIX_exception
	// / Table consisting of exception records, split in according to stack
	// trace. The idea here is to prevent huge exception stack traces
	// / from flooding the database storage system, when they are essentially,
	// the same messages.
	// /
	// / - expHash (indexed) // Full exception message hash
	// / - reqsID (indexed) // request ID, used to trace the logging source
	// / - creTime (indexed) // Exception created timestamp
	// / - systemHash (indexed) // systemHash, used to trace the hashing source
	// /
	// / (note the stack messages is filled in in the following order)
	// / - excRoot (indexed) // Exception root cause message
	// / - excTrace (indexed) // Exception thrown by message
	// / - excR01-XX (indexed) // Exception after root cause tracing messages
	// / - excT01-XX (indexed) // Exception thrown by tracing messages
	// / - excMid (not indexed) // The remaining exception message in a JSON
	// array
	// /
	// / - stkRoot (indexed) // Stack tracing corresponding to the exception
	// message
	// / - stkTrace (indexed) // Stack tracing corresponding to the exception
	// message
	// / - stkR01-XX (indexed) // Stack tracing corresponding to the exception
	// message
	// / - stkT01-XX (indexed) // Stack tracing corresponding to the exception
	// message
	// / - stkMid (not indexed) // Stack tracing corresponding to the exception
	// message, in a JSON array
	// /
	// / The following is sqlite only, used to cache configs, such as systemHash
	// /
	// / # PREFIX_config
	// / - key (unique, indexed)
	// / - sVal
	// /
	public ServletLogging tableSetup() throws JSqlException {
		// table setup for sqlite
		tableSetup(sqliteObj);
		
		// table setup for db
		tableSetup(jSqlObj);

      return this;
	}
	
	private void tableSetup(JSql jSqlObj) throws JSqlException {
		// / config table
		if (jSqlObj.sqlType == JSqlType.sqlite) {
			jSqlObj.execute("CREATE TABLE IF NOT EXISTS `config` ( " + "key VARCHAR(" + objColumnLength
				+ "), sVal TEXT, PRIMARY KEY (key) );");
		}
		
		// / logFormat table
		jSqlObj.execute("CREATE TABLE IF NOT EXISTS `logFormat` ( " + "hash VARCHAR(" + objColumnLength + "), "
			+ "format VARCHAR(" + keyColumnLength + "), " + " PRIMARY KEY (hash) );");
		
		// / logStrHashes table
		jSqlObj.execute("CREATE TABLE IF NOT EXISTS `logStrHashes` ( " + "hash VARCHAR(" + objColumnLength
			+ "), sVal TEXT, PRIMARY KEY (hash) );");
		
		// / excStrHash table
		jSqlObj.execute("CREATE TABLE IF NOT EXISTS `excStrHash` ( " + "hash VARCHAR(" + objColumnLength
			+ "), sVal TEXT, PRIMARY KEY (hash) );");
		
		// / exception table
		jSqlObj.execute("CREATE TABLE IF NOT EXISTS `exception` ( " + "expHash VARCHAR(" + objColumnLength + "), "
			+ "reqsID VARCHAR(" + objColumnLength + "), " + "creTime BIGINT, systemHash VARCHAR(60), " + "excRoot VARCHAR("
			+ valColumnLength + "), " + "excTrace VARCHAR(" + valColumnLength + "), " + "excR01-XX VARCHAR("
			+ valColumnLength + "), " + "excT01-XX VARCHAR(" + valColumnLength + "), " + "excMid VARCHAR("
			+ valColumnLength + ")," + "stkRoot VARCHAR(" + valColumnLength + "), " + "stkTrace VARCHAR("
			+ valColumnLength + "), " + "stkR01-XX VARCHAR(" + valColumnLength + "), " + "stkT01-XX VARCHAR("
			+ valColumnLength + "), " + "stkMid VARCHAR(" + valColumnLength + ")," + "PRIMARY KEY (expHash) );");
      
		// / logTable table
		jSqlObj.execute("CREATE TABLE IF NOT EXISTS `logTable` ( " + "systemHash VARCHAR(" + objColumnLength + "), "
			+ "reqsID VARCHAR(" + objColumnLength + "), " + "creTime BIGINT, " + "fmtHash VARCHAR(" + objColumnLength
			+ "), " + "logType VARCHAR(" + valColumnLength + "), " + "expHash VARCHAR(" + objColumnLength + "), "
			+ "offSync BIT, " + "reqID VARCHAR(" + objColumnLength + "), " + "l01-XX VARCHAR(" + valColumnLength + "), "
			+ "s01-XX VARCHAR(" + valColumnLength + "), " + "lXX-YY VARCHAR(" + valColumnLength + "), "
			+ "sXX-YY VARCHAR(" + valColumnLength + ") )");
	}
	
	// / Returns the systemHash stored inside the SQLite DB, if it does not
	// exists, generate one
	public String systemHash() throws JSqlException {
		String val = null;
		JSqlResult r = sqliteObj.executeQuery("SELECT sVal FROM `config` WHERE key='systemHash'");
		if (r.fetchAllRows() > 0) {
			val = (String) r.readRowCol(0, "sVal");
		return val;
      }
      //if systemHash does not exists generate new
		return GUID.base58();
	}
	
	// / Validate if the systemHash if it belongs to the current physical 
	// virtual server. If it fails, it reissues the systemHash. Used on servlet startup

	public String validateSystemHash() {
      //fetch systemHash from config table
      String sysHash = systemHash();
      //compare systemHash
      if(sysHash != null && sysHash.equalsIgnoreCase(systemInfo.systemaHash())){
         return sysHash;
      }
      // reissues the systemHash if validation failes.
      sysHash = systemInfo.systemaHash();
      
      // persist the hash value in SQLite
		sqliteObj.execute("INSERT INTO `config` (systemHash, sVal) VALUES (?, ?)", "systemHash", sysHash);
      
		return sysHash;
	}
	
	// / Returns the current request ID
	public String requestID() throws JSqlException {
		String requestId = null;
		JSqlResult r = jSqlObj.executeQuery("SELECT reqID FROM `logTable` WHERE systemHash = " + systemHash());
		if (r.fetchAllRows() > 0) {
			requestId = (String) r.readRowCol(0, "reqID");
		}
		return requestId;
	}
	
	// / Reissue a new requestID, used at start of servlet call
	public String reissueRequestID() throws Exception {
		return GUID.base58();
	}
	
	// / Add the format to the system ---->will be deleted
	//hash   (base58 md5, indexed) ,format (indexed, unique)
	public void addFormat(String fmtHash, String format) throws Exception {
		jSqlObj.execute("INSERT INTO `logFormat` (hash, format) VALUES (?, ?)", fmtHash, format);
	}
	
	/// You should not be calling log using format with no arguments
	public void log(String format) {
		throw new RuntimeException("Do not use the logger, without arguments (it makes this whole class pointless");
	}
	
	/// Performs a logging with a format name and argument
	public void log(String format, Object... args) {
		//call addFormat to add format
		addFormat(GUID.base58(), format);
      
		//insert args to `logTable`
		jSqlObj.execute("INSERT INTO `logTable` "
			+ "(systemHash, reqsID, creTime, fmtHash, logType, expHash, offSync, reqID, l01-XX, s01-XX, lXX-YY, sXX-YY) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", args);
	}
	
	/// Performs a logging with a formant name, argument, and attached exception
	public void logException(Exception e, String format, Object... args) {
		//call addFormat to add format
		addFormat(GUID.base58(), format);

      //insert args to `exception`
		jSqlObj.execute("INSERT INTO `exception` "
			+ "(expHash,reqsID,creTime,systemHash,excRoot,excTrace,excR01-XX,excT01-XX,excMid,stkRoot"
			+ ",stkTrace,stkR01-XX,stkT01-XX,stkMid) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", args);
      
      logger.log(Level.SEVERE, "Exception", e);
	}
	
}