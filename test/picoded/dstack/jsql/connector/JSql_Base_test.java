package picoded.dstack.jsql.connector;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.TestConfig;

///
/// Common base JSql Test case which is applied to various implmentation
///
public class JSql_Base_test {
	
	protected JSql jsqlObj;
	protected static String testTableName = "JSqlTest_" + TestConfig.randomTablePrefix().toUpperCase();
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = testTableName.toUpperCase();
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	@Before
	public void setUp() {
		jsqlObj = JSql.sqlite();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (jsqlObj != null) {
			jsqlObj.update("DROP TABLE IF EXISTS `" + testTableName + "`");
			jsqlObj = null;
		}
	}
	
	/// Simple constructor test
	@Test
	public void constructor() {
		assertNotNull(jsqlObj);
	}

	/// Simple raw query of creating, writing, reading, and deleting test
	/// This is considered the simplest minimal test flow
	@Test
	public void simpleQueryFlow_raw() {
		// Creating and inserting the result
		assertTrue( jsqlObj.update_raw("CREATE TABLE "+testTableName+" ( COL1 INTEGER )") );
		assertTrue( jsqlObj.update_raw("INSERT INTO "+testTableName+" VALUES (1)") );

		// Query and validating data
		JSqlResult res = null;
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );

		// Note that expected is in lower case, 
		// as results is stored in case insensitive hashmap
		Map<String,Object> expected = ConvertJSON.toMap("{ \"col1\" : [ 1 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );
		res.dispose();

		// Table cleanup
		assertTrue( jsqlObj.update_raw("DROP TABLE "+testTableName+"") );
	}

	/// simpleQueryFlow, with built CREATE TABLE statement instead
	@Test
	public void simpleQueryFlow_createTable() {
		// Creating and inserting the result
		assertTrue( jsqlObj.createTable(testTableName, new String[] { "COL1" }, new String[] { "INTEGER" }) );
		assertTrue( jsqlObj.update_raw("INSERT INTO "+testTableName+" VALUES (1)") );

		// Query and validating data
		JSqlResult res = null;
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );

		// Note that expected is in lower case, 
		// as results is stored in case insensitive hashmap
		Map<String,Object> expected = ConvertJSON.toMap("{ \"col1\" : [ 1 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );
		res.dispose();

		// Table cleanup
		assertTrue( jsqlObj.update_raw("DROP TABLE "+testTableName+"") );
	}


