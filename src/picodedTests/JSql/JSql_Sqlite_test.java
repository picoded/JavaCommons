package picodedTests.JSql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlQuerySet;
import picoded.JSql.JSqlResult;
import picodedTests.TestConfig;

public class JSql_Sqlite_test {
	
	protected JSql JSqlObj;
	protected static String testTableName = "JSqlTest_default";
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = "JSqlTest_" + TestConfig.randomTablePrefix();
		testTableName = testTableName.toUpperCase();
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	@Before
	public void setUp() {
		JSqlObj = JSql.sqlite();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (JSqlObj != null) {
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose();
			JSqlObj.dispose();
			JSqlObj = null;
		}
	}
	
	@Test
	public void sqliteInMemoryConstructor() {
		assertNotNull("JSql constructed object must not be null", JSqlObj);
	}
	
	/// Create table if not exists test
	@Test
	public void createTableQueryBuilder() throws JSqlException {
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose(); //cleanup (just incase)
		
		JSqlObj.createTableQuerySet(testTableName, new String[] { "col1", "col2" },
			new String[] { "INT PRIMARY KEY", "TEXT" }).execute(); //valid table creation : no exception
		
		JSqlObj.createTableQuerySet(testTableName, new String[] { "col1", "col2" },
			new String[] { "INT PRIMARY KEY", "TEXT" }).execute(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("TRUNCATE TABLE " + testTableName + "").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing")
			.dispose();
	}
	
	/// This is the base execute sql test example, in which other examples are built on
	@Test
	public void executeQuery() throws JSqlException {
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )")
			.dispose(); //valid table creation : no exception
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )")
			.dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("TRUNCATE TABLE " + testTableName + "").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2, col3 ) VALUES (?,?,?)", 404, "has nothing",
			"do nothing").dispose();
		
		JSqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
		JSqlObj.executeQuery("CREATE VIEW " + testTableName + "_View AS  SELECT * FROM " + testTableName).dispose();
		
		JSqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
	}
	
	///*
	@Test
	public void executeQuery_expectedExceptions() throws JSqlException {
		executeQuery(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.executeQuery("CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing")
				.dispose(); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void execute() throws JSqlException {
		assertTrue(JSqlObj.execute("DROP TABLE IF EXISTS " + testTableName + "")); //cleanup (just incase)
		
		//valid table creation : no exception
		assertTrue(JSqlObj
			.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
		//run twice to ensure "IF NOT EXISTS" works
		assertTrue(JSqlObj
			.execute("CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
		assertTrue(JSqlObj.execute("TRUNCATE TABLE " + testTableName + "")); //cleanup (just incase)
		
		assertTrue(JSqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing"));
	}
	
	@Test
	public void execute_expectedExceptions() throws JSqlException {
		execute(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			assertTrue(JSqlObj.execute("CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)")); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			assertTrue(JSqlObj
				.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing")); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void query() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
		JSqlObj.query("CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
		JSqlObj.query("CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing").dispose();
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "has nothing").dispose();
		
		JSqlResult r = JSqlObj.query("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
		
		r.dispose();
	}
	
	@Test
	public void query_expectedExceptions() throws JSqlException {
		query(); //runs the no exception varient. to pre populate the tables for exceptions
		
		//java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.query("CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing").dispose(); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		//java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	@Test
	public void JSqlResultFetch() throws JSqlException {
		executeQuery();
		
		// added more data to test
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello").dispose();
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world").dispose();
		
		JSqlResult r = JSqlObj.executeQuery("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
		
		r.fetchAllRows();
		
		assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("via readRowCol", "has nothing", r.readRowCol(0, "col2"));
		assertEquals("via readRowCol", 405, ((Number) r.readRowCol(1, "col1")).intValue());
		assertEquals("via readRowCol", "hello", r.readRowCol(1, "col2"));
		assertEquals("via readRowCol", 406, ((Number) r.readRowCol(2, "col1")).intValue());
		assertEquals("via readRowCol", "world", r.readRowCol(2, "col2"));
		
		assertEquals("via get().get()", 404, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(2)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(2));
		
		r.dispose();
	}
	
	/// Test if the "INDEX IF NOT EXISTS" clause is being handled correctly
	@Test
	public void uniqueIndexIfNotExists() throws JSqlException {
		executeQuery();
		
		assertTrue(
			"1st uniq index",
			JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `" + testTableName
				+ "` ( col1, col2 )"));
		
		assertTrue(
			"2nd uniq index",
			JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `" + testTableName
				+ "` ( col3 )"));
	}
	
	@Test
	public void JSqlQuerySetConstructor() {
		JSqlQuerySet qSet = null;
		
		assertNotNull(qSet = new JSqlQuerySet("hello", (new String[] { "world", "one" }), JSqlObj));
		
		assertEquals(JSqlObj, qSet.getJSql());
		assertEquals("hello", qSet.getQuery());
		assertArrayEquals((new String[] { "world", "one" }), qSet.getArguments());
	}
	
	public void row1to7setup() throws JSqlException {
		executeQuery();
		
		// added more data to test
		JSqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 407, "no.7");
	}
	
	@Test
	public void selectQuerySet() throws JSqlException {
		row1to7setup();
		
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		// Select all as normal
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, null, null, null)); //select all
		r = qSet.executeQuery();
		assertNotNull("SQL result should return a result", r);
		
		r.fetchAllRows();
		assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("via readRowCol", "has nothing", r.readRowCol(0, "col2"));
		assertEquals("via readRowCol", 405, ((Number) r.readRowCol(1, "col1")).intValue());
		assertEquals("via readRowCol", "hello", r.readRowCol(1, "col2"));
		assertEquals("via readRowCol", 406, ((Number) r.readRowCol(2, "col1")).intValue());
		assertEquals("via readRowCol", "world", r.readRowCol(2, "col2"));
		
		assertEquals("via get().get()", 404, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(2)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(2));
		
		assertEquals("via readRowCol", 407, ((Number) r.readRowCol(3, "col1")).intValue());
		assertEquals("via readRowCol", "no.7", r.readRowCol(3, "col2"));
		assertEquals("via get().get()", 407, ((Number) r.get("col1").get(3)).intValue());
		assertEquals("via get().get()", "no.7", r.get("col2").get(3));
		
		r.dispose();
		
		// orderby DESC, limits 2, offset 1
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, null, null, null, "col1 DESC", 2, 1));
		assertNotNull("SQL result should return a result", r = qSet.query());
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(0));
		
		// select all, with select clause, orderby DESC
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, "col1, col2", null, null, "col1 DESC", 2, 1));
		assertNotNull("SQL result should return a result", r = qSet.query());
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(0));
		
		// select 404, with col2 clause
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, "col2", "col1 = ?", (new Object[] { 404 })));
		assertNotNull("SQL result should return a result", r = qSet.query());
		
		assertNull("no column", r.get("col1"));
		assertNotNull("has column check", r.get("col2"));
		assertEquals("1 length check", 1, r.get("col2").size());
		assertEquals("via get().get()", "has nothing", r.get("col2").get(0));
	}
	
	/// @TODO extend test coverage to include default, and misc columns
	@Test
	public void upsertQuerySet() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
		//Upsert query
		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
			testTableName, //
			new String[] { "col1" }, new Object[] { 404 }, //
			new String[] { "col2", "col3" }, new Object[] { "not found", "not found" } //
			));
		assertTrue("SQL result should return true", qSet.execute());
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
	}
	
	@Test
	public void upsertQuerySetDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )")
			.dispose(); //valid table creation : no exception
		
		//JSqlObj.executeQuery("ALTER TABLE " + testTableName + "_1 ADD CONSTRAINT c_col4 DEFAULT (ABC) FOR col4;");
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
		//Upsert query
		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
			testTableName + "_1", //
			new String[] { "col1" }, new Object[] { 404 }, //
			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
			new String[] { "col2" }, new Object[] { "not found" },  //
			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
			new String[] { "col3" }, new Object[] { "3 not found" },
			new String[] { "col4" } //
			));
		assertTrue("SQL result should return true", qSet.execute());
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
		assertEquals("Upsert value check failed", null, r.readRowCol(0, "col4"));
	}
	
	@Test
	public void upsertQuerySetWithDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) DEFAULT 'ABC' NOT NULL )")
			.dispose(); //valid table creation : no exception
		
		//JSqlObj.executeQuery("ALTER TABLE " + testTableName + "_1 ADD CONSTRAINT c_col4 DEFAULT (ABC) FOR col4;");
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
		//Upsert query
		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
			testTableName + "_1", //
			new String[] { "col1" }, new Object[] { 404 }, //
			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
			new String[] { "col2" }, new Object[] { "not found" },  //
			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
			new String[] { "col3" }, new Object[] { "3 not found" },
			new String[] { "col4" } //
			));
		assertTrue("SQL result should return true", qSet.execute());
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
		assertEquals("Upsert value check failed", "ABC", r.readRowCol(0, "col4"));
	}
	
	@Test
	public void selectRangeSet() throws JSqlException {
		try {
			row1to7setup();
			JSqlResult r = null;
			JSqlQuerySet qSet = null;
			
			//Select range query
			assertNotNull(qSet = JSqlObj.selectQuerySet( //
				testTableName, //
				"*", //
				"col1 > ?", //
				new Object[] { 0 }, //
				"col1 DESC", //
				5, //
				1 //
				)); //
			assertNotNull("query should return a JSql result", qSet.query());
			
			//Select range query
			assertNotNull(qSet = JSqlObj.selectQuerySet( //
				testTableName, //
				"*", //
				"col1 > ?", //
				new Object[] { 0 }, //
				"col1 DESC", //
				5, //
				0 //
				)); //
			assertNotNull("query should return a JSql result", qSet.query());
		} catch (Exception e) {
			
		}
	}
	
	//*/
	@Test
	public void recreate() throws JSqlException {
		// close connection
		JSqlObj.dispose();
		
		// recoreate connection
		JSqlObj.recreate(true);
		
		// recreate the database and table
		createTableQueryBuilder();
		
		// query should get executed
		JSqlResult r = JSqlObj.query("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
	}
	
	/// JSQL table collumn with ending bracket ], which may breaks MS-SQL
	@Test
	public void mssqlClosingBracketInCollumnName() throws JSqlException {
		try {
			JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
			
			JSqlObj.query("CREATE TABLE IF NOT EXISTS " + testTableName + " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )")
				.dispose(); //valid table creation : no exception
			JSqlObj.query("CREATE TABLE IF NOT EXISTS " + testTableName + " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )")
				.dispose(); //run twice to ensure "IF NOT EXISTS" works
			
			JSqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404, "has nothing")
				.dispose();
			JSqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 405, "has nothing")
				.dispose();
			
			JSqlResult r = null;
			
			assertNotNull("SQL result returns as expected",
				r = JSqlObj.query("SELECT `col[1].pk` FROM " + testTableName + ""));
			r.dispose();
			
			assertNotNull("SQL result returns as expected",
				r = JSqlObj.query("SELECT `col[1].pk` AS `test[a].pk` FROM " + testTableName + " WHERE `col[1].pk` > 404"));
			r.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
