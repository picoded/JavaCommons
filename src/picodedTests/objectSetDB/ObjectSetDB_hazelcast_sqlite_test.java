package picodedTests.objectSetDB;

// Target test class
import picoded.objectSetDB.*;
import picoded.jSql.*;
import picoded.jCache.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

//
import java.util.Map;
import java.util.HashMap;

///
/// Test Case for picoded.objectSetDB.ObjectSetDB
///
public class ObjectSetDB_hazelcast_sqlite_test extends ObjectSetDB_sqlite_test {
	
	// Setsup the object set DB with JSqlite
	public void setupObjectSetDB() {
		OSDB = new ObjectSetDB(JCache.hazelcast(LocalCacheSetup.setupHazelcastServer()), JSql.sqlite());
	}
	
	@AfterClass
	public static void teardownDB() {
		// one-time cleanup code
		LocalCacheSetup.teardownRedisServer();
		LocalCacheSetup.teardownHazelcastServer();
	}
}
