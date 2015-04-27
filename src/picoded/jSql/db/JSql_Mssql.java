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
import java.util.regex.Matcher;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.jSql.JSqlType;
import picoded.jSql.JSqlResult;
import picoded.jSql.JSqlException;

import picoded.jSql.JSql;
import picoded.jSql.db.BaseInterface;

/// Pure SQL Server 2012 implentation of JSql
/// Support only for SQL Server 2012 and above version for the pagination query, the OFFSET / FETCH keywords
/// are used which are faster and better in performance in comparison of old ROW_NUMBER()
public class JSql_Mssql extends JSql implements BaseInterface {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Mssql.class.getName());
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		sqlType = JSqlType.mssql;
		
		String connectionUrl = "jdbc:jtds:sqlserver://" + dbUrl;
		
		if (dbName != null && dbName.length() > 0) {
			connectionUrl = connectionUrl + ";DatabaseName=" + dbName + ";uselobs=false;"; //disable clobs
		}
		
		try {
			Class.forName("net.sourceforge.jtds.jdbcx.JtdsDataSource"); //connection pooling
			sqlConn = java.sql.DriverManager.getConnection(connectionUrl, dbUser, dbPass);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load mssql connection: ", e);
		}
	}
	
	// Internal parser that converts some of the common sql statements to mssql
	public static String genericSqlParser(String inString) {
		
		String fixedQuotes = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("'", "\"").replaceAll("`", "\"");
		String upperCaseStr = fixedQuotes.toUpperCase();
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
			prefixOffset = drop.length() + 1;
			
			if (upperCaseStr.startsWith(table, prefixOffset)) { //TABLE
				prefixOffset += table.length() + 1;
				
				if (upperCaseStr.startsWith(ifExists, prefixOffset)) { //IF EXISTS
					prefixOffset += ifExists.length() + 1;
					
					qStringPrefix = "BEGIN TRY IF OBJECT_ID('" + fixedQuotes.substring(prefixOffset).toUpperCase()
						+ "', 'U')" + " IS NOT NULL DROP TABLE " + fixedQuotes.substring(prefixOffset)
						+ " END TRY BEGIN CATCH END CATCH";
				} else {
					qStringPrefix = "DROP TABLE ";
				}
				qString = qStringPrefix;
			} else if (upperCaseStr.startsWith(index, prefixOffset)) { //INDEX
			
			}
		} else if (upperCaseStr.startsWith(create)) { //CREATE
			prefixOffset = create.length() + 1;
			
			if (upperCaseStr.startsWith(table, prefixOffset)) { //TABLE
				prefixOffset += table.length() + 1;
				
				if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += ifNotExists.length() + 1;
					//get the table name from incoming query
					String tableName = getTableName(fixedQuotes.substring(prefixOffset));
					qStringPrefix = "BEGIN TRY IF NOT EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'" + tableName
						+ "')" + " AND OBJECTPROPERTY(id, N'" + tableName + "')" + " = 1) CREATE TABLE ";
					qStringSuffix = " END TRY BEGIN CATCH END CATCH";
				} else {
					qStringPrefix = "CREATE TABLE ";
				}
				qString = _fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
				//qString = _simpleMysqlToOracle_collumnSubstitude(qString);
			} else {
				logger.finer("Trying to matched INDEX : " + upperCaseStr.substring(prefixOffset));
				if (createIndexType.matcher(upperCaseStr.substring(prefixOffset)).matches()) { //UNIQUE|FULLTEXT|SPATIAL|_ INDEX
					logger.finer("Matched INDEX : " + inString);
					
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
					if (upperCaseStr.startsWith(index, prefixOffset)) {
						prefixOffset += index.length() + 1;
						
						//If not exists wrapper
						if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) {
							prefixOffset += ifNotExists.length() + 1;
							qStringPrefix = "";
							qStringSuffix = "";
						}
						
						tmpStr = _fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
						tmpIndx = tmpStr.indexOf(" ON ");
						
						if (tmpIndx > 0) {
							qString = "BEGIN TRY CREATE " + ((indexType != null) ? indexType + " " : "") + "INDEX "
								+ tmpStr.substring(0, tmpIndx) + " ON "
								+ _fixTableNameInMssqlSubQuery(tmpStr.substring(tmpIndx + 4))
								+ " END TRY BEGIN CATCH END CATCH";
						}
						
					}
				}
			}
		} else if (upperCaseStr.startsWith(insertInto)) { //INSERT INTO
			prefixOffset = insertInto.length() + 1;
			
			tmpStr = _fixTableNameInMssqlSubQuery(fixedQuotes.substring(prefixOffset));
			
			qString = "INSERT INTO " + tmpStr;
		} else if (upperCaseStr.startsWith(select)) { //SELECT
			prefixOffset = select.length() + 1;
			
			tmpStr = qString.substring(prefixOffset);
			tmpIndx = qString.toUpperCase().indexOf(" FROM ");
			
			if (tmpIndx > 0) {
				qString = "SELECT " + tmpStr.substring(0, tmpIndx - 7).replaceAll("\"", "'").replaceAll("`", "'")
					+ " FROM " + _fixTableNameInMssqlSubQuery(tmpStr.substring(tmpIndx - 1));
			} else {
				qString = _fixTableNameInMssqlSubQuery(fixedQuotes);
			}
			
			prefixOffset = 0;
			//Fix the "AS" quotation
			while ((tmpIndx = qString.indexOf(" AS ", prefixOffset)) > 0) {
				prefixOffset = qString.indexOf(" ", tmpIndx + 4);
				
				if (prefixOffset > 0) {
					qString = qString.substring(0, tmpIndx)
						+ qString.substring(tmpIndx, prefixOffset).replaceAll("`", "\"").replaceAll("'", "\"")
						+ qString.substring(prefixOffset);
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
			
			tmpStr = _fixTableNameInMssqlSubQuery(qString.substring(prefixOffset));
			qString = deleteFrom + " " + tmpStr;
			
		} else if (upperCaseStr.startsWith(update)) { //UPDATE
			prefixOffset = update.length() + 1;
			
			tmpStr = _fixTableNameInMssqlSubQuery(qString.substring(prefixOffset));
			qString = update + " " + tmpStr;
		}
		//Drop table query modication
		if (qString.contains("DROP")) {
			qString = qStringPrefix;
		} else {
			qString = qStringPrefix + qString + qStringSuffix;
		}
		
		//Convert MY-Sql NUMBER data type to NUMERIC data type for Ms-sql
		if (qString.contains("CREATE TABLE") && qString.contains("NUMBER")) {
			qString = qString.replaceAll("NUMBER", "NUMERIC");
		}
		
		//remove ON DELETE FOR CLIENTSTATUSHISTORY---> this block needs to be refined for future.
		if (qString.contains("ON DELETE")) { //qString.contains("CLIENTSTATUSHISTORY") &&
			qString = qString.replaceAll("ON DELETE SET NULL", "");
		}
		
		//logger.finer("Converting MySQL query to MsSql query");
		logger.finer("MySql -> " + inString);
		logger.finer("MsSql -> " + qString);
		
		//logger.warning("MySql -> "+inString);
		//logger.warning("OracleSql -> "+qString);
		//System.out.println("[Query]: "+qString);
		return qString; //no change of data
	}
	
	//Method to return table name from incoming query string
	private static String getTableName(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		String tableStr = qString.substring(0, indxPt).toUpperCase();
		return tableStr; //retrun the table name
	}
	
	private static String _fixTableNameInMssqlSubQuery(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		String tableStr = qString.substring(0, indxPt).toUpperCase();
		
		qString = tableStr + qString.substring(indxPt);
		
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
	
}