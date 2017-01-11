package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;

public class MetaTable_Mysql_test extends MetaTable_Sqlite_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	@Override
	public JSql sqlImplmentation() {
		return JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
}
