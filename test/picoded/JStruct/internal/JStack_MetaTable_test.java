package picoded.JStruct.internal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.JStack.JStack;
import picoded.JStack.JStackLayer;

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
		new JStack_MetaTable(jStructObj, "test");
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
	}
	
	@Test
	public void keySetTest() {
		Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		map.put("key1", "value");
		map.put("key2", "value");
		_valueMap.put("test", map);
		JStruct_MetaTable jStruct_MetaTable = new JStruct_MetaTable();
		jStruct_MetaTable._valueMap = _valueMap;
		JStruct_MetaTable[] implementationLayersReversed = { jStruct_MetaTable };
		jStack_MetaTable.implementationLayersReversed = implementationLayersReversed;
		assertNotNull(jStack_MetaTable.keySet());
	}
	
	@Test
	public void keySetTest1() {
		
	}
	
	@Test
	public void keySetTest2() {
		
	}
	
	@Test
	public void systemSetupTest() {
		JStackLayer jStackLayer = new JStackLayer() {
		};
		JStack jStructObj = new JStack(jStackLayer);
		jStack_MetaTable.stackObj = jStructObj;
		jStack_MetaTable.systemSetup();
		
	}
	
	@Test
	public void systemTeardownTest() {
		JStackLayer jStackLayer = new JStackLayer() {
		};
		JStack jStructObj = new JStack(jStackLayer);
		jStack_MetaTable.stackObj = jStructObj;
		jStack_MetaTable.systemTeardown();
		
	}
}
