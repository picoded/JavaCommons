package picoded.JSql.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlQuerySet;
import picoded.JSql.JSqlResult;
import picoded.set.JSqlType;

/// Pure ORACLE-SQL implentation of JSql
public class JSql_Oracle extends JSql {
	
	/// Internal self used logger
	private static final Logger LOGGER = Logger.getLogger(JSql_Oracle.class.getName());
	
	private static String number = "NUMBER";
	private static String queryStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
	private static String from = "FROM";
	
	/// Runs JSql with the JDBC ORACLE SQL engine
	///
	/// **Note:** urlString, is just IP:PORT. For example, "127.0.0.1:3306"
	public JSql_Oracle(String oraclePath, String dbUser, String dbPass) {
		// store database connection properties
		setConnectionProperties(oraclePath, null, dbUser, dbPass, null);
		// call internal method to create the connection
		setupConnection();
	}
	
	public JSql_Oracle(java.sql.Connection inSqlConn) {
		if (inSqlConn != null) {
			sqlConn = inSqlConn;
		}
	}
	
	/// Internal common reuse constructor
	private void setupConnection() {
		sqlType = JSqlType.ORACLE;
		
		// Get the assumed oracle table space
		String oraclePath = (String) connectionProps.get("dbUrl");
		String connectionUrl = "jdbc:oracle:thin:" + oraclePath;
		try {
			Class.forName("oracle.jdbc.OracleDriver").newInstance(); //ensure oracle driver is loaded
			sqlConn = java.sql.DriverManager.getConnection(connectionUrl,
				(String) connectionProps.get("dbUser"), (String) connectionProps.get("dbPass"));
			// Try to alter & ensure the current session roles
			execute("SET ROLE ALL");
		} catch (Exception e) {
			throw new RuntimeException("Failed to load SQL connection: ", e);
		}
	}
	
