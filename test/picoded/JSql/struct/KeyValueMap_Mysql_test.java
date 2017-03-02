package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;

public class KeyValueMap_Mysql_test extends KeyValueMap_Sqlite_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	@Override
	public JSql sqlImplmentation() {
		return JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
}
