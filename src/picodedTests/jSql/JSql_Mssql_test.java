package picodedTests.jSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.jSql.*;
import picodedTests.jSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Mssql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(), TestConfig.MSSQL_USER(), TestConfig
			.MSSQL_PASS());
	}
}
