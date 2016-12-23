package picodedTests.JSql;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picodedTests.TestConfig;

public class JSql_Demo_test {
	
	//
	// Various JSql implmentation to test
	//
	protected static String testTableName = "JSqlTestDemo_default";
	
	protected static JSql sqlite = null;
	protected static JSql my_sql = null;
	protected static JSql ms_sql = null;
	protected static JSql oracle = null;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = "JSqlTestDemo_" + TestConfig.randomTablePrefix();
		testTableName = testTableName.toUpperCase();
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	@Before
	public void setUp() {
		sqlite = JSql.sqlite();
		my_sql = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
		ms_sql = JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(),
			TestConfig.MSSQL_USER(), TestConfig.MSSQL_PASS());
		oracle = JSql.oracle(TestConfig.ORACLE_PATH(), TestConfig.ORACLE_USER(),
			TestConfig.ORACLE_PASS());
	}
	
	//
	// Demostartion run for JSQL, and its common conversions
	//
	// Written by: Eugene =)
	//
	@Test
	public void demoRun() throws JSqlException {
		
		//------------------------------------------------------------------------------
		// `DROP TABLE IF EXISTS [tablename]`
		//------------------------------------------------------------------------------
		
		//
		// Lets start with making sure any existing tables for the demo is removed
		// Specifically the "IF EXISTS" clause
		//
		//    `DROP TABLE IF EXISTS [tablename]`
		//
		// Native support: MySQL, SQLite
		//
		sqlite.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
		my_sql.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
		
		//
		// MSSQL specific
		//
		// Things start getting tricky in MSSQL (unless ur using MSSQL 2016+)
		// Where the IF clause is now different (assuming non-temp table)
		//
		// JSQL will automatically convert it to the following
		//
		//    ```
		//    IF OBJECT_ID('[tablename]', 'U') IS NOT NULL
		//       DROP TABLE [tablename]
		//    ```
		//
		// Note that JSQL automatically assumes "dbo." namespace for tablename
		//
		ms_sql.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
		
		//
		// ORACLE specific
		//
		// And gosh does the IF clause workaround start getting unreadable in oracle
		//
		//    ```
		//    BEGIN
		//       EXECUTE IMMEDIATE 'DROP TABLE [tablename]';
		//    EXCEPTION
		//       WHEN OTHERS THEN
		//          IF SQLCODE != -942 THEN
		//             RAISE;
		//          END IF;
		//       END;
		//    ```
		//
		// I will be honest here, oracle is the main reason why i created this class
		// Because I do not walk around rembering the SQLCODE exception for 
		// `ORA-00942: table or view does not exist`
		//
		// Nor did I expect all my other developers who are NOT trained in oracle sql
		// to be able to support this intuitively.
		//
		oracle.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
		
		//------------------------------------------------------------------------------
		// `CREATE TABLE IF NOT EXISTS [tablename] ( [... collumn definitions ...] )`
		//------------------------------------------------------------------------------
		
		//
		// After clearing any existing table, we move on to creating the table itself
		// Once again with the IF clause, as "IF NOT EXISTS".
		//
		// An example is also given for its usage using in built functions
		//
		//    ```
		//    CREATE TABLE IF NOT EXISTS [tablename] ( 
		//       col1 INT PRIMARY KEY, col2 BIGINT, 
		//       col3 TEXT, col4 VARCHAR(50)
		//    )
		//    ```
		//
		// Native support: MySQL, SQLite
		//
		sqlite.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( "
			+ "col1 INT PRIMARY KEY, col2 BIGINT, " + "col3 TEXT, col4 VARCHAR(50) " + ")");
		my_sql.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( "
			+ "col1 INT PRIMARY KEY, col2 BIGINT, " + "col3 TEXT, col4 VARCHAR(50) " + ")");
		
		sqlite.createTableQuerySet(testTableName, new String[] { "col1", "col2", "col3", "col4" },
			new String[] { "INT PRIMARY KEY", "BIGINT", "TEXT", "VARCHAR(50)" }).execute();
		my_sql.createTableQuerySet(testTableName, new String[] { "col1", "col2", "col3", "col4" },
			new String[] { "INT PRIMARY KEY", "BIGINT", "TEXT", "VARCHAR(50)" }).execute();
		
		//
		// MSSQL specific
		//
		// Once again the IF clause
		// 
		//    ```
		//    IF NOT EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'[tablename]') 
		//       AND OBJECTPROPERTY(id, N'[tablename]') = 1) 
		//    BEGIN
		//       CREATE TABLE [tablename] (
		//          col1 INT PRIMARY KEY, col2 BIGINT, 
		//          col3 TEXT, col4 VARCHAR(50)
		//       )
		//    END
		//    ```
		//
		ms_sql.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( "
			+ "col1 INT PRIMARY KEY, col2 BIGINT, " + "col3 TEXT, col4 VARCHAR(50) " + ")");
		
		ms_sql.createTableQuerySet(testTableName, new String[] { "col1", "col2", "col3", "col4" },
			new String[] { "INT PRIMARY KEY", "BIGINT", "TEXT", "VARCHAR(50)" }).execute();
		
		//
		// ORACLE specific
		//
		// Antes up the game, with the lack of many collumn type direct support,
		// which is then automatically replaced with its closest equivalent
		//
		//    ```
		//    BEGIN
		//       EXECUTE IMMEDIATE 'CREATE TABLE [tablename] ( 
		//          col1 INT PRIMARY KEY, col2 NUMBER(19, 0),
		//          col3 TEXT, col4 VARCHAR2(50)
		//       )';
		//    EXCEPTION
		//       WHEN OTHERS THEN
		//          IF SQLCODE != -955 THEN
		//             RAISE;
		//          END IF;
		//       END;
		//    ```
		//
		oracle.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( "
			+ "col1 INT PRIMARY KEY, col2 BIGINT, " + "col3 TEXT, col4 VARCHAR(50) " + ")");
		oracle.createTableQuerySet(testTableName, new String[] { "col1", "col2", "col3", "col4" },
			new String[] { "INT PRIMARY KEY", "BIGINT", "TEXT", "VARCHAR(50)" }).execute();
		
	}
	
	//
	// Clean up script, cause this is a shared test DB. Wouldnt want to pollute it.
	//
	@After
	public void tearDown() throws JSqlException {
		if (sqlite != null) {
			sqlite.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
			my_sql.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
			ms_sql.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
			oracle.execute("DROP TABLE IF EXISTS `" + testTableName + "`");
			
			sqlite.dispose();
			my_sql.dispose();
			ms_sql.dispose();
			oracle.dispose();
			
			sqlite = null;
			my_sql = null;
			ms_sql = null;
			oracle = null;
		}
	}
	
}
