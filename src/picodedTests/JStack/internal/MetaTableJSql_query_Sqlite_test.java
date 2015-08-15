package picodedTests.JStack.internal;

import org.junit.*;

import static org.junit.Assert.*;

import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.JStack.internal.*;
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;

import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

import picodedTests.TestConfig;
import picodedTests.JStack.*;

public class MetaTableJSql_query_Sqlite_test extends JStackData_testBase_test {
	
	// Setup test
	//-----------------------------------------------

	protected MetaTableJSql_columnTypes colTypes = new MetaTableJSql_columnTypes();
	
	/// The table name to test
	protected String mtTableName = null;
	
	@Override
	public void testObjSetup() throws JStackException {
		mtTableName = "M" + TestConfig.randomTablePrefix();
		assertNotNull(JSqlObj);
		
		try {
			MetaTableJSql_query.JSqlSetup(JSqlObj, mtTableName, colTypes);
		} catch(JSqlException e) {
			throw new JStackException(e);
		}
	}

	@Override
	public void testObjTeardown() throws JStackException {
		
	}

	
}
