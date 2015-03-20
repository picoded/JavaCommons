package picodedTests.mmDB.dataStore;

// Target test class
import picoded.jSql.*;
import picoded.mmDB.MetaMap;
import picodedTests.mmDB.dataStore.MetaMap_Sqlite_test;

// Hazelcast testing support
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;

///
/// Test Case for picoded.mmDB.MetaMap (using mysql)
///
public class MetaMap_Mssql_test extends MetaMap_Sqlite_test {
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(), TestConfig.MSSQL_USER(), TestConfig
			.MSSQL_PASS());
		commonSetUp();
	}
}