package picoded.JSql;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mysql;

public class JSql_Mysql_test extends JSql_Sqlite_test {
	
	protected static String testTableName = "JSqlTest_Mysql_" + TestConfig.randomTablePrefix();
	
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		testTableName = testTableName.toUpperCase();
	}
	
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
	
	@Test(expected = Exception.class)
	public void upsertQuerySetDefault() throws JSqlException {
		row1to7setup();
		JSqlObj.executeQuery("DROP TABLE IF EXISTS `" + testTableName + "_1`").dispose(); //cleanup (just incase)
		JSqlObj.executeQuery(
			"CREATE TABLE IF NOT EXISTS " + testTableName + "_1 ( "
				+ "col1 INT PRIMARY KEY, col2 TEXT, col3 VARCHAR(50), col4 VARCHAR(100) )").dispose();
		String qString = "CREATE VIEW TEST AS  SELECT * FROM (";
		JSqlObj.execute(qString, "test");
	}
	
	@Test(expected = Exception.class)
	public void ConnectionTest() throws Exception {
		JSqlObj = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		JSqlObj = new JSql_Mysql("test-mysql.test", "test", "root", "admin");
	}
	
	@Test(expected = Exception.class)
	public void executeTest() throws JSqlException, Exception {
		String qString = "CREATE VIEW " + testTableName + "_View AS  SELECT * FROM (" + testTableName
			+ "_1 NATURAL FULL OUTER JOIN " + testTableName + "_View )";
		JSqlObj.execute(qString, "test");
	}
	
	@Test(expected = Exception.class)
	public void executeTest1() throws JSqlException, Exception {
		JSqlObj.execute("CREATE VIEW FROM", "test");
	}
	
	@Test(expected = Exception.class)
	public void getQStringUpperTest() throws JSqlException, Exception {
		JSql_Mysql jsqlMysql = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		jsqlMysql.getQStringUpper(null, null, null);
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("test", "test");
		metadata.put("TEXT", "TEXT");
		metadata.put("BLOB", "BLOB");
		String columns = "test, TEXT, BLOB";
		String qStringUpper = "test";
		jsqlMysql.getQStringUpper(metadata, qStringUpper, columns);
		JSqlObj.execute("INDEX IF NOT EXISTS", "test");
	}
	
	@Test(expected = Exception.class)
	public void getQStringUpperTest1() throws JSqlException, Exception {
		JSqlObj.execute("INDEX IF NOT EXISTS ON", "test");
	}
	
	@Test(expected = Exception.class)
	public void getQStringExecuteTest() throws JSqlException, Exception {
		String qString = "CREATE TABLE " + testTableName
			+ "(id int, first_name VARCHAR(15), last_name VARCHAR(15), "
			+ "start_date DATE, end_date DATE, salary FLOAT(8,2), city VARCHAR(10))";
		JSqlObj.execute(qString);
		qString = "DROP TABLE IF EXISTS Job";
		JSqlObj.execute(qString);
		qString = "CREATE TABLE Job (id int, title VARCHAR(20))";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values (1,'Jason', 'Martin', '19960725', '20060725', 1234.56, 'Toronto')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(2, 'Alison', 'Mathews', '19760321', '19860221', 6661.78, 'Vancouver')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(3, 'James', 'Smith', '19781212', '19900315', 6544.78, 'Vancouver')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(4, 'Celia', 'Rice', '19821024', '19990421', 2344.78, 'Vancouver')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(5, 'Robert', 'Black', '19840115', '19980808', 2334.78, 'Vancouver')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(6, 'Linda', 'Green', '19870730', '19960104', 4322.78, 'New York')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(7, 'David', 'Larry', '19901231', '19980212', 7897.78, 'New York')";
		JSqlObj.execute(qString);
		qString = "insert into "
			+ testTableName
			+ "(id,first_name, last_name, "
			+ "start_date, end_Date, salary, City) values(8, 'James', 'Cat', '19960917', '20020415', 1232.78, 'Vancouver')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (1,'Tester')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (2,'Accountant')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (3,'Developer')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (4,'Coder')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (5,'Director')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (6,'Mediator')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (7,'Proffessor')";
		JSqlObj.execute(qString);
		qString = "insert into job (id, title) values (8,'Programmer')";
		JSqlObj.execute(qString);
		qString = "CREATE VIEW " + testTableName + "_View AS SELECT testTableName.first_name, "
			+ "job.title FROM (SELECT " + testTableName + ".first_name, job.title FROM "
			+ testTableName + " join job))";
		JSqlObj.execute(qString);
	}
}
