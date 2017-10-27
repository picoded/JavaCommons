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

public class JSql_KeyValueMap_test extends StructSimple_KeyValueMap_test {
	
	// To override for implementation
	//-----------------------------------------------------
	
	/// Note that this SQL connector constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public JSql jsqlConnection() {
		return JSqlTest.sqlite();
	}
	
	/// Impomentation constructor for SQL
	public KeyValueMap implementationConstructor() {
		return new JSql_KeyValueMap(jsqlConnection(), TestConfig.randomTablePrefix());
	}
	
}