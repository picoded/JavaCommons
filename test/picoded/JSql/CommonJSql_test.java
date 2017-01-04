package picoded.JSql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;

public class CommonJSql_test {
	JSqlResult jSqlResult = null;
	ResultSet rs;
	PreparedStatement ps;
	
	@Before
	public void setUp() {
		jSqlResult = new JSqlResult();
		
	}
	
	@Test
	public void rowCountTest() throws JSqlException {
		jSqlResult.rowCount();
		jSqlResult.readRow(10);
		jSqlResult.readRowCol(1, "name");
		jSqlResult.readCol("name");
		jSqlResult.fetchMetaData();
	}
	
}
