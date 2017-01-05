package picoded.JSql;

import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mysql;

public class CommonJSql_test {
	JSqlResult jSqlResult = null;
	JSql jSql = null;
	JSql_Mysql jSql_Mysql;
	protected static String testTableName = "JSqlTest_default";
	
	@Before
	public void setUp() {
		jSqlResult = new JSqlResult();
		jSql_Mysql = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		jSql = new JSql();
	}
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = "JSqlTest_" + TestConfig.randomTablePrefix();
		testTableName = testTableName.toUpperCase();
	}
	
	@Test(expected = Exception.class)
	public void rowCountTest() throws JSqlException {
		jSqlResult.rowCount();
		jSqlResult.readRow(10);
		jSqlResult.readRowCol(1, "name");
		jSqlResult.readCol("name");
		jSqlResult.fetchMetaData();
		jSqlResult.equals(null);
		jSqlResult.hashCode();
		jSql.isDisposed();
		jSql.setAutoCommit(false);
	}
	
	@Test(expected = Exception.class)
	public void getAutoCommitTest() throws JSqlException {
		jSql.getAutoCommit();
	}
	
	@Test(expected = Exception.class)
	public void commitTest() throws JSqlException {
		jSql.selectQuerySet(testTableName, "name");
		jSql.genericSqlParser(null);
		jSql.commit();
	}
	
	@Test(expected = Exception.class)
	public void createBlobTest() throws Exception {
		jSql.createBlob();
	}
	
	@Test(expected = Exception.class)
	public void executeQuery_metadataTest() throws Exception {
		jSql.executeQuery_metadata(null);
	}
	
	@SuppressWarnings("static-access")
	@Test(expected = Exception.class)
	public void jSqlTest() throws Exception {
		jSql.setConnectionProperties(null, null, null, null, null);
		jSql.sqlite(null);
		jSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(),
			TestConfig.MYSQL_PASS());
		jSql.recreate(false);
		String query = "insert into " + testTableName + " values(?, ?, ?, ?, ?, ?, ?)";
		String qString = "CREATE TABLE " + testTableName
			+ " (id int, first_name VARCHAR(15), last_name VARCHAR(15), "
			+ "start_date DATE, end_date DATE, salary FLOAT(8,2), city VARCHAR(10))";
		jSql_Mysql.executeQuery_raw(qString);
		jSql_Mysql.executeQuery_raw(query, 3, "James", "Smith", "19781212", "19900315", 6544.78,
			"Vancouver");
		jSql_Mysql.execute_raw("Select * From " + testTableName + " where id=?", 4);
		jSql_Mysql.execute_query("Select * From " + testTableName + " where id=4");
		Blob blob = jSql_Mysql.createBlob();
		jSql_Mysql.prepareSqlStatment(query, null, 1l, 1d, 1.0f, blob, new Object());
	}
	
	@Test(expected = Exception.class)
	public void executeQueryTest() throws JSqlException, SerialException, SQLException {
		jSql.executeQuery("Select * From " + testTableName + " where id=?", 4);
	}
	
	@Test(expected = Exception.class)
	public void queryTest() throws JSqlException, SerialException, SQLException {
		jSql.query("Select * From " + testTableName + " where id=?", 4);
	}
	
	@Test(expected = Exception.class)
	public void executeTest() throws JSqlException, SerialException, SQLException {
		jSql.execute("Select * From " + testTableName + " where id=?", 4);
	}
	
	@Test(expected = Exception.class)
	public void execute_queryTest() throws JSqlException, SerialException, SQLException {
		jSql_Mysql.execute_query("Select * From testTableName where id=");
	}
}