	/// As this is the base class varient, this funciton isnt suported
	public void recreate(boolean force) {
		if (force) {
			dispose();
		}
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Collumn type correction from mysql to oracle sql
	private static String simpleMysqlToOracleCollumnSubstitude(String qString) {
		return qString.replaceAll("(?i)BIT", "RAW").replaceAll("(?i)TINYBLOB", "RAW")
			.replaceAll("(?i)DECIMAL", number).replaceAll("(?i)DOUBLE", "FLOAT(24)")
			.replaceAll("(?i)DOUBLE PRECISION", "FLOAT(24)").replaceAll("(?i)REAL", "FLOAT (24)")
			.replaceAll("(?i)INTEGER", "INT").replaceAll("(?i)BIGINT", number + "(19, 0)")
			.replaceAll("(?i)MEDIUMINT", number + "(7,0)")
			.replaceAll("(?i)SMALLINT", number + "(5,0)").replaceAll("(?i)TINYINT", number + "(3,0)")
			.replaceAll("(?i)YEAR", number).replaceAll("(?i)" + number, number)
			.replaceAll("(?i)BLOB", "BLOB").replaceAll("(?i)LONGBLOB", "BLOB")
			.replaceAll("(?i)MEDIUMBLOB", "BLOB").replaceAll("(?i)LONGTEXT", "CLOB")
			.replaceAll("(?i)MEDIUMTEXT", "CLOB").replaceAll("(?i)TEXT", "CLOB")
			.replaceAll("(?i)TIME", "DATE").replaceAll("(?i)TIMESTAMP", "DATE")
			.replaceAll("(?i)DATETIME", "DATE").replaceAll("(?i)VARCHAR(?!\\()", "VARCHAR2(4000)")
			.replaceAll("(?i)VARCHAR\\(", "VARCHAR2(").replaceAll("MAX", "4000");
	}
	
	/// Fixes the table name, and removes any trailing ";" if needed
	private static String fixTableNameInOracleSubQuery(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		String tableStr = qString.substring(0, indxPt).toUpperCase(Locale.ENGLISH);
		qString = tableStr + qString.substring(indxPt);
		while (qString.endsWith(";")) { //Remove uneeded trailing ";" semi collons
			qString = qString.substring(0, qString.length() - 1);
		}
		return qString;
	}
	
	public static final String IFEXISTS = "IF EXISTS";
	public static final String IFNOTEXISTS = "IF NOT EXISTS";
	public static final String CREATE = "CREATE";
	public static final String DROP = "DROP";
	public static final String VIEW = "VIEW";
	public static final String TABLE = "TABLE";
	public static final String SELECT = "SELECT";
	public static final String UPDATE = "UPDATE";
	public static final String INSERTINTO = "INSERT INTO";
	public static final String DELETEFROM = "DELETE FROM";
	protected static final String[] INDEXTYPEARR = { "UNIQUE", "FULLTEXT", "SPATIAL" };
	public static final String INDEX = "INDEX";
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public String genericSqlParser(String inString) throws JSqlException {
		//Unique to oracle prefix, automatically terminates all additional conversion attempts
		final String oracleImmediateExecute = "BEGIN EXECUTE IMMEDIATE";
		if (inString.startsWith(oracleImmediateExecute)) {
			return inString;
		}
		String fixedQuotes = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("\\s+", " ")
			.replaceAll("'", "\"").replaceAll("`", "\""); //.replaceAll("\"", "'");
		String upperCaseStr = fixedQuotes.toUpperCase(Locale.ENGLISH);
		String qString = fixedQuotes;
		String qStringPrefix = "";
		String qStringSuffix = "";
		String indexType;
		String tmpStr;
		int tmpIndx;
		Pattern createIndexType = Pattern.compile("((UNIQUE|FULLTEXT|SPATIAL) ){0,1}INDEX.*");
		int prefixOffset = 0;
		if (upperCaseStr.startsWith(DROP)) { //DROP
			upperCaseStr = upperCaseStr.replaceAll(IFNOTEXISTS, IFEXISTS);
			fixedQuotes = fixedQuotes.replaceAll(IFNOTEXISTS, IFEXISTS);
			prefixOffset = DROP.length() + 1;
			if (upperCaseStr.startsWith(TABLE, prefixOffset)) { //TABLE
				prefixOffset += TABLE.length() + 1;
				if (upperCaseStr.startsWith(IFEXISTS, prefixOffset)) { //IF EXISTS
					prefixOffset += IFEXISTS.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'DROP TABLE ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
				} else {
					qStringPrefix = "DROP TABLE ";
				}
				qString = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			} else if (upperCaseStr.startsWith(VIEW, prefixOffset)) { //VIEW
				prefixOffset += VIEW.length() + 1;
				if (upperCaseStr.startsWith(IFEXISTS, prefixOffset)) { //IF EXISTS
					prefixOffset += IFEXISTS.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'DROP VIEW ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
				} else {
					qStringPrefix = "DROP VIEW ";
				}
				qString = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			}
		} else if (upperCaseStr.startsWith(CREATE)) { //CREATE
			prefixOffset = CREATE.length() + 1;
			if (upperCaseStr.startsWith(TABLE, prefixOffset)) { //TABLE
				prefixOffset += TABLE.length() + 1;
				if (upperCaseStr.startsWith(IFNOTEXISTS, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += IFNOTEXISTS.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'CREATE TABLE ";
					qStringSuffix = queryStringSuffix;
				} else {
					qStringPrefix = "CREATE TABLE ";
				}
				qString = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
				qString = simpleMysqlToOracleCollumnSubstitude(qString);
			} else if (upperCaseStr.startsWith(VIEW, prefixOffset)) { //VIEW
				prefixOffset += VIEW.length() + 1;
				if (upperCaseStr.startsWith(IFNOTEXISTS, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += IFNOTEXISTS.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'CREATE VIEW ";
					qStringSuffix = queryStringSuffix;
				} else {
					qStringPrefix = "CREATE VIEW ";
				}
				qString = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
				qString = simpleMysqlToOracleCollumnSubstitude(qString);
				int fromKeywordIndex = qString.indexOf(from) + from.length();
				String qStringBeforeFromKeyword = qString.substring(0, fromKeywordIndex);
				// remove 'AS' keywords after table name
				String qStringAfterFromKeyword = qString.substring(fromKeywordIndex, qString.length())
					.replaceAll("AS", "");
				// replace double quotes (") with single quotes
				qStringAfterFromKeyword = qStringAfterFromKeyword.replace("\"", "'");
				qString = qStringBeforeFromKeyword + qStringAfterFromKeyword;
			} else {
				LOGGER.finer("Trying to matched INDEX : " + upperCaseStr.substring(prefixOffset));
				if (createIndexType.matcher(upperCaseStr.substring(prefixOffset)).matches()) { //UNIQUE|FULLTEXT|SPATIAL|_ INDEX
					LOGGER.finer("Matched INDEX : " + inString);
					//Find the index type
					indexType = null;
					Map<String, String> map = getPrefixOffsetAndIndexType(upperCaseStr, INDEXTYPEARR,
						prefixOffset, indexType);
					prefixOffset = Integer.parseInt(map.get("prefixOffset"));
					indexType = map.get("indexType");
					
					//only bother if it matches (shd be right?)
					//					if (upperCaseStr.startsWith(INDEX, prefixOffset)) {
					prefixOffset += INDEX.length() + 1;
					//If not exists wrapper
					if (upperCaseStr.startsWith(IFNOTEXISTS, prefixOffset)) {
						prefixOffset += IFNOTEXISTS.length() + 1;
						qStringPrefix = "BEGIN EXECUTE IMMEDIATE '";
						qStringSuffix = queryStringSuffix;
					}
					tmpStr = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
					tmpIndx = tmpStr.indexOf(" ON ");
					String tableAndColumns = tmpStr.substring(tmpIndx + " ON ".length());
					// check column's type
					String metaDataQuery = "SELECT "
						+ tableAndColumns.substring(tableAndColumns.indexOf('(') + 1,
							tableAndColumns.indexOf(')')) + " FROM "
						+ tableAndColumns.substring(0, tableAndColumns.indexOf('('));
					Map<String, String> metadata = null;
					try {
						metadata = getMetaData(metaDataQuery);
					} catch (JSqlException e) {
						//throw e;
					}
					checkMetadata(metadata);
					if (tmpIndx > 0) {
						qString = "CREATE " + (indexType + " ") + "INDEX " + tmpStr.substring(0, tmpIndx)
							+ " ON " + fixTableNameInOracleSubQuery(tmpStr.substring(tmpIndx + 4));
					}
					// check if column type is blob
					//					}
				}
			}
		} else if (upperCaseStr.startsWith(INSERTINTO)) { //INSERT INTO
			prefixOffset = INSERTINTO.length() + 1;
			tmpStr = fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			qString = "INSERT INTO " + tmpStr;
		} else if (upperCaseStr.startsWith(SELECT)) { //SELECT
			prefixOffset = SELECT.length() + 1;
			tmpStr = qString.substring(prefixOffset);
			tmpIndx = qString.toUpperCase(Locale.ENGLISH).indexOf(" " + from + " ");
			if (tmpIndx > 0) {
				qString = "SELECT " + tmpStr.substring(0, tmpIndx - 7)
				//.replaceAll("\"", "'")
					.replaceAll("`", "\"") + " " + from + " "
					+ fixTableNameInOracleSubQuery(tmpStr.substring(tmpIndx - 1));
			} else {
				qString = fixTableNameInOracleSubQuery(fixedQuotes);
			}
			prefixOffset = 0;
			//Fix the "AS" quotation
			while ((tmpIndx = qString.indexOf(" AS ", prefixOffset)) > 0) {
				prefixOffset = qString.indexOf(' ', tmpIndx + 4);
				if (prefixOffset > 0) {
					qString = qString.substring(0, tmpIndx)
						+ qString.substring(tmpIndx, prefixOffset).replaceAll("`", "\"")
							.replaceAll("'", "\"") + qString.substring(prefixOffset);
				} else {
					break;
				}
			}
			// Remove 'AS' from table alias
			qString = removeAsAfterTablename(qString);
			qString = removeAsAfterOpeningBracket(qString);
			// Fix the pagination using the ROWNUM which is supported by versions below than Oracle 12C
			String prefixQuery = null;
			int startRowNum = -1;
			int endRowNum = -1;
			int limitIndex = qString.indexOf("LIMIT");
			if (limitIndex != -1) {
				prefixQuery = qString.substring(0, limitIndex);
			}
			
			if (prefixQuery != null) {
				limitIndex += "LIMIT".length();
				int offsetIndex = qString.indexOf("OFFSET");
				if (offsetIndex != -1) {
					startRowNum = Integer.parseInt(qString.substring(offsetIndex + "OFFSET".length())
						.trim());
					endRowNum = Integer.parseInt(qString.substring(limitIndex, offsetIndex).trim())
						+ startRowNum;
				}
			}
			if (startRowNum != -1 && endRowNum != -1) {
				qString = "SELECT * FROM (SELECT a.*, rownum AS rnum FROM (" + prefixQuery
					+ ") a WHERE rownum <= " + endRowNum + ") WHERE rnum > " + startRowNum;
			}
		} else if (upperCaseStr.startsWith(DELETEFROM)) {
			prefixOffset = DELETEFROM.length() + 1;
			tmpStr = fixTableNameInOracleSubQuery(qString.substring(prefixOffset));
			qString = DELETEFROM + " " + tmpStr;
		} else if (upperCaseStr.startsWith(UPDATE)) { //UPDATE
			prefixOffset = UPDATE.length() + 1;
			tmpStr = fixTableNameInOracleSubQuery(qString.substring(prefixOffset));
			qString = UPDATE + " " + tmpStr;
		}
		qString = qStringPrefix + qString + qStringSuffix;
		return qString; //no change of data
	}
	
	public void checkMetadata(Map<String, String> metadata) throws JSqlException {
		if (metadata != null && !metadata.isEmpty()) {
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				if (entry.getValue() != null
					&& entry.getValue().trim().toUpperCase(Locale.ENGLISH).contains("LOB")) {
					throw new JSqlException(
						"Cannot create index on expression with datatype LOB for field '"
							+ entry.getKey() + "'.");
				}
			}
		}
	}
	
	public Map<String, String> getPrefixOffsetAndIndexType(String upperCaseStr,
		String[] indextypearr, int prefixOffset, String indexType) {
		String prefixOff = "prefixOffset";
		String indexTy = "indexType";
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefixOff, String.valueOf(prefixOffset));
		map.put(indexTy, indexType);
		for (int a = 0; a < indextypearr.length; ++a) {
			if (upperCaseStr.startsWith(indextypearr[a], prefixOffset)) {
				map.put(prefixOff, String.valueOf(prefixOffset += indextypearr[a].length() + 1));
				map.put(indexTy, indextypearr[a]);
				break;
			}
		}
		return map;
	}
	
	private static String removeAsAfterTablename(String qString) {
		int prefixOffset = 0;
		String tmpStr = null;
		// parse table name
		String searchString = from;
		String tablename = "";
		while ((prefixOffset = qString.indexOf(searchString, prefixOffset)) > 0) {
			prefixOffset += searchString.length();
			tmpStr = qString.substring(prefixOffset, qString.length()).trim();
			if (!tmpStr.startsWith("(")) {
				// parse table name
				for (int i = 0; i < tmpStr.length(); i++) {
					if (tmpStr.charAt(i) == ' ' || tmpStr.charAt(i) == ')') {
						break;
					}
					tablename += Character.toString(tmpStr.charAt(i));
				}
				tablename = tablename.replaceAll("\"", "").trim();
			}
		}
		// Fix the "AS" quotation
		searchString = " " + tablename + " ";
		while ((prefixOffset = qString.indexOf(searchString, prefixOffset)) > 0) {
			prefixOffset += searchString.length();
			tmpStr = qString.substring(prefixOffset, qString.length()).trim();
			if (tmpStr.startsWith("AS")) {
				qString = qString.substring(0, prefixOffset)
					+ qString.substring(prefixOffset + "AS".length() + 1, qString.length()).trim();
			}
		}
		return qString;
	}
	
	private static String removeAsAfterOpeningBracket(String qString) {
		String searchString = " FROM (";
		int prefixOffset = qString.indexOf(searchString);
		if (prefixOffset != -1) {
			prefixOffset += searchString.length();
			int offsetIndex = prefixOffset;
			int obc = 1;
			int cbc = 0;
			// find the closing index
			for (; offsetIndex < qString.length(); offsetIndex++) {
				if (qString.charAt(offsetIndex) == ')') {
					cbc++;
					if (obc == cbc) {
						break;
					}
				} else if (qString.charAt(offsetIndex) == '(') {
					obc++;
				}
			}
			String strBetweenBracket = qString.substring(prefixOffset, offsetIndex).trim();
			// Remove AS
			// increment for space before bracket
			offsetIndex++;
			String tmpStr = qString.substring(offsetIndex, qString.length()).trim();
			if (tmpStr.startsWith("AS")) {
				// increment for space before bracket
				offsetIndex++;
				qString = qString.substring(0, offsetIndex)
					+ qString.substring(offsetIndex + "AS ".length(), qString.length()).trim();
				offsetIndex = offsetIndex - "AS".length();
			}
			// make recursive call
			if (strBetweenBracket.indexOf(searchString) != -1) {
				qString = qString.substring(0, prefixOffset)
					+ removeAsAfterOpeningBracket(strBetweenBracket)
					+ qString.substring(offsetIndex, qString.length());
			}
		}
		return qString;
	}
	
	/// Executes the argumented query, and returns the result object *without*
	/// fetching the result data from the database. (not fetching may not apply to all implementations)
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		return executeQuery_raw(genericSqlParser(qString), values);
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		return query_raw(genericSqlParser(qString), values);
	}
	
