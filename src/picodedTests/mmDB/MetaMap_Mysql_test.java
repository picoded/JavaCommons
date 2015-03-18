package picodedTests.mmDB;

// Target test class
import picoded.jSql.*;
import picoded.mmDB.MetaMap;
import picodedTests.mmDB.MetaMap_Sqlite_test;

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
public class MetaMap_Mysql_test extends MetaMap_Sqlite_test {
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(), TestConfig
			.MYSQL_PASS());
		commonSetUp();
	}
}