package picoded.JSql.struct;

// Target test class
import picoded.TestConfig;
import picoded.JSql.*;
import picoded.JStruct.*;

// Test Case include

public class KeyValueMap_Sqlite_test extends KeyValueMap_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	public JSql sqlImplmentation() {
		return JSql.sqlite();
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	@Override
	public KeyValueMap implementationConstructor() {
		JSqlStruct jsObj = new JSqlStruct(sqlImplmentation());
		return jsObj.getKeyValueMap("KVM_" + tableName);
	}
	
}
