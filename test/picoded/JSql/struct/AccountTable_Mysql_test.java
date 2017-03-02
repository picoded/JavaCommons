package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;

// Test Case include
import org.junit.*;

import static org.junit.Assert.*;

// Test depends

public class AccountTable_Mysql_test extends AccountTable_Sqlite_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	@Override
	public JSql sqlImplmentation() {
		return JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
}
