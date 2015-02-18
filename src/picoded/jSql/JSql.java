package picoded.jSql;

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
import java.util.regex.Matcher;
import java.util.*;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.jSql.JSqlType;
import picoded.jSql.JSqlResult;
import picoded.jSql.JSqlException;

import picoded.jSql.db.BaseInterface;

/// Database intreface base class.
public class JSql implements BaseInterface {
	
	/// SQLite static constructor, returns picoded.jSql.JSql_Sqlite
	public static JSql sqlite() {
		return new picoded.jSql.db.JSql_Sqlite();
	}
	
	/// SQLite static constructor, returns picoded.jSql.JSql_Sqlite
	public static JSql sqlite(String sqliteLoc) {
		return new picoded.jSql.db.JSql_Sqlite(sqliteLoc);
	}
	
	/// MySql static constructor, returns picoded.jSql.JSql_Mysql
	public static JSql mysql(String urlStr, String dbName, String dbUser, String dbPass) {
		return new picoded.jSql.db.JSql_Mysql(urlStr, dbName, dbUser, dbPass);
	}
	
	/// Mssql static constructor, returns picoded.jSql.JSql_Mssql
	public static JSql mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		return new picoded.jSql.db.JSql_Mssql(dbUrl, dbName, dbUser, dbPass);
	}
	
	/// Oracle static constructor, returns picoded.jSql.db.JSql_Oracle
	public static JSql oracle(String oraclePath, String dbUser, String dbPass) {
		return new picoded.jSql.db.JSql_Oracle(oraclePath, dbUser, dbPass);
	}
	
	/// As this is the base class varient, this funciton isnt suported
	public void recreate(boolean force) {
		throw new RuntimeException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// database connection
	protected Connection sqlConn = null;
	
	/// Internal refrence of the current sqlType the system is running as
	public JSqlType sqlType = JSqlType.invalid;
	
	/// [private] Helper function, used to prepare the sql statment in multiple situations
	protected PreparedStatement prepareSqlStatment(String qString, Object... values) throws JSqlException {
		int pt = 0;
		final Object parts[] = values;
		
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
	
	/// Executes the argumented query, and returns the result object *without*
	/// fetching the result data from the database. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery_raw(String qString, Object... values) throws JSqlException {
		JSqlResult res = null;
		final String query = qString;
		final Object parts[] = values;
		try {
			PreparedStatement ps = prepareSqlStatment(qString, values);
			ResultSet rs = null;
			//Try and finally : prevent memory leaks
			try {
				//is a select statment
				if (qString.trim().toUpperCase().substring(0, 6).equals("SELECT")) {
					rs = ps.executeQuery();
					res = new JSqlResult(ps, rs);
					
					//let JSqlResult "close" it
					ps = null;
					rs = null;
					return res;
				} else {
					int r = ps.executeUpdate();
					if (r != -1) {
						return new JSqlResult(); //returns a blank JSqlResult, for consistency
					} else {
						return null;
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_raw exception", e);
		}
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query_raw(String qString, Object... values) throws JSqlException {
		JSqlResult result = executeQuery_raw(qString, values);
		if (result != null) {
			result.fetchAllRows();
		}
		return result;
	}
	
	/// Executes and dispose the sqliteResult object. Similar to executeQuery
	/// Returns false if no result object is given by the execution call. This is raw execution.
	public boolean execute_raw(String qString, Object... values) throws JSqlException {
		try {
			PreparedStatement ps = prepareSqlStatment(qString, values);
			ResultSet rs = null;
			try {
				//is a select statment
				if (qString.trim().toUpperCase().substring(0, 6).equals("SELECT")) {
					rs = ps.executeQuery();
					if (rs != null) {
						return true;
					}
				} else {
					int r = ps.executeUpdate();
					if (r != -1) {
						return true;
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("execute_raw exception", e);
		}
		return false;
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public boolean execute(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Returns true, if dispose() function was called prior
	public boolean isDisposed() {
		return (sqlConn == null);
	}
	
	/// Dispose of the respective SQL driver / connection
	public void dispose() {
		// Disposes the instancce connection
		if (sqlConn != null) {
			try {
				//sqlConn.join();
				sqlConn.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			sqlConn = null;
		}
	}
	
	/// Just incase a user forgets to dispose "as per normal"
	protected void finalize() throws Throwable {
		try {
			dispose(); // close open files
		} finally {
			super.finalize();
		}
	}
	
	//--------------------------------------------------------------------------
	// Utility helper functions used to prepare common complex SQL quries
	//--------------------------------------------------------------------------
	public Object[] joinArguments(Object[] arr1, Object[] arr2) {
		return org.apache.commons.lang3.ArrayUtils.addAll(arr1, arr2);
	}
	
	public void setAutoCommit(boolean autoCommit) throws JSqlException {
		try {
			sqlConn.setAutoCommit(autoCommit);
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	public boolean getAutoCommit() throws JSqlException {
		try {
			return sqlConn.getAutoCommit();
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	public void rollback() throws JSqlException {
		try {
			sqlConn.rollback();
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	//--------------------------------------------------------------------------
	// Utility helper functions used to prepare common complex SQL quries
	//--------------------------------------------------------------------------
	
	public JSqlQuerySet selectQuerySet( //
	   String tableName, // Table name to select from
	   String[] selectColumns, // The Columns to select, null means all
	   
	   String whereStatement, // The Columns to apply where clause, this must be sql neutral
	   Object[] whereValues // Values that corresponds to the where statement
	) {
		return selectQuerySet(tableName, selectColumns, whereStatement, whereValues, null, 0, 0);
	}
	
	public JSqlQuerySet selectQuerySet( //
	   String tableName, // Table name to select from
	   String[] selectColumns, // The Columns to select, null means all
	   
	   String whereStatement, // The Columns to apply where clause, this must be sql neutral
	   Object[] whereValues, // Values that corresponds to the where statement
	   
	   String orderStatement, // Order by statements, must be either ASC / DESC
	   
	   long limit, // Limit row count to, use 0 to ignore / disable
	   long offset // Offset limit by?
	) {
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder querySB = new StringBuilder("SELECT ");
		
		// Select collumns
		if (selectColumns == null || selectColumns.length == 0) {
			querySB.append("*");
		} else {
			for (int b = 0; b < selectColumns.length; ++b) {
				if (b > 0) {
					querySB.append(",");
				}
				querySB.append(selectColumns[b]);
			}
		}
		
		// From table names
		querySB.append(" FROM `" + tableName + "`");
		
		// Where clauses
		if (whereStatement != null && whereStatement.length() > 3) {
			
			querySB.append(" WHERE ");
			querySB.append(whereStatement);
			
			for (int b = 0; b < whereValues.length; ++b) {
				queryArgs.add(whereValues[b]);
			}
		}
		
		// Order By clause
		if (orderStatement != null && orderStatement.length() > 3) {
			querySB.append(" ORDER BY ");
			querySB.append(orderStatement);
		}
		
		// Limit and offset clause
		if (limit > 0) {
			querySB.append(" LIMIT " + limit);
			
			if (offset > 0) {
				querySB.append(" OFFSET " + offset);
			}
		}
		
		// Create the query set
		return new JSqlQuerySet(querySB.toString(), queryArgs.toArray(), this);
	}
	
	/*
	public JSqlQuerySet prepareSelectQuerySet(
													  String tableName, // Table name to select from
													  String[] selectColumns, // The Columns to select, null means all
													  Map<String,Object> whereMapping, // The Columns and / or where mapping to filter
													  long limit, //limit row count to
													  long offset //offset limit by?
													  ) {
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder querySB = new StringBuilder( "SELECT " );
		
		if( selectColumns == null || selectColumns.length == 0 ) {
			querySB.append("*");
		} else {
			for(int b=0; b<selectColumns.length; ++b) {
				if(b>0) {
					querySB.append(",");
				}
				querySB.append( selectColumns[b] );
			}
		}
		
		querySB.append(" FROM `"+tableName+"` WHERE " );
		
		for(int b=0; b<whereColumns.length; ++b) {
			if(b>0) {
				querySB.append(" AND ");
			}
			querySB.append(whereColumns[b]+" = ?");
			queryArgs.add(whereValues[b]);
		}
		
		return new JSqlQuerySet( querySB.toString(), queryArgs.toArray(), this );
	}
	 */

	/*
	
	public Object[] prepareUpsertQuerySet( //
		String tableName, // Table name to upsert on
	   String[] uniqueColumns, // The unique column names
	   Object[] uniqueValues, // The row unique identifier values
	   String[] valueColumns, // Columns values to update
	   Object[] updateValues, // Values to update, note that null is skipped
	   Object[] insertValues, // Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
	   String[] miscColumns // Various column names where its existing value needs to be maintained (if any)
	) throws JSqlException {
		
		/// Note the following is for SQLite only
		
		/// Building the query for INSERT OR REPLACE
		ArrayList<String> columnNames = new ArrayList<String>();
		ArrayList<String> columnValues = new ArrayList<String>();
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		
		
		/// Inserting unique values
		if( uniqueColumns != null && uniqueValues != null && uniqueColumns.length == uniqueValues.length ) {
			for(int a=0; a<uniqueColumns.length; ++a) {
				columnNames.add(uniqueColumns[a]);
				columnValues.add("?");
				queryArgs.add(uniqueValues[a]);
			}
		} else {
			throw new JSqlException("Upsert query requires unique column values");
		}
		
		
		
		if( miscColumns != null ) {
			for(int a=0; a<miscColumns.length; ++a) {
				columnNames.add(miscColumns[a]);
				
				tmpSB = new StringBuilder( "(SELECT "+miscColumns[a]+" FROM `"+tableName+"` WHERE " );
				for(int b=0; b<uniqueColumns.length; ++b) {
					if(b>0) {
						tmpSB.append(" AND ");
					}
					tmpSB.append(uniqueColumns[b]+" = ?");
					queryArgs.add(uniqueValues[b]);
				}
				tmpSB.append(")");
				
				columnValues.add( tmpSB.toString() );
			}
		}
		
		return null;
	}
	 */
}