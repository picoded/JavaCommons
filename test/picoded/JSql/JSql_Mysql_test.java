package picoded.JSql;

import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mysql;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	@Before
	public void setUp() {
		//create connection
		JSqlObj = JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
	@Test
	public void upsertQuerySetWithDefault() throws JSqlException {
		row1to7setup();
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1 ( "
				+ "col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(50) default '1')")
			.dispose();
		JSqlObj.recreate(false);
	}
	
	@Test
	public void upsertQuerySetDefault() throws JSqlException {
		row1to7setup();
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1 ( "
				+ "col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )").dispose();
	}
	
	@Test(expected = Exception.class)
	public void ConnectionTest() throws Exception {
		JSqlObj = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		JSqlObj = new JSql_Mysql("test-mysql.test", "test", "root", "admin");
	}
	
	@Test(expected = JSqlException.class)
	public void executeTest() throws JSqlException {
		String qString = "CREATE VIEW " + testTableName + "_View AS  SELECT * FROM (" + testTableName
			+ "_1 NATURAL FULL OUTER JOIN " + testTableName + "_View )";
		JSqlObj.execute(qString, "test");
	}
}
