package picodedTests.mmData.dataStore;

// Target test class
import picoded.jSql.*;
import picodedTests.mmData.dataStore.MetaMap_Sqlite_test;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;

///
/// Test Case for picoded.mmData.MetaMap (using mysql)
///
public class MetaMap_Mssql_test extends MetaMap_Sqlite_test {
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(), TestConfig.MSSQL_USER(), TestConfig
			.MSSQL_PASS());
		commonSetUp();
	}
}
