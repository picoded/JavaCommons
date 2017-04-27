package picoded.dstack.jsql.connector.db;

import java.util.Locale;
import java.util.ArrayList;

import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;

/// Pure "MY"SQL implentation of JSql
public class JSql_Mysql extends JSql_Base {
	
	/// Runs JSql with the JDBC "MY"SQL engine
	///
	/// @param   dbServerAddress, is just IP:PORT. For example, "127.0.0.1:3306"
	/// @param   database name to connect to
	/// @param   database user to connect to
	/// @param   database password to use
	public JSql_Mysql(String dbServerAddress, String dbName, String dbUser, String dbPass) {
		// set connection properties
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbUser);
		connectionProps.put("password", dbPass);
		connectionProps.put("autoReconnect", "true");
		connectionProps.put("failOverReadOnly", "false");
		connectionProps.put("maxReconnects", "5");
		
		String connectionUrl = "jdbc:mysql://" + dbServerAddress + "/" + dbName;
		
		// store database connection properties
		setConnectionProperties(connectionUrl, null, null, null, connectionProps);
		
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Runs JSql with the JDBC "MY"SQL engine
	/// Avoid direct usage, use `JSql_Mysql(dbServerAdress, dbName, dbUser, dbPass)` instead
	///
	/// @param   JDBC connectionUrl, for example, "jdbc:mysql://127.0.0.1:3306/JAVACOMMONS"
	/// @param   Connection properties
	public JSql_Mysql(String connectionUrl, Properties connectionProps) {
		// store database connection properties
		setConnectionProperties(connectionUrl, null, null, null, connectionProps);
		
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	/// Setsup the internal connection settings and driver
	private void setupConnection() {
		sqlType = JSqlType.MYSQL;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance(); //ensure jdbc driver is loaded
			sqlConn = java.sql.DriverManager.getConnection((String) connectionProps.get("dbUrl"),
				(Properties) connectionProps.get("connectionProps"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load sql connection: ", e);
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
		String qString = inString.toUpperCase(Locale.ENGLISH);
		qString = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("\\s+", " ")
			.replaceAll("\"", "`")
			//.replaceAll("\'", "`")
			.replaceAll("AUTOINCREMENT", "AUTO_INCREMENT").replace("VARCHAR(MAX)", "TEXT");
		
		return qString;
	}

	//-------------------------------------------------------------------------
	//
	// UPSERT Query Builder
	//
	//-------------------------------------------------------------------------
	
	///
	/// SQLite specific UPSERT support
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// INSERT OR REPLACE INTO Employee (
	///	id,      // Unique Columns to check for upsert
	///	fname,   // Insert Columns to update
	///	lname,   // Insert Columns to update
	///	role,    // Default Columns, that has default fallback value
	///   note,    // Misc Columns, which existing values are preserved (if exists)
	/// ) VALUES (
	///	1,       // Unique value
	/// 	'Tom',   // Insert value
	/// 	'Hanks', // Update value
	///	COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer'), // Values with default
	///	(SELECT note FROM Employee WHERE id = 1) // Misc values to preserve
	/// );
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Unique column names        (eg: id)
	/// @param  Unique column values       (eg: 1)
	/// @param  Upsert column names        (eg: fname,lname)
	/// @param  Upsert column values       (eg: 'Tom','Hanks')
	/// @param  Default column to use existing values if exists   (eg: 'role')
	/// @param  Default column values to use if not exists        (eg: 'Benchwarmer')
	/// @param  All other column names to maintain existing value (eg: 'note')
	///
	/// @return  A prepared upsert statement
	///
	public JSqlPreparedStatement upsertStatement( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, //
		// Columns names to apply default value, if not exists
		// Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
		Object[] defaultValues, //
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		String[] miscColumns //
	) {
		
		/// Checks that unique collumn and values length to be aligned
		if (uniqueColumns == null || uniqueValues == null
			|| uniqueColumns.length != uniqueValues.length) {
			throw new JSqlException(
				"Upsert query requires unique column and values to be equal length");
		}
		
		/// Preparing inner default select, this will be used repeatingly for COALESCE, DEFAULT and MISC values
		ArrayList<Object> innerSelectArgs = new ArrayList<Object>();
		StringBuilder innerSelectSB = new StringBuilder(" FROM ");
		innerSelectSB.append("`" + tableName + "`");
		innerSelectSB.append(WHERE);
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				innerSelectSB.append(" AND ");
			}
			innerSelectSB.append(uniqueColumns[a] + " = ?");
			innerSelectArgs.add(uniqueValues[a]);
		}
		innerSelectSB.append(")");
		
		String innerSelectPrefix = "(SELECT ";
		String innerSelectSuffix = innerSelectSB.toString();
		/// Building the query for INSERT OR REPLACE
		StringBuilder queryBuilder = new StringBuilder("INSERT OR REPLACE INTO `" + tableName + "` (");
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		/// Building the query for both sides of '(...columns...) VALUE (...vars...)' clauses in upsert
		/// Note that the final trailing ", " seperator will be removed prior to final query conversion
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnValues = new StringBuilder();
		String columnSeperator = ", ";
		/// Setting up unique values
		for (int a = 0; a < uniqueColumns.length; ++a) {
			columnNames.append(uniqueColumns[a]);
			columnNames.append(columnSeperator);
			//
			columnValues.append("?");
			columnValues.append(columnSeperator);
			//
			queryArgs.add(uniqueValues[a]);
		}
		/// Inserting updated values
		if (insertColumns != null) {
			for (int a = 0; a < insertColumns.length; ++a) {
				columnNames.append(insertColumns[a]);
				columnNames.append(columnSeperator);
				//
				columnValues.append("?");
				columnValues.append(columnSeperator);
				//
				queryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a]
					: null);
			}
		}
		/// Handling default values
		if (defaultColumns != null) {
			for (int a = 0; a < defaultColumns.length; ++a) {
				columnNames.append(defaultColumns[a]);
				columnNames.append(columnSeperator);
				columnValues.append(COALESCE);
				columnValues.append(innerSelectPrefix);
				columnValues.append(defaultColumns[a]);
				columnValues.append(innerSelectSuffix);
				queryArgs.addAll(innerSelectArgs);
				columnValues.append(", ?)");
				columnValues.append(columnSeperator);
				queryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a]
					: null);
			}
		}
		/// Handling Misc values
		if (miscColumns != null) {
			for (int a = 0; a < miscColumns.length; ++a) {
				columnNames.append(miscColumns[a]);
				columnNames.append(columnSeperator);
				columnValues.append(innerSelectPrefix);
				columnValues.append(miscColumns[a]);
				columnValues.append(innerSelectSuffix);
				queryArgs.addAll(innerSelectArgs);
				columnValues.append(columnSeperator);
			}
		}
		/// Building the final query
		queryBuilder
			.append(columnNames.substring(0, columnNames.length() - columnSeperator.length()));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(columnValues.substring(0,
			columnValues.length() - columnSeperator.length()));
		queryBuilder.append(")");
		return new JSqlPreparedStatement(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
}