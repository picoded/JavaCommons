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
	
	/// SQLite static constructor, returns picoded.jSql.dbSqlite
	public static JSql sqlite() {
		return new picoded.jSql.db.JSql_Sqlite();
	}
	
	/// SQLite static constructor, returns picoded.jSql.dbSqlite
	public static JSql sqlite(String sqliteLoc) {
		return new picoded.jSql.db.JSql_Sqlite(sqliteLoc);
	}
	
	/// MySql static constructor, returns picoded.jSql.dbMysql
	public static JSql mysql(String urlStr, String dbName,String dbUser, String dbPass) {
		return new picoded.jSql.db.JSql_Mysql(urlStr, dbName, dbUser, dbPass);
	}
	/*
	
	/// Oracle static constructor, returns picoded.jSql.dbOracle
	public static JSql oracle(String oraclePath, String dbUser, String dbPass) {
		return new picoded.jSql.dbOracle(oraclePath, dbUser, dbPass);
	}
   
   /// Mssql static constructor, returns picoded.jSql.dbMssql
	public static JSql msSql(String dbUrl,String dbName,String dbUser, String dbPass) {
		return new picoded.jSql.dbMssql(dbUrl, dbName, dbUser, dbPass);
	}
	*/
	
	// Throws an exception, as this functionality isnt supported in the base class
	// also allows backwards competibility with test cases
	//public JSql() {
		//throw new RuntimeException(JSqlException.invalidDatabaseImplementationException);
	//}
	
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
		int pt=0;
		final Object parts[] = values;
		
		Object argObj;
		PreparedStatement ps;
		
		try {
			ps=sqlConn.prepareStatement(qString);
			
			for(pt=0; pt<parts.length; ++pt) {
				argObj = parts[pt];
				if( argObj == null) {
					ps.setNull(pt+1,0);
				} else if( String.class.isInstance(argObj) ) {
					ps.setString(pt+1,(String)argObj);
				} else if( Integer.class.isInstance(argObj) ) {
					ps.setInt(pt+1, (Integer)argObj);
				} else if( Long.class.isInstance(argObj) ) {
					ps.setLong(pt+1,(Long)argObj);
				} else if( Double.class.isInstance(argObj) ) {
					ps.setDouble(pt+1,(Double)argObj);
				} else {
					String argClassName = argObj.getClass().getName();
					throw new JSqlException("Unknown argument type ("+pt+") : "+(argClassName) );
				}
			}
		} catch(Exception e) {
			throw new JSqlException("Invalid statement argument/parameter ("+pt+")", e);
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
				if(qString.trim().toUpperCase().substring(0,6).equals("SELECT")) {
					rs = ps.executeQuery();
					res = new JSqlResult(ps, rs);

					//let JSqlResult "close" it
					ps = null;
					rs = null;
					return res;
				} else {
					int r = ps.executeUpdate();
					if(r != -1) {
						return new JSqlResult(); //returns a blank JSqlResult, for consistency
					} else {
						return null;
					}
				}
			} finally {
				if(rs != null) {
					rs.close();
				}
				if(ps != null) {
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
		if(result != null) {
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
					if(qString.trim().toUpperCase().substring(0,6).equals("SELECT")) {
						rs = ps.executeQuery();
						if(rs != null) {
							return true;
						}
					} else {
						int r = ps.executeUpdate();
						if(r != -1) {
							return true;
						}
					}
				} finally {
					if(rs != null) {
						rs.close();
					}
					if(ps != null) {
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
		if(sqlConn != null) {
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
			dispose();        // close open files
		} finally {
			super.finalize();
		}
	}
	

}