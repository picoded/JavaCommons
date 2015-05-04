package picodedTests.objectSetDB.internal;

// Target test class
import picoded.jSql.*;
import picoded.objectSetDB.internal.ObjectSet_JSql;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;

// Various utility includes
import java.util.Random;

///
/// Test Case for picoded.mmData.MetaMap (using in memory Sqlite)
///
public class ObjectSet_JSql_mysql_test extends ObjectSet_JSql_sqlite_test {
	
	/// This function is to be overriden for the various JSQL implementation
	public void setUpDB() {
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
}
