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

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.JSql.JSqlType;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlException;

import picoded.JSql.*;
import picoded.JSql.db.BaseInterface;

/// Pure SQL Server 2012 implentation of JSql
/// Support only for SQL Server 2012 and above version for the pagination query, the OFFSET / FETCH keywords
/// are used which are faster and better in performance in comparison of old ROW_NUMBER()
public class JSql_Mssql extends JSql {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Mssql.class.getName());
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		sqlType = JSqlType.mssql;
		
		// store database connection properties
		setConnectionProperties(dbUrl, dbName, dbUser, dbPass, null);
		
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
		
		final String view = "VIEW";
		
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
			
			} else if (upperCaseStr.startsWith(view, prefixOffset)) { //VIEW
				prefixOffset += view.length() + 1;
				
				if (upperCaseStr.startsWith(ifExists, prefixOffset)) { //IF EXISTS
					prefixOffset += ifExists.length() + 1;
					
					qStringPrefix = "BEGIN TRY IF OBJECT_ID('" + fixedQuotes.substring(prefixOffset).toUpperCase()
						+ "', 'V')" + " IS NOT NULL DROP VIEW " + fixedQuotes.substring(prefixOffset)
						+ " END TRY BEGIN CATCH END CATCH";
				} else {
					qStringPrefix = "DROP VIEW ";
				}
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
		
		if (qString.contains("CREATE TABLE")) {
			// Replace PRIMARY KEY AUTOINCREMENT with IDENTITY
			if (qString.contains("AUTOINCREMENT")) {
				qString = qString.replaceAll("AUTOINCREMENT", "IDENTITY");
			}
			
			//Convert MY-Sql NUMBER data type to NUMERIC data type for Ms-sql
			if (qString.contains("NUMBER")) {
				qString = qString.replaceAll("NUMBER", "NUMERIC");
			}
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
	/// INTO Employee AS destTable
	/// USING (SELECT  
	///	1 AS id,      // Unique value
	/// 	'C3PO' AS name, // Insert value
	///	COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer') AS role, // Values with default
	///	(SELECT note FROM Employee WHERE id = 1) AS note // Misc values to preserve
	/// ) AS sourceTable
	/// ON (destTable.id = sourceTable.id)
	/// WHEN MATCHED THEN
	/// INSERT (
	///	id,     // Unique Columns to check for upsert
	///	name,   // Insert Columns to update
	///	role,   // Default Columns, that has default fallback value
	///   note,   // Misc Columns, which existing values are preserved (if exists)
	/// ) VALUES (
	///	1,      // Unique value
	/// 	'C3PO', // Insert value
	///	COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer'), // Values with default
	///	(SELECT note FROM Employee WHERE id = 1) // Misc values to preserve
	/// )
	/// WHEN NOT MATCHED THEN
	/// UPDATE
	/// SET     name = 'C3PO', // Insert value
	///         role = COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer'), // Values with default
	///         note = (SELECT note FROM Employee WHERE id = 1) // Misc values to preserve
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
		String targetTableAlias = "target";
		String sourceTableAlias = "source";
		String statementTerminator = ";";
		
		/// Building the query for INSERT OR REPLACE
		StringBuilder queryBuilder = new StringBuilder("MERGE INTO `" + tableName + "` AS " + targetTableAlias);
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		ArrayList<Object> insertQueryArgs = new ArrayList<Object>();
		ArrayList<Object> updateQueryArgs = new ArrayList<Object>();
		ArrayList<Object> selectQueryArgs = new ArrayList<Object>();
		
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
			selectColumnNames.append("?");
			selectColumnNames.append(" AS ");
			selectColumnNames.append(uniqueColumns[a]);
			selectColumnNames.append(columnSeperator);
			
			selectQueryArgs.add(uniqueValues[a]);
			
			// insert column list
			insertColumnNames.append(uniqueColumns[a]);
			insertColumnNames.append(columnSeperator);
			// insert column value list
			insertColumnValues.append("?");
			insertColumnValues.append(columnSeperator);
			//
			insertQueryArgs.add(uniqueValues[a]);
		}
		
		/// Inserting updated values
		if (insertColumns != null) {
			for (int a = 0; a < insertColumns.length; ++a) {
				// update column
				updateColumnNames.append(insertColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append("?");
				updateColumnNames.append(columnSeperator);
				
				updateQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a] : null);
				
				// select dual
				selectColumnNames.append("?");
				selectColumnNames.append(" AS ");
				selectColumnNames.append(insertColumns[a]);
				selectColumnNames.append(columnSeperator);
				
				selectQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a] : null);
				
				// insert column
				insertColumnNames.append(insertColumns[a]);
				insertColumnNames.append(columnSeperator);
				
				insertColumnValues.append("?");
				insertColumnValues.append(columnSeperator);
				
				insertQueryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a] : null);
			}
		}
		
		/// Handling default values
		if (defaultColumns != null) {
			for (int a = 0; a < defaultColumns.length; ++a) {
				// insert column
				insertColumnNames.append(defaultColumns[a]);
				insertColumnNames.append(columnSeperator);
				
				insertColumnValues.append("COALESCE(");
				insertColumnValues.append(innerSelectPrefix);
				insertColumnValues.append(defaultColumns[a]);
				insertColumnValues.append(innerSelectSuffix);
				
				insertQueryArgs.addAll(innerSelectArgs);
				
				insertColumnValues.append(", ?)");
				insertColumnValues.append(columnSeperator);
				
				insertQueryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
				
				// update column
				updateColumnNames.append(defaultColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append("COALESCE(");
				updateColumnNames.append(innerSelectPrefix);
				updateColumnNames.append(defaultColumns[a]);
				updateColumnNames.append(innerSelectSuffix);
				
				updateQueryArgs.addAll(innerSelectArgs);
				
				updateColumnNames.append(", ?)");
				updateColumnNames.append(columnSeperator);
				updateQueryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
				
				// select dual
				// COALESCE((SELECT col3 from t where a=?), ?) as col3
				selectColumnNames.append("COALESCE(");
				selectColumnNames.append(innerSelectPrefix);
				selectColumnNames.append(defaultColumns[a]);
				selectColumnNames.append(innerSelectSuffix);
				selectColumnNames.append(", ?)");
				
				selectQueryArgs.addAll(innerSelectArgs);
				
				selectColumnNames.append(" AS " + defaultColumns[a] + columnSeperator);
				selectQueryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
			}
		}
		
		/// Handling Misc values
		if (miscColumns != null) {
			for (int a = 0; a < miscColumns.length; ++a) {
				// insert column
				insertColumnNames.append(miscColumns[a]);
				insertColumnNames.append(columnSeperator);
				
				insertColumnValues.append(innerSelectPrefix);
				insertColumnValues.append(miscColumns[a]);
				insertColumnValues.append(innerSelectSuffix);
				
				insertQueryArgs.addAll(innerSelectArgs);
				
				insertColumnValues.append(columnSeperator);
				
				// updtae column
				updateColumnNames.append(miscColumns[a]);
				updateColumnNames.append(equalSign);
				updateColumnNames.append(innerSelectPrefix);
				updateColumnNames.append(miscColumns[a]);
				updateColumnNames.append(innerSelectSuffix);
				updateColumnNames.append(columnSeperator);
				
				updateQueryArgs.addAll(innerSelectArgs);
				
				// select dual
				selectColumnNames.append(innerSelectPrefix);
				selectColumnNames.append(miscColumns[a]);
				selectColumnNames.append(innerSelectSuffix);
				
				selectColumnNames.append(" AS ");
				selectColumnNames.append(miscColumns[a]);
				selectColumnNames.append(columnSeperator);
				
				selectQueryArgs.addAll(innerSelectArgs);
				
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
		queryBuilder.append(")");
		queryBuilder.append(" AS ");
		queryBuilder.append(sourceTableAlias);
		queryBuilder.append(" ON ( ");
		queryBuilder.append(condition.toString());
		queryBuilder.append(" ) ");
		queryBuilder.append(" WHEN MATCHED ");
		queryBuilder.append(" THEN UPDATE SET ");
		queryBuilder.append(updateColumnNames.substring(0, updateColumnNames.length() - columnSeperator.length()));
		queryBuilder.append(" WHEN NOT MATCHED ");
		queryBuilder.append(" THEN INSERT (");
		queryBuilder.append(insertColumnNames.substring(0, insertColumnNames.length() - columnSeperator.length()));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(insertColumnValues.substring(0, insertColumnValues.length() - columnSeperator.length()));
		queryBuilder.append(")");
		queryBuilder.append(statementTerminator);
		
		queryArgs.addAll(selectQueryArgs);
		queryArgs.addAll(updateQueryArgs);
		queryArgs.addAll(insertQueryArgs);
		
		//System.out.println("JSql -> upsertQuerySet -> query : " + queryBuilder.toString());
		//System.out.println("JSql -> upsertQuerySet -> queryArgs : " + queryArgs);
		
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