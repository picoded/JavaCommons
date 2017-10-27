package picoded.dstack.jsql;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

// Test Case include
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Test depends
import picoded.TestConfig;
import picoded.dstack.*;
import picoded.dstack.jsql.*;
import picoded.dstack.jsql.connector.*;
import picoded.dstack.struct.simple.*;

/// Utility test class to get the respective JSQL connection used in TESTING
public class JSqlTest {
	
	/// SQLite connection
	public static JSql sqlite() {
		return JSql.sqlite();
	}
	
	/// MYSQL connection
	public static JSql mysql() {
		return JSql.mysql(TestConfig.MYSQL_CONN(), TestConfig.MYSQL_DATA(), TestConfig.MYSQL_USER(),
			TestConfig.MYSQL_PASS());
	}

	/// MSSQL connection
	public static JSql mssql() {
		return JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(), TestConfig.MSSQL_USER(),
			TestConfig.MSSQL_PASS());
	}
}