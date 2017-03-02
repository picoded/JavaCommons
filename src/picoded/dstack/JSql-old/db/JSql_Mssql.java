package picoded.JSql.db;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlQuerySet;
import picoded.JSql.JSqlResult;
import picoded.set.JSqlType;

/// Pure SQL Server 2012 implentation of JSql
/// Support only for SQL Server 2012 and above version for the pagination query, the OFFSET / FETCH keywords
/// are used which are faster and better in performance in comparison of old ROW_NUMBER()
public class JSql_Mssql extends JSql {
	
	/// Internal self used logger
	private static final Logger LOGGER = Logger.getLogger(JSql_Mssql.class.getName());
	
	protected String dbName = "dbName";
	
	private String exceptionMsg = " END TRY BEGIN CATCH END CATCH";
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		// store database connection properties
		setConnectionProperties(dbUrl, dbName, dbUser, dbPass, null);
		
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	private void setupConnection() {
		sqlType = JSqlType.MSSQL;
		
		String connectionUrl = "jdbc:jtds:sqlserver://" + (String) connectionProps.get("dbUrl");
		
		if (connectionProps.get(this.dbName) != null
			&& connectionProps.get(this.dbName).toString().trim().length() > 0) {
			connectionUrl += ";DatabaseName=" + (String) connectionProps.get(this.dbName)
				+ ";uselobs=false;"; //disable clobs
		}
		try {
			Class.forName("net.sourceforge.jtds.jdbcx.JtdsDataSource"); //connection pooling
			sqlConn = java.sql.DriverManager.getConnection(connectionUrl,
				(String) connectionProps.get("dbUser"), (String) connectionProps.get("dbPass"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load mssql connection: ", e);
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
	
	// Internal parser that converts some of the common sql statements to mssql
	public String genericSqlParser(String inString) {
		
		String fixedQuotes = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("`", "\"")
			.replaceAll("'", "\"").replaceAll("\\s+", " ").replaceAll(" =", "=").replaceAll("= ", "=")
			.trim();
		
		String upperCaseStr = fixedQuotes.toUpperCase(Locale.ENGLISH);
		String qString = fixedQuotes;
		
		String qStringPrefix = "";
		String qStringSuffix = "";
		
		final String ifExists = "IF EXISTS";
		final String ifNotExists = "IF NOT EXISTS";
		
		final String create = "CREATE";
		final String drop = "DROP";
		final String table = "TABLE";
		final String select = "SELECT";
		final String update = "UPDATE";
		
		final String view = "VIEW";
		
		final String insertInto = "INSERT INTO";
		final String deleteFrom = "DELETE FROM";
		
		final String[] indexTypeArr = { "UNIQUE", "FULLTEXT", "SPATIAL" };
		final String index = "INDEX";
		
		String indexType;
		String tmpStr;
		int tmpIndx;
		
		Pattern createIndexType = Pattern.compile("((UNIQUE|FULLTEXT|SPATIAL) ){0,1}INDEX.*");
		
		int prefixOffset = 0;
		if (upperCaseStr.startsWith(drop)) { //DROP
			upperCaseStr = upperCaseStr.replaceAll(ifNotExists, ifExists);
			fixedQuotes = fixedQuotes.replaceAll(ifNotExists, ifExists);
			
			prefixOffset = drop.length() + 1;
			
			if (upperCaseStr.startsWith(table, prefixOffset)) { //TABLE
				prefixOffset += table.length() + 1;
				
				if (upperCaseStr.startsWith(ifExists, prefixOffset)) { //IF EXISTS
					prefixOffset += ifExists.length() + 1;
					
					qStringPrefix = "BEGIN TRY IF OBJECT_ID('"
						+ fixedQuotes.substring(prefixOffset).toUpperCase(Locale.ENGLISH) + "', 'U')"
						+ " IS NOT NULL DROP TABLE " + fixedQuotes.substring(prefixOffset) + exceptionMsg;
				} else {
					qStringPrefix = "DROP TABLE ";
				}
				qString = qStringPrefix;
			} else if (upperCaseStr.startsWith(index, prefixOffset)) { //INDEX
			
			} else if (upperCaseStr.startsWith(view, prefixOffset)) { //VIEW
				prefixOffset += view.length() + 1;
				
				if (upperCaseStr.startsWith(ifExists, prefixOffset)) { //IF EXISTS
					prefixOffset += ifExists.length() + 1;
					
					qStringPrefix = "BEGIN TRY IF OBJECT_ID('"
						+ fixedQuotes.substring(prefixOffset).toUpperCase(Locale.ENGLISH) + "', 'V')"
						+ " IS NOT NULL DROP VIEW " + fixedQuotes.substring(prefixOffset) + exceptionMsg;
				} else {
					qStringPrefix = "DROP VIEW ";
				}
			}
		} else if (upperCaseStr.startsWith(create)) { //CREATE
			prefixOffset = create.length() + 1;
			
			if (upperCaseStr.startsWith(table, prefixOffset)) { //TABLE
				prefixOffset += table.length() + 1;
				
				if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += ifNotExists.length() + 1;
					//get the table name from incoming query
					String tableName = getTableName(fixedQuotes.substring(prefixOffset));
					qStringPrefix = "BEGIN TRY IF NOT EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'"
						+ tableName
						+ "')"
						+ " AND OBJECTPROPERTY(id, N'"
						+ tableName
						+ "')"
						+ " = 1) CREATE TABLE ";
					qStringSuffix = exceptionMsg;
				} else {
					qStringPrefix = "CREATE TABLE ";
				}
				qString = fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
				//qString = _simpleMysqlToOracle_collumnSubstitude(qString);
			} else {
				LOGGER.finer("Trying to matched INDEX : " + upperCaseStr.substring(prefixOffset));
				if (createIndexType.matcher(upperCaseStr.substring(prefixOffset)).matches()) { //UNIQUE|FULLTEXT|SPATIAL|_ INDEX
					LOGGER.finer("Matched INDEX : " + inString);
					
					//Find the index type
					indexType = null;
					for (int a = 0; a < indexTypeArr.length; ++a) {
						if (upperCaseStr.startsWith(indexTypeArr[a], prefixOffset)) {
							prefixOffset += indexTypeArr[a].length() + 1;
							indexType = indexTypeArr[a];
							break;
						}
					}
					
					//only bother if it matches (shd be right?)
					//					if (upperCaseStr.startsWith(index, prefixOffset)) {
					prefixOffset += index.length() + 1;
					
					//If not exists wrapper
					if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) {
						prefixOffset += ifNotExists.length() + 1;
						qStringPrefix = "";
						qStringSuffix = "";
					}
					
					tmpStr = fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
					tmpIndx = tmpStr.indexOf(" ON ");
					
					if (tmpIndx > 0) {
						qString = "BEGIN TRY CREATE " + (indexType + " ") + "INDEX "
							+ tmpStr.substring(0, tmpIndx) + " ON "
							+ fixTableNameInMssqlSubQuery(tmpStr.substring(tmpIndx + 4)) + exceptionMsg;
					}
					
					//					}
				}
			}
		} else if (upperCaseStr.startsWith(insertInto)) { //INSERT INTO
			prefixOffset = insertInto.length() + 1;
			
			tmpStr = fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
			
			qString = "INSERT INTO " + tmpStr;
		} else if (upperCaseStr.startsWith(select)) { //SELECT
			prefixOffset = select.length() + 1;
			
			tmpStr = qString.substring(prefixOffset);
			tmpIndx = qString.toUpperCase(Locale.ENGLISH).indexOf(" FROM ");
			
			if (tmpIndx > 0) {
				qString = "SELECT " + tmpStr.substring(0, tmpIndx - 7)
				//.replaceAll("\"", "'")
					.replaceAll("`", "\"") + " FROM "
					+ fixTableNameInMssqlSubQuery(tmpStr.substring(tmpIndx - 1));
			} else {
				qString = fixTableNameInMssqlSubQuery(fixedQuotes);
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
			// Fix the pagination query as per the SQL Server 2012 syntax by using the OFFSET/FETCH
			String prefixQuery = null;
			int offsetIndex = qString.indexOf("OFFSET");
			String offsetQuery = "";
			if (offsetIndex != -1) {
				prefixQuery = qString.substring(0, offsetIndex);
				offsetQuery = qString.substring(offsetIndex);
				offsetQuery += " ROWS ";
			}
			int limitIndex = qString.indexOf("LIMIT");
			String limitQuery = "";
			if (limitIndex != -1) {
				
				// Includes offset 0, if its missing (required for MSSQL)
				if (offsetIndex == -1) {
					offsetQuery = "OFFSET 0 ROWS ";
				}
				
				prefixQuery = qString.substring(0, limitIndex);
				if (offsetIndex != -1) {
					limitQuery = qString.substring(limitIndex, offsetIndex);
				} else {
					limitQuery = qString.substring(limitIndex);
				}
				limitQuery = limitQuery.replace("LIMIT", "FETCH NEXT");
				limitQuery += " ROWS ONLY ";
			}
			if (prefixQuery != null) {
				qString = prefixQuery + offsetQuery + limitQuery;
			}
		} else if (upperCaseStr.startsWith(deleteFrom)) {
			prefixOffset = deleteFrom.length() + 1;
			
			tmpStr = fixTableNameInMssqlSubQuery(qString.substring(prefixOffset));
			qString = deleteFrom + " " + tmpStr;
			
		} else if (upperCaseStr.startsWith(update)) { //UPDATE
			prefixOffset = update.length() + 1;
			
			tmpStr = fixTableNameInMssqlSubQuery(qString.substring(prefixOffset));
			qString = update + " " + tmpStr;
		}
		//Drop table query modication
		if (qString.contains("DROP")) {
			qString = qStringPrefix;
		} else {
			qString = qStringPrefix + qString + qStringSuffix;
		}
		
		return getQString(qString);
	}
	
	public String getQString(String qString) {
		if (qString.contains("CREATE TABLE")) {
			// Replace PRIMARY KEY AUTOINCREMENT with IDENTITY
			if (qString.contains("AUTOINCREMENT")) {
				qString = qString.replaceAll("AUTOINCREMENT", "IDENTITY");
			}
			
			//Convert MY-Sql NUMBER data type to NUMERIC data type for Ms-sql
			if (qString.contains("NUMBER")) {
				qString = qString.replaceAll("NUMBER", "NUMERIC");
			}
		}
		
		//remove ON DELETE FOR CLIENTSTATUSHISTORY---> this block needs to be refined for future.
		if (qString.contains("ON DELETE")) { //qString.contains("CLIENTSTATUSHISTORY") &&
			qString = qString.replaceAll("ON DELETE SET NULL", "");
		}
		
		// Replace double quote (") with single quote (') for assignment values 
		StringBuilder sb = new StringBuilder(qString);
		int endIndex = qString.indexOf('=');
		int beginIndex = 0;
		while (endIndex != -1) {
			endIndex++;
			beginIndex = endIndex;
			if (sb.charAt(beginIndex) == '"') {
				for (; beginIndex < sb.length(); beginIndex++) {
					if (sb.charAt(beginIndex) == '"') {
						sb.setCharAt(beginIndex, '\'');
					} else if (sb.charAt(beginIndex) == ' ') {
						break;
					}
				}
			}
			endIndex = sb.indexOf("=", beginIndex);
		}
		return qString = sb.toString();
	}
	
	//Method to return table name from incoming query string
	private static String getTableName(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		return qString.substring(0, indxPt).toUpperCase(Locale.ENGLISH); //retrun the table name
	}
	
	public static String fixTableNameInMssqlSubQuery(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		qString = qString.substring(0, indxPt).toUpperCase(Locale.ENGLISH)
			+ qString.substring(indxPt);
		while (qString.endsWith(";")) { //Remove uneeded trailing ";" semi collons
			qString = qString.substring(0, qString.length() - 1);
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
		return execute_raw(genericSqlParser(qString), values);
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
