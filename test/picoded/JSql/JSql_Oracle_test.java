package picoded.JSql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Oracle;

public class JSql_Oracle_test /*extends JSql_Mysql_test*/{
	
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
		//create connection
		JSqlObj = JSql.oracle(TestConfig.ORACLE_PATH(), TestConfig.ORACLE_USER(),
			TestConfig.ORACLE_PASS());
	}
	
	@Test
	public void simpleMysqlToOracleParser() throws JSqlException {
		assertEquals(
			"BEGIN EXECUTE IMMEDIATE 'DROP TABLE \"JSQLTEST\"'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
			JSqlObj.genericSqlParser("DROP TABLE IF EXISTS `jsqlTest`"));
		assertEquals(
			"BEGIN EXECUTE IMMEDIATE 'CREATE TABLE \"JSQLTEST\" ( col1 INT PRIMARY KEY, col2 CLOB )'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;",
			JSqlObj
				.genericSqlParser("CREATE TABLE IF NOT EXISTS `jsqlTest` ( col1 INT PRIMARY KEY, col2 TEXT )"));
		assertEquals(
			"CREATE UNIQUE INDEX \"TESTMETATABLE_UNIQUE\" ON \"TESTMETATABLE\" (obj, metaKey)",
			JSqlObj
				.genericSqlParser("CREATE UNIQUE INDEX `testMetaTable_unique` ON `testMetaTable` (obj, metaKey)"));
	}
	
	/// Test if the "INDEX IF NOT EXISTS" clause is being handled correctly
	@Test
	public void uniqueIndexIfNotExists() throws JSqlException {
		executeQuery();
		
		JSqlException caughtException = null;
		try {
			assertTrue(
				"1st uniq index",
				JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `"
					+ testTableName + "` ( col1, col2 )"));
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		assertTrue(
			"2nd uniq index",
			JSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + testTableName + "_unique` ON `"
				+ testTableName + "` ( col3 )"));
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
	public void oracleInMemoryConstructor() {
		assertNotNull("Oracle constructed object must not be null", JSqlObj);
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
		
		JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
			"has nothing").dispose();
	}
	
	/// This is the base execute sql test example, in which other examples are built on
	@Test
	public void executeQuery() throws JSqlException {
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )").dispose(); //valid table creation : no exception
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50) )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("TRUNCATE TABLE " + testTableName + "").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2, col3 ) VALUES (?,?,?)",
			404, "has nothing", "do nothing").dispose();
		
		JSqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
		JSqlObj.executeQuery(
			"CREATE VIEW " + testTableName + "_View AS  SELECT * FROM " + testTableName).dispose();
		
		JSqlObj.executeQuery("DROP VIEW IF EXISTS `" + testTableName + "_View`").dispose();
		
	}
	
	///*
	@Test
	public void executeQuery_expectedExceptions() throws JSqlException {
		executeQuery(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.executeQuery(
				"CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
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
			JSqlObj.executeQuery("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
				"has nothing").dispose(); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void execute() throws JSqlException {
		assertTrue(JSqlObj.execute("DROP TABLE IF EXISTS " + testTableName + "")); //cleanup (just incase)
		
		//valid table creation : no exception
		assertTrue(JSqlObj.execute("CREATE TABLE IF NOT EXISTS " + testTableName
			+ " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
		//run twice to ensure "IF NOT EXISTS" works
		assertTrue(JSqlObj.execute("CREATE TABLE IF NOT EXISTS " + testTableName
			+ " ( col1 INT PRIMARY KEY, col2 TEXT )"));
		
		assertTrue(JSqlObj.execute("TRUNCATE TABLE " + testTableName + "")); //cleanup (just incase)
		
		assertTrue(JSqlObj.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)",
			404, "has nothing"));
	}
	
	@Test
	public void execute_expectedExceptions() throws JSqlException {
		execute(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			assertTrue(JSqlObj.execute("CREATE TABLE " + testTableName
				+ " (col1 INT PRIMARY KEY, col2 TEXT)")); //invalid table creation : should have exception
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
			assertTrue(JSqlObj.execute(
				"INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404, "has nothing")); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended", e);
		} finally {
			if (caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(
			java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void query() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
			.dispose(); //valid table creation : no exception
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( col1 INT PRIMARY KEY, col2 TEXT )")
			.dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
			"has nothing").dispose();
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405,
			"has nothing").dispose();
		
		JSqlResult r = JSqlObj.query("SELECT * FROM " + testTableName + "");
		assertNotNull("Oracle result returns as expected", r);
		
		r.dispose();
	}
	
	@Test
	public void query_expectedExceptions() throws JSqlException {
		query(); //runs the no exception varient. to pre populate the tables for exceptions
		
		//java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.query("CREATE TABLE " + testTableName + " (col1 INT PRIMARY KEY, col2 TEXT)")
				.dispose(); //invalid table creation : should have exception
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
			JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 404,
				"has nothing").dispose(); //inserts into : Expect exception
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
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello")
			.dispose();
		JSqlObj.query("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world")
			.dispose();
		
		JSqlResult r = JSqlObj.executeQuery("SELECT * FROM " + testTableName + "");
		assertNotNull("Oracle result returns as expected", r);
		
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
		JSqlObj
			.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 405, "hello");
		JSqlObj
			.execute("INSERT INTO " + testTableName + " ( col1, col2 ) VALUES (?,?)", 406, "world");
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
		assertNotNull("Oracle result should return a result", r);
		
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
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, null, null, null, "col1 DESC", 2,
			1));
		assertNotNull("Oracle result should return a result", r = qSet.query());
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(0));
		
		// select all, with select clause, orderby DESC
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, "col1, col2", null, null,
			"col1 DESC", 2, 1));
		assertNotNull("Oracle result should return a result", r = qSet.query());
		
		assertEquals("DESC, limit 2, offset 1 length check", 2, r.get("col1").size());
		assertEquals("via get().get()", 405, ((Number) r.get("col1").get(1)).intValue());
		assertEquals("via get().get()", "hello", r.get("col2").get(1));
		assertEquals("via get().get()", 406, ((Number) r.get("col1").get(0)).intValue());
		assertEquals("via get().get()", "world", r.get("col2").get(0));
		
		// select 404, with col2 clause
		assertNotNull(qSet = JSqlObj.selectQuerySet(testTableName, "col2", "col1 = ?",
			(new Object[] { 404 })));
		assertNotNull("Oracle result should return a result", r = qSet.query());
		
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
		
		assertNotNull("query should return a Oracle result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Initial value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Initial value check failed", "has nothing", r.readRowCol(0, "col2"));
		
		//Upsert query
		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
			testTableName, //
			new String[] { "col1" }, new Object[] { 404 }, //
			new String[] { "col2", "col3" }, new Object[] { "not found", "not found" } //
			));
		assertTrue("Oracle result should return true", qSet.execute());
		
		assertNotNull("query should return a Oracle result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + " ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
	}
	
	@Test
	public void upsertQuerySetWithDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1"
				+ " ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(10) )").dispose(); //valid table creation : no exception
		
		//Upsert query
		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
			testTableName + "_1", //
			new String[] { "col1" }, new Object[] { 404 }, //
			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
			new String[] { "col2" }, new Object[] { "not found" }, //
			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
			new String[] { "col3" }, new Object[] { "3 not found" }, new String[] { "col4" } //
			));
		assertTrue("SQL result should return true", qSet.execute());
		
		assertNotNull("query should return a JSql result",
			r = JSqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
		assertEquals("Upsert value check failed", null, r.readRowCol(0, "col4"));
	}
	
	@Test
	public void selectRangeSet() throws JSqlException {
		try {
			row1to7setup();
			//			JSqlResult r = null;
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
			assertNotNull("query should return a Oracle result", qSet.query());
			
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
			assertNotNull("query should return a Oracle result", qSet.query());
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
		assertNotNull("Oracle result returns as expected", r);
	}
	
	@Test
	public void commitTest() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose(); //cleanup (just incase)
		
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
		JSqlObj.setAutoCommit(false);
		assertFalse(JSqlObj.getAutoCommit());
		JSqlObj.query("INSERT INTO " + testTableName + " ( `col[1].pk`, col2 ) VALUES (?,?)", 404,
			"has nothing").dispose();
		JSqlObj.commit();
		JSqlResult r = JSqlObj.executeQuery("SELECT * FROM " + testTableName + "");
		assertNotNull("SQL result returns as expected", r);
		r.fetchAllRows();
		assertEquals("via readRowCol", 404, ((Number) r.readRowCol(0, "col[1].pk")).intValue());
	}
	
	@Test
	public void createTableIndexQuerySetTest() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( `col1` INT PRIMARY KEY, col2 TEXT )")
			.dispose();
		JSqlObj.createTableIndexQuerySet(testTableName, "col2");
	}
	
	@Test
	public void createTableIndexQuerySetTestThreeParam() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
		JSqlObj.createTableIndexQuerySet(testTableName, "col2", "ASC");
	}
	
	@Test
	public void createTableIndexQuerySetTestFourParam() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ " ( `col[1].pk` INT PRIMARY KEY, col2 TEXT )").dispose();
		JSqlObj.createTableIndexQuerySet(testTableName, "col2", "DESC", "IDX");
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void joinArgumentsTest() throws JSqlException {
		Object[] array1 = new Object[] { 1, 2, 3 };
		Object[] array2 = new Object[] { 4, 5, 6 };
		Object[] array = new Object[] { 1, 2, 3, 4, 5, 6 };
		Object[] rArray = JSqlObj.joinArguments(array1, array2);
		assertEquals(array, rArray);
	}
	
	@Test
	public void genericSqlParserTest() throws JSqlException {
		String s = JSqlObj.genericSqlParser("SELECT * FROM " + testTableName + " WHERE COL1 = ?");
		assertEquals("SELECT * FROM " + testTableName + " WHERE COL1 = ?", s);
		
		s = JSqlObj.genericSqlParser("DROP TABLE IF EXISTS MY_TABLE");
		assertEquals(
			"BEGIN EXECUTE IMMEDIATE 'DROP TABLE MY_TABLE'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
			s);
		
		s = JSqlObj.genericSqlParser("DROP TABLE MY_TABLE ; ");
		//assertEquals("DROP TABLE MY_TABLE", s); //Should be
		assertEquals("DROP TABLE MY_TABLE ", s);
		
		s = JSqlObj.genericSqlParser("DELETE FROM my_table WHERE col1 = ? ");
		assertEquals("DELETE FROM MY_TABLE WHERE col1 = ?", s);
		
		s = JSqlObj.genericSqlParser("DELETE FROM my_table WHERE col1 = 'ABC' ");
		String ss = "DELETE FROM MY_TABLE WHERE col1 = " + '"' + "ABC" + '"';
		assertEquals(ss, s);
		
		s = JSqlObj.genericSqlParser("INSERT INTO my_table ( col1, col2 ) VALUES (?,?)");
		assertEquals("INSERT INTO MY_TABLE ( col1, col2 ) VALUES (?,?)", s);
		
		s = JSqlObj.genericSqlParser("UPDATE my_table SET col1 = ?, col2 = ? ");
		assertEquals("UPDATE MY_TABLE SET col1 = ?, col2 = ?", s);
		
		s = JSqlObj.genericSqlParser("UPDATE my_table SET col1 = 405 ");
		assertEquals("UPDATE MY_TABLE SET col1 = 405", s);
		
		s = JSqlObj.genericSqlParser("ALTER TABLE my_table ADD COLUMN col3 varchar(10)");
		assertEquals("ALTER TABLE my_table ADD COLUMN col3 varchar(10)", s);
	}
	
	@Test
	public void upsertWithMisc() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
		JSqlObj
			.execute_raw("CREATE TABLE "
				+ testTableName
				+ " ( PTK VARCHAR2(32), RNO VARCHAR2(32), CDATE DATE, ACODE VARCHAR2(5), CBY VARCHAR2(50), LDATE TIMESTAMP(6) )");
		JSqlObj.createTableIndexQuerySet(testTableName, "PTK", "PRIMARY KEY");
		
		JSqlObj.upsertQuerySet(testTableName, new String[] { "PTK" }, new Object[] { "TEST_KEY" },
			new String[] { "RNO", "ACODE" }, new Object[] { "12345", "CODE" }, null, null,
			new String[] { "CDATE", "CBY", "LDATE" }).execute();
	}
	
	@Test
	public void upsertWithDate() throws JSqlException {
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		
		JSqlObj
			.execute_raw("CREATE TABLE "
				+ testTableName
				+ " ( PTK VARCHAR2(32), RNO VARCHAR2(32), CDATE DATE, ACODE VARCHAR2(5), CBY VARCHAR2(50), LDATE TIMESTAMP(6) )");
		JSqlObj.createTableIndexQuerySet(testTableName, "PTK", "PRIMARY KEY");
		
		JSqlObj.upsertQuerySet(testTableName, new String[] { "PTK" }, new Object[] { "TEST_KEY" },
			new String[] { "CDATE", "RNO", "ACODE" }, new Object[] { new Date(), "12345", "CODE" },
			null, null, new String[] { "CBY", "LDATE" }).execute();
	}
	
	@SuppressWarnings("static-access")
	@Test(expected = Exception.class)
	public void JSql_OracleTest() throws Exception {
		JSqlObj.oracle(JSqlObj.sqlConn);
		JSqlObj.oracle(null);
		JSqlObj.recreate(false);
		assertEquals("BEGIN EXECUTE IMMEDIATE", JSqlObj.genericSqlParser("BEGIN EXECUTE IMMEDIATE"));
		JSqlObj = JSql.oracle("test", "test", "test");
	}
	
	@Test(expected = Exception.class)
	public void genericSqlParser1Test() throws JSqlException {
		JSqlObj.genericSqlParser("DROP TABLE " + testTableName);
		JSqlObj.genericSqlParser("DROP TABLE IF EXISTS " + testTableName);
		
		JSqlObj.genericSqlParser("DROP VIEW " + testTableName);
		JSqlObj.genericSqlParser("DROP VIEW IF EXISTS " + testTableName);
		JSqlObj.genericSqlParser("DROP");
		
		JSqlObj.genericSqlParser("CREATE TABLE " + testTableName);
		JSqlObj.genericSqlParser("CREATE IF NOT EXISTS " + testTableName);
		JSqlObj.genericSqlParser("CREATE VIEW " + testTableName);
		JSqlObj.genericSqlParser("CREATE VIEW IF NOT EXISTS " + testTableName);
		JSqlObj.genericSqlParser("CREATE " + testTableName);
		JSqlObj.genericSqlParser("SELECT * FROM");
		JSqlObj.genericSqlParser("SELECT * FROM AS ");
		JSqlObj.genericSqlParser("SELECT * FROM AS " + testTableName);
		JSqlObj.genericSqlParser("SELECT * FROM " + testTableName + " AS  t where t.id=123");
		JSqlObj.genericSqlParser("CREATE UNIQUE INDEX IF NOT EXISTS " + testTableName + " ()");
		JSqlObj.genericSqlParser("CREATE UNIQUE INDEX " + testTableName);
	}
	
	@Test(expected = Exception.class)
	public void upsertQuerySet1() throws JSqlException {
		String tableName = "test";
		String[] uniqueColumns = new String[] {};
		Object[] uniqueValues = new Object[] {};
		String[] insertColumns = new String[] {};
		Object[] insertValues = new Object[] {};
		String[] defaultColumns = new String[] {};
		Object[] defaultValues = new Object[] {};
		String[] miscColumns = new String[] {};
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
		
		tableName = "Upsert query requires unique column and values to be equal length";
		uniqueColumns = new String[] {};
		uniqueValues = new Object[] {};
		insertColumns = null;
		insertValues = null;
		defaultColumns = null;
		defaultValues = null;
		miscColumns = null;
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
		uniqueColumns = new String[] {};
		uniqueValues = new Object[] {};
		uniqueColumns = null;
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
	}
	
	@Test(expected = Exception.class)
	public void JSqlOracleTest() throws JSqlException {
		JSql_Oracle jSqlOracle = new JSql_Oracle(JSqlObj.sqlConn);
		jSqlOracle.getPrefixOffsetAndIndexType("", new String[] {}, 0, "");
		jSqlOracle.getPrefixOffsetAndIndexType("test", new String[] { "abc" }, 0, "");
		
		Map<String, String> metadata = null;
		jSqlOracle.checkMetadata(metadata);
		metadata = new HashMap<String, String>();
		jSqlOracle.checkMetadata(metadata);
		metadata.put("test", null);
		jSqlOracle.checkMetadata(metadata);
		JSqlObj.genericSqlParser("SELECT * FROM ( " + testTableName
			+ " AS  t where t.id=123 ( FROM (" + testTableName + " AS  t where t.id=123)))");
		String qString = "CREATE TABLE "
			+ testTableName
			+ "(ID int NOT NULL AUTO_INCREMENT,LastName varchar(255) NOT NULL, FirstName varchar(255),Address varchar(255),City varchar(255),PRIMARY KEY (ID))";
		JSqlObj.execute(qString);
		JSqlObj.execute("AUTOINCREMENT");
	}
	
	@Test(expected = Exception.class)
	public void JSqlOracleTest1() throws JSqlException {
		String qString = "IF NOT EXISTS ,CREATE TABLE IF NOT EXISTS "
			+ testTableName
			+ "( ID int NOT NULL() AUTOINCREMENT ( ID int NOT NULL AUTOINCREMENT ) ) PRIMARY KEY (ID) )";
		JSqlObj.execute(qString);
	}
	
	@Test(expected = Exception.class)
	public void JSqlOracleTest2() throws JSqlException {
		JSqlObj.execute("CREATE AUTOINCREMENT");
	}
	
	@Test(expected = Exception.class)
	public void upsertQuerySet2() throws JSqlException {
		String tableName = "test";
		String[] uniqueColumns = new String[] { "col1", "col2" };
		Object[] uniqueValues = new Object[] { "col1", "col2" };
		
		String[] insertColumns = new String[] {};
		Object[] insertValues = new Object[] {};
		
		String[] defaultColumns = new String[] {};
		Object[] defaultValues = new Object[] {};
		
		String[] miscColumns = new String[] {};
		
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
		
		insertColumns = new String[] { "col1", "col2" };
		
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
	}
	
	@Test(expected = Exception.class)
	public void upsertQuerySet3() throws JSqlException {
		String tableName = "test";
		String[] uniqueColumns = new String[] {};
		Object[] uniqueValues = new Object[] { "col1", "col2" };
		
		String[] insertColumns = new String[] {};
		Object[] insertValues = new Object[] {};
		
		String[] defaultColumns = new String[] {};
		Object[] defaultValues = new Object[] {};
		
		String[] miscColumns = new String[] {};
		
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
	}
	
	@Test(expected = Exception.class)
	public void upsertQuerySet4() throws JSqlException {
		String tableName = "test";
		String[] uniqueColumns = new String[] { "col1", "col2" };
		Object[] uniqueValues = new Object[] {};
		
		String[] insertColumns = new String[] {};
		Object[] insertValues = new Object[] {};
		
		String[] defaultColumns = new String[] {};
		Object[] defaultValues = new Object[] {};
		
		String[] miscColumns = new String[] {};
		
		JSqlObj.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
	}
}
