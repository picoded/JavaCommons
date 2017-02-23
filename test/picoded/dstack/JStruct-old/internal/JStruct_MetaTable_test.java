package picoded.JStruct.internal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.set.ObjectToken;

public class JStruct_MetaTable_test {
	JStruct_MetaTable jStruct_MetaTable = null;
	
	@Before
	public void setUp() {
		jStruct_MetaTable = new JStruct_MetaTable();
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void getTempHintTest() {
		assertFalse(jStruct_MetaTable.getTempHintVal());
	}
	
	@Test
	public void setTempHintTest() {
		assertFalse(jStruct_MetaTable.setTempHintVal(true));
		assertTrue(jStruct_MetaTable.getTempHintVal());
		jStruct_MetaTable.systemSetup();
		jStruct_MetaTable.systemTeardown();
	}
	
	@Test
	public void getTest() {
		assertNull(jStruct_MetaTable.get(null));
		
	}
	
	@Test
	public void metaObjectRemoteDataMap_update() {
		Map<String, Object> fullMap = new HashMap<String, Object>();
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, null);
		fullMap.put("test", ObjectToken.NULL);
		fullMap.put("test1", null);
		Set<String> keys = new HashSet<String>();
		keys.add("test");
		keys.add("test1");
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, keys);
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, null);
	}
}