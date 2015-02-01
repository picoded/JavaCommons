package picodedTests.jSql;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.jSql.*;
import picodedTests.jSql.JSql_Sqlite_test;
import picodedTests.TestConfig;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp(){
		//create connection
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
	}
	
	/// This is the base execute sql test example, in which other examples are built on
	@Test
	public void executeQuery() throws JSqlException{
		JSqlObj.executeQuery( "DROP TABLE IF EXISTS '"+testTableName+"'").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery( "CREATE TABLE IF NOT EXISTS \""+testTableName+"\" ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //valid table creation : no exception
		JSqlObj.executeQuery( "CREATE TABLE IF NOT EXISTS "+testTableName+" ( col1 INT PRIMARY KEY, col2 TEXT )").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery( "TRUNCATE TABLE "+testTableName+"").dispose(); //run twice to ensure "IF NOT EXISTS" works
		
		JSqlObj.executeQuery( "INSERT INTO "+testTableName+" ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ).dispose();
	}
	
	/// Modified duplicate of executeQuery set
	@Test
	public void execute() throws JSqlException{
      
		assertTrue(JSqlObj.execute( "DROP TABLE IF EXISTS "+testTableName+"")); //cleanup (just incase)
		
		assertTrue(JSqlObj.execute( "CREATE TABLE IF NOT EXISTS "+testTableName+" ( col1 INT PRIMARY KEY, col2 TEXT )")); //valid table creation : no exception
		assertTrue(JSqlObj.execute( "CREATE TABLE IF NOT EXISTS "+testTableName+" ( col1 INT PRIMARY KEY, col2 TEXT )")); //run twice to ensure "IF NOT EXISTS" works
		
		assertTrue(JSqlObj.execute( "TRUNCATE TABLE "+testTableName+"")); //cleanup (just incase)
		
		assertTrue(JSqlObj.execute( "INSERT INTO "+testTableName+" ( col1, col2 ) VALUES (?,?)", 404, "has nothing" ));
	}
	
}
