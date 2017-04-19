package picoded.dstack.jsql.connector.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import picoded.set.JSqlType;
import picoded.dstack.jsql.connector.*;

/// Default generic JSQL implmentation,
/// with shared usage across multiple DB's
/// while not being usable for any of them on its own.
public class JSql_Base extends JSql {
	
	//-------------------------------------------------------------------------
	//
	// Database keywords, reuse vars
	//
	//-------------------------------------------------------------------------
	
	protected static final String COALESCE = "COALESCE(";
	protected static final String WHERE = " WHERE ";

	/// Blank JSQL result set, initialized once and reused
	protected static final JSqlResult BLANK_JSQLRESULT = new JSqlResult();

	//-------------------------------------------------------------------------
	//
	// Helper utility functions
	//
	//-------------------------------------------------------------------------
	
	/// Helper function, used to prepare the sql statment in multiple situations
	/// 
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  The SQL prepared statement
	protected PreparedStatement prepareSqlStatment(String qString, Object... values) {
		int pt = 0;
		final Object[] parts = (values != null) ? values : (new Object[] {});
		
		Object argObj;
		PreparedStatement ps;
		
		try {
			ps = sqlConn.prepareStatement(qString);
			
			for (pt = 0; pt < parts.length; ++pt) {
				argObj = parts[pt];
				if (argObj == null) {
					ps.setNull(pt + 1, 0);
				} else if (String.class.isInstance(argObj)) {
					ps.setString(pt + 1, (String) argObj);
				} else if (Integer.class.isInstance(argObj)) {
					ps.setInt(pt + 1, (Integer) argObj);
				} else if (Long.class.isInstance(argObj)) {
					ps.setLong(pt + 1, (Long) argObj);
				} else if (Double.class.isInstance(argObj)) {
					ps.setDouble(pt + 1, (Double) argObj);
				} else if (Float.class.isInstance(argObj)) {
					ps.setFloat(pt + 1, (Float) argObj);
				} else if (Date.class.isInstance(argObj)) {
					java.sql.Date sqlDate = new java.sql.Date(((Date) argObj).getTime());
					ps.setDate(pt + 1, sqlDate);
				} else if (java.sql.Blob.class.isInstance(argObj)) {
					ps.setBlob(pt + 1, (java.sql.Blob) argObj);
				} else {
					String argClassName = argObj.getClass().getName();
					throw new JSqlException("Unknown argument type (" + pt + ") : " + (argClassName));
				}
			}
		} catch (Exception e) {
			throw new JSqlException("Invalid statement argument/parameter (" + pt + ")", e);
		}
		return ps;
	}
	
	//-------------------------------------------------------------------------
	//
	// Standard raw query command sets
	//
	//-------------------------------------------------------------------------
	
	/// Executes the argumented query, and returns the result object *without*
	/// fetching the result data from the database. This is a raw execution.
	///
	/// As such no special parsing occurs to the request
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult noFetchQuery_raw(String qString, Object... values) {
		JSqlResult res = null;
		try {
			PreparedStatement ps = prepareSqlStatment(qString, values);
			ResultSet rs = null;
			//Try and finally : prevent memory leaks
			try {
				//is a select statment
				if ("SELECT".equals(qString.trim().toUpperCase(Locale.ENGLISH).substring(0, 6))) {
					rs = ps.executeQuery();
					res = new JSqlResult(ps, rs);
					
					//let JSqlResult "close" it
					ps = null;
					rs = null;
				} else {
					int r = ps.executeUpdate();
					if (r >= 0) {
						res = BLANK_JSQLRESULT;
					}
				}
				return res;
			} finally {
				//
				// In event an exception occur in JSqlResult
				// This cleans up any existing connections if needed
				//
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("query_raw exception: " + qString, e);
		}
	}

	//-------------------------------------------------------------------------
	//
	// Generic SQL conversion and query
	//
	//-------------------------------------------------------------------------
	
	/// Internal parser that converts some of the common sql statements to sqlite
	/// This converts one SQL convention to another as needed
	///
	/// @param  SQL query to "normalize"
	///
	/// @return  SQL query that was converted
	public String genericSqlParser(String qString) {
		return qString;
	}

	/// Executes the argumented SQL query, and immediately fetches the result from
	/// the database into the result set. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult query(String qString, Object... values) {
		return query_raw( genericSqlParser(qString), values );
	}
	
