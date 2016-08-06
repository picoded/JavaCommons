package picodedTests.JSql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlQuerySet;
import picoded.JSql.JSqlResult;
import picodedTests.TestConfig;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
	@Test
	public void upsertQuerySetWithDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(50) default '1')").dispose(); //valid table creation : no exception
		
		//TODO Not working Upsert for MySQL
		
		//Upsert query
		//		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
		//			testTableName + "_1", //
		//			new String[] { "col1" }, new Object[] { 404 }, //
		//			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
		//			new String[] { "col2" }, new Object[] { "not found" },  //
		//			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
		//			new String[] { "col3" }, new Object[] { "3 not found" },
		//			new String[] { "col4" } //
		//			));
		//		assertTrue("SQL result should return true", qSet.execute());
		//		
		//		assertNotNull("query should return a JSql result",
		//			r = JSqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		//		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		//		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
		//		assertEquals("Upsert value check failed",  null, r.readRowCol(0, "col4")); 
	}
	
	@Test
	public void upsertQuerySetDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )").dispose(); //valid table creation : no exception
		
		//TODO Not working Upsert for MySQL
		
		//		//Upsert query
		//		assertNotNull(qSet = JSqlObj.upsertQuerySet( //
		//			testTableName + "_1", //
		//			new String[] { "col1" }, new Object[] { 404 }, //
		//			//new String[] { "col2", "col3" }, new Object[] { "not found", "not found" },  //
		//			new String[] { "col2" }, new Object[] { "not found" },  //
		//			//new String[] { "col4", "col5" }, new Object[] { "not found", "not found" },
		//			new String[] { "col3" }, new Object[] { "3 not found" },
		//			new String[] { "col4" } //
		//			));
		//		assertTrue("SQL result should return true", qSet.execute());
		//		
		//		assertNotNull("query should return a JSql result",
		//			r = JSqlObj.query("SELECT * FROM " + testTableName + "_1 ORDER BY col1 ASC"));
		//		assertEquals("Upsert value check failed", 404, ((Number) r.readRowCol(0, "col1")).intValue());
		//		assertEquals("Upsert value check failed", "not found", r.readRowCol(0, "col2"));
		//		assertEquals("Upsert value check failed", null, r.readRowCol(0, "col4"));
	}
}
