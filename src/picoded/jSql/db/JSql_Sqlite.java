package picoded.jSql.db;

import java.lang.String;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.lang.RuntimeException;
import java.util.regex.Pattern;
import java.util.regex.Matcher; //import java.util.*;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.jSql.JSqlType;
import picoded.jSql.JSqlResult;
import picoded.jSql.JSqlException;

import picoded.jSql.JSql;
import picoded.jSql.db.BaseInterface;

/// Pure SQLite implentation of JSql
public class JSql_Sqlite extends JSql implements BaseInterface {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Sqlite.class.getName());
	
	/// Internal reuse sqlite file location, used for recreate
	private String sqliteLocation = null;
	
	/// Runs with in memory SQLite
	public JSql_Sqlite() {
		setupSqliteConnection(null);
	}
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Sqlite(String sqliteLoc) {
		setupSqliteConnection(sqliteLoc);
	}
	
	/// Internal common reuse constructor
	private void setupSqliteConnection(String sqliteLoc) {
		sqlType = JSqlType.sqlite;
		sqliteLocation = sqliteLoc;
		
		if (sqliteLocation == null) {
			sqliteLocation = ":memory:";
		}
		
		try {
			Class.forName("org.sqlite.JDBC");
			sqlConn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + sqliteLocation);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load sqlite connection: ", e);
		}
	}
	
	/// Creates the connection as needed
	public void recreate(boolean force) {
		if (force) {
			dispose();
		}
		
		if (sqlConn != null || force) {
			setupSqliteConnection(sqliteLocation);
		}
	}
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public static String genericSqlParser(String inString) {
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