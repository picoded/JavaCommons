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

/// Pure ORACLE-SQL implentation of JSql
public class JSql_Oracle extends JSql implements BaseInterface {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Oracle.class.getName());
	
	/// 
	private String oracleTablespace = null;
	
	/// Runs JSql with the JDBC ORACLE SQL engine
	///
	/// **Note:** urlString, is just IP:PORT. For example, "127.0.0.1:3306"
	public JSql_Oracle(String oraclePath, String dbUser, String dbPass) {
		sqlType = JSqlType.oracle;
		
		// Get the assumed oracle table space
		int tPoint = oraclePath.indexOf("@");
		if (tPoint > 0) {
			oracleTablespace = oraclePath.substring(0, tPoint);
		} else {
			oracleTablespace = null;
		}
		
		String connectionUrl = "jdbc:oracle:thin:" + oraclePath;
		try {
			Class.forName("oracle.jdbc.OracleDriver").newInstance(); //ensure oracle driver is loaded
			sqlConn = java.sql.DriverManager.getConnection(connectionUrl, dbUser, dbPass);
			
			// Try to alter & ensure the current session roles
			try {
				execute("SET ROLE ALL");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to alter current session roles", e);
			}
			
			// Try to alter & ensure the current session schema to the right tablespace
			//try {
			//	if( oracleTablespace != null && !dbUser.equals(oracleTablespace) ) {
			//		execute("ALTER SESSION SET current_schema = ?", oracleTablespace);
			//	}
			//} catch (Exception e) {
			//	logAndFormatError("Failed to alter sesion to target table space", e);
			//}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to load SQL connection: ", e);
		}
		
	}
	
	/// Collumn type correction from mysql to oracle sql
	private static String _simpleMysqlToOracle_collumnSubstitude(String qString) {
		return qString.replaceAll("(?i)BIGINT", "NUMBER(19, 0)")
			.replaceAll("(?i)BIT", "RAW")
			.replaceAll("(?i)BLOB", "BLOB")
			//, RAW
			//.replaceAll("(?i)CHAR","CHAR")
			//.replaceAll("(?i)DATE","DATE")
			.replaceAll("(?i)DATETIME", "DATE").replaceAll("(?i)DECIMAL", "FLOAT(24)").replaceAll("(?i)DOUBLE",
				"FLOAT(24)").replaceAll("(?i)DOUBLE PRECISION", "FLOAT(24)")
			//.replaceAll("(?i)FLOAT","FLOAT")
			.replaceAll("(?i)INTEGER", "INT")
			//.replaceAll("(?i)INT","NUMBER(10,0)")
			.replaceAll("(?i)LONGBLOB", "BLOB")
			//, RAW
			.replaceAll("(?i)LONGTEXT", "CLOB")
			//, RAW
			.replaceAll("(?i)MEDIUMBLOB", "BLOB")
			//, RAW
			.replaceAll("(?i)MEDIUMINT", "NUMBER(7,0)").replaceAll("(?i)MEDIUMTEXT", "CLOB")
			//, RAW
			.replaceAll("(?i)NUMERIC", "NUMBER").replaceAll("(?i)REAL", "FLOAT (24)").replaceAll("(?i)SMALLINT",
				"NUMBER(5,0)").replaceAll("(?i)TEXT", "CLOB")
			//VARCHAR2,
			.replaceAll("(?i)TIME", "DATE").replaceAll("(?i)TIMESTAMP", "DATE").replaceAll("(?i)TINYBLOB", "RAW")
			.replaceAll("(?i)TINYINT", "NUMBER(3,0)")

			/*
			.replaceAll("(?i)ENUM(?=\\()","VARCHAR2")
			.replaceAll("(?i)ENUM(?!\\()","VARCHAR2(n)")
			.replaceAll("(?i)SET(?=\\()","VARCHAR2")
			.replaceAll("(?i)SET(?!\\()","VARCHAR2(n)")
			.replaceAll("(?i)TINYTEXT(?=\\()","VARCHAR2")
			.replaceAll("(?i)TINYTEXT(?!\\()","VARCHAR2(n)")
			 */

			.replaceAll("(?i)VARCHAR(?!\\()", "VARCHAR2(4000)") //, CLOB
			.replaceAll("(?i)VARCHAR\\(", "VARCHAR2(") //, CLOB
			.replaceAll("(?i)YEAR", "NUMBER");
	}
	
	/// Fixes the table name, and removes any trailing ";" if needed
	private static String _fixTableNameInOracleSubQuery(String qString) {
		qString = qString.trim();
		int indxPt = ((indxPt = qString.indexOf(' ')) <= -1) ? qString.length() : indxPt;
		String tableStr = qString.substring(0, indxPt).toUpperCase();
		
		/*
		if( !tableStr.substring(0,1).equals("\"") ) {
			tableStr = "\"" + tableStr;
			if( !tableStr.substring(tableStr.length()-1).equals("\"") ) {
				tableStr = tableStr+"\"";
			}
		}
		 */
		qString = tableStr + qString.substring(indxPt);
		
		while (qString.endsWith(";")) { //Remove uneeded trailing ";" semi collons
			qString = qString.substring(0, qString.length() - 1);
		}
		return qString;
	}
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public static String genericSqlParser(String inString) {
		//Unique to oracle prefix, automatically terminates all additional conversion attempts
		final String oracleImmediateExecute = "BEGIN EXECUTE IMMEDIATE";
		if (inString.startsWith(oracleImmediateExecute)) {
			return inString;
		}
		
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
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'DROP TABLE ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
				} else {
					qStringPrefix = "DROP TABLE ";
				}
				qString = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			} else if (upperCaseStr.startsWith(index, prefixOffset)) { //INDEX
			
			}
		} else if (upperCaseStr.startsWith(create)) { //CREATE
			prefixOffset = create.length() + 1;
			
			if (upperCaseStr.startsWith(table, prefixOffset)) { //TABLE
				prefixOffset += table.length() + 1;
				
				if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += ifNotExists.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'CREATE TABLE ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
				} else {
					qStringPrefix = "CREATE TABLE ";
				}
				qString = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
				qString = _simpleMysqlToOracle_collumnSubstitude(qString);
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
							qStringPrefix = "BEGIN EXECUTE IMMEDIATE '";
							qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
						}
						
						tmpStr = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
						tmpIndx = tmpStr.indexOf(" ON ");
						
						if (tmpIndx > 0) {
							qString = "CREATE " + ((indexType != null) ? indexType + " " : "") + "INDEX "
								+ tmpStr.substring(0, tmpIndx) + " ON "
								+ _fixTableNameInOracleSubQuery(tmpStr.substring(tmpIndx + 4));
						}
						
					}
				}
			}
		} else if (upperCaseStr.startsWith(insertInto)) { //INSERT INTO
			prefixOffset = insertInto.length() + 1;
			
			tmpStr = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			
			//-- Not fully supported
			//tmpIndx = tmpStr.indexOf(" ON DUPLICATE KEY UPDATE ");
			//if(tmpIndx > 0) {
			//	qStringPrefix = "BEGIN EXECUTE IMMEDIATE '";
			//	qString = tmpStr.substring(0, tmpIndx);
			//	qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
			//}
			
			qString = "INSERT INTO " + tmpStr;
		} else if (upperCaseStr.startsWith(select)) { //SELECT
			prefixOffset = select.length() + 1;
			
			tmpStr = qString.substring(prefixOffset);
			tmpIndx = qString.toUpperCase().indexOf(" FROM ");
			
			if (tmpIndx > 0) {
				qString = "SELECT " + tmpStr.substring(0, tmpIndx - 7).replaceAll("\"", "'").replaceAll("`", "'")
					+ " FROM " + _fixTableNameInOracleSubQuery(tmpStr.substring(tmpIndx - 1));
			} else {
				qString = _fixTableNameInOracleSubQuery(fixedQuotes);
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
			
		} else if (upperCaseStr.startsWith(deleteFrom)) {
			prefixOffset = deleteFrom.length() + 1;
			
			tmpStr = _fixTableNameInOracleSubQuery(qString.substring(prefixOffset));
			qString = deleteFrom + " " + tmpStr;
			
		} else if (upperCaseStr.startsWith(update)) { //UPDATE
			prefixOffset = update.length() + 1;
			
			tmpStr = _fixTableNameInOracleSubQuery(qString.substring(prefixOffset));
			qString = update + " " + tmpStr;
		}
		
		qString = qStringPrefix + qString + qStringSuffix;
		
		//logger.finer("Converting MySQL query to oracleSQL query");
		//logger.finer("MySql -> "+inString);
		//logger.finer("OracleSql -> "+qString);
		
		//logger.warning("MySql -> "+inString);
		//logger.warning("OracleSql -> "+qString);
		return qString; //no change of data
	}
	
	/// Executes the argumented query, and returns the result object *without* 
	/// fetching the result data from the database. (not fetching may not apply to all implementations)
	/// 
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		//try {
		return executeQuery_raw(genericSqlParser(qString), values);
		//} catch(JSqlException e) {
		//	logger.log( Level.SEVERE, "ExecuteQuery Exception" ); //, e 
		//	logger.log( Level.SEVERE, "-> Original query : " + qString );
		//	logger.log( Level.SEVERE, "-> Parsed query   : " + genericSqlParser(qString) );
		//	throw e;
		//}
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		//try {
		return query_raw(genericSqlParser(qString), values);
		//} catch(JSqlException e) {
		//	logger.log( Level.SEVERE, "Query Exception" ); //, e 
		//	logger.log( Level.SEVERE, "-> Original query : " + qString );
		//	logger.log( Level.SEVERE, "-> Parsed query   : " + genericSqlParser(qString) );
		//	throw e;
		//}
	}
	
	/// Executes and dispose the sqliteResult object.
	///
	/// Returns false if no result is given by the execution call, else true on success
	public boolean execute(String qString, Object... values) throws JSqlException {
		//try {
		return execute_raw(genericSqlParser(qString), values);
		//} catch(JSqlException e) {
		//	logger.log( Level.SEVERE, "Execute Exception" ); //, e 
		//	logger.log( Level.SEVERE, "-> Original query : " + qString );
		//	logger.log( Level.SEVERE, "-> Parsed query   : " + genericSqlParser(qString) );
		//	throw e;
		//}
	}
	
}