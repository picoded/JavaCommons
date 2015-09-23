package picodedTests.JSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.JSql.*;
import picoded.JSql.db.JSql_Oracle;
import picodedTests.JSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Oracle_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.oracle(TestConfig.ORACLE_PATH(), TestConfig.ORACLE_USER(), TestConfig.ORACLE_PASS());
	}
	
	@Test
	public void simpleMysqlToOracleParser() throws JSqlException {
		assertEquals(
			"BEGIN EXECUTE IMMEDIATE 'DROP TABLE \"JSQLTEST\"'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
			JSqlObj.genericSqlParser("DROP TABLE IF EXISTS `jsqlTest`"));
		assertEquals(
			"BEGIN EXECUTE IMMEDIATE 'CREATE TABLE \"JSQLTEST\" ( col1 INT PRIMARY KEY, col2 CLOB )'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;",
			JSqlObj.genericSqlParser("CREATE TABLE IF NOT EXISTS `jsqlTest` ( col1 INT PRIMARY KEY, col2 TEXT )"));
		assertEquals("CREATE UNIQUE INDEX \"TESTMETATABLE_UNIQUE\" ON \"TESTMETATABLE\" (obj, metaKey)",
			JSqlObj.genericSqlParser("CREATE UNIQUE INDEX `testMetaTable_unique` ON `testMetaTable` (obj, metaKey)"));
	}
	
	/// Test if the "INDEX IF NOT EXISTS" clause is being handled correctly
	@Test
	public void uniqueIndexIfNotExists() throws JSqlException {
		executeQuery();
		
		JSqlException caughtException = null;
		try {
			assertTrue(
				"1st uniq index",
				JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `" + testTableName
					+ "` ( col1, col2 )"));
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		assertTrue(
			"2nd uniq index",
			JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `" + testTableName
				+ "` ( col3 )"));
	}
	
}
