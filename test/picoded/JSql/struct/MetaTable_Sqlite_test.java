package picoded.JSql.struct;

// Target test class
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.*;
import picoded.JStruct.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetaTable_Sqlite_test extends MetaTable_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	@Override
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
	
	@Test
	public void systemSetupExceptionTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.sqlTableName = tableName;
		jsObj.systemSetup();
	}
	
	@Test
	public void systemTeardownExceptionTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.systemTeardown();
	}
	
	@Test(expected = RuntimeException.class)
	public void keySetExceptionTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.keySet();
	}
	
	@Test(expected = RuntimeException.class)
	public void removeExceptionTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.remove("key");
	}
	
	@Test(expected = RuntimeException.class)
	public void metaObjectRemoteDataMap_updateTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		Map<String, Object> fullMap = new HashMap<String, Object>();
		fullMap.put("key", "value");
		Set<String> keys = new HashSet<String>();
		keys.add("key");
		jsObj.metaObjectRemoteDataMap_update("_oid", fullMap, keys);
	}
	
	@Test
	public void queryKeysTest() {
		String whereClause = "_id=?";
		Object[] whereValues = new Object[] { 1 };
		String orderByStr = "ASC";
		int offset = 1;
		int limit = 10;
		assertNotNull(mtObj.queryKeys(whereClause, whereValues, orderByStr, offset, limit));
	}
	
	@Test(expected = RuntimeException.class)
	public void getKeyNamesExceptionTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.getKeyNames();
	}
	
	@Test
	public void getKeyNamesJSqlResultIsNullTest() {
		Set<String> keyNames = mtObj.getKeyNames();
		Set<String> expected = new HashSet<String>(Arrays.asList(new String[] {}));
		assertNotNull(keyNames);
		assertEquals(keyNames, expected);
	}
	
	@Test(expected = RuntimeException.class)
	public void systemSetupTest() {
		JSql_MetaTable jsObj = new JSql_MetaTable(sqlImplmentation(), "");
		jsObj.sqlObj = null;
		jsObj.systemSetup();
	}
}
