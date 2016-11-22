package picoded.JStruct.internal;

import static org.junit.Assert.*;

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
		JStack_MetaTable jStack_MetaTable = new JStack_MetaTable(jStructObj, "test");
		JStruct_MetaTable[] _implementationLayers = { jStack_MetaTable };
		jStack_MetaTable._implementationLayers = _implementationLayers;
		assertNotNull(jStack_MetaTable.implementationLayers());
	}
}
