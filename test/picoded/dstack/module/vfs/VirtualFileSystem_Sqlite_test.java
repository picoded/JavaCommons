package picoded.dstack.module.vfs;

// Target test class
import static org.junit.Assert.*;
import org.junit.*;

// Test depends
import java.util.*;
import picoded.dstack.*;
import picoded.dstack.jsql.*;
import picoded.dstack.jsql.connector.*;
import picoded.dstack.jsql.connector.db.*;
import picoded.dstack.module.vfs.*;
import picoded.dstack.struct.simple.*;
import picoded.TestConfig;

public class VirtualFileSystem_Sqlite_test extends VirtualFileSystem_test {

	// To override for implementation
	//-----------------------------------------------------

	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public JSql sqlConn() {
		return new JSql_Sqlite();
	}

	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public VirtualFileSystem implementationConstructor() {
		JSql conn = sqlConn();
		return new VirtualFileSystem( //
			new JSql_DataTable(conn, TestConfig.randomTablePrefix().toUpperCase()), //
			new JSql_DataTable(conn, TestConfig.randomTablePrefix().toUpperCase()) //
		);
	}
}