package picoded.JSql.db;

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
import java.util.Map;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.JSql.JSqlType;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlException;

import picoded.JSql.*;
import picoded.JSql.db.BaseInterface;

/// Pure ORACLE-SQL implentation of JSql
public class JSql_Oracle extends JSql {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Oracle.class.getName());
	
	/// 
	private String oracleTablespace = null;
	
	/// Runs JSql with the JDBC ORACLE SQL engine
	///
	/// **Note:** urlString, is just IP:PORT. For example, "127.0.0.1:3306"
	public JSql_Oracle(String oraclePath, String dbUser, String dbPass) {
		// store database connection properties
		setConnectionProperties(oraclePath, null, dbUser, dbPass, null);
		
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	private void setupConnection() {
		sqlType = JSqlType.oracle;
		
		// Get the assumed oracle table space
		String oraclePath = (String) connectionProps.get("dbUrl");
		int tPoint = oraclePath.indexOf("@");
		if (tPoint > 0) {
			oracleTablespace = oraclePath.substring(0, tPoint);
		} else {
			oracleTablespace = null;
		}
		
		String connectionUrl = "jdbc:oracle:thin:" + oraclePath;
		try {
			Class.forName("oracle.jdbc.OracleDriver").newInstance(); //ensure oracle driver is loaded
			sqlConn = java.sql.DriverManager.getConnection(connectionUrl, (String) connectionProps.get("dbUser"),
				(String) connectionProps.get("dbPass"));
			
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
	
	/// As this is the base class varient, this funciton isnt suported
	public void recreate(boolean force) {
		if (force) {
			dispose();
		}
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Collumn type correction from mysql to oracle sql
	private static String _simpleMysqlToOracle_collumnSubstitude(String qString) {
		return qString.replaceAll("(?i)BIT", "RAW")
			.replaceAll("(?i)TINYBLOB", "RAW")
			//, RAW
			//.replaceAll("(?i)CHAR","CHAR")
			.replaceAll("(?i)DECIMAL", "NUMBER")
			.replaceAll("(?i)DOUBLE", "FLOAT(24)")
			.replaceAll("(?i)DOUBLE PRECISION", "FLOAT(24)")
			.replaceAll("(?i)REAL", "FLOAT (24)")
			//.replaceAll("(?i)FLOAT","FLOAT")
			.replaceAll("(?i)INTEGER", "INT")
			//.replaceAll("(?i)INT","NUMBER(10,0)")
			.replaceAll("(?i)BIGINT", "NUMBER(19, 0)")
			.replaceAll("(?i)MEDIUMINT", "NUMBER(7,0)")
			.replaceAll("(?i)SMALLINT", "NUMBER(5,0)")
			.replaceAll("(?i)TINYINT", "NUMBER(3,0)")
			.replaceAll("(?i)YEAR", "NUMBER")
			.replaceAll("(?i)NUMERIC", "NUMBER")
			.replaceAll("(?i)BLOB", "BLOB")
			.replaceAll("(?i)LONGBLOB", "BLOB")
			.replaceAll("(?i)MEDIUMBLOB", "BLOB")
			.replaceAll("(?i)LONGTEXT", "CLOB")
			.replaceAll("(?i)MEDIUMTEXT", "CLOB")
			.replaceAll("(?i)TEXT", "CLOB")
			//.replaceAll("(?i)DATE","DATE")
			.replaceAll("(?i)TIME", "DATE")
			.replaceAll("(?i)TIMESTAMP", "DATE")
			.replaceAll("(?i)DATETIME", "DATE")
			/*
			.replaceAll("(?i)ENUM(?=\\()","VARCHAR2")
			.replaceAll("(?i)ENUM(?!\\()","VARCHAR2(n)")
			.replaceAll("(?i)SET(?=\\()","VARCHAR2")
			.replaceAll("(?i)SET(?!\\()","VARCHAR2(n)")
			.replaceAll("(?i)TINYTEXT(?=\\()","VARCHAR2")
			.replaceAll("(?i)TINYTEXT(?!\\()","VARCHAR2(n)")
			 */
			.replaceAll("(?i)VARCHAR(?!\\()", "VARCHAR2(4000)")
			.replaceAll("(?i)VARCHAR\\(", "VARCHAR2(")
			.replaceAll("MAX","4000");
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
	
	final String ifExists = "IF EXISTS";
	final String ifNotExists = "IF NOT EXISTS";
	
	final String create = "CREATE";
	final String drop = "DROP";
	final String view ="VIEW";
	final String table = "TABLE";
	final String select = "SELECT";
	final String update = "UPDATE";
	
	final String insertInto = "INSERT INTO";
	final String deleteFrom = "DELETE FROM";
	
	final String[] indexTypeArr = { "UNIQUE", "FULLTEXT", "SPATIAL" };
	final String index = "INDEX";
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public String genericSqlParser(String inString) throws JSqlException {
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
				} 
				else {
					qStringPrefix = "DROP TABLE ";
				} 
				qString = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
			} else if (upperCaseStr.startsWith(view, prefixOffset)) { //VIEW
				prefixOffset += view.length() + 1;
				if (upperCaseStr.startsWith(ifExists, prefixOffset)) { //IF EXISTS
					prefixOffset += ifExists.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'DROP VIEW ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
				} 
				else {
					qStringPrefix = "DROP VIEW ";
				} 
				qString = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
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
			} else if (upperCaseStr.startsWith(view, prefixOffset)) { //VIEW
				prefixOffset += view.length() + 1;
				if (upperCaseStr.startsWith(ifNotExists, prefixOffset)) { //IF NOT EXISTS
					prefixOffset += ifNotExists.length() + 1;
					qStringPrefix = "BEGIN EXECUTE IMMEDIATE 'CREATE VIEW ";
					qStringSuffix = "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
				} else {
					qStringPrefix = "CREATE VIEW ";
				}
				qString = _fixTableNameInOracleSubQuery(fixedQuotes.substring(prefixOffset));
				qString = _simpleMysqlToOracle_collumnSubstitude(qString);

				int fromKeywordIndex = qString.indexOf("FROM") + "FROM".length();
				String qStringBeforeFromKeyword = qString.substring(0, fromKeywordIndex);
				// remove 'AS' keywords after table name
				String qStringAfterFromKeyword =  qString.substring(fromKeywordIndex, qString.length()).replaceAll("AS","");
				// replace double quotes (") with sinfle quotes
				qStringAfterFromKeyword = qStringAfterFromKeyword.replace("\"", "'");
				
				qString = qStringBeforeFromKeyword + qStringAfterFromKeyword;

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
						
						String tableAndColumns = tmpStr.substring(tmpIndx + " ON ".length());
						// check column's type
						String metaDataQuery = "SELECT "
							+ tableAndColumns.substring(tableAndColumns.indexOf("(") + 1, tableAndColumns.indexOf(")"))
							+ " FROM " + tableAndColumns.substring(0, tableAndColumns.indexOf("("));
						Map<String, String> metadata = null;
						try {
							metadata = getMetaData(metaDataQuery);
						} catch (JSqlException e) {
							//throw e;
						}
						if (metadata != null && !metadata.isEmpty()) {
							for (Map.Entry<String, String> entry : metadata.entrySet()) {
								if (entry.getValue() != null && entry.getValue().trim().toUpperCase().contains("LOB")) {
									throw new JSqlException("Cannot create index on expression with datatype LOB for field '"
										+ entry.getKey() + "'.");
								}
							}
						}
						
						if (tmpIndx > 0) {
							qString = "CREATE " + ((indexType != null) ? indexType + " " : "") + "INDEX "
								+ tmpStr.substring(0, tmpIndx) + " ON "
								+ _fixTableNameInOracleSubQuery(tmpStr.substring(tmpIndx + 4));
						}
						// check if column type is blob
						
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
				qString = "SELECT " 
				    + tmpStr.substring(0, tmpIndx - 7)
				    //.replaceAll("\"", "'")
				    .replaceAll("`", "\"")
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
			
			// Fix the pagination query as per the Oracle 12C
			// The Oracle 12C supports the pagination query with the OFFSET/FETCH keywords
			/*
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
			 */

			// Fix the pagination using the ROWNUM which is supported by versions below than Oracle 12C
			String prefixQuery = null;
			int startRowNum = -1;
			int endRowNum = -1;
			
			int limitIndex = qString.indexOf("LIMIT");
			if (limitIndex != -1) {
				prefixQuery = qString.substring(0, limitIndex);
			}
			
			if (prefixQuery != null) {
				limitIndex += "LIMIT".length();
				int offsetIndex = qString.indexOf("OFFSET");
				if (offsetIndex != -1) {
					startRowNum = Integer.parseInt(qString.substring(offsetIndex + "OFFSET".length()).trim());
					endRowNum = Integer.parseInt(qString.substring(limitIndex, offsetIndex).trim()) + startRowNum;
				}
			}
			if (startRowNum != -1 && endRowNum != -1) {
				qString = "SELECT * FROM (SELECT a.*, rownum AS rnum FROM (" + prefixQuery + ") a WHERE rownum <= "
					+ endRowNum + ") WHERE rnum > " + startRowNum;
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
	    qString = genericSqlParser(qString);
	    
	    String sequenceQuery = null;
	    String triggerQuery = null;
	    
	    // check if there is any AUTO INCREMENT field
	    if (qString.indexOf("AUTOINCREMENT") !=-1 || qString.indexOf("AUTO_INCREMENT") !=-1) {
    
    	    // Create sequence and trigger if it is CREATE TABLE query and has the AUTO INCREMENT column
    	    int prefixOffset = qString.indexOf(create);
    	    // check if create statement
    	    if (prefixOffset != -1) { //CREATE

    			prefixOffset += create.length() + 1;
    			
    			// check if create table statement
    			if (qString.startsWith(table, prefixOffset)) { //TABLE

    			    prefixOffset += table.length() + 1;
    			    
    		        // check if 'IF NOT EXISTS' exists in query
    	            if (qString.startsWith(ifNotExists, prefixOffset)) {
    					prefixOffset += ifNotExists.length() + 1;
    				}
    				
    		        // parse table name
            		String tableName = qString.substring(prefixOffset, qString.indexOf( "(", prefixOffset) );
            		tableName = tableName.replaceAll("\"", "").trim();
            		
            		prefixOffset += tableName.length();
                    //prefixOffset = qString.indexOf("(", prefixOffset) + 1;
                    
    		        // parse primary key column
    		   	    String primaryKeyColumn = "";
    		   	    
    		        String tmpStr = qString.substring( prefixOffset, qString.indexOf("PRIMARY KEY") ).trim();

    		   	    if (tmpStr.charAt(tmpStr.length() - 1) == ')') {
        				tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf("(")).trim();
        			}
        			// find last space
        			tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf(" ")).trim();
        			
        			for (int i = tmpStr.length() - 1; i >= 0; i--) {
        				// find space, comma or opening bracket
        				if (tmpStr.charAt(i) == ' ' || tmpStr.charAt(i) == ','
        						|| tmpStr.charAt(i) == '(') {
        					break;
        				}
        				primaryKeyColumn = tmpStr.charAt(i) + primaryKeyColumn;
        			}
    		   	    
    		   	    // create sequence sql query
    		        sequenceQuery = "CREATE SEQUENCE \""+tableName+"_SEQ\" START WITH 1001 INCREMENT BY 1 CACHE 10"; 
    		        
    		        // create trigger sql query
    		        triggerQuery = "CREATE OR REPLACE TRIGGER \""+tableName+"_TRIGGER\" "
    		                    + " BEFORE INSERT ON \""+tableName+"\" FOR EACH ROW " 
    		                    + " BEGIN SELECT \""+tableName+"_SEQ\".nextval INTO :NEW."+primaryKeyColumn+" FROM dual; END;";
    			}
    	    }
	    }
	    // Replace the AUTO INCREMENT with blank
	    qString = qString.replaceAll("AUTOINCREMENT", "");
	    qString = qString.replaceAll("AUTO_INCREMENT", "");

		//try {
		boolean retvalue = execute_raw(qString, values);

        //Create Sequence
        if (sequenceQuery != null) {
            execute_raw( genericSqlParser(sequenceQuery) );
        }

        //Create trigger
        if (triggerQuery != null) {
            execute_query( genericSqlParser(triggerQuery) );
        }

        return retvalue;
		//} catch(JSqlException e) {
		//	logger.log( Level.SEVERE, "Execute Exception" ); //, e 
		//	logger.log( Level.SEVERE, "-> Original query : " + qString );
		//	logger.log( Level.SEVERE, "-> Parsed query   : " + genericSqlParser(qString) );
		//	throw e;
		//}
	}
	
	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
	///
	/// The syntax below, is an example of such an UPSERT statement for Oracle.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// MERGE
	/// INTO Employee destTable
	/// USING (SELECT  
	///	1 id,      // Unique value
	/// 	'C3PO' name, // Insert value
	///	COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer') role, // Values with default
	///	(SELECT note FROM Employee WHERE id = 1) note // Misc values to preserve
	///	FROM DUAL
	/// ) sourceTable
	/// ON (destTable.id = sourceTable.id)
	/// WHEN NOT MATCHED THEN
	/// INSERT VALUES (
	///	sourceTable.id,      // Unique value
	/// 	sourceTable.name, // Insert value
	///	sourceTable.role, // Values with default
	///	sourceTable.note // Misc values to preserve
	/// )
	/// WHEN MATCHED THEN
	/// UPDATE
	/// SET     destTable.name = sourceTable.name, // Insert value
	///         destTable.role = sourceTable.role, // Values with default
	///         destTable.note = sourceTable.name.note // Misc values to preserve
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlQuerySet upsertQuerySet( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, // Columns names to apply default value, if not exists
		Object[] defaultValues, // Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
		//
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		String[] miscColumns //
	) throws JSqlException {
		
		if (tableName.length() > 30) {
			logger.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		/// Checks that unique collumn and values length to be aligned
		if (uniqueColumns == null || uniqueValues == null || uniqueColumns.length != uniqueValues.length) {
			throw new JSqlException("Upsert query requires unique column and values to be equal length");
		}
		
		/// Preparing inner default select, this will be used repeatingly for COALESCE, DEFAULT and MISC values
		ArrayList<Object> innerSelectArgs = new ArrayList<Object>();
		StringBuilder innerSelectSB = new StringBuilder(" FROM ");
		innerSelectSB.append("`" + tableName + "`");
		innerSelectSB.append(" WHERE ");
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
		
		String equalSign = "=";
		String targetTableAlias = "destTable";
		String sourceTableAlias = "srcTable";
		
		/// Building the query for INSERT OR REPLACE
		StringBuilder queryBuilder = new StringBuilder("MERGE INTO `" + tableName + "` ");
		queryBuilder.append(targetTableAlias);
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		
		/// Building the query for both sides of '(...columns...) VALUE (...vars...)' clauses in upsert
		/// Note that the final trailing ", " seperator will be removed prior to final query conversion
		StringBuilder selectColumnNames = new StringBuilder();
		StringBuilder updateColumnNames = new StringBuilder();
		StringBuilder insertColumnNames = new StringBuilder();
		StringBuilder insertColumnValues = new StringBuilder();
		StringBuilder condition = new StringBuilder();
		String columnSeperator = ", ";
		
		/// Setting up unique values
		for (int a = 0; a < uniqueColumns.length; ++a) {
			// dual select
			selectColumnNames.append("? ");
			selectColumnNames.append(uniqueColumns[a]);
			selectColumnNames.append(columnSeperator);
			
			queryArgs.add(uniqueValues[a]);
			
			// insert column names
			insertColumnNames.append(uniqueColumns[a]);
			insertColumnNames.append(columnSeperator);
			
			// insert column values
			insertColumnValues.append(sourceTableAlias);
			insertColumnValues.append(".");
			insertColumnValues.append(uniqueColumns[a]);
			insertColumnValues.append(columnSeperator);
		}
		
		/// Inserting updated values
		if (insertColumns != null) {
			for (int a = 0; a < insertColumns.length; ++a) {
				// insert column names
				insertColumnNames.append(insertColumns[a]);
				insertColumnNames.append(columnSeperator);
				
				// insert column values
				insertColumnValues.append(sourceTableAlias);
				insertColumnValues.append(".");
				insertColumnValues.append(insertColumns[a]);
				insertColumnValues.append(columnSeperator);
				
				// update column
				updateColumnNames.append(targetTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(insertColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append(sourceTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(insertColumns[a]);
				
				updateColumnNames.append(columnSeperator);
				
				// select dual
				selectColumnNames.append("? ");
				selectColumnNames.append(insertColumns[a]);
				selectColumnNames.append(columnSeperator);
				
				queryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a] : null);
				
			}
		}
		
		/// Handling default values
		if (defaultColumns != null) {
			for (int a = 0; a < defaultColumns.length; ++a) {
				// insert column names
				insertColumnNames.append(defaultColumns[a]);
				insertColumnNames.append(columnSeperator);
				
				// insert column values
				insertColumnValues.append(sourceTableAlias);
				insertColumnValues.append(".");
				insertColumnValues.append(defaultColumns[a]);
				insertColumnValues.append(columnSeperator);
				
				// update column
				updateColumnNames.append(targetTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(defaultColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append(sourceTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(defaultColumns[a]);
				
				// select dual
				// COALESCE((SELECT col3 from t where a=?), ?) as col3
				selectColumnNames.append("COALESCE(");
				selectColumnNames.append(innerSelectPrefix);
				selectColumnNames.append(defaultColumns[a]);
				selectColumnNames.append(innerSelectSuffix);
				selectColumnNames.append(", ?)");
				
				queryArgs.addAll(innerSelectArgs);
				
				selectColumnNames.append(defaultColumns[a]);
				selectColumnNames.append(columnSeperator);
				
				queryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
			}
		}
		
		/// Handling Misc values
		if (miscColumns != null) {
			for (int a = 0; a < miscColumns.length; ++a) {
				// insert column names
				insertColumnNames.append(miscColumns[a]);
				insertColumnNames.append(columnSeperator);

				// insert column values
				insertColumnValues.append(sourceTableAlias);
				insertColumnValues.append(".");
				insertColumnValues.append(miscColumns[a]);
				insertColumnValues.append(columnSeperator);
				
				// updtae column
				updateColumnNames.append(targetTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(miscColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append(sourceTableAlias);
				updateColumnNames.append(".");
				updateColumnNames.append(miscColumns[a]);
				
				// select dual
				selectColumnNames.append(innerSelectPrefix);
				selectColumnNames.append(miscColumns[a]);
				selectColumnNames.append(innerSelectSuffix);
				
				selectColumnNames.append(miscColumns[a]);
				selectColumnNames.append(columnSeperator);
				
				queryArgs.addAll(innerSelectArgs);
				
			}
		}
		
		/// Setting up the condition
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				condition.append(" and ");
			}
			condition.append(targetTableAlias);
			condition.append(".");
			condition.append(uniqueColumns[a]);
			condition.append(equalSign);
			condition.append(sourceTableAlias);
			condition.append(".");
			
			condition.append(uniqueColumns[a]);
		}
		
		/// Building the final query
		
		queryBuilder.append(" USING (SELECT ");
		queryBuilder.append(selectColumnNames.substring(0, selectColumnNames.length() - columnSeperator.length()));
		queryBuilder.append(" FROM DUAL");
		queryBuilder.append(") ");
		queryBuilder.append(sourceTableAlias);
		queryBuilder.append(" ON (");
		queryBuilder.append(condition.toString());
		queryBuilder.append(") ");
		queryBuilder.append(" WHEN MATCHED THEN");
		queryBuilder.append(" UPDATE SET ");
		queryBuilder.append(updateColumnNames.substring(0, updateColumnNames.length() - columnSeperator.length()));
		queryBuilder.append(" WHEN NOT MATCHED THEN");
		queryBuilder.append(" INSERT ( ");
		queryBuilder.append(insertColumnNames.substring(0, insertColumnNames.length() - columnSeperator.length()));
		queryBuilder.append(" ) VALUES (");
		queryBuilder.append(insertColumnValues.substring(0, insertColumnValues.length() - columnSeperator.length()));
		queryBuilder.append(")");
		
		return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
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
		return upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues, null, null, null);
	}

}