package picoded.ServletLogging;

import picoded.ServletLogging.LogMessage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import picoded.conv.Base58;
import picoded.conv.GUID;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlType;
import picoded.util.systemInfo;

import java.math.BigInteger;
import java.security.MessageDigest;

import java.util.Arrays;

/// SerlvetLogging, is a utility class meant to facilitate the logging of server sideded application events, and errors
/// This is meant to push the logging into a central SQL database, and fallbacks onto its local SQLite file, in event the
/// JSql connection fails.
///
/// !Note this is a conceptual draft. You may suggest better naming of variables / collumns / suggest changes to better facilitate the class
///
/// Core features
/// - DB failure resistent. Fallback onto a local sqlite db
/// - SQLite local DB syncing data upwards to remote DB
/// - Centralised remote DB for all logs, to do useful stuff if live system does goes down (or do funny stuff)
/// - Possible extension of analytics options with SQL db data
/// - Querable logs, with their respective formats
/// - Java exception handling, with de-duplication of stack trace data (since recurring exceptions, can easily pile up gigs of logs)
///
/// Notes
/// - Hash refers to MD5 of the string to a byte[16] array, then base58 convert them to a string. Produces a compact 22 character hash
/// - Hash collisions are ignored. Really, what are the odds for unintentional hash collision.
///
/// Tricky bits (to settle later):
/// - how to set the JSql connection to low timeout? or must it be part of the JDBC constructor options parameters
///   - if so, can this be changed?. For example, can the JSql connection be replicated with the parameters, with the low timeout options
///   - you may need to settle issue #36 for this to work
///   - To consider a logMessage class?
/// # Example usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // Returns the cached instance ID, unique to the package 
/// logger.instanceID(); //GUID1
///
/// // Returns a request id unique to the instance object. 
/// logger.requestID(); //GUID1
///
/// // Returns the same GUID as the previous, call as it has already been initialised
/// logger.requestID(); //GUID1
///
/// // Returns a new GUID, as it is reissued
/// logger.reissueRequestID(); //GUID2
/// 
/// // Returns the same GUID as the previous
/// logger.requestID(); //GUID2
///
/// // Ensures the error format is added to the logFormat table (both locally, and remotely)
/// logger.log("page error code: %i error performed by user %s", );
/// 
/// // Logs the error, with the additional parameters
/// logger.log("standard page error", "page error code: %i error performed by user %s", 500, "cats");
/// 
/// // Logs the error, with an exception
/// logger.log(caughtException, "page error code: %i error performed by user %s", 500, "dogs");
/// 
/// // Query and list the log messages
/// logMessage[] msgs1 = logger.list(offset, limit)
///
/// // Query with message format
/// logMessage[] msgs2 = logger.list( "page error code: %i error performed by user %s", offset, limit);
///
/// // Query with message search
/// logMessage[] msgs3 = logger.list( "page error code: %i error performed by user %s", offset, limit, "i1 = ? AND s1 = ?", 500, "dogs" );
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class ServletLogging {
	
	private static Logger logger = Logger.getLogger(ServletLogging.class.getName());

	/// Object ID field type
	protected String objColumnType = "VARCHAR(32)";

	/// Key name field type
	protected String keyColumnType = "VARCHAR(32)";

	/// Full text value field type
	protected String fullTextColumnType = "VARCHAR(MAX)";

	/// Text value field type
	protected String textColumnType = "TEXT";

	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";

	/// Long collumn type
	protected String longColumnType = "LONG";
	
	/// Bit collumn type
	protected String bitColumnType = "BIT";

	/// Primary key
	protected String pKeyColumnType = " PRIMARY KEY";
	
	/// String indexed column prefix
	private String stringIndexedPrefix = "s";
	
	/// Long indexed column prefix
	private String longIndexedPrefix = "l";
	
	public int longIndexed = 02;
	public int stringIndexed = 02;
	
	public int longNotIndexed = 02;
	public int stringNotIndexed = 02;
	
	public int exceptionRootTraceIndexed = 02;
	public int exceptionThrownTraceIndexed = 02;
	
	protected JSql sqliteObj;
	protected JSql jSqlObj;
	
	public ServletLogging(JSql sqlite, JSql dbObj) {
		this.sqliteObj = sqlite;
		this.jSqlObj = dbObj;
	}
	
	public ServletLogging(String sqlitePath, JSql dbObj) {
		this.sqliteObj = JSql.sqlite(sqlitePath);
		this.jSqlObj = dbObj;
	}
	
	/// Performs the needed table setup, required for the class. Do note that this includes the setup of logging formats and its respective tables
    /// Note that this table exists on both the sqlite, and the actual DB, all tables keep their data locally on sqlite, except logTable,
    /// which pushes to the central DB, and clears locally.
    ///
    /// # PREFIX_logFormat
    /// This table represents the raw table formats, used by the logging framework, and has the following columns
    ///
    /// - hash   (base58 md5, indexed)
    /// - format (indexed, unique)
    ///
    /// # PREFIX_logStrHashes
    /// Stores strings larger then certain size (21?) and hash them, this is so that duplicate strings can be found and reused
    ///
    /// - hash (base58 md5, indexed)
    /// - sVal (indexed, unique TEXT string)
    ///
    /// # PREFIX_logTable
    /// The actual log storage, note that all string values that are larger then a defined size (21?) is stored in PREFIX_logStrHashes instead,
    /// where its hash value is used. Note that the process to check for existing hashes, is done locally on the sqlite, before the central DB.
    ///
    /// - instID  (indexed)        // instance ID, used to trace the logging source
    /// - reqsID  (indexed)        // request ID, used to trace the logging source
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
    /// # PREFIX_excStrHash
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
    /// - reqsID     (indexed)     // request ID, used to trace the logging source
    /// - creTime    (indexed)     // Exception created timestamp
    /// - instID     (indexed)     // instance ID, used to trace the hashing source
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
    /// # PREFIX_logConfig
    /// - key (unique, indexed)
    /// - sVal
    ///
	public void tableSetup() throws JSqlException {
		// table setup for sqlite
		tableSetup(sqliteObj);
		
		// table setup for db if not sqlite
		if (jSqlObj.sqlType != JSqlType.sqlite) {
			tableSetup(jSqlObj);
		}
	}
	
	private void tableSetup(JSql jSql) throws JSqlException {
		// / config table
		if (jSql.sqlType == JSqlType.sqlite) {
			jSql.createTableQuerySet(
							"config",
							new String[] {
								"key",
								"sVal"
							},
							new String[] {
								keyColumnType + pKeyColumnType,
								textColumnType
							}
						).execute();
		}

		// / logFormat table
		jSql.createTableQuerySet(
						"logFormat",
						new String[] {
							"hash",
							"format"
						},
						new String[] {
							keyColumnType + pKeyColumnType,
							fullTextColumnType
						}
					).execute();
		
		// / logStrHashes table
		jSql.createTableQuerySet(
						"logStrHashes",
						new String[] {
							"hash",
							"sVal"
						},
						new String[] {
							keyColumnType + pKeyColumnType,
							textColumnType
						}
					).execute();
		
		// / logTable table
		List<String> columnName =  new ArrayList<String>(Arrays.asList("systemHash", "reqsID", "creTime", "fmtHash", "logType", "expHash", "offSync","reqID"));
		List<String> columnDefine =  new ArrayList<String>(Arrays.asList((keyColumnType + pKeyColumnType), keyColumnType, tStampColumnType, keyColumnType, keyColumnType, keyColumnType, bitColumnType, keyColumnType));
		
		// long indexed
		for (int i=1; i<=longIndexed;i++) {
			columnName.add( longIndexedPrefix+(i<10?"0":"")+i );
			columnDefine.add( longColumnType );
		}
		// string indexed
		for (int i=1; i<=stringIndexed;i++) {
			columnName.add( stringIndexedPrefix+(i<10?"0":"")+i );
			columnDefine.add( keyColumnType );
		}
		// long not indexed
		columnName.add( longIndexedPrefix+((longIndexed+1)<10?"0":"")+(longIndexed+1) );
		columnDefine.add( longColumnType );
		
		// string not indexed
		columnName.add( stringIndexedPrefix+((stringIndexed+1)<10?"0":"")+(stringIndexed+1) );
		columnDefine.add( fullTextColumnType );

		jSql.createTableQuerySet(
						"logTable",
						columnName.toArray(new String[columnName.size()]),
						columnDefine.toArray(new String[columnDefine.size()])
					).execute();

		// / excStrHash table
		jSql.createTableQuerySet(
						"excStrHash",
						new String[] {
							"hash",
							"sVal"
						},
						new String[] {
							keyColumnType + pKeyColumnType,
							textColumnType
						}
					).execute();
		
		// / exception table
		columnName =  new ArrayList<String>(Arrays.asList("expHash", "reqsID", "creTime", "systemHash", "excRoot", "excTrace", "excMid", "stkRoot", "stkTrace", "stkMid"));
		columnDefine = new ArrayList<String>(Arrays.asList((keyColumnType + pKeyColumnType), keyColumnType, tStampColumnType, keyColumnType, keyColumnType, keyColumnType, keyColumnType, keyColumnType, keyColumnType, keyColumnType));
		
		// / Exception Root Trace Indexed
		for (int i=1; i<=exceptionRootTraceIndexed;i++) {
			columnName.add("excR"+(i<10?"0":"")+i);
			columnDefine.add(keyColumnType);

			columnName.add("stkR"+(i<10?"0":"")+i);
			columnDefine.add(keyColumnType);
		}
		
		// / Exception Thrown Trace Indexed
		for (int i=1; i<=exceptionThrownTraceIndexed;i++) {
			columnName.add("excT"+(i<10?"0":"")+i);
			columnDefine.add(keyColumnType);

			columnName.add("stkT"+(i<10?"0":"")+i);
			columnDefine.add(keyColumnType);
		}
		
		jSql.createTableQuerySet(
						"exception",
						columnName.toArray(new String[columnName.size()]),
						columnDefine.toArray(new String[columnDefine.size()])
					).execute();
	}
	
	// / Dispose all created tables
	public void tearDown(JSql jSql) throws Exception {
			jSql.executeQuery("DROP TABLE IF EXISTS `config`").dispose();
			jSql.executeQuery("DROP TABLE IF EXISTS `logFormat`").dispose();
			jSql.executeQuery("DROP TABLE IF EXISTS `logStrHashes`").dispose();
			jSql.executeQuery("DROP TABLE IF EXISTS `logTable`").dispose();
			jSql.executeQuery("DROP TABLE IF EXISTS `excStrHash`").dispose();
			jSql.executeQuery("DROP TABLE IF EXISTS `exception`").dispose();
	}
	
	// / Returns the current time in seconds
   private int createTime() {
      return (int)(System.currentTimeMillis() / 1000);
   }
   
	// / Returns the systemHash stored inside the SQLite DB, if it does not
	// exists, generate one
	public String systemHash(JSql jSql) throws JSqlException {
		String sysHash = null;
		try {
			// Fetch meta fields
			JSqlResult r = jSql.selectQuerySet("config", "sVal", "key=?", new Object[] { "systemHash" }).query();
			
			if (r.fetchAllRows() > 0) {
				sysHash = (String) r.readRowCol(0, "sVal");
	      }
	      if (!StringUtils.isBlank(sysHash)) {
	      	return sysHash;
	      }
	      //if systemHash does not exists generate new and persist the hash value in SQLite
			sysHash = systemInfo.systemaHash();

			// save systemHash
			addConfig(jSql, "systemHash", sysHash);
		} catch(Exception e) {
			e.printStackTrace();
			throw new JSqlException(e.getMessage());
		}
		return sysHash;
	}
	
	private void addConfig(JSql jSql, String key, String sVal) throws JSqlException {
		jSql.upsertQuerySet( //
					"config", //
					new String[] { "key" }, //
					new Object[] { key, }, //
					//
					new String[] { "sVal" }, //
					new Object[] { sVal }, //
					null, null, null//
					).execute();
	}
	
	// / Validate if the systemHash if it belongs to the current physical 
	// virtual server. If it fails, it reissues the systemHash. Used on servlet startup
	public String validateSystemHash(JSql jSql) throws JSqlException {
		try {
	      // fetch the current systemHash from config table
	      String sysHash = systemHash(jSql);
	      
	      // compare systemHash, if does not match, reissue.
	      if(!StringUtils.isBlank(sysHash) && sysHash.equals(systemInfo.systemaHash())){
	         return sysHash;
	      }
	      // reissues the systemHash
	      sysHash = systemInfo.systemaHash();
	      
	      // persist the hash value in SQLite
	      addConfig(jSql, "systemHash", sysHash);
	
			return sysHash;
		} catch(Exception e) {
			throw new JSqlException(e.getMessage());
		}
	}
	
	// / Returns the current request ID
	public String requestID(JSql jSql) throws JSqlException {
		String requestId = null;
		// Fetch the meta fields
		JSqlResult r = jSql.selectQuerySet("logTable", "reqID", "systemHash=?", new Object[] { "systemHash" }).query();
		
		if (r.fetchAllRows() > 0) {
			requestId = (String) r.readRowCol(0, "reqID");
      }
	      
		if (requestId == null || requestId.trim().length() == 0) {
			return reissueRequestID();
		}
		return requestId;
	}
	
	// / Reissue a new requestID, used at start of servlet call
	public String reissueRequestID() {
		return GUID.base58();
	}
	
	// / Add the format to the system
	//hash   (base58 md5, indexed) ,format (indexed, unique)
	public void addFormat(JSql jSql, String fmtHash, String format) throws JSqlException {
		jSql.upsertQuerySet( //
					"logFormat", //
					new String[] { "hash" }, //
					new Object[] { fmtHash, }, //
					//
					new String[] { "format" }, //
					new Object[] { format }, //
					null, null, null//
					).execute();
	}
	
	/// You should not be calling log using format with no arguments
	public void log(String format) {
		throw new RuntimeException("Do not use the logger, without arguments (it makes this whole class pointless");
	}
	
	/// Performs a logging with a format name and argument
	public void log(JSql jSql, String format, Object... args) throws Exception {
		// Add format string
		String fmtHash = md5Base58(format);
		addFormat(jSql, fmtHash, format);
      
      String sql ="INSERT INTO `logTable` (systemHash, reqsID, creTime, fmtHash, logType, expHash, offSync, reqID ";
      String sqlValues="?, ?, ?, ?, ?, ?, ?, ?";		
		
		List<Object> values = new ArrayList<Object>();
		values.add(systemHash(jSql));
		values.add(requestID(jSql));
		values.add(createTime());
		values.add(fmtHash);
		values.add("L");
		// For now, add blank for the columns where not sure about the exact value
		values.add("");
		values.add("");
		values.add("");
		
		String findStr = "%";
		int longIndexedCount = 1;
		int stringIndexedCount = 1;
		int lastIndex = 0;
		int index = 0;
		List<Long> extraLong = new ArrayList<Long>();
		List<String> extraString = new ArrayList<String>();
		while (lastIndex != -1) {
			lastIndex = format.indexOf(findStr, lastIndex);
			if (lastIndex != -1) {
				if (format.charAt(lastIndex + 1) == 's') {
					if (stringIndexedCount > stringIndexed) {
						extraString.add((String) args[index++]);
					} else {
						sql += ", "+ stringIndexedPrefix + (stringIndexedCount < 10 ? "0" : "")
								+ stringIndexedCount++;
						sqlValues += ", ?";
						values.add(args[index++]);
					}
				} else if (format.charAt(lastIndex + 1) == 'i' || format.charAt(lastIndex + 1) == 'd' || format.charAt(lastIndex + 1) == 'f') {
					if (longIndexedCount > longIndexed) {
						extraLong.add(new Long(String.valueOf(args[index++])));
					} else {
						sql += ", " + longIndexedPrefix + (longIndexedCount < 10 ? "0" : "")
								+ longIndexedCount++;
						sqlValues += ", ?";
						values.add(args[index++]);
					}
				}
				lastIndex += findStr.length();
			}
		}
		if (!extraString.isEmpty()) {
			sql += ", "+ stringIndexedPrefix + (stringIndexedCount < 10 ? "0" : "")
									+ stringIndexedCount++;
			sqlValues += ", ?";
			values.add(extraString.toString());
		}
		if (!extraLong.isEmpty()) {
			sql += ", " + longIndexedPrefix + (longIndexedCount < 10 ? "0" : "")
									+ longIndexedCount++;
			sqlValues += ", ?";
			values.add(extraLong.toString());
		}
		
		sql += ") VALUES (" + sqlValues + ");";

		// insert to `logTable`
		jSql.execute(sql, values.toArray(new Object[values
					.size()]));
			
	}
	
	public List<LogMessage> list(JSql jSql) throws JSqlException {
		List<LogMessage> list = null;
	
		JSqlResult r = jSql.executeQuery("SELECT lf.format, lt.* FROM logFormat lf"+
       " INNER JOIN logTable lt on lf.hash = lt.fmtHash");
	
	   int rowCount = r.fetchAllRows();
		if(rowCount > 0) {
			list = new ArrayList<LogMessage>();
			LogMessage logMessage = null;
			Object val = null;
			String format = null;
			String col = null;
			for (int pt = 0; pt < rowCount; pt++) {
				logMessage = new LogMessage();
				
				// get format String
				format = (String) r.readRowCol(pt, "format");
				logMessage.setFormat(format);
				
				// parse the format string
				// fetch the fields value in the same order as present in the format string
				String findStr = "%";
				int longIndexedCount = 1;
				int stringIndexedCount = 1;
				int lastIndex = 0;
				int stringNotIndexedCount = 0;
				int longNotIndexedCount = 0;
				while (lastIndex != -1) {
					lastIndex = format.indexOf(findStr, lastIndex);
					if (lastIndex != -1) {
						if (format.charAt(lastIndex + 1) == 's') {
							if (stringIndexedCount > stringIndexed) {
								col = stringIndexedPrefix + (stringIndexedCount<10?"0":"")+stringIndexedCount;
								// read column value from db
								val = r.readRowCol(pt, col);
								if (val != null && val.toString().trim().length() != 0 && !"null".equalsIgnoreCase(val.toString().trim())) {
									String[] arr = (val.toString().replaceAll("\\[", "").replaceAll("\\]","")).split(",");
									if (arr != null && arr.length > 0) {
										logMessage.addArgs(arr[stringNotIndexedCount++].trim());
									}
								}
							} else {
								col = stringIndexedPrefix + (stringIndexedCount<10?"0":"")+stringIndexedCount;
								// read column value from db
								val = r.readRowCol(pt, col);
								if (val != null && val.toString().trim().length() != 0 && !"null".equalsIgnoreCase(val.toString().trim())) {
									logMessage.addArgs(val);
								}
								stringIndexedCount++;
							}
						} else if (format.charAt(lastIndex + 1) == 'i' || format.charAt(lastIndex + 1) == 'd' || format.charAt(lastIndex + 1) == 'f') {
							if (longIndexedCount > longIndexed) {
									col = longIndexedPrefix + (longIndexedCount<10?"0":"")+longIndexedCount;
									// read column value from db
									val = r.readRowCol(pt, col);
									if (val != null) {
										String[] arr = (val.toString().replaceAll("\\[", "").replaceAll("\\]","")).split(",");
										if (arr != null && arr.length > 0) {
											if (arr[longNotIndexedCount].indexOf(".") == -1) {
												logMessage.addArgs(Integer.valueOf(arr[longNotIndexedCount++].trim()));
											} else {
												logMessage.addArgs(Float.valueOf(arr[longNotIndexedCount++].trim()));
											}
										}
									}
							} else {
								col = longIndexedPrefix + (longIndexedCount<10?"0":"")+longIndexedCount;
								// read column value from db
								val = r.readRowCol(pt, col);
								if (val != null) {
									logMessage.addArgs(val);
								}
								longIndexedCount++;
							}
						}
						lastIndex += findStr.length();
					}
				}
				// add the logMessage class to list
				list.add(logMessage);
			}
		}
		return list;
	}

	/// Performs a logging with a formant name, argument, and attached exception
	public void logException(JSql jSql, Exception e, String format, Object... args) throws Exception {
		// Add format string
		String fmtHash = md5Base58(format);
		addFormat(jSql, fmtHash, format);

      //insert args to `exception`
		jSql.execute("INSERT INTO `exception` "
			+ "(expHash,reqsID,creTime,systemHash,excRoot,excTrace,excR01-XX,excT01-XX,excMid,stkRoot,stkTrace,stkR01-XX,stkT01-XX,stkMid) " 
         + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", systemHash(jSql), requestID(jSql), createTime(), fmtHash, args);
      
      logger.log(Level.SEVERE, "Exception", e);
	}
	
	// / Returns format string hash value
	public String hashFormat(JSql jSql, String fmtString) throws JSqlException {
			JSqlResult r = jSql.selectQuerySet("logFormat", "hash", "format=?", new Object[] { fmtString }).query();
			int rowCount = r.fetchAllRows();
			if(rowCount > 0) {
				return  (String) r.readRowCol(0, "hash");
			}
			return null;
	}

	//generate Hash based on format string
	public String md5Base58(String format) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.reset();
		messageDigest.update(format.getBytes());
		BigInteger bigInt = new BigInteger(1, messageDigest.digest());
		String hash = bigInt.toString(16);
		// limit hash string to 22 chars
		if (hash.length() > 22) {
			hash = hash.substring(0, 22);
		}
		return hash;
	}
	
}