	/// Executes the argumented SQL query, and returns the result object *without*
	/// fetching the result data from the database. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult noFetchQuery(String qString, Object... values) {
		return noFetchQuery_raw( genericSqlParser(qString), values );
	}

	/// Executes the argumented SQL update.
	///
	/// Returns false if no result object is given by the execution call. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public boolean update(String qString, Object... values) {
		return update_raw( genericSqlParser(qString), values );
	}

	//-------------------------------------------------------------------------
	//
	// SELECT Query builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an SELECT statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// SELECT
	///	col1, col2   //select collumn
	/// FROM tableName //table name to select from
	/// WHERE
	///	col1=?       //where clause
	/// ORDER BY
	///	col2 DESC    //order by clause
	/// LIMIT 2        //limit clause
	/// OFFSET 3       //offset clause
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to select          (eg: col1, col2)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	/// @param  Order by statement         (eg: col2 DESC)
	/// @param  Row count limit            (eg: 2)
	/// @param  Row offset                 (eg: 3)
	///
	/// @return  A prepared select statement
	///
	public JSqlPreparedStatement selectStatement( //
		String tableName, // Table name to select from
		//
		String selectStatement, // The Columns to select, null means all
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues, // Values that corresponds to the where statement
		//
		String orderStatement, // Order by statements, must be either ASC / DESC
		//
		long limit, // Limit row count to, use 0 to ignore / disable
		long offset // Offset limit by?
	) {

		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("SELECT ");
		
		// Select collumns
		if (selectStatement == null || (selectStatement = selectStatement.trim()).length() <= 0) {
			queryBuilder.append("*");
		} else {
			queryBuilder.append(selectStatement);
		}
		
		// From table names
		queryBuilder.append(" FROM `" + tableName + "`");
		
		// Where clauses
		if (whereStatement != null && (whereStatement = whereStatement.trim()).length() >= 3) {
			
			queryBuilder.append(WHERE);
			queryBuilder.append(whereStatement);
			
			if (whereValues != null) {
				for (int b = 0; b < whereValues.length; ++b) {
					queryArgs.add(whereValues[b]);
				}
			}
		}
		
		// Order By clause
		if (orderStatement != null && (orderStatement = orderStatement.trim()).length() > 3) {
			queryBuilder.append(" ORDER BY ");
			queryBuilder.append(orderStatement);
		}
		
		// Limit and offset clause
		if (limit > 0) {
			queryBuilder.append(" LIMIT " + limit);
			
			if (offset > 0) {
				queryBuilder.append(" OFFSET " + offset);
			}
		}
		
		// Create the query set
		return prepareStatement(queryBuilder.toString(), queryArgs.toArray());
	}

	//-------------------------------------------------------------------------
	//
	// CREATE TABLE Query builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL CREATE TABLE IF NOT EXISTS request. This function was created to acommedate the various
	/// syntax differances of CREATE TABLE IF NOT EXISTS across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an CREATE TABLE IF NOT EXISTS statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE TABLE IF NOT EXISTS TABLENAME ( COLLUMNS_NAME TYPE, ... )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to create          (eg: col1, col2)
	/// @param  Columns types              (eg: int, text)
	public JSqlPreparedStatement createTableStatement( //
		String tableName, // Table name to create
		String[] columnName, // The column names
		String[] columnTypes // The column types
	) {

		// Tablename length warning
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		// Column names checks
		if (columnName == null || columnTypes == null || columnTypes.length != columnName.length) {
			throw new IllegalArgumentException("Invalid columnName/Type provided: " + columnName
				+ " : " + columnTypes);
		}
		
		StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ( ");
		
		for (int a = 0; a < columnName.length; ++a) {
			if (a > 0) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(columnName[a]);
			queryBuilder.append(" ");
			queryBuilder.append(columnTypes[a]);
		}
		queryBuilder.append(" )");
		
		// Create the query set
		return prepareStatement(queryBuilder.toString());
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
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);

		// String equalSign = "=";
		// String targetTableAlias = "target";
		// String sourceTableAlias = "source";
		// String statementTerminator = ";";
		// /// Building the query for INSERT OR REPLACE
		// StringBuilder queryBuilder = new StringBuilder("MERGE INTO `" + tableName + "` AS "
		// 	+ targetTableAlias);
		// ArrayList<Object> queryArgs = new ArrayList<Object>();
		// ArrayList<Object> insertQueryArgs = new ArrayList<Object>();
		// ArrayList<Object> updateQueryArgs = new ArrayList<Object>();
		// ArrayList<Object> selectQueryArgs = new ArrayList<Object>();
		
		// /// Building the query for both sides of '(...columns...) VALUE (...vars...)' clauses in upsert
		// /// Note that the final trailing ", " seperator will be removed prior to final query conversion
		// StringBuilder selectColumnNames = new StringBuilder();
		// StringBuilder updateColumnNames = new StringBuilder();
		// StringBuilder insertColumnNames = new StringBuilder();
		// StringBuilder insertColumnValues = new StringBuilder();
		// StringBuilder condition = new StringBuilder();
		// String columnSeperator = ", ";
		// /// Setting up unique values
		// for (int a = 0; a < uniqueColumns.length; ++a) {
		// 	// dual select
		// 	selectColumnNames.append("?");
		// 	selectColumnNames.append(" AS ");
		// 	selectColumnNames.append(uniqueColumns[a]);
		// 	selectColumnNames.append(columnSeperator);
		// 	selectQueryArgs.add(uniqueValues[a]);
		// 	// insert column list
		// 	insertColumnNames.append(uniqueColumns[a]);
		// 	insertColumnNames.append(columnSeperator);
		// 	// insert column value list
		// 	insertColumnValues.append("?");
		// 	insertColumnValues.append(columnSeperator);
		// 	insertQueryArgs.add(uniqueValues[a]);
		// }
		
		// /// Inserting updated values
		// if (insertColumns != null) {
		// 	for (int a = 0; a < insertColumns.length; ++a) {
		// 		// update column
		// 		updateColumnNames.append(insertColumns[a]);
		// 		updateColumnNames.append(equalSign);
		// 		updateColumnNames.append("?");
		// 		updateColumnNames.append(columnSeperator);
		// 		updateQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a]
		// 			: null);
		// 		// select dual
		// 		selectColumnNames.append("?");
		// 		selectColumnNames.append(" AS ");
		// 		selectColumnNames.append(insertColumns[a]);
		// 		selectColumnNames.append(columnSeperator);
		// 		selectQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a]
		// 			: null);
		// 		// insert column
		// 		insertColumnNames.append(insertColumns[a]);
		// 		insertColumnNames.append(columnSeperator);
		// 		insertColumnValues.append("?");
		// 		insertColumnValues.append(columnSeperator);
		// 		insertQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a]
		// 			: null);
		// 	}
		// }
		
		// /// Handling default values
		// if (defaultColumns != null) {
		// 	for (int a = 0; a < defaultColumns.length; ++a) {
		// 		// insert column
		// 		insertColumnNames.append(defaultColumns[a]);
		// 		insertColumnNames.append(columnSeperator);
		// 		insertColumnValues.append(COALESCE);
		// 		insertColumnValues.append(innerSelectPrefix);
		// 		insertColumnValues.append(defaultColumns[a]);
		// 		insertColumnValues.append(innerSelectSuffix);
		// 		insertQueryArgs.addAll(innerSelectArgs);
		// 		insertColumnValues.append(", ?)");
		// 		insertColumnValues.append(columnSeperator);
		// 		insertQueryArgs
		// 			.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
		// 		// update column
		// 		updateColumnNames.append(defaultColumns[a]);
		// 		updateColumnNames.append(equalSign);
		// 		updateColumnNames.append(COALESCE);
		// 		updateColumnNames.append(innerSelectPrefix);
		// 		updateColumnNames.append(defaultColumns[a]);
		// 		updateColumnNames.append(innerSelectSuffix);
		// 		updateQueryArgs.addAll(innerSelectArgs);
		// 		updateColumnNames.append(", ?)");
		// 		updateColumnNames.append(columnSeperator);
		// 		updateQueryArgs
		// 			.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
		// 		// select dual
		// 		// COALESCE((SELECT col3 from t where a=?), ?) as col3
		// 		selectColumnNames.append(COALESCE);
		// 		selectColumnNames.append(innerSelectPrefix);
		// 		selectColumnNames.append(defaultColumns[a]);
		// 		selectColumnNames.append(innerSelectSuffix);
		// 		selectColumnNames.append(", ?)");
		// 		selectQueryArgs.addAll(innerSelectArgs);
		// 		selectColumnNames.append(" AS " + defaultColumns[a] + columnSeperator);
		// 		selectQueryArgs
		// 			.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
		// 	}
		// }
		
		// /// Handling Misc values
		// if (miscColumns != null) {
		// 	for (int a = 0; a < miscColumns.length; ++a) {
		// 		// insert column
		// 		insertColumnNames.append(miscColumns[a]);
		// 		insertColumnNames.append(columnSeperator);
		// 		insertColumnValues.append(innerSelectPrefix);
		// 		insertColumnValues.append(miscColumns[a]);
		// 		insertColumnValues.append(innerSelectSuffix);
		// 		insertQueryArgs.addAll(innerSelectArgs);
		// 		insertColumnValues.append(columnSeperator);
		// 		// updtae column
		// 		updateColumnNames.append(miscColumns[a]);
		// 		updateColumnNames.append(equalSign);
		// 		updateColumnNames.append(innerSelectPrefix);
		// 		updateColumnNames.append(miscColumns[a]);
		// 		updateColumnNames.append(innerSelectSuffix);
		// 		updateColumnNames.append(columnSeperator);
		// 		updateQueryArgs.addAll(innerSelectArgs);
		// 		// select dual
		// 		selectColumnNames.append(innerSelectPrefix);
		// 		selectColumnNames.append(miscColumns[a]);
		// 		selectColumnNames.append(innerSelectSuffix);
		// 		selectColumnNames.append(" AS ");
		// 		selectColumnNames.append(miscColumns[a]);
		// 		selectColumnNames.append(columnSeperator);
		// 		selectQueryArgs.addAll(innerSelectArgs);
		// 	}
		// }
		
		// /// Setting up the condition
		// for (int a = 0; a < uniqueColumns.length; ++a) {
		// 	if (a > 0) {
		// 		condition.append(" and ");
		// 	}
		// 	condition.append(targetTableAlias);
		// 	condition.append(".");
		// 	condition.append(uniqueColumns[a]);
		// 	condition.append(equalSign);
		// 	condition.append(sourceTableAlias);
		// 	condition.append(".");
		// 	condition.append(uniqueColumns[a]);
		// }
		
		// /// Building the final query
		// queryBuilder.append(" USING (SELECT ");
		// queryBuilder.append(selectColumnNames.substring(0, selectColumnNames.length()
		// 	- columnSeperator.length()));
		// queryBuilder.append(")");
		// queryBuilder.append(" AS ");
		// queryBuilder.append(sourceTableAlias);
		// queryBuilder.append(" ON ( ");
		// queryBuilder.append(condition.toString());
		// queryBuilder.append(" ) ");
		// queryBuilder.append(" WHEN MATCHED ");
		// queryBuilder.append(" THEN UPDATE SET ");
		// queryBuilder.append(updateColumnNames.substring(0, updateColumnNames.length()
		// 	- columnSeperator.length()));
		// queryBuilder.append(" WHEN NOT MATCHED ");
		// queryBuilder.append(" THEN INSERT (");
		// queryBuilder.append(insertColumnNames.substring(0, insertColumnNames.length()
		// 	- columnSeperator.length()));
		// queryBuilder.append(") VALUES (");
		// queryBuilder.append(insertColumnValues.substring(0, insertColumnValues.length()
		// 	- columnSeperator.length()));
		// queryBuilder.append(")");
		// queryBuilder.append(statementTerminator);
		
		// queryArgs.addAll(selectQueryArgs);
		// queryArgs.addAll(updateQueryArgs);
		// queryArgs.addAll(insertQueryArgs);
		
		// return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	/*
	
	
	///
	/// Helps generate an SQL DELETE request. This function was created to acommedate the various
	/// syntax differances of DELETE across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an DELETE statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// DELETE
	/// FROM tableName //table name to select from
	/// WHERE
	///	col1=?       //where clause
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlPreparedStatement deleteQuerySet( //
		String tableName, // Table name to select from
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues // Values that corresponds to the where statement
	) {
		
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("DELETE ");
		
		// From table names
		queryBuilder.append(" FROM `" + tableName + "`");
		
		// Where clauses
		if (whereStatement != null && (whereStatement = whereStatement.trim()).length() >= 3) {
			queryBuilder.append(WHERE);
			queryBuilder.append(whereStatement);
			
			if (whereValues != null) {
				for (int b = 0; b < whereValues.length; ++b) {
					queryArgs.add(whereValues[b]);
				}
			}
		}
		
		// Create the query set
		return new JSqlPreparedStatement(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	///
	/// Helps generate an SQL CREATE TABLE IF NOT EXISTS request. This function was created to acommedate the various
	/// syntax differances of CREATE TABLE IF NOT EXISTS across the various SQL vendors (if any).
	///
	/// 
	///
	/// The syntax below, is an example of such an CREATE TABLE IF NOT EXISTS statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE TABLE IF NOT EXISTS TABLENAME ( COLLUMNS_NAME TYPE, ... )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlPreparedStatement createTableQuerySet(String tableName, // Table name to create
		//
		String[] columnName, // The column names
		String[] columnDefine // The column types
	) {
		if (columnName == null || columnDefine == null || columnDefine.length != columnName.length) {
			throw new IllegalArgumentException("Invalid columnName/Type provided: " + columnName
				+ " : " + columnDefine);
		}
		
		StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ( ");
		
		for (int a = 0; a < columnName.length; ++a) {
			if (a > 0) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(columnName[a]);
			queryBuilder.append(" ");
			queryBuilder.append(columnDefine[a]);
		}
		queryBuilder.append(" )");
		
		// Create the query set
		return new JSqlPreparedStatement(queryBuilder.toString(), null, this);
	}
	
	///
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// 
	///
	/// The syntax below, is an example of such an SELECT statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE (UNIQUE|FULLTEXT) INDEX IF NOT EXISTS TABLENAME_SUFFIX ON TABLENAME ( COLLUMNS )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlPreparedStatement createTableIndexQuerySet( //
		String tableName, // Table name to select from
		//
		String columnNames, // The column name to create the index on
		//
		String indexType, // The index type if given, can be null
		//
		String indexSuffix // The index name suffix, its auto generated if null
	) {
		
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("CREATE ");
		
		if (indexType != null && indexType.length() > 0) {
			queryBuilder.append(indexType);
			queryBuilder.append(" ");
		}
		
		queryBuilder.append("INDEX IF NOT EXISTS ");
		
		// Creates a suffix, based on the collumn names
		if (indexSuffix == null || indexSuffix.length() <= 0) {
			indexSuffix = columnNames.replaceAll("/[^A-Za-z0-9]/", ""); //.toUpperCase(Locale.ENGLISH)?
		}
		
		if ((tableName.length() + 1 + indexSuffix.length()) > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName + "_" + indexSuffix);
		}
		
		queryBuilder.append("`");
		queryBuilder.append(tableName);
		queryBuilder.append("_");
		queryBuilder.append(indexSuffix);
		queryBuilder.append("` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("` (");
		queryBuilder.append(columnNames);
		queryBuilder.append(")");
		
		// Create the query set
		return new JSqlPreparedStatement(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	/// Helper varient, where indexSuffix is defaulted to auto generate (null)
	public JSqlPreparedStatement createTableIndexQuerySet( //
		String tableName, // Table name to select from
		String columnNames, // The column name to create the index on
		String indexType // The index type if given, can be null
	) {
		return createTableIndexQuerySet(tableName, columnNames, indexType, null);
	}
	
	/// Helper varient, where idnexType and indexSuffix is defaulted(null)
	public JSqlPreparedStatement createTableIndexQuerySet( //
		String tableName, // Table name to select from
		String columnNames // The column name to create the index on
	) {
		return createTableIndexQuerySet(tableName, columnNames, null, null);
	}
	
	/// Executes the table meta data query, and returns the result object
	public JSqlResult executeQuery_metadata(String table) {
		JSqlResult res = null;
		try {
			ResultSet rs = null;
			//Try and finally : prevent memory leaks
			try {
				DatabaseMetaData meta = sqlConn.getMetaData();
				rs = meta.getColumns(null, null, table, null);
				res = new JSqlResult(null, rs);
				
				//let JSqlResult "close" it
				rs = null;
				return res;
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_metadata exception", e);
		}
	}
	
	/// Executes the table meta data query, and returns the result object
	public Map<String, String> getMetaData(String sql) {
		Map<String, String> metaData = null;
		ResultSet rs = null;
		//Try and finally : prevent memory leaks
		try {
			try {
				Statement st = sqlConn.createStatement();
				rs = st.executeQuery(sql);
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int numberOfColumns = rsMetaData.getColumnCount();
				for (int i = 1; i <= numberOfColumns; i++) {
					if (metaData == null) {
						metaData = new HashMap<String, String>();
					}
					metaData.put(rsMetaData.getColumnName(i), rsMetaData.getColumnTypeName(i));
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				rs = null;
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_metadata exception", e);
		}
		return metaData;
	}
	
	public String genericSqlParser(String inString) {
		return null;
	}
	
	//
	// Added by Sam
	//
	public java.sql.Blob createBlob() throws Exception {
		try {
			return sqlConn.createBlob();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	*/


}
