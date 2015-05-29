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

public class MetaTable_Oracle_test extends MetaTable_Sqlite_test {
	
	// JStack setup
	//-----------------------------------------------
	protected void JStackSetup() {
		JStackObj = new JStack(
			 JSql.oracle(TestConfig.ORACLE_PATH(), TestConfig.ORACLE_USER(), TestConfig.ORACLE_PASS())
		);
	}
	
	protected void JStackTearDown() {
		JStackObj = null;
	}
	
}