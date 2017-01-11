package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;
import picoded.JStruct.*;

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
}