	/// simpleQueryFlow, with built SELECT statement instead
	@Test
	public void simpleQueryFlow_select() {
		// Creating and inserting the result
		assertTrue( jsqlObj.update_raw("CREATE TABLE "+testTableName+" ( COL1 INTEGER )") );
		assertTrue( jsqlObj.update_raw("INSERT INTO "+testTableName+" VALUES (1)") );

		// Query and validating data
		JSqlResult res = null;
		assertNotNull( res = jsqlObj.select(testTableName, "*") );

		// Note that expected is in lower case, 
		// as results is stored in case insensitive hashmap
		Map<String,Object> expected = ConvertJSON.toMap("{ \"col1\" : [ 1 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );
		res.dispose();

		// Table cleanup
		assertTrue( jsqlObj.update_raw("DROP TABLE "+testTableName+"") );
	}

	/// simpleQueryFlow, with built UPSERT statement instead, modified with primary key
	@Test
	public void simpleQueryFlow_upsert() {
		// Creating and inserting the result
		assertTrue( jsqlObj.update_raw("CREATE TABLE "+testTableName+" ( COL1 INTEGER PRIMARY KEY )") );

		// Upserting only the primary key, rest is to be facilitated in other tests
		assertTrue( jsqlObj.upsert(testTableName, new String[] { "COL1" }, new Object[] { 1 }, null, null, null, null, null) );

		// Query and validating data
		JSqlResult res = null;
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );

		// Note that expected is in lower case, 
		// as results is stored in case insensitive hashmap
		Map<String,Object> expected = ConvertJSON.toMap("{ \"col1\" : [ 1 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );
		res.dispose();

		// Table cleanup
		assertTrue( jsqlObj.update_raw("DROP TABLE "+testTableName+"") );
	}

	/// Simple raw query of creating, writing, reading, and deleting test
	/// This is considered the simplest minimal test flow
	@Test
	public void simpleQueryFlow_delete() {
		// Creating and inserting the result
		assertTrue( jsqlObj.update_raw("CREATE TABLE "+testTableName+" ( COL1 INTEGER )") );
		assertTrue( jsqlObj.update_raw("INSERT INTO "+testTableName+" VALUES (1)") );

		// Query and validating data
		JSqlResult res = null;
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );
		assertEquals( 1, res.rowCount() );

		// Note that expected is in lower case, 
		// as results is stored in case insensitive hashmap
		Map<String,Object> expected = ConvertJSON.toMap("{ \"col1\" : [ 1 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );

		// Delete from the table
		assertTrue( jsqlObj.delete(testTableName) );

		// Check for no data
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );
		assertEquals( 0, res.rowCount() );

		// Reinsert a different data
		assertTrue( jsqlObj.update_raw("INSERT INTO "+testTableName+" VALUES (2)") );

		// Requery for new data strictly
		assertNotNull( res = jsqlObj.query_raw("SELECT * FROM "+testTableName+"") );
		assertEquals( 1, res.rowCount() );

		// Validate the result
		expected = ConvertJSON.toMap("{ \"col1\" : [ 2 ] }");
		assertEquals( ConvertJSON.fromMap(expected), ConvertJSON.fromMap(res) );

		// Table cleanup
		assertTrue( jsqlObj.update_raw("DROP TABLE "+testTableName+"") );
	}


