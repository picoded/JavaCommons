package picoded.dstack.jsql.perf;

import picoded.dstack.jsql.connector.*;
import picoded.dstack.jsql.*;
import picoded.TestConfig;

/// [Mysql varient]
/// Testing of metatable full indexless fixed table performance
public class JSqlFixedIndexless_Mysql_perf extends JSqlFixedIndexless_perf {

	/// Note that this SQL connector constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public JSql jsqlConnection() {
		return JSqlTest.mysql();
	}
	
}