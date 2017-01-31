package picoded.JSql.struct;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import picoded.TestConfig;
// Target test class
import picoded.JSql.*;
import picoded.JStruct.AtomicLongMap;
import picoded.JStruct.AtomicLongMap_test;

public class AtomicLongMap_Sqlite_test extends AtomicLongMap_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	@Override
	public AtomicLongMap implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getAtomicLongMap("ALM_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void getExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.get("ALM_" + tableName);
	}
	
	@Test(expected = RuntimeException.class)
	public void getAndAddExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.getAndAdd("key", "value");
	}
	
	@Test
	public void systemTeardownExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.systemTeardown();
	}
	
	@Test(expected = RuntimeException.class)
	public void maintenanceExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.maintenance();
	}
	
	@Test(expected = RuntimeException.class)
	public void putExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.put("key", 1);
	}
	
	@Test(expected = RuntimeException.class)
	public void putWithNumberParamExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		Number n = Integer.valueOf(1);
		jsObj.put("key", n);
	}
	
	@Test(expected = RuntimeException.class)
	public void putWithLongParamExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		Long l = Long.valueOf(1);
		jsObj.put("key", l);
	}
	
	@Test(expected = RuntimeException.class)
	public void putLongParamExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.put("key", 1l);
	}
	
	@Test(expected = RuntimeException.class)
	public void getAndIncrementExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.getAndIncrement(1l);
	}
	
	@Test(expected = RuntimeException.class)
	public void incrementAndGetExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.incrementAndGet(1l);
	}
	
	//@Test
	public void weakCompareAndSetTest() throws Exception {
		
		almObj.put("a", new Long(3));
		almObj.put("b", new Long(4));
		almObj.put("c", new Long(5));
		
		almObj.put("d", null);
		assertEquals(true, almObj.weakCompareAndSet("d", new Long(7), new Long(8)));
		assertEquals(new Long(8), almObj.get("d"));
	}
	
	@Test(expected = RuntimeException.class)
	public void weakCompareAndSetExceptionTest() {
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.weakCompareAndSet("d", new Long(7), new Long(8));
	}
	
	@Test
	public void getAndAddTest() {
		//JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "ALM_" + tableName);
		almObj.put("hello", 1);
		almObj.systemSetup();
		assertEquals(Long.valueOf(1), almObj.getAndAdd("hello", Long.valueOf(2)));
		assertEquals(Long.valueOf(3), almObj.get("hello"));
	}
	
	@Test(expected = Exception.class)
	public void systemSetupTest()throws Exception{
		JSql_AtomicLongMap jsObj = new JSql_AtomicLongMap(sqlImplmentation(), "");
		jsObj.sqlObj=null;
		jsObj.systemSetup();
	}
}
