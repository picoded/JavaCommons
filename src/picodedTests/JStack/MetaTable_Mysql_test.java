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

public class MetaTable_Mysql_test extends MetaTable_Sqlite_test {
	
	// JSql override setup
	//-----------------------------------------------
	public JSql JSqlObj() {
		return JSql.mysql(TestConfig.MYSQL_CONN_JDBC(), TestConfig.MYSQL_CONN_PROPS());
	}
	
}