package picoded.JSql.struct.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.struct.JSqlStruct;
import picoded.JSql.struct.JSql_KeyValueMap;
import picoded.JStruct.MetaType;
import picoded.JStruct.MetaTypeMap;

public class JSql_MetaTableUtils_test {
	
	@Test(expected = IllegalAccessError.class)
	public void constructorTest() throws JSqlException {
		JSql_MetaTableUtils temp = new JSql_MetaTableUtils();
	}
	
	//@Test
	public void fetchResultPositionTest() throws JSqlException {
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
	}
	
	@Test(expected = RuntimeException.class)
	public void getOrderByObjectTest() {
		JSql_MetaTableUtils.getOrderByObject(" ");
		
	}
	
	private class TempClass extends JSql_KeyValueMap {
		public TempClass() {
			super(JSql.sqlite(), "KVM_" + TestConfig.randomTablePrefix());
		}
		
		public JSqlResult getResultSet() throws JSqlException {
			systemSetup();
			put("key", "value");
			return sqlObj.selectQuerySet(sqlTableName, "eTm", "kID=?", new Object[] { "key" }).query();
		}
	}
}
