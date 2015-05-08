package picodedTests.JSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.JSql.*;
import picodedTests.JSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
}
