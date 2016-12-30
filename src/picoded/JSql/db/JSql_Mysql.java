package picoded.JSql.db;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlQuerySet;
import picoded.JSql.JSqlResult;
import picoded.enums.JSqlType;

/// Pure MySQL implentation of JSql
public class JSql_Mysql extends JSql {
	
	/// Internal self used logger
	//	private static final Logger LOGGER = Logger.getLogger(JSql_Mysql.class.getName());
	
	/// Runs JSql with the JDBC "MY"SQL engine
	///
	/// **Note:** dbServerAddress, is just IP:PORT. For example, "127.0.0.1:3306"
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
	///
	/// **Note:** connectionUrl, for example, "jdbc:mysql://54.169.34.78:3306/JAVACOMMONS"
	public JSql_Mysql(String connectionUrl, Properties connectionProps) {
		// store database connection properties
		setConnectionProperties(connectionUrl, null, null, null, connectionProps);
		
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
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
	
	/// Internal parser that converts some of the common sql statements to mysql
	public String genericSqlParser(String inString) {
		String qString = inString.toUpperCase(Locale.ENGLISH);
		qString = inString.trim().replaceAll("(\\s){1}", " ").replaceAll("\\s+", " ")
			.replaceAll("\"", "`")
			//.replaceAll("\'", "`")
			.replaceAll("AUTOINCREMENT", "AUTO_INCREMENT").replace("VARCHAR(MAX)", "TEXT");
		
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
	public boolean execute(String qString, Object... values) throws JSqlException, SQLException {
		qString = genericSqlParser(qString);
		String qStringUpper = qString.toUpperCase(Locale.ENGLISH);
		/// MySQL does not support the inner query in create view
		/// Check if create view query has an inner query.
		/// If yes, create a view from the inner query and replace the inner query with created view.
		if (qStringUpper.contains("CREATE VIEW")) {
			// get view name
			int indexAs = qStringUpper.indexOf("AS");
			String viewName = "";
			if (indexAs != -1) {
				viewName = qStringUpper.substring("CREATE VIEW".length(), indexAs);
			}
			// check if any inner query
			int indexOpeningBracket = -1;
			int indexFrom = qStringUpper.indexOf("FROM") + "FROM".length();
			// find if next char is bracket
			for (int i = indexFrom; i < qStringUpper.length(); i++) {
				if (qStringUpper.charAt(i) == ' ') {
					continue;
				}
				if (qStringUpper.charAt(i) == '(') {
					indexOpeningBracket = i;
				} else {
					break;
				}
			}
			
			int indexClosingIndex = -1;
			String tmpViewName = null;
			String createViewQuery = null;
			if (indexOpeningBracket != -1) {
				tmpViewName = viewName;
				if (viewName.indexOf('_') != -1) {
					tmpViewName = viewName.split("_")[0];
				}
				tmpViewName += "_inner_view";
				indexClosingIndex = qStringUpper.indexOf(')', indexOpeningBracket);
				
				String innerQuery = qStringUpper.substring(indexOpeningBracket + 1, indexClosingIndex);
				createViewQuery = "CREATE VIEW " + tmpViewName + " AS " + innerQuery;
			}
			
			if (createViewQuery != null) {
				// execute query to drop the view if exist
				String dropViewQuery = "DROP VIEW IF EXISTS " + tmpViewName;
				
				execute_raw(genericSqlParser(dropViewQuery));
				
				/// execute the query to create view
				execute_raw(genericSqlParser(createViewQuery));
				
				// replace the inner query with created view name
				qStringUpper = qStringUpper.substring(0, indexFrom) + tmpViewName
					+ qStringUpper.substring(indexClosingIndex + 1);
			}
		}
		// Possible "INDEX IF NOT EXISTS" call for mysql, suppress duplicate index error if needed
		//
		// This is a work around for MYSQL not supporting the "CREATE X INDEX IF NOT EXISTS" syntax
		//
		if (qStringUpper.indexOf("INDEX IF NOT EXISTS") != -1) {
			// index conflict try catch
			try {
				qStringUpper = qStringUpper.replaceAll("INDEX IF NOT EXISTS", "INDEX");
				
				// It is must to define the The length of the BLOB and TEXT column type
				// Append the maximum length "333" to BLOB and TEXT columns
				// Extract the table name and the columns from the sql statement i.e. 
				// "CREATE UNIQUE INDEX `JSQLTEST_UNIQUE` ON `JSQLTEST` ( COL1, COL2, COL3 )"
				// Find the "ON" word index
				int onIndex = qStringUpper.indexOf("ON");
				// if index == -1 then it is not a valid sql statement
				if (onIndex != -1) {
					// subtract the table name and columns from the sql statement string
					String tableAndColumnsName = qStringUpper.substring(onIndex + "ON".length());
					// Find the index of opening bracket index.
					// The column names will be enclosed between the opening and closing bracket
					// And table name will be before the opening bracket
					int openBracketIndex = tableAndColumnsName.indexOf('(');
					if (openBracketIndex != -1) {
						// extract the table name which is till the opening bracket
						String tablename = tableAndColumnsName.substring(0, openBracketIndex);
						// find the closing bracket index
						int closeBracketIndex = tableAndColumnsName.lastIndexOf(')');
						// extract the columns between the opening and closing brackets
						String columns = tableAndColumnsName.substring(openBracketIndex + 1,
							closeBracketIndex);
						// fetch the table meta data info
						JSqlResult jsql = executeQuery_metadata(tablename.trim());
						Map<String, String> metadata = jsql.fetchMetaData();
						qStringUpper = getQStringUpper(metadata, qStringUpper, columns);
					}
				}
				return execute_raw(qStringUpper);
			} catch (JSqlException e) {
				if (e
					.getCause()
					.toString()
					.indexOf(
						"com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Duplicate key name '") == -1) {
					// throws as its not a duplicate key exception
					throw e;
				}
				return true;
			}
		}
		return execute_raw(qStringUpper, values);
	}
	
	public String getQStringUpper(Map<String, String> metadata, String qStringUpper, String columns) {
		if (metadata != null) {
			String[] columnsArr = columns.split(",");
			for (String column : columnsArr) {
				column = column.trim();
				// check if column type is BLOB or TEXT
				if ("BLOB".equals(metadata.get(column)) || "TEXT".equals(metadata.get(column))) {
					// repalce the column name in the origin sql statement with column name and suffic "(333)
					qStringUpper = qStringUpper.replace(column, column + "(333)");
				}
			}
		}
		return qStringUpper;
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
		return upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			null, null, null);
	}
}
