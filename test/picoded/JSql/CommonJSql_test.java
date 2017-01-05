package picoded.JSql;

import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mysql;
import picoded.JSql.db.JSql_Sqlite;

public class CommonJSql_test {
	JSqlResult jSqlResult = null;
	JSql jSql = null;
	JSql_Mysql jSql_Mysql;
	JSql_Sqlite jSql_Sqlite = null;
	protected static String testTableName = "JSqlTest_default";
	
	@Before
	public void setUp() {
		jSqlResult = new JSqlResult();
		jSql_Mysql = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		jSql = new JSql();
		jSql_Sqlite = new JSql_Sqlite();
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
	
	@Test(expected = Exception.class)
	public void createTableIndexQuerySet() throws Exception {
		jSql.createTableIndexQuerySet("this is the table nname " + testTableName, "test", null, null);
		
		jSql.selectQuerySet("this is the table nname " + testTableName, null, null, null, null, 0, 0);
		
		jSql.createTableIndexQuerySet("this is the table nname " + testTableName, "test", "", "");
		jSql.deleteQuerySet(testTableName, "name", new Object[] { 4 });
		
		jSql.selectQuerySet("this is the table nname " + testTableName, "", "", new Object[] { "" },
			"ab", 0, 0);
		
		jSql.deleteQuerySet("this is the table nname " + testTableName, null, null);
		jSql.deleteQuerySet("this is the table nname " + testTableName, "abc", new Object[] {});
		
		jSql.selectQuerySet("this is the table nname " + testTableName, "abca", "abca",
			new Object[] { "abca" }, "abca", 0, 0);
		
		jSql.createTableQuerySet("tableName", new String[] { "test" }, new String[] {});
	}
	
	@Test(expected = Exception.class)
	public void createTableIndexQuerySet1() throws Exception {
		jSql.createTableQuerySet("tableName", null, new String[] {});
	}
	
	@Test(expected = Exception.class)
	public void createTableIndexQuerySet2() throws Exception {
		jSql.createTableQuerySet("tableName", new String[] {}, new String[] {});
		jSql.createTableQuerySet("tableName", new String[] {}, null);
	}
	
	@Test(expected = Exception.class)
	public void execute_query() throws Exception {
		jSql.execute_query(null);
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
	
	@Test(expected = Exception.class)
	public void upsertQuerySet1() throws JSqlException {
		String tableName = "Select * From testTableName where id=";
		String[] uniqueColumns = new String[] {};
		Object[] uniqueValues = new Object[] {};
		String[] insertColumns = new String[] {};
		Object[] insertValues = new Object[] {};
		String[] defaultColumns = new String[] {};
		Object[] defaultValues = new Object[] {};
		String[] miscColumns = new String[] {};
		jSql.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
		uniqueValues = new Object[] { "tets" };
		
		jSql.upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues,
			defaultColumns, defaultValues, miscColumns);
		
	}
	
}
