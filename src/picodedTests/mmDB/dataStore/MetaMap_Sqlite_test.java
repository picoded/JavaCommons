package picodedTests.mmDB.dataStore;

// Target test class
import picoded.jSql.*;
import picoded.mmDB.dataStore.MetaMap_JSql;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;

// Various utility includes
import java.util.Random;

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
	
	static int basicTestIterations = 15;
	
	/// Test basic put/get keyValue
	@Test
	public void basicPutGetKeyValue() throws JSqlException {
		Random rObj = new Random();
		
		assertNull("No value test", mmObj.getKeyValue("hello", "world", 0));
		
		assertTrue("String value test", mmObj.putKeyValue("hello", "world", 0, "domination"));
		assertEquals("String value test", "domination", mmObj.getKeyValue("hello", "world", 0));
		
		assertTrue("null value test", mmObj.putKeyValue("hello", "world", 0, null));
		assertNull("null value test", mmObj.getKeyValue("hello", "world", 0));
		
		int tmp_int;
		long tmp_long;
		double tmp_double;
		float tmp_float;
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_int = rObj.nextInt();
			
			assertTrue("Int value test", mmObj.putKeyValue("hello", "world", 0, tmp_int));
			assertEquals("Int value test", (long) tmp_int, mmObj.getKeyValue("hello", "world", 0));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_long = rObj.nextLong();
			
			assertTrue("Long value test", mmObj.putKeyValue("hello", "world", 0, tmp_long));
			assertEquals("Long value test", tmp_long, mmObj.getKeyValue("hello", "world", 0));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_double = rObj.nextDouble() + rObj.nextInt();
			
			assertTrue("Double value test", mmObj.putKeyValue("hello", "world", 0, tmp_double));
			assertEquals("Double value test", tmp_double, ((Double) mmObj.getKeyValue("hello", "world", 0)).doubleValue(),
				0.000000001);
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_float = rObj.nextFloat() + rObj.nextInt();
			
			assertTrue("Float value test", mmObj.putKeyValue("hello", "world", 0, tmp_float));
			assertEquals("Float value test", tmp_float, ((Float) mmObj.getKeyValue("hello", "world", 0)).floatValue(),
				0.000000001);
		}
	}
}