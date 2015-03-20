package picodedTests.mmDB.dataStore;

// Target test class
import picoded.jSql.*;
import picoded.mmDB.dataStore.MetaMap_JSql;

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
/// Test Case for picoded.mmDB.MetaMap (using in memory Sqlite)
///
public class MetaMap_Sqlite_test {
	
	protected static String testTableName = "mmDB-metaMap";
	
	protected JSql JSqlObj = null;
	protected MetaMap_JSql mmObj = null;
	
	///
	/// Setsup the test case hazelcast instance
	///
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = "mmDB-metaMap_" + TestConfig.randomTablePrefix();
	}
	
	///
	/// Shutsdown the test case hazelcast instance
	///
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	protected void commonSetUp() throws JSqlException {
		mmObj = new MetaMap_JSql(JSqlObj, testTableName);
		assertNotNull(mmObj.tableSetup());
	}
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.sqlite();
		commonSetUp();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (mmObj != null) {
			mmObj.tableDrop();
		}
		if (JSqlObj != null) {
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose();
			JSqlObj = null;
		}
	}
	
	/// Test the object constructor
	@Test
	public void constructor() throws JSqlException {
		assertNotNull(mmObj);
		assertEquals(testTableName, mmObj.tableName());
		assertEquals(JSqlObj, mmObj.JSqlObject());
		
		assertNotNull(mmObj.tableSetup());
	}
	
	/// Test basic put/get keyValue
	@Test
	public void basicPutGetKeyValue() throws JSqlException {
		assertTrue(mmObj.putKeyValue("hello", "world", 0, "domination"));
		assertEquals("domination", mmObj.getKeyValue("hello", "world", 0));
	}
	
}