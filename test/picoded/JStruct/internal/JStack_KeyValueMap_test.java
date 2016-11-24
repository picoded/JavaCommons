package picoded.JStruct.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;
import picoded.JSql.JSql;
import picoded.JStack.JStack;
import picoded.JStack.JStackLayer;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaTable;

public class JStack_KeyValueMap_test {
	
	JStack_KeyValueMap jStack_KeyValueMap = null;
	
	// JStack layering
	public JStack jsObj = null;
	
	@Before
	public void setUp() {
		implementationConstructor();
		jStack_KeyValueMap = new JStack_KeyValueMap(jsObj, "_oid");
	}
	
	@After
	public void tearDown() {
		
	}
	
	// Tablename to string
	private String tableName = TestConfig.randomTablePrefix();
	
	// Implementation
	private MetaTable implementationConstructor() {
		jsObj = new JStack(stackLayers());
		return jsObj.getMetaTable("MT_" + tableName);
	}
	
	private JStackLayer[] stackLayers() {
		return new JStackLayer[] { new JStruct(), JSql.sqlite() };
	}
	
	@Test
	public void name() {
		
	}
}
