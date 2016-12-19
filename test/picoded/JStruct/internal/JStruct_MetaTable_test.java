package picoded.JStruct.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import picoded.RunInThread;
import picoded.RunInThreadRule;
import picoded.enums.ObjectTokens;

public class JStruct_MetaTable_test {
	
	@Rule
	public RunInThreadRule runInThread = new RunInThreadRule();
	
	JStruct_MetaTable jStruct_MetaTable = null;
	
	@Before
	@RunInThread
	public void setUp() {
		jStruct_MetaTable = new JStruct_MetaTable();
	}
	
	@After
	@RunInThread
	public void tearDown() {
	}
	
	@Test
	@RunInThread
	public void getTempHintTest() {
		assertFalse(jStruct_MetaTable.getTempHintVal());
	}
	
	@Test
	@RunInThread
	public void setTempHintTest() {
		assertFalse(jStruct_MetaTable.setTempHintVal(true));
		assertTrue(jStruct_MetaTable.getTempHintVal());
		jStruct_MetaTable.systemSetup();
		jStruct_MetaTable.systemTeardown();
	}
	
	@Test
	@RunInThread
	public void getTest() {
		assertNull(jStruct_MetaTable.get(null));
		
	}
	
	@Test
	@RunInThread
	public void metaObjectRemoteDataMap_update() {
		Map<String, Object> fullMap = new HashMap<String, Object>();
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, null);
		fullMap.put("test", ObjectTokens.NULL);
		fullMap.put("test1", null);
		Set<String> keys = new HashSet<String>();
		keys.add("test");
		keys.add("test1");
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, keys);
		jStruct_MetaTable.metaObjectRemoteDataMap_update("str", fullMap, null);
	}
}
