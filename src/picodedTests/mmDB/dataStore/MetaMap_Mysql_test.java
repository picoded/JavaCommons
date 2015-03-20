package picodedTests.mmDB.dataStore;

// Target test class
import picoded.jSql.*;
import picodedTests.mmDB.dataStore.MetaMap_Sqlite_test;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;

///
/// Test Case for picoded.mmDB.MetaMap (using mysql)
///
public class MetaMap_Mysql_test extends MetaMap_Sqlite_test {
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(), TestConfig
			.MYSQL_PASS());
		commonSetUp();
	}
}