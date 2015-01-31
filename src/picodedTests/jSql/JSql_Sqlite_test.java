package picodedTests.jSql;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import picoded.jSql.*;

public class JSql_Sqlite_test {
	
	protected JSql JSqlObj;
	
	@Before
	public void setUp() {
		JSqlObj = JSql.sqlite();
	}
	
	@After
	public void tearDown() {
		if( JSqlObj != null ) {
			JSqlObj.dispose();
			JSqlObj = null;
		}
	}
	
	@Test
	public void sqliteInMemoryConstructor() {
		assertNotNull("JSql constructed object must not be null", JSqlObj);
	}
	
	/// This is the base execute sql test example, in which other examples are built on
	@Test
	public void executeQuery() throws JSqlException{
		JSqlObj.executeQuery( "DROP TABLE IF EXISTS `JSqlTest`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
		JSqlObj.executeQuery( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		//JSqlObj.executeQuery( "TRUNCATE TABLE JSqlTest").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ).dispose();
	}
	
	///*
	@Test
	public void executeQuery_expectedExceptions() throws JSqlException {
		executeQuery(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.executeQuery( "CREATE TABLE JSqlTest (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			JSqlObj.executeQuery( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ).dispose(); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void execute() throws JSqlException{
		assertTrue(JSqlObj.execute( "DROP TABLE IF EXISTS JSqlTest")); //cleanup (just incase)
		
		assertTrue(JSqlObj.execute( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )")); //valid table creation : no exception
		assertTrue(JSqlObj.execute( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )")); //run twice to ensure "IF NOT EXISTS" works
	 
		//assertTrue(JSqlObj.execute( "TRUNCATE TABLE JSqlTest")); //cleanup (just incase)
	 
		assertTrue(JSqlObj.execute( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ));
	}
	
	@Test
	public void execute_expectedExceptions() throws JSqlException {
		execute(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			assertTrue(JSqlObj.execute( "CREATE TABLE JSqlTest (col1 INT PRIMARY KEY, col2 TEXT)")); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			assertTrue(JSqlObj.execute( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" )); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void query() throws JSqlException{
		JSqlObj.query( "DROP TABLE IF EXISTS JSqlTest").dispose(); //cleanup (just incase)
		
		JSqlObj.query( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
		JSqlObj.query( "CREATE TABLE IF NOT EXISTS JSqlTest ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.query( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ).dispose();
		JSqlObj.query( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 405, "has nothing" ).dispose();
		
		JSqlResult r = JSqlObj.query( "SELECT * FROM JSqlTest" );
		assertNotNull( "SQL result returns as expected", r );
		
		r.dispose();
	}
	
	@Test
	public void query_expectedExceptions() throws JSqlException {
		query(); //runs the no exception varient. to pre populate the tables for exceptions
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); //sets it to tolerate the error
		JSqlException caughtException;
		
		caughtException = null;
		try {
			JSqlObj.query( "CREATE TABLE JSqlTest (col1 INT PRIMARY KEY, col2 TEXT)").dispose(); //invalid table creation : should have exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		caughtException = null;
		try {
			JSqlObj.query( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ).dispose(); //inserts into : Expect exception
		} catch (JSqlException e) {
			caughtException = e; //fish caught
			assertNotNull("Exception caught as intended",e);
		} finally {
			if(caughtException == null) {
				fail("Failed to catch an exception as intended");
			}
		}
		
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); //sets it back to warning
	}
	
	@Test
	public void JSqlResultFetch() throws JSqlException {
		executeQuery();
		
		// added more data to test
		JSqlObj.query( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 405, "hello" ).dispose();
		JSqlObj.query( "INSERT INTO JSqlTest ( col1, col2 ) VALUES (?,?)", 406, "world" ).dispose();
		
		JSqlResult r = JSqlObj.executeQuery( "SELECT * FROM JSqlTest" );
		assertNotNull( "SQL result returns as expected", r );
		
		r.fetchAllRows();
		
		assertEquals("via readRowCol",  404,           ((Number)r.readRowCol(0,"col1")).intValue() );
		assertEquals("via readRowCol",  "has nothing", r.readRowCol(0,"col2") );
		assertEquals("via readRowCol",  405,           ((Number)r.readRowCol(1,"col1")).intValue() );
		assertEquals("via readRowCol",  "hello",       r.readRowCol(1,"col2") );
		assertEquals("via readRowCol",  406,           ((Number)r.readRowCol(2,"col1")).intValue() );
		assertEquals("via readRowCol",  "world",       r.readRowCol(2,"col2") );
		
		assertEquals("via get().get()", 404,           ((Number)r.get("col1").get(0)).intValue() );
		assertEquals("via get().get()", "has nothing", r.get("col2").get(0) );
		assertEquals("via get().get()", 405,           ((Number)r.get("col1").get(1)).intValue() );
		assertEquals("via get().get()", "hello",       r.get("col2").get(1) );
		assertEquals("via get().get()", 406,           ((Number)r.get("col1").get(2)).intValue() );
		assertEquals("via get().get()", "world",       r.get("col2").get(2) );
		
		r.dispose();
	}
	//*/
}