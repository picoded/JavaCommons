package picoded.JSql.struct.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.struct.JSql_MetaTable;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaType;
import picoded.JStruct.MetaTypeMap;
import picoded.JStruct.internal.JStruct_MetaObject;

public class JSql_MetaTableUtils_test {
	
	protected static String testTableName = "JSql_MetaTableUtils_" + TestConfig.randomTablePrefix();
	protected JSql JSqlObj;
	
	@Before
	public void setUp() {
		JSqlObj = JSql.sqlite();
	}
	
	@Test(expected = IllegalAccessError.class)
	public void constructorTest() throws JSqlException {
		 new JSql_MetaTableUtils();
	}
	
	@Test
	public void fetchResultPositionTest() throws Exception {
		TempClass temp = new TempClass();
		assertEquals(-1, JSql_MetaTableUtils.fetchResultPosition(temp.getResultSet(), "key", "key", 1));
	}
	
	//@Test
	public void extractKeyValueTest() throws JSqlException {
		TempClass temp = new TempClass();
		assertEquals("value", JSql_MetaTableUtils.extractKeyValue(temp.getResultSet(), "key"));
	}
	
	@Test
	public void valueToOptionSetTest() {
		MetaTypeMap mtm = new MetaTypeMap();
		String key = "key";
		Object value = Integer.valueOf(1);
		assertEquals(21, JSql_MetaTableUtils.valueToOptionSet(mtm, key, value)[0]);
		value = Float.valueOf(1);
		assertEquals(24, JSql_MetaTableUtils.valueToOptionSet(mtm, key, value)[0]);
		value = Double.valueOf(1);
		assertEquals(23, JSql_MetaTableUtils.valueToOptionSet(mtm, key, value)[0]);
		value = "1";
		assertEquals(25, JSql_MetaTableUtils.valueToOptionSet(mtm, key, value)[0]);
		assertEquals(33, JSql_MetaTableUtils.valueToOptionSet(mtm, key, "1".getBytes())[0]);
	}
	
	@Test
	public void valueToMetaTypeTest() {
		Object value = Integer.valueOf(1);
		assertEquals(MetaType.INTEGER, JSql_MetaTableUtils.valueToMetaType(value));
		value = Float.valueOf(1);
		assertEquals(MetaType.FLOAT, JSql_MetaTableUtils.valueToMetaType(value));
		value = Double.valueOf(1);
		assertEquals(MetaType.DOUBLE, JSql_MetaTableUtils.valueToMetaType(value));
		value = "1";
		assertEquals(MetaType.STRING, JSql_MetaTableUtils.valueToMetaType(value));
		assertEquals(MetaType.BINARY, JSql_MetaTableUtils.valueToMetaType("1".getBytes()));
		value = Long.valueOf(1);
		assertEquals(MetaType.LONG, JSql_MetaTableUtils.valueToMetaType(value));
		value = new ArrayList<>();
		assertNull(JSql_MetaTableUtils.valueToMetaType(value));
	}
	
	@Test(expected = RuntimeException.class)
	public void getOrderByObjectTest() {
		JSql_MetaTableUtils.getOrderByObject(" ");
		
	}
	
	@Test
	public void shortenStringValueTest() {
		assertEquals(
			64,
			JSql_MetaTableUtils.shortenStringValue(
				"1234567890a 1234567890a 1234567890a 1234567890a 1234567890a 1234567890a 1234567890a")
				.length());
		
	}
	
	private class TempClass extends JSql_MetaTable {
		public TempClass() {
			super(JSql.sqlite(), "MT_" + TestConfig.randomTablePrefix());
		}
		
		public JSqlResult getResultSet() throws JSqlException {
			systemSetup();
			Map<String, Object> deltaDataMap = new HashMap<String, Object>();
			Object object = null;
			deltaDataMap.put("key5", object = new Object());
			JStruct_MetaObject jStruct_MetaObject = new JStruct_MetaObject(
				(new JStruct()).getMetaTable("test"), "_oid", deltaDataMap, true);
			jStruct_MetaObject.put("key5", object);
			return sqlObj.selectQuerySet(sqlTableName, "DISTINCT kID").query();
		}
	}
	
	@Test(expected = Exception.class)
	public void fetchResultPositionTest1() throws Exception, JSqlException {
		JSqlObj.recreate(true);
		JSqlObj.query("DROP TABLE IF EXISTS " + testTableName + "").dispose();
		JSqlObj.query(
			"CREATE TABLE IF NOT EXISTS " + testTableName + " ( oID INT PRIMARY KEY, kID INT , idx INT, typ varchar(255))")
			.dispose();
		JSqlResult jSqlResult = JSqlObj.query("SELECT * FROM " + testTableName);
		JSql_MetaTableUtils.fetchResultPosition(jSqlResult, "kID", 401);
		JSql_MetaTableUtils.extractKeyValue(jSqlResult, "kID");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ ) VALUES (?,?,?,?)", 401, 401, 401, "11");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ  ) VALUES (?,?,?,?)", 402, 402, 402, "11.0f");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ  ) VALUES (?,?,?,?)", 403, 403, 403, "11.0d");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ  ) VALUES (?,?,?,?)", 404, 404, 404, "11l");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ  ) VALUES (?,?,?,?)", 405, 405, 405, "11");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ ) VALUES (?,?,?,?)", 406, 406, 406, "11");
		JSqlObj.execute("INSERT INTO " + testTableName + " ( oID, kID, idx, typ  ) VALUES (?,?,?,?)", 0, 0, 0, "11");
		jSqlResult = JSqlObj.query("SELECT * FROM " + testTableName);
		JSql_MetaTableUtils.fetchResultPosition(jSqlResult, "kID", 401);
		JSql_MetaTableUtils.fetchResultPosition(jSqlResult, "401", "401", 401);
		JSql_MetaTableUtils.fetchResultPosition(jSqlResult, "kID", "kID", 123);
		JSql_MetaTableUtils.fetchResultPosition(jSqlResult, "401", 401);
		JSql_MetaTableUtils.extractKeyValue(jSqlResult, "0");
		JSql_MetaTableUtils.JSqlObjectMapFetch( null, null, null, null, null );
	}
	
	@Test(expected = Exception.class)
	public void JSqlObjectMapFetchTest() throws Exception, JSqlException {
		JSql_MetaTableUtils.JSqlObjectMapFetch( null, JSqlObj, "test123", "test", new HashMap<String, Object>() );
	}
}
