package picoded.dstack.jsql.connector.db;

import java.util.Locale;

import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;

/// Pure SQLite implentation of JSql
public class JSql_Sqlite extends JSql_Base {

	/// Setup database as pure SQLite mode
	public JSql_Sqlite() {
		this(":memory:");
	}
	
	/// Runs JSql with the JDBC sqlite engine
	///
	/// @param  File path for the sqlite file
	public JSql_Sqlite(String sqliteLoc) {
		// store database connection properties
		setConnectionProperties(sqliteLoc, null, null, null, null);
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	/// Setsup the internal connection settings and driver
	private void setupConnection() {
		sqlType = JSqlType.SQLITE;
		
		try {
			// This is only imported on demand, avoid preloading until needed
			Class.forName("org.sqlite.JDBC");

			// Getting the required SQLite connection
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
	/// This converts one SQL convention to another as needed
	///
	/// @param  SQL query to "normalize"
	///
	/// @return  SQL query that was converted
	public String genericSqlParser(String inString) {
		final String truncateTable = "TRUNCATE TABLE";
		final String deleteFrom = "DELETE FROM";
		
		inString = inString.toUpperCase(Locale.ENGLISH);
		inString = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("\\s+", " ")
			.replaceAll("(?i)VARCHAR\\(MAX\\)", "VARCHAR").replaceAll("(?i)BIGINT", "INTEGER");
		
		if (inString.startsWith(truncateTable)) {
			inString = inString.replaceAll(truncateTable, deleteFrom);
		}
		return inString;
	}
}