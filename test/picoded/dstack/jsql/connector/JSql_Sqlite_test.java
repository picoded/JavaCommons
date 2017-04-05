package picoded.dstack.jsql.connector;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.TestConfig;

public class JSql_Sqlite_test {
	
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
			// jsqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose();
			// jsqlObj.dispose();
			// jsqlObj = null;
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
	public void rawSimpleQueryFlow() {
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

	/// rawSimpleQueryFlow, with built select statement instead
	@Test
	public void selectSimpleQueryFlow() {
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

	// /// Create table if not exists test
	// @Test
	// public void createTableQueryBuilder() throws JSqlException {
	// 	jsqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose(); //cleanup (just incase)
		
	// 	jsqlObj.createTableQuerySet(testTableName, new String[] { "col1", "col2" },
	// 		new String[] { "INT PRIMARY KEY", "TEXT" }).execute(); //valid table creation : no exception
		
	// 	jsqlObj.createTableQuerySet(testTableName, new String[] { "col1", "col2" },
	// 		new String[] { "INT PRIMARY KEY", "TEXT" }).execute(); //run twice to ensure "IF NOT EXISTS" works
		
	// 	jsqlObj.executeQuery("TRUNCATE TABLE " + testTableName + "").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
	// 	jsqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
	// 		"has nothing").dispose();
	// }
	
	// /// This is the base execute sql test example, in which other examples are built on
	// @Test
	// public void executeQuery() throws JSqlException {
	// 	jsqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose(); //cleanup (just incase)
		
	// 	jsqlObj.executeQuery(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )").dispose(); //valid table creation : no exception
	// 	jsqlObj.executeQuery(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
	// 	jsqlObj.executeQuery("TRUNCATE TABLE " + testTableName + "").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
	// 	jsqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2, col3 ) VALUES (?,?,?)",
	// 		404, "has nothing", "do nothing").dispose();
		
	// 	jsqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
	// 	jsqlObj.executeQuery(
	// 		"CREATE VIEW " + testTableName + "_View AS  SELECT * FROM " + testTableName).dispose();
		
	// 	jsqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
	// }
	
	// ///*
	// @Test
	// public void executeQuery_expectedExceptions() throws JSqlException {
	// 	executeQuery(); //runs the no exception varient. to pre populate the tables for exceptions
		
	// 	java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
	// 		java.util.logging.Level.SEVERE); //sets it to tolerate the error
	// 	JSqlException caughtException;
		
	// 	caughtException = null;
	// 	try {
	// 		jsqlObj.executeQuery(
	// 			"CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	caughtException = null;
	// 	try {
	// 		jsqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
	// 			"has nothing").dispose(); //inserts into : Expect exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
	// 		java.util.logging.Level.WARNING); //sets it back to warning
	// }
	
	// /// Modified duplicate of executeQuery set
	// @Test
	// public void execute() throws JSqlException {
	// 	assertTrue(jsqlObj.execute("DROP TABLE IF EXISTS " + testTableName + "")); //cleanup (just incase)
		
	// 	//valid table creation : no exception
	// 	assertTrue(jsqlObj.execute("CREATE TABLE IF NOT EXISTS " + testTableName
	// 		+ " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
	// 	//run twice to ensure "IF NOT EXISTS" works
	// 	assertTrue(jsqlObj.execute("CREATE TABLE IF NOT EXISTS " + testTableName
	// 		+ " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
	// 	assertTrue(jsqlObj.execute("TRUNCATE TABLE " + testTableName + "")); //cleanup (just incase)
		
	// 	assertTrue(jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)",
	// 		404, "has nothing"));
	// }
	
	// @Test
	// public void execute_expectedExceptions() throws JSqlException {
	// 	execute(); //runs the no exception varient. to pre populate the tables for exceptions
		
	// 	java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
	// 		java.util.logging.Level.SEVERE); //sets it to tolerate the error
	// 	JSqlException caughtException;
		
	// 	caughtException = null;
	// 	try {
	// 		assertTrue(jsqlObj.execute("CREATE TABLE " + testTableName
	// 			+ " (col1 INT PRIMARY KEY, col2 TEXT)")); //invalid table creation : should have exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	caughtException = null;
	// 	try {
	// 		assertTrue(jsqlObj.execute(
	// 			"INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing")); //inserts into : Expect exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
	// 		java.util.logging.Level.WARNING); //sets it back to warning
	// }
	
	// /// Modified duplicate of executeQuery set
	// @Test
	// public void query() throws JSqlException {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose(); //valid table creation : no exception
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose(); //run twice to ensure "IF NOT EXISTS" works
		
	// 	jsqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
	// 		"has nothing").dispose();
	// 	jsqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405,
	// 		"has nothing").dispose();
		
	// 	JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + "");
	// 	assertNotNull("SQL result returns as expected", r);
		
	// 	r.dispose();
	// }
	
	// @Test
	// public void query_expectedExceptions() throws JSqlException {
	// 	query(); //runs the no exception varient. to pre populate the tables for exceptions
		
	// 	//java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
	// 	JSqlException caughtException;
		
	// 	caughtException = null;
	// 	try {
	// 		jsqlObj.query("CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)")
	// 			.dispose(); //invalid table creation : should have exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	caughtException = null;
	// 	try {
	// 		jsqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
	// 			"has nothing").dispose(); //inserts into : Expect exception
	// 	} catch (JSqlException e) {
	// 		caughtException = e; //fish caught
	// 		assertNotNull("Exception caught as intended", e);
	// 	} finally {
	// 		if (caughtException == null) {
	// 			fail("Failed to catch an exception as intended");
	// 		}
	// 	}
		
	// 	//java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	// }
	
	// @Test
	// public void JSqlResultFetch() throws JSqlException {
	// 	executeQuery();
		
	// 	// added more data to test
	// 	jsqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello")
	// 		.dispose();
	// 	jsqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world")
	// 		.dispose();
		
	// 	JSqlResult r = jsqlObj.executeQuery("SELECT * FROM " + testTableName + "");
	// 	assertNotNull("SQL result returns as expected", r);
		
	// 	r.fetchAllRows();
		
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("via readRowCol", "has nothing", r.readRowCol(0, "col2"));
	// 	assertEquals("via readRowCol", 405, ((Number) r.readRowCol(1, "col1")).intValue());
	// 	assertEquals("via readRowCol", "hello", r.readRowCol(1, "col2"));
	// 	assertEquals("via readRowCol", 406, ((Number) r.readRowCol(2, "col1")).intValue());
	// 	assertEquals("via readRowCol", "world", r.readRowCol(2, "col2"));
		
	// 	assertEquals("via get().get()", 404, ((Number) r.get("col1").get(0)).intValue());
	// 	assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
	// 	assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
	// 	assertEquals("via get().get()", "hello", r.get("col2").get(1));
	// 	assertEquals("via get().get()", 406, ((Number) r.get("col1").get(2)).intValue());
	// 	assertEquals("via get().get()", "world", r.get("col2").get(2));
		
	// 	r.dispose();
	// }
	
	// /// Test if the "INDEX IF NOT EXISTS" clause is being handled correctly
	// @Test
	// public void uniqueIndexIfNotExists() throws JSqlException {
	// 	executeQuery();
		
	// 	assertTrue(
	// 		"1st uniq index",
	// 		jsqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `"
	// 			+ testTableName + "` ( col1, col2 )"));
		
	// 	assertTrue(
	// 		"2nd uniq index",
	// 		jsqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `"
	// 			+ testTableName + "` ( col3 )"));
	// }
	
	// @Test
	// public void JSqlQuerySetConstructor() {
	// 	JSqlQuerySet qSet = null;
		
	// 	assertNotNull(qSet = new JSqlQuerySet("hello", (new String[] { "world", "one" }), jsqlObj));
		
	// 	assertEquals(jsqlObj, qSet.getJSql());
	// 	assertEquals("hello", qSet.getQuery());
	// 	assertArrayEquals((new String[] { "world", "one" }), qSet.getArguments());
	// }
	
	// public void row1to7setup() throws JSqlException {
	// 	executeQuery();
		
	// 	// added more data to test
	// 	jsqlObj
	// 		.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello");
	// 	jsqlObj
	// 		.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world");
	// 	jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 407, "no.7");
	// }
	
	// @Test
	// public void selectQuerySet() throws JSqlException {
	// 	row1to7setup();
		
	// 	JSqlResult r = null;
	// 	JSqlQuerySet qSet = null;
		
	// 	// Select all as normal
	// 	assertNotNull(qSet = jsqlObj.selectQuerySet(testTableName, null, null, null)); //select all
	// 	r = qSet.executeQuery();
	// 	assertNotNull("SQL result should return a result", r);
		
	// 	r.fetchAllRows();
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("via readRowCol", "has nothing", r.readRowCol(0, "col2"));
	// 	assertEquals("via readRowCol", 405, ((Number) r.readRowCol(1, "col1")).intValue());
	// 	assertEquals("via readRowCol", "hello", r.readRowCol(1, "col2"));
	// 	assertEquals("via readRowCol", 406, ((Number) r.readRowCol(2, "col1")).intValue());
	// 	assertEquals("via readRowCol", "world", r.readRowCol(2, "col2"));
		
	// 	assertEquals("via get().get()", 404, ((Number) r.get("col1").get(0)).intValue());
	// 	assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
	// 	assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
	// 	assertEquals("via get().get()", "hello", r.get("col2").get(1));
	// 	assertEquals("via get().get()", 406, ((Number) r.get("col1").get(2)).intValue());
	// 	assertEquals("via get().get()", "world", r.get("col2").get(2));
		
	// 	assertEquals("via readRowCol", 407, ((Number) r.readRowCol(3, "col1")).intValue());
	// 	assertEquals("via readRowCol", "no.7", r.readRowCol(3, "col2"));
	// 	assertEquals("via get().get()", 407, ((Number) r.get("col1").get(3)).intValue());
	// 	assertEquals("via get().get()", "no.7", r.get("col2").get(3));
		
	// 	r.dispose();
		
	// 	// orderby DESC, limits 2, offset 1
	// 	assertNotNull(qSet = jsqlObj.selectQuerySet(testTableName, null, null, null, "col1 DESC", 2,
	// 		1));
	// 	assertNotNull("SQL result should return a result", r = qSet.query());
		
	// 	assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
	// 	assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
	// 	assertEquals("via get().get()", "hello", r.get("col2").get(1));
	// 	assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
	// 	assertEquals("via get().get()", "world", r.get("col2").get(0));
		
	// 	// select all, with select clause, orderby DESC
	// 	assertNotNull(qSet = jsqlObj.selectQuerySet(testTableName, "col1, col2", null, null,
	// 		"col1 DESC", 2, 1));
	// 	assertNotNull("SQL result should return a result", r = qSet.query());
		
	// 	assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
	// 	assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
	// 	assertEquals("via get().get()", "hello", r.get("col2").get(1));
	// 	assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
	// 	assertEquals("via get().get()", "world", r.get("col2").get(0));
		
	// 	// select 404, with col2 clause
	// 	assertNotNull(qSet = jsqlObj.selectQuerySet(testTableName, "col2", "col1 = ?",
	// 		(new Object[] { 404 })));
	// 	assertNotNull("SQL result should return a result", r = qSet.query());
		
	// 	assertNull("no column", r.get("col1"));
	// 	assertNotNull("has column check", r.get("col2"));
	// 	assertEquals("1 length check", 1, r.get("col2").size());
	// 	assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
	// }
	
	// /// @TODO extend test coverage to include default, and misc columns
	// @Test
	// public void upsertQuerySet() throws JSqlException {
	// 	row1to7setup();
	// 	JSqlResult r = null;
	// 	JSqlQuerySet qSet = null;
		
	// 	assertNotNull("query should return a JSql result",
	// 		r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
	// 	assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
	// 	//Upsert query
	// 	assertNotNull(qSet = jsqlObj.upsertQuerySet( //
	// 		testTableName, //
	// 		new String[] { "col1" }, new Object[] { 404 }, //
	// 		new String[] { "col2", "col3" }, new Object[] { "not found", "not found" } //
	// 		));
	// 	assertTrue("SQL result should return true", qSet.execute());
		
	// 	assertNotNull("query should return a JSql result",
	// 		r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
	// 	assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
	// }
	
	// @Test
	// public void upsertQuerySetDefault() throws JSqlException {
	// 	row1to7setup();
	// 	JSqlResult r = null;
	// 	JSqlQuerySet qSet = null;
		
	// 	jsqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
	// 	jsqlObj.executeQuery(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )")
	// 		.dispose(); //valid table creation : no exception
		
	// 	//Upsert query
	// 	assertNotNull(qSet = jsqlObj.upsertQuerySet( //
	// 		testTableName + "_1", //
	// 		new String[] { "col1" }, new Object[] { 404 }, //
	// 		//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
	// 		new String[] { "col2" }, new Object[] { "not found" }, //
	// 		//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
	// 		new String[] { "col3" }, new Object[] { "3 not found" }, new String[] { "col4" } //
	// 		));
	// 	assertTrue("SQL result should return true", qSet.execute());
		
	// 	assertNotNull("query should return a JSql result",
	// 		r = jsqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
	// 	assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
	// 	assertEquals("Upsert value check failed", null, r.readRowCol(0, "col4"));
	// }
	
	// @Test
	// public void upsertQuerySetWithDefault() throws JSqlException {
	// 	row1to7setup();
	// 	JSqlResult r = null;
	// 	JSqlQuerySet qSet = null;
		
	// 	jsqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
	// 	jsqlObj
	// 		.executeQuery(
	// 			"CREATE TABLE IF NOT EXISTS "
	// 				+ testTableName
	// 				+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) DEFAULT 'ABC' NOT NULL )")
	// 		.dispose(); //valid table creation : no exception
		
	// 	//jsqlObj.executeQuery("ALTER TABLE " + testTableName + "_1 ADD CONSTRAINT c_col4 DEFAULT (ABC) FOR col4;");
		
	// 	assertNotNull("query should return a JSql result",
	// 		r = jsqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
	// 	assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
	// 	//Upsert query
	// 	assertNotNull(qSet = jsqlObj.upsertQuerySet( //
	// 		testTableName + "_1", //
	// 		new String[] { "col1" }, new Object[] { 404 }, //
	// 		//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
	// 		new String[] { "col2" }, new Object[] { "not found" }, //
	// 		//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
	// 		new String[] { "col3" }, new Object[] { "3 not found" }, new String[] { "col4" } //
	// 		));
	// 	assertTrue("SQL result should return true", qSet.execute());
		
	// 	assertNotNull("query should return a JSql result",
	// 		r = jsqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
	// 	assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
	// 	assertEquals("Upsert value check failed", "ABC", r.readRowCol(0, "col4"));
	// }
	
	// @Test
	// public void selectRangeSet() throws JSqlException {
	// 	try {
	// 		row1to7setup();
	// 		//			JSqlResult r = null;
	// 		JSqlQuerySet qSet = null;
			
	// 		//Select range query
	// 		assertNotNull(qSet = jsqlObj.selectQuerySet( //
	// 			testTableName, //
	// 			"*", //
	// 			"col1 > ?", //
	// 			new Object[] { 0 }, //
	// 			"col1 DESC", //
	// 			5, //
	// 			1 //
	// 			)); //
	// 		assertNotNull("query should return a JSql result", qSet.query());
			
	// 		//Select range query
	// 		assertNotNull(qSet = jsqlObj.selectQuerySet( //
	// 			testTableName, //
	// 			"*", //
	// 			"col1 > ?", //
	// 			new Object[] { 0 }, //
	// 			"col1 DESC", //
	// 			5, //
	// 			0 //
	// 			)); //
	// 		assertNotNull("query should return a JSql result", qSet.query());
	// 	} catch (Exception e) {
			
	// 	}
	// }
	
	// //*/
	// @Test
	// public void recreate() throws JSqlException {
	// 	// close connection
	// 	jsqlObj.dispose();
		
	// 	// recoreate connection
	// 	jsqlObj.recreate(true);
		
	// 	// recreate the database and table
	// 	createTableQueryBuilder();
		
	// 	// query should get executed
	// 	JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + "");
	// 	assertNotNull("SQL result returns as expected", r);
	// }
	
	// /// JSQL table collumn with ending bracket ], which may breaks MS-SQL
	// @Test
	// public void mssqlClosingBracketInCollumnName() throws JSqlException {
	// 	try {
	// 		jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
			
	// 		jsqlObj.query(
	// 			"CREATE TABLE IF NOT EXISTS " + testTableName
	// 				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
	// 		jsqlObj.query(
	// 			"CREATE TABLE IF NOT EXISTS " + testTableName
	// 				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
			
	// 		jsqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404,
	// 			"has nothing").dispose();
	// 		jsqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 405,
	// 			"has nothing").dispose();
			
	// 		JSqlResult r = null;
			
	// 		assertNotNull("SQL result returns as expected",
	// 			r = jsqlObj.query("SELECT `col[1].pk` FROM " + testTableName + ""));
	// 		r.dispose();
			
	// 		assertNotNull(
	// 			"SQL result returns as expected",
	// 			r = jsqlObj.query("SELECT `col[1].pk` AS `test[a].pk` FROM " + testTableName
	// 				+ " WHERE `col[1].pk` > 404"));
	// 		r.dispose();
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}
	// }
	
	// @Test
	// public void commitTest() throws JSqlException {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
	// 	jsqlObj.setAutoCommit(false);
	// 	assertFalse(jsqlObj.getAutoCommit());
	// 	jsqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404,
	// 		"has nothing").dispose();
	// 	jsqlObj.commit();
	// 	JSqlResult r = jsqlObj.executeQuery("SELECT * FROM " + testTableName + "");
	// 	assertNotNull("SQL result returns as expected", r);
	// 	r.fetchAllRows();
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col[1].pk")).intValue());
	// }
	
	// @Test
	// public void createTableIndexQuerySetTest() throws JSqlException {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( `col1` INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose();
	// 	jsqlObj.createTableIndexQuerySet(testTableName, "col2");
	// }
	
	// @Test
	// public void createTableIndexQuerySetTestThreeParam() throws JSqlException {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
	// 	jsqlObj.createTableIndexQuerySet(testTableName, "col2", "ASC");
	// }
	
	// @Test
	// public void createTableIndexQuerySetTestFourParam() throws JSqlException {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName
	// 			+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
	// 	jsqlObj.createTableIndexQuerySet(testTableName, "col2", "DESC", "IDX");
	// }
	
	// @Test
	// public void executeTest() throws JSqlException, Exception {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose();
	// 	boolean b = jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)",
	// 		404, "has nothing");
	// 	assertTrue(b);
	// 	JSqlResult r = jsqlObj.executeQuery("SELECT * FROM " + testTableName + "");
	// 	assertNotNull("SQL result returns as expected", r);
	// 	r.fetchAllRows();
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// }
	
	// @Test(expected = Exception.class)
	// public void executeQueryTest() throws JSqlException, Exception {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose();
	// 	assertFalse(jsqlObj.isDisposed());
	// 	boolean b = jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)",
	// 		404, "has nothing");
	// 	assertTrue(b);
	// 	JSqlResult r = jsqlObj
	// 		.executeQuery("SELECT * FROM " + testTableName + " where col1 = ?", 404);
	// 	assertNotNull("SQL result returns as expected", r);
	// 	r.fetchAllRows();
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	JSql_Sqlite jSql_Sqlite = new JSql_Sqlite();
	// 	jSql_Sqlite.className = null;
	// 	jSql_Sqlite.recreate(false);
		
	// }
	
	// @Test
	// public void genericSqlParserTest() throws JSqlException {
	// 	String s = jsqlObj.genericSqlParser("SELECT * FROM " + testTableName + " WHERE COL1 = ?");
	// 	assertEquals("SELECT * FROM " + testTableName + " WHERE COL1 = ?", s);
	// }
	
	// @SuppressWarnings("deprecation")
	// @Test
	// public void joinArgumentsTest() throws JSqlException {
	// 	Object[] array1 = new Object[] { 1, 2, 3 };
	// 	Object[] array2 = new Object[] { 4, 5, 6 };
	// 	Object[] array = new Object[] { 1, 2, 3, 4, 5, 6 };
	// 	Object[] rArray = jsqlObj.joinArguments(array1, array2);
	// 	assertEquals(array, rArray);
	// }
	
	// @Test(expected = Exception.class)
	// public void queryTest() throws JSqlException, Exception {
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose();
	// 	assertFalse(jsqlObj.isDisposed());
	// 	boolean b = jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)",
	// 		404, "has nothing");
	// 	assertTrue(b);
	// 	JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + " where col1 = ?", 404);
	// 	assertNotNull("SQL result returns as expected", r);
	// 	r.fetchAllRows();
	// 	assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
	// 	JSql_Sqlite jSql_Sqlite = new JSql_Sqlite();
	// 	jSql_Sqlite.className = null;
	// 	jSql_Sqlite.recreate(false);
		
	// }
	
	// @Test(expected = Exception.class)
	// public void recreateTest() throws Exception, JSqlException {
	// 	jsqlObj.recreate(true);
	// 	jsqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
	// 	jsqlObj.query(
	// 		"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
	// 		.dispose();
	// 	//		boolean b = 
	// 	jsqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
	// 		"has nothing");
	// 	JSqlResult r = jsqlObj.query("SELECT * FROM " + testTableName + " where col1 = ? ", 404);
	// 	assertNotNull("SQL result returns as expected", r);
	// 	r.fetchAllRows();
	// 	Map<String, Object> row = r.readRow(0);
	// 	assertEquals("via readRow", 404, ((Number) row.get("col1")).intValue());
	// 	assertEquals("via readCol", "has nothing", r.readCol("col2")[0]);
	// 	assertEquals("via readCol", "has nothing", r.readCol_StringArr("col2")[0]);
	// 	JSql_Sqlite jSql_Sqlite = new JSql_Sqlite();
	// 	jSql_Sqlite.className = null;
	// 	jSql_Sqlite.recreate(false);
	// }
}