	/// Executes and dispose the sqliteResult object.
	///
	/// Returns false if no result is given by the execution call, else true on success
	public boolean execute(String qString, Object... values) throws JSqlException {
		qString = genericSqlParser(qString);
		
		String sequenceQuery = null;
		String triggerQuery = null;
		
		// check if there is any AUTO INCREMENT field
		if (qString.indexOf("AUTOINCREMENT") != -1 || qString.indexOf("AUTO_INCREMENT") != -1) {
			
			// Create sequence and trigger if it is CREATE TABLE query and has the AUTO INCREMENT column
			int prefixOffset = qString.indexOf(CREATE);
			// check if create statement
			if (prefixOffset != -1) { //CREATE
			
				prefixOffset += CREATE.length() + 1;
				
				// check if create table statement
				if (qString.startsWith(TABLE, prefixOffset)) { //TABLE
				
					prefixOffset += TABLE.length() + 1;
					
					// check if 'IF NOT EXISTS' exists in query
					if (qString.startsWith(IFNOTEXISTS, prefixOffset)) {
						prefixOffset += IFNOTEXISTS.length() + 1;
					}
					
					// parse table name
					String tableName = qString.substring(prefixOffset,
						qString.indexOf('(', prefixOffset));
					tableName = tableName.replaceAll("\"", "").trim();
					
					prefixOffset += tableName.length();
					//prefixOffset = qString.indexOf("(", prefixOffset) + 1;
					
					// parse primary key column
					String primaryKeyColumn = "";
					
					String tmpStr = qString.substring(prefixOffset, qString.indexOf("PRIMARY KEY"))
						.trim();
					
					if (tmpStr.charAt(tmpStr.length() - 1) == ')') {
						tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf('(')).trim();
					}
					// find last space
					if (tmpStr.lastIndexOf(' ') != -1) {
						tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf(' ')).trim();
					}
					
					for (int i = tmpStr.length() - 1; i >= 0; i--) {
						// find space, comma or opening bracket
						if (tmpStr.charAt(i) == ' ' || tmpStr.charAt(i) == ',' || tmpStr.charAt(i) == '(') {
							break;
						}
						primaryKeyColumn = tmpStr.charAt(i) + primaryKeyColumn;
					}
					
					// create sequence sql query
					sequenceQuery = "CREATE SEQUENCE \"" + tableName
						+ "_SEQ\" START WITH 1001 INCREMENT BY 1 CACHE 10";
					
					// create trigger sql query
					triggerQuery = "CREATE OR REPLACE TRIGGER \"" + tableName + "_TRIGGER\" "
						+ " BEFORE INSERT ON \"" + tableName + "\" FOR EACH ROW " + " BEGIN SELECT \""
						+ tableName + "_SEQ\".nextval INTO :NEW." + primaryKeyColumn + " FROM dual; END;";
				}
			}
		}
		// Replace the AUTO INCREMENT with blank
		qString = qString.replaceAll("AUTOINCREMENT", "");
		qString = qString.replaceAll("AUTO_INCREMENT", "");
		
		//try {
		boolean retvalue = execute_raw(qString, values);
		
		//Create Sequence
		if (sequenceQuery != null) {
			execute_raw(genericSqlParser(sequenceQuery));
		}
		
		//Create trigger
		if (triggerQuery != null) {
			execute_query(genericSqlParser(triggerQuery));
		}
		
		return retvalue;
	}
	
	///
	/// NOTE: This assumes Oracle 11g onwards
	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
	///
	/// The syntax below, is an example of such an UPSERT statement for Oracle.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// MERGE
	/// INTO destTable
	/// USING (SELECT
	///		? id,
	///		? name,
	///		from dual
	/// ) sourceTable
	/// ON (destTable.id = sourceTable.id)
	/// WHEN NOT MATCHED THEN
	/// INSERT (id, name, role, note) VALUES (
	///		sourceTable.id,   // Unique value
	/// 	sourceTable.name, // Insert value
	///		sourceTable.role, // Values with default
	///		sourceTable.note  // Misc values to preserve
	/// )
	/// WHEN MATCHED THEN
	/// UPDATE
	///		destTable.role = ?, 
	///		// destTable.note = sourceTable.note // Default value
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlQuerySet upsertQuerySet( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, // Columns names to apply default value, if not exists
		Object[] defaultValues, // Values to insert, that is not updated. 
		// Note that this is ignored if pre-existing values exists
		//
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		//
		String[] miscColumns //
	) throws JSqlException {
		
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		/// Checks that unique collumn and values length to be aligned
		if (uniqueColumns == null || uniqueValues == null
			|| uniqueColumns.length != uniqueValues.length) {
			throw new JSqlException(
				"Upsert query requires unique column and values to be equal length");
		}
		
		/// Table aliasing names
		String targetTableAlias = "destTable";
		String sourceTableAlias = "srcTable";
		/// Final actual query set
		StringBuilder queryBuilder = new StringBuilder();
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		/// The actual query building
		queryBuilder
			.append("MERGE INTO `" + tableName + "` " + targetTableAlias + " USING ( SELECT ");
		/// The fields to select to search for unique
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("? ");
			queryBuilder.append(uniqueColumns[a]);
			queryArgs.add(uniqueValues[a]);
		}
		
		/// From dual
		queryBuilder.append(" FROM DUAL ) " + sourceTableAlias);
		/// On unique keys
		queryBuilder.append(" ON ( ");
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				queryBuilder.append(" and ");
			}
			queryBuilder.append(targetTableAlias + "." + uniqueColumns[a]);
			queryBuilder.append(" = ");
			queryBuilder.append(sourceTableAlias + "." + uniqueColumns[a]);
		}
		queryBuilder.append(" ) ");
		
		// Has insert collumns and values
		if (insertColumns != null && insertColumns.length > 0) {
			
			if (insertColumns.length != insertValues.length) {
				throw new JSqlException(
					"Upsert query requires insert column and values to be equal length");
			}
			
			// Found it, do an insert
			queryBuilder.append(" WHEN MATCHED THEN UPDATE SET ");
			
			// For insert keys
			for (int a = 0; a < insertColumns.length; ++a) {
				if (a > 0) {
					queryBuilder.append(", ");
				}
				queryBuilder.append(targetTableAlias);
				queryBuilder.append(".");
				queryBuilder.append(insertColumns[a]);
				queryBuilder.append(" = ? ");
				queryArgs.add(insertValues[a]);
			}
		}
		
		// Found it, do an insert
		queryBuilder.append(" WHEN NOT MATCHED THEN INSERT ( ");
		// Insert query building
		StringBuilder insertNameString = new StringBuilder();
		StringBuilder insertValuesString = new StringBuilder();
		
		// Insert UNIQUE collumns
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				insertNameString.append(", ");
				insertValuesString.append(", ");
			}
			insertNameString.append(uniqueColumns[a]);
			insertValuesString.append("?");
			queryArgs.add(uniqueValues[a]);
		}
		
		// Insert INSERT collumns
		if (insertColumns != null && insertColumns.length > 0) {
			for (int a = 0; a < insertColumns.length; ++a) {
				insertNameString.append(", ");
				insertValuesString.append(", ");
				insertNameString.append(insertColumns[a]);
				insertValuesString.append("?");
				queryArgs.add(insertValues[a]);
			}
		}
		
		// Insert DEFAULT collumns
		if (defaultColumns != null && defaultColumns.length > 0) {
			for (int a = 0; a < defaultColumns.length; ++a) {
				insertNameString.append(", ");
				insertValuesString.append(", ");
				insertNameString.append(defaultColumns[a]);
				insertValuesString.append("?");
				queryArgs.add(defaultValues[a]);
			}
		}
		
		// Build the actual insert
		queryBuilder.append(insertNameString);
		queryBuilder.append(" ) VALUES ( ");
		queryBuilder.append(insertValuesString);
		queryBuilder.append(" )");
		// The actual query
		return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	// Helper varient, without default or misc fields
	public JSqlQuerySet upsertQuerySet( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues // Values to update
	) throws JSqlException {
		return upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			null, null, null);
	}
	
}
