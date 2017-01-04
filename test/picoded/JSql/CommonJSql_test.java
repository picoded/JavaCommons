package picoded.JSql;

import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.db.JSql_Mysql;

public class CommonJSql_test {
	JSqlResult jSqlResult = null;
	JSql jSql = null;
	JSql_Mysql jSql_Mysql;
	
	@Before
	public void setUp() {
		jSqlResult = new JSqlResult();
		jSql_Mysql = new JSql_Mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(), TestConfig.MYSQL_PASS());
		jSql = new JSql();
	}
	
	@Test
	public void rowCountTest() throws JSqlException {
		jSqlResult.rowCount();
		jSqlResult.readRow(10);
		jSqlResult.readRowCol(1, "name");
		jSqlResult.readCol("name");
		jSqlResult.fetchMetaData();
		jSqlResult.equals(null);
		jSqlResult.hashCode();
	}
	
	@SuppressWarnings("static-access")
	@Test(expected = Exception.class)
	public void jSqlTest() throws JSqlException, SerialException, SQLException {
		jSql.setConnectionProperties(null, null, null, null, null);
		jSql.sqlite(null);
		jSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(),
			TestConfig.MYSQL_PASS());
		jSql.recreate(false);
		String query = "insert into tableName values(?,?,?,?,?,?)";
		jSql_Mysql.prepareSqlStatment(query, null, 1l, 1d, 1.0f, new SerialBlob("test".getBytes()),
			new Object());
	}
}
