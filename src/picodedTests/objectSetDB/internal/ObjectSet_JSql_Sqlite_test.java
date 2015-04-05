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
public class ObjectSet_JSql_sqlite_test {
	
	protected static String testTableName = "mmData-metaMap";
	
	protected JSql JSqlObj = null;
	protected ObjectSet_JSql objSetJSql = null;
	
	///
	/// Setsup the test case hazelcast instance
	///
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = TestConfig.randomTablePrefix();
	}
	
	///
	/// Shutsdown the test case hazelcast instance
	///
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	protected void commonSetUp() throws JSqlException {
		objSetJSql = new ObjectSet_JSql(JSqlObj, testTableName);
		assertNotNull(objSetJSql.tableSetup());
	}
	
	/// This function is to be overriden for the various JSQL implementation
	public void setUpDB() {
		JSqlObj = JSql.sqlite();
	}
	
	@Before
	public void setUp() throws JSqlException {
		setUpDB();
		commonSetUp();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (objSetJSql != null) {
			objSetJSql.tableDrop();
		}
		if (JSqlObj != null) {
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose();
			JSqlObj = null;
		}
	}
	
	/// Test the object constructor
	@Test
	public void constructor() throws JSqlException {
		assertNotNull(objSetJSql);
		assertEquals(testTableName, objSetJSql.tableName());
		assertEquals(JSqlObj, objSetJSql.JSqlObject());
		
		assertNotNull(objSetJSql.tableSetup());
	}
	
	/// Iteration count
	static int basicTestIterations = 15;
	
	/// Test basic put/get keyValue
	@Test
	public void basicPutget() throws JSqlException {
		Random rObj = new Random();
		
		assertNull("No value test", objSetJSql.get("hello", "world", 0));
		
		assertTrue("String value test", objSetJSql.put("hello", "world", 0, "domination"));
		assertEquals("String value test", "domination", objSetJSql.get("hello", "world", 0));
		
		assertTrue("null value test", objSetJSql.put("hello", "world", 0, null));
		assertNull("null value test", objSetJSql.get("hello", "world", 0));
		
		int tmp_int;
		long tmp_long;
		double tmp_double;
		float tmp_float;
		
		double accuracy = 0.000000001;
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_int = rObj.nextInt();
			
			assertTrue("String value test", objSetJSql.put("hello", "world", 0, "s" + tmp_int));
			assertEquals("String value test", "s" + tmp_int, objSetJSql.get("hello", "world", 0));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_int = rObj.nextInt();
			
			assertTrue("Int value test", objSetJSql.put("hello", "world", 0, tmp_int));
			assertEquals("Int value test", (long) tmp_int, objSetJSql.get("hello", "world", 0));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_long = rObj.nextLong();
			
			assertTrue("Long value test", objSetJSql.put("hello", "world", 0, tmp_long));
			assertEquals("Long value test", tmp_long, objSetJSql.get("hello", "world", 0));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_double = rObj.nextDouble() + rObj.nextInt();
			
			assertTrue("Double value test", objSetJSql.put("hello", "world", 0, tmp_double));
			assertEquals("Double value test", tmp_double, ((Double) objSetJSql.get("hello", "world", 0)).doubleValue(),
				accuracy);
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_float = rObj.nextFloat() + rObj.nextInt();
			
			assertTrue("Float value test", objSetJSql.put("hello", "world", 0, tmp_float));
			assertEquals("Float value test", tmp_float, ((Float) objSetJSql.get("hello", "world", 0)).floatValue(),
				accuracy);
		}
	}
	// */
}
