package picodedTests.jSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.jSql.*;
import picodedTests.jSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
}