	/// Create table if not exists test
	@Test
	public void createTableStatementBuilder() throws JSqlException {
		// cleanup (just incase)
		assertTrue( jsqlObj.update("DROP TABLE IF EXISTS `" + testTableName + "`") ); 
		
		// valid table creation : no exception
		assertTrue( jsqlObj.createTable(testTableName, new String[] { "col1", "col2" },
			new String[] { "INT PRIMARY KEY", "TEXT" }) ); 
		
		// run twice to ensure "IF NOT EXISTS" works
		assertTrue( jsqlObj.createTable(testTableName, new String[] { "col1", "col2" },
			new String[] { "INT PRIMARY KEY", "TEXT" }) ); 
		
		// Truncate call, (ensure no prior data)
		assertTrue(  jsqlObj.update("TRUNCATE TABLE " + testTableName + "") ); 
		
		// Data insertion
		assertTrue( jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
			"has nothing") );
	}
	
	/// This is the base execute sql test example, in which other examples are built on
	@Test
	public void updateStatements() throws JSqlException {
		// cleanup (just incase)
		assertTrue( jsqlObj.update("DROP TABLE IF EXISTS `" + testTableName + "`") ); 
		
		assertTrue(jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )")); //valid table creation : no exception
		assertTrue(jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )")); //run twice to ensure "IF NOT EXISTS" works
		
		// Truncate call, (ensure no prior data)
		assertTrue(  jsqlObj.update("TRUNCATE TABLE " + testTableName + "") ); 
		
		// Data insertion
		jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2, col3 ) VALUES (?,?,?)",
			404, "has nothing", "do nothing");
		
		// Drop non existent view
		jsqlObj.update("DROP VIEW IF EXISTS `" + testTableName + "_View`");
		
		// Create view
		jsqlObj.update(
			"CREATE VIEW " + testTableName + "_View AS  SELECT * FROM " + testTableName);
		
		// Drop created view
		jsqlObj.update("DROP VIEW IF EXISTS `" + testTableName + "_View`");
		
	}
	
	
	@Test
	public void update_expectedExceptions() throws JSqlException {
		// runs the no exception varient. to pre populate the tables for exceptions
		updateStatements(); 
		
		// Reduce exception level of sqlite library
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.SEVERE); //sets it to tolerate the error

		// The caught exception
		JSqlException caughtException = null;

		// Try creating an invalid table
		try {
			jsqlObj.update(
				"CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)"); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;

		// Try doing an invalid insertion
		try {
			jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
				"has nothing"); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		// Reset logging level
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	@Test
	public void JSqlResultFetch() throws JSqlException {
		updateStatements();
		
		// added more data to test
		jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello");
		jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world");
		
		JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
		r.fetchAllRows();
		
		assertEquals("via readRow", 404, r.readRow(0).getInt("col1"));
		assertEquals("via readRow", "has nothing", r.readRow(0).getString("col2"));
		assertEquals("via readRow", 405, r.readRow(1).getInt("col1"));
		assertEquals("via readRow", "hello", r.readRow(1).getString("col2"));
		assertEquals("via readRow", 406, r.readRow(2).getInt("col1"));
		assertEquals("via readRow", "world", r.readRow(2).getString("col2"));
		
		assertEquals("via get()[]", 404, ((Number) r.get("col1")[0]).intValue());
		assertEquals("via get()[]", "has nothing", r.get("col2")[0]);
		assertEquals("via get()[]", 405, ((Number) r.get("col1")[1]).intValue());
		assertEquals("via get()[]", "hello", r.get("col2")[1]);
		assertEquals("via get()[]", 406, ((Number) r.get("col1")[2]).intValue());
		assertEquals("via get()[]", "world", r.get("col2")[2]);
		
		r.dispose();
	}
	
	/// Test if the "INDEX IF NOT EXISTS" clause is being handled correctly
	@Test
	public void uniqueIndexIfNotExists() throws JSqlException {
		updateStatements();
		
		assertTrue(
			"1st uniq index",
			jsqlObj.update("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_uni1` ON `"
				+ testTableName + "` ( col1, col2 )"));
		
		assertTrue(
			"2nd uniq index",
			jsqlObj.update("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_uni2` ON `"
				+ testTableName + "` ( col3 )"));
	}
	
	public void row1to7setup() throws JSqlException {
		updateStatements();
		
		// added more data to test
		jsqlObj
			.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello");
		jsqlObj
			.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world");
		jsqlObj.update("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 407, "no.7");
	}
	
	@Test
	public void selectStatement() throws JSqlException {
		row1to7setup();
		
		JSqlResult r = null;
		
		// Select all as normal
		assertNotNull(r = jsqlObj.select(testTableName, null, null, null)); //select all
		assertNotNull("SQL result should return a result", r);
		r.fetchAllRows();
	
		assertEquals("via get()[]", 404, ((Number) r.get("col1")[0]).intValue());
		assertEquals("via get()[]", "has nothing", r.get("col2")[0]);
		assertEquals("via get()[]", 405, ((Number) r.get("col1")[1]).intValue());
		assertEquals("via get()[]", "hello", r.get("col2")[1]);
		assertEquals("via get()[]", 406, ((Number) r.get("col1")[2]).intValue());
		assertEquals("via get()[]", "world", r.get("col2")[2]);
		
		assertEquals("via readRow", 407, ((Number) r.readRow(3).getInt("col1")) );
		assertEquals("via readRow", "no.7", r.readRow(3).getString("col2"));
		assertEquals("via get()[]", 407, ((Number) r.get("col1")[3]).intValue());
		assertEquals("via get()[]", "no.7", r.get("col2")[3]);
		
		r.dispose();
		
		// orderby DESC, limits 2, offset 1
		assertNotNull(r = jsqlObj.select(testTableName, null, null, null, "col1 DESC", 2,
			1));
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").length);
		assertEquals("via get()[]", 405, ((Number) r.get("col1")[1]).intValue());
		assertEquals("via get()[]", "hello", r.get("col2")[1]);
		assertEquals("via get()[]", 406, ((Number) r.get("col1")[0]).intValue());
		assertEquals("via get()[]", "world", r.get("col2")[0]);
		
		// select all, with select clause, orderby DESC
		assertNotNull(r = jsqlObj.select(testTableName, "col1, col2", null, null,
			"col1 DESC", 2, 1));
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").length);
		assertEquals("via get()[]", 405, ((Number) r.get("col1")[1]).intValue());
		assertEquals("via get()[]", "hello", r.get("col2")[1]);
		assertEquals("via get()[]", 406, ((Number) r.get("col1")[0]).intValue());
		assertEquals("via get()[]", "world", r.get("col2")[0]);
		
		// select 404, with col2 clause
		assertNotNull(r = jsqlObj.select(testTableName, "col2", "col1 = ?",
			(new Object[] { 404 })));
		
		assertNull("no column", r.get("col1"));
		assertNotNull("has column check", r.get("col2"));
		assertEquals("1 length check", 1, r.get("col2").length);
		assertEquals("via get()[]", "has nothing", r.get("col2")[0]);
	}
	
	/// @TODO extend test coverage to include default, and misc columns
	@Test
	public void upsertStatement() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		
		assertNotNull("query should return a JSql result",
			r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRow(0).getInt("col1")));
		assertEquals("Initial value check failed", "has nothing", r.readRow(0).getString("col2"));
		
		//Upsert query
		assertTrue(jsqlObj.upsert( //
			testTableName, //
			new String[] { "col1" }, new Object[] { 404 }, //
			new String[] { "col2", "col3" }, new Object[] { "not found", "not found" } //
		));
		
		assertNotNull("query should return a JSql result",
			r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRow(0).getInt("col1")));
		assertEquals("Upsert value check failed", "not found", r.readRow(0).getString("col2"));
	}
	
	@Test
	public void upsertStatementDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlPreparedStatement preparedStatment = null;
		
		jsqlObj.update("DROP TABLE IF EXISTS `" + testTableName + "_1`"); //cleanup (just incase)
		
		jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )"); //valid table creation : no exception
		
		//Upsert query
		assertNotNull(jsqlObj.upsert( //
			testTableName + "_1", //
			new String[] { "col1" }, new Object[] { 404 }, //
			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
			new String[] { "col2" }, new Object[] { "not found" }, //
			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
			new String[] { "col3" }, new Object[] { "3 not found" }, new String[] { "col4" } //
		));
		
		assertNotNull("query should return a JSql result",
			r = jsqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRow(0).getInt("col1")));
		assertEquals("Upsert value check failed", "not found", r.readRow(0).getString("col2"));
		assertEquals("Upsert value check failed", null, r.readRow(0).get("col4"));
	}
	
	@Test
	public void upsertStatementWithDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		
		jsqlObj.update("DROP TABLE IF EXISTS `" + testTableName + "_1`"); //cleanup (just incase)
		
		jsqlObj
			.update(
				"CREATE TABLE IF NOT EXISTS "
					+ testTableName
					+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) DEFAULT 'ABC' NOT NULL )"); //valid table creation : no exception
		
		//jsqlObj.update("ALTER TABLE " + testTableName + "_1 ADD CONSTRAINT c_col4 DEFAULT (ABC) FOR col4;");
		
		assertNotNull("query should return a JSql result",
			r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRow(0).getInt("col1")));
		assertEquals("Initial value check failed", "has nothing", r.readRow(0).getString("col2"));
		
		//Upsert query
		assertTrue(jsqlObj.upsert( //
			testTableName + "_1", //
			new String[] { "col1" }, new Object[] { 404 }, //
			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
			new String[] { "col2" }, new Object[] { "not found" }, //
			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
			new String[] { "col3" }, new Object[] { "3 not found" }, new String[] { "col4" } //
		));
		
		assertNotNull("query should return a JSql result",
			r = jsqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRow(0).getInt("col1")));
		assertEquals("Upsert value check failed", "not found", r.readRow(0).getString("col2"));
		assertEquals("Upsert value check failed", "ABC", r.readRow(0).getString("col4"));
	}
	
	@Test
	public void selectRangeSet() {
		row1to7setup();
		JSqlPreparedStatement preparedStatment = null;
		
		//Select range query
		assertNotNull(preparedStatment = jsqlObj.selectStatement( //
			testTableName, //
			"*", //
			"col1 > ?", //
			new Object[] { 0 }, //
			"col1 DESC", //
			5, //
			1 //
			)); //
		assertNotNull("query should return a JSql result", preparedStatment.query());
		
		//Select range query
		assertNotNull(preparedStatment = jsqlObj.selectStatement( //
			testTableName, //
			"*", //
			"col1 > ?", //
			new Object[] { 0 }, //
			"col1 DESC", //
			5, //
			0 //
			)); //
		assertNotNull("query should return a JSql result", preparedStatment.query());
	}
	
	@Test
	public void recreate() throws JSqlException {

		// Create setup
		simpleQueryFlow_raw();

		// close connection
		jsqlObj.dispose();
		
		// Expected exeception with connection gone of course
		Exception expected = null;
		try {
			simpleQueryFlow_raw();
		} catch(Exception e) {
			expected = e;
		}
		assertNotNull(expected);

		// recoreate connection
		jsqlObj.recreate(true);
		
		// Recreated
		simpleQueryFlow_raw();
	}
	
	/// JSQL table collumn with ending bracket ], which may breaks MS-SQL
	@Test
	public void mssqlClosingBracketInCollumnName() throws JSqlException {
		jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
		jsqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
		jsqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		jsqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404,
			"has nothing").dispose();
		jsqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 405,
			"has nothing").dispose();
		
		JSqlResult r = null;
		
		assertNotNull("SQL result returns as expected",
			r = jsqlObj.query("SELECT `col[1].pk` FROM " + testTableName + ""));
		r.dispose();
		
		assertNotNull(
			"SQL result returns as expected",
			r = jsqlObj.query("SELECT `col[1].pk` AS `test[a].pk` FROM " + testTableName
				+ " WHERE `col[1].pk` > 404"));
		r.dispose();
	}
	
	@Test
	public void commitTest() throws JSqlException {
		jsqlObj.update("DROP TABLE IF EXISTS " + testTableName + ""); //cleanup (just incase)
		
		jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )");
		jsqlObj.setAutoCommit(false);
		assertFalse(jsqlObj.getAutoCommit());
		jsqlObj.update("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404,
			"has nothing");
		jsqlObj.commit();
		JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
		r.fetchAllRows();
		assertEquals("via readRow", 404, ((Number) r.readRow(0).getInt("col[1].pk")).intValue());
	}
	
	@Test
	public void createTableIndexStatementTest() throws JSqlException {
		jsqlObj.update("DROP TABLE IF EXISTS " + testTableName + "");
		
		jsqlObj.update("CREATE TABLE IF NOT EXISTS " + testTableName + " ( `col1` INT PRIMARY KEY, col2 TEXT )");
		jsqlObj.createIndex(testTableName,"col2");
	}
	
	@Test
	public void createTableIndexStatementTestThreeParam() throws JSqlException {
		jsqlObj.update("DROP TABLE IF EXISTS " + testTableName + "");
		
		jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )");
		jsqlObj.createIndex(testTableName, "col2", "UNIQUE");
	}
	
	@Test
	public void createTableIndexStatementTestFourParam() throws JSqlException {
		jsqlObj.update("DROP TABLE IF EXISTS " + testTableName + "");
		
		jsqlObj.update(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )");
		jsqlObj.createIndex(testTableName, "col2", "UNIQUE", "IDX");
	}
	
	@Test
	public void genericSqlParserTest() throws JSqlException {
		String s = jsqlObj.genericSqlParser("SELECT * FROM " + testTableName + " WHERE COL1 = ?");
		assertEquals("SELECT * FROM " + testTableName + " WHERE COL1 = ?", s);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void joinArgumentsTest() throws JSqlException {
		Object[] array1 = new Object[] { 1, 2, 3 };
		Object[] array2 = new Object[] { 4, 5, 6 };
		Object[] array = new Object[] { 1, 2, 3, 4, 5, 6 };
		Object[] rArray = jsqlObj.joinArguments(array1, array2);
		assertEquals(array, rArray);
	}
	
}