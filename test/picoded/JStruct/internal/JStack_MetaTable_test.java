package picoded.JStruct.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.JSql.JSql;
import picoded.JStack.JStack;
import picoded.JStack.JStackLayer;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaTable;
import picoded.conv.GUID;

public class JStack_MetaTable_test {
	
	JStack_MetaTable jStack_MetaTable = null;
	
	@Before
	public void setUp() {
		jStack_MetaTable = new JStack_MetaTable();
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void jStack_MetaTable() {
		JStackLayer jStackLayer = new JStackLayer() {
		};
		JStack jStructObj = new JStack(jStackLayer);
		new JStack_MetaTable(jStructObj, "_oid");
	}
	
	// / To override for implementation
	// /------------------------------------------------------
	public MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}
	
	@Test
	public void implementationLayersTest() {
		JStackLayer jStackLayer = new JStackLayer() {
		};
		JStack jStructObj = new JStack(jStackLayer);
		jStack_MetaTable.stackObj = jStructObj;
		assertNotNull(jStack_MetaTable.implementationLayers());
		jStack_MetaTable = new JStack_MetaTable(jStructObj, "test");
		jStack_MetaTable.stackObj = jStructObj;
		assertNotNull(jStack_MetaTable.implementationLayers());
		JStruct_MetaTable[] _implementationLayers = { jStack_MetaTable };
		jStack_MetaTable.implementationLayer = _implementationLayers;
		assertNotNull(jStack_MetaTable.implementationLayers());
		assertNotNull(jStack_MetaTable.implementationLayersReversed = jStack_MetaTable
			.implementationLayers_reverse());
		assertNotNull(jStack_MetaTable.implementationLayers_reverse());
		jStack_MetaTable.implementationLayer = null;
		JStack jStack = new JStack(stackLayers());
		jStack_MetaTable.stackObj = jStack;
		assertNotNull(jStack_MetaTable.implementationLayers());
		
	}
	
	@Test
	public void keySetTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		_valueMap.put("test", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayersReversed = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayersReversed = implementationLayersReversed;
		assertNotNull(jStack_MetaTable.keySet());
	}
	
	@Test
	public void removeTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		_valueMap.put("test1", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayersReversed = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayersReversed = implementationLayersReversed;
		assertNull(jStack_MetaTable.remove("test1"));
	}
	
	@Test
	public void metaObjectRemoteDataMap_updateTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		_valueMap.put("test1", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayersReversed = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayersReversed = implementationLayersReversed;
		String _oid = GUID.base58();
		Map<String, Object> fullMap = new HashMap<String, Object>();
		fullMap.put("key", "value");
		fullMap.put("key1", "value");
		fullMap.put("key2", "value");
		Set<String> keys = new HashSet<String>();
		keys.add(_oid);
		jStack_MetaTable.metaObjectRemoteDataMap_update(_oid, fullMap, keys);
		jStack_MetaTable.implementationLayer = implementationLayersReversed;
		assertNotNull(jStack_MetaTable.metaObjectRemoteDataMap_get(_oid));
		assertNull(jStack_MetaTable.metaObjectRemoteDataMap_get("test"));
		jStack_MetaTable.implementationLayer = null;
		assertNull(jStack_MetaTable.metaObjectRemoteDataMap_get("test"));
	}
	
	private JStackLayer[] stackLayers() {
		return new JStackLayer[] { new JStruct(), JSql.sqlite() };
	}
	
	@Test
	public void queryKeysTest() {
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		JStruct_MetaTable[] implementationLayersReversed = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayersReversed = implementationLayersReversed;
		assertNotNull(jStack_MetaTable.queryKeys("num > ? AND num < ?", new Object[] { 2, 5 },
			"num ASC", 2, 2));
		assertNotNull(jStack_MetaTable.queryKeys(null, new Object[] { 2, 5 }, "num ASC", 2, 2));
		assertNotNull(jStack_MetaTable.query("num > ? AND num < ?", new Object[] { 2, 5 }, "num ASC",
			2, 2));
		assertNotNull(jStack_MetaTable.queryCount("num > ? AND num < ?", new Object[] { 2, 5 }));
	}
	
	@Test
	public void systemSetupTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		_valueMap.put("test", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayer = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayer = implementationLayer;
		jStack_MetaTable.systemSetup();
		
	}
	
	@Test
	public void systemTeardownTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		_valueMap.put("test", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayer = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayer = implementationLayer;
		jStack_MetaTable.systemTeardown();
		
	}
}
