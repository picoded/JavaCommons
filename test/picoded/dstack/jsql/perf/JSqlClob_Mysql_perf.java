package picoded.dstack.jsql.perf;

import picoded.dstack.jsql.connector.*;
import picoded.dstack.jsql.*;
import picoded.TestConfig;

/// [Mysql varient]
/// Testing of DataTable full CLOB structure performance
public class JSqlClob_Mysql_perf extends JSqlClob_perf {
	
	/// Note that this SQL connector constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public JSql jsqlConnection() {
		return JSqlTest.mysql();
	}
	
}