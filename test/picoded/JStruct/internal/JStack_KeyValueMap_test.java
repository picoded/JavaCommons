package picoded.JStruct.internal;

import static org.junit.Assert.assertNotNull;

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
	
	private JStackLayer[] stackLayersNull() {
		return new JStackLayer[] { null };
	}
	
	@Test
	public void implementationLayersTest() {
		JStruct_KeyValueMap[] implementationLayer = {};
		assertNotNull(implementationLayer = jStack_KeyValueMap.implementationLayers());
		assertNotNull(new JStack_KeyValueMap(new JStack(stackLayersNull()), "hello123")
			.implementationLayers());
		jStack_KeyValueMap.implementationLayer = implementationLayer;
		assertNotNull(jStack_KeyValueMap.implementationLayers());
	}
	
	@Test
	public void implementationLayersReverseTest() {
		JStruct_KeyValueMap[] implementationLayersReversed = {};
		assertNotNull(implementationLayersReversed = jStack_KeyValueMap
			.implementationLayers_reverse());
		jStack_KeyValueMap.implementationLayersReversed = implementationLayersReversed;
		assertNotNull(jStack_KeyValueMap.implementationLayers_reverse());
	}
	
	@Test(expected = Exception.class)
	public void maintenanceTest() throws Exception {
		jStack_KeyValueMap.maintenance();
	}
	
	@Test(expected = Exception.class)
	public void incrementalMaintenanceTest() throws Exception {
		jStack_KeyValueMap.incrementalMaintenance();
	}
	
	@Test
	public void clearTest() {
		jStack_KeyValueMap.clear();
	}
	
	@Test(expected = Exception.class)
	public void getExpiryRawTest() throws Exception {
		assertNotNull(jStack_KeyValueMap.getExpiryRaw("value"));
	}
	
	@Test(expected = Exception.class)
	public void setExpiryRawTest() throws Exception {
		jStack_KeyValueMap.setExpiryRaw("value", 123l);
	}
	
	@Test(expected = Exception.class)
	public void getValueRawTest() throws Exception {
		assertNotNull(jStack_KeyValueMap.getValueRaw("value", 0l));
	}
	
	@Test(expected = Exception.class)
	public void setValueRawTest() throws Exception {
		jStack_KeyValueMap.setValueRaw("value", "123l", 1);
	}
	
	@Test(expected = Exception.class)
	public void getKeysTest() throws Exception {
		assertNotNull(jStack_KeyValueMap.getKeys("value"));
	}
	
	@Test(expected = Exception.class)
	public void removeTest() throws Exception {
		assertNotNull(jStack_KeyValueMap.remove("value"));
	}
	
	@Test(expected = Exception.class)
	public void systemSetupTest() throws Exception {
		jStack_KeyValueMap.systemSetup();
		jStack_KeyValueMap.systemTeardown();
	}
}
