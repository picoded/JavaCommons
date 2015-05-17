package picodedTests.JStack;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;

import java.util.Random;
import org.apache.commons.lang3.RandomUtils;

import picodedTests.TestConfig;

public class MetaTable_Mssql_test extends MetaTable_Sqlite_test {
	
	// JStack setup
	//-----------------------------------------------
	protected void JStackSetup() {
		JStackObj = new JStack(
									  JSql.mssql(TestConfig.MSSQL_CONN(), TestConfig.MSSQL_NAME(), TestConfig.MSSQL_USER(), TestConfig
													 .MSSQL_PASS())
		);
	}
	
	protected void JStackTearDown() {
		JStackObj = null;
	}
	
}