package picoded.JSql.struct;

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
	
	public AtomicLongMap implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getAtomicLongMap("ALM_" + tableName);
	}
	
}
