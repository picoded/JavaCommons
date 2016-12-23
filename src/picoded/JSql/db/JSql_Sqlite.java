package picoded.JSql.db;

import java.util.Locale;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.enums.JSqlType;
//import java.util.*;

/// Pure SQLite implentation of JSql
public class JSql_Sqlite extends JSql {
	
	/// Internal self used logger
	//	private static Logger LOGGER = Logger.getLogger(JSql_Sqlite.class.getName());
	
	public String className = "org.sqlite.JDBC";
	
	/// Runs with in memory SQLite
	public JSql_Sqlite() {
		this(":memory:");
	}
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Sqlite(String sqliteLoc) {
		// store database connection properties
		setConnectionProperties(sqliteLoc, null, null, null, null);
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	private void setupConnection() {
		sqlType = JSqlType.SQLITE;
		
		try {
			Class.forName(className);
			sqlConn = java.sql.DriverManager.getConnection("jdbc:sqlite:"
				+ (String) connectionProps.get("dbUrl"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load sqlite connection: ", e);
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
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public String genericSqlParser(String inString) {
		final String truncateTable = "TRUNCATE TABLE";
		final String deleteFrom = "DELETE FROM";
		
		inString = inString.toUpperCase(Locale.ENGLISH);
		inString = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("\\s+", " ")
			.replaceAll("(?i)VARCHAR\\(MAX\\)", "VARCHAR").replaceAll("(?i)BIGINT", "INTEGER");
		//System.out.println( inString );
		
		if (inString.startsWith(truncateTable)) {
			inString = inString.replaceAll(truncateTable, deleteFrom);
		}
		return inString;
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
	
}