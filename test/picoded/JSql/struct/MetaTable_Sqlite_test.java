package picodedTests.JSql.struct;

// Target test class
import picoded.JSql.*;
import picoded.JSql.struct.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

import picodedTests.JStruct.*;
import picodedTests.*;

public class MetaTable_Sqlite_test extends MetaTable_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	public MetaTable implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getMetaTable("MT_" + tableName);
	}
	
	// @Test
	// public void orderByTestLoop() {
	// 	for(int i=0; i<25; ++i) {
	// 		orderByTest();
	// 		
	// 		tearDown();
	// 		tableName = TestConfig.randomTablePrefix();
	// 		setUp();
	// 	}
	// }
}
