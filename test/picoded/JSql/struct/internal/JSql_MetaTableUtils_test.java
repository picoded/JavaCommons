package picoded.JSql.struct.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.struct.JSqlStruct;
import picoded.JSql.struct.JSql_KeyValueMap;
import picoded.JSql.struct.JSql_MetaTable;
import picoded.JStruct.AccountObject;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaType;
import picoded.JStruct.MetaTypeMap;
import picoded.JStruct.internal.JStruct_MetaObject;

public class JSql_MetaTableUtils_test {
	
	@Test(expected = IllegalAccessError.class)
	public void constructorTest() throws JSqlException {
		JSql_MetaTableUtils temp = new JSql_MetaTableUtils();
	}
	
	@Test (expected = Exception.class)
	public void fetchResultPositionTest() throws Exception {
		TempClass temp = new TempClass();
		assertEquals(1, JSql_MetaTableUtils.fetchResultPosition(temp.getResultSet(), "key", "key", 1));
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
}
