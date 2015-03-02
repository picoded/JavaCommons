package picodedTests.jSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.jSql.*;
import picoded.jSql.db.JSql_Oracle;
import picodedTests.jSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Oracle_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.oracle(TestConfig.ORACLE_PATH(), TestConfig.ORACLE_USER(), TestConfig.ORACLE_PASS());
	}
	
	@Test
	public void simpleMysqlToOracleParser() {
		assertEquals(
		   "BEGIN EXECUTE IMMEDIATE 'DROP TABLE \"JSQLTEST\"'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
		   JSql_Oracle.genericSqlParser("DROP TABLE IF EXISTS `jsqlTest`"));
		assertEquals(
		   "BEGIN EXECUTE IMMEDIATE 'CREATE TABLE \"JSQLTEST\" ( col1 INT PRIMARY KEY, col2 CLOB )'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;",
		   JSql_Oracle.genericSqlParser("CREATE TABLE IF NOT EXISTS `jsqlTest` ( col1 INT PRIMARY KEY, col2 TEXT )"));
		assertEquals("CREATE UNIQUE INDEX \"TESTMETATABLE_UNIQUE\" ON \"TESTMETATABLE\" (obj, metaKey)", JSql_Oracle
		   .genericSqlParser("CREATE UNIQUE INDEX `testMetaTable_unique` ON `testMetaTable` (obj, metaKey)"));
	}
	
}
