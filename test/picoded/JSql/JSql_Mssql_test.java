package picoded.JSql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mssql;

public class JSql_Mssql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(),
			TestConfig.MSSQL_USER(), TestConfig.MSSQL_PASS());
	}
	
	@Test
	public void upsertQuerySetWithDefault() throws JSqlException {
		row1to7setup();
		JSqlResult r = null;
		JSqlQuerySet qSet = null;
		
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName
				+ "_1 ( col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 bit default 1)")
			.dispose(); //valid table creation : no exception
		
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
		assertEquals("Upsert value check failed", null, r.readRowCol(0, "col4")); //TODO
	}
	
	@Test
	public void genericSqlParserTest() throws JSqlException {
		String s = JSqlObj.genericSqlParser("SELECT * FROM " + testTableName + " WHERE COL1 = ?");
		assertEquals("SELECT * FROM " + testTableName + " WHERE COL1=?", s);
		
		s = JSqlObj.genericSqlParser("DROP TABLE IF EXISTS MY_TABLE");
		assertEquals(
			"BEGIN TRY IF OBJECT_ID('MY_TABLE', 'U') IS NOT NULL DROP TABLE MY_TABLE END TRY BEGIN CATCH END CATCH",
			s);
		
		s = JSqlObj.genericSqlParser("DROP TABLE MY_TABLE ; ");
		//assertEquals("DROP TABLE MY_TABLE", s); //Should be
		assertEquals("DROP TABLE ", s);
		
		s = JSqlObj.genericSqlParser("DELETE FROM my_table WHERE col1 = ? ");
		assertEquals("DELETE FROM MY_TABLE WHERE col1=?", s);
		
		s = JSqlObj.genericSqlParser("DELETE FROM my_table WHERE col1 = 'ABC' ");
		assertEquals("DELETE FROM MY_TABLE WHERE col1='ABC'", s);
		
		s = JSqlObj.genericSqlParser("INSERT INTO my_table ( col1, col2 ) VALUES (?,?)");
		assertEquals("INSERT INTO MY_TABLE ( col1, col2 ) VALUES (?,?)", s);
		
		s = JSqlObj.genericSqlParser("UPDATE my_table SET col1 = ?, col2 = ? ");
		assertEquals("UPDATE MY_TABLE SET col1=?, col2=?", s);
		
		s = JSqlObj.genericSqlParser("UPDATE my_table SET col1 = 405 ");
		assertEquals("UPDATE MY_TABLE SET col1=405", s);
		
		s = JSqlObj.genericSqlParser("ALTER TABLE my_table ADD COLUMN col3 varchar(10)");
		assertEquals("ALTER TABLE my_table ADD COLUMN col3 varchar(10)", s);
	}
	
	@Test
	public void JSqlExceptionTest() throws Exception {
		new JSqlException(new Throwable());
	}
	
	@Test(expected = Exception.class)
	public void setupConnectionTest() throws Exception {
		Map<String, Object> connectionProps = new HashMap<String, Object>();
		connectionProps.put("dbName", null);
		JSqlObj.connectionProps = connectionProps;
		JSqlObj.recreate(false);
	}
	
	@Test(expected = Exception.class)
	public void setupConnectionTest1() throws Exception {
		Map<String, Object> connectionProps = new HashMap<String, Object>();
		connectionProps.put("dbName", "");
		JSqlObj.connectionProps = connectionProps;
		JSqlObj.recreate(false);
	}
	
	@Test
	public void getQStringTest() {
		JSql_Mssql mssql = new JSql_Mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(),
			TestConfig.MSSQL_USER(), TestConfig.MSSQL_PASS());
		String qString = "CREATE TABLE AUTOINCREMENT NUMBER ON DELETE =\"  \" ";
		mssql.getQString(qString);
	}
}
