package picoded.JStruct.internal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaTable;
import picoded.enums.ObjectTokens;

public class JStruct_MetaObject_test {
	JStruct_MetaObject jStruct_MetaObject = null;
	
	@Before
	public void setUp() {
		jStruct_MetaObject = new JStruct_MetaObject(implementationConstructor(), "_oid");
	}
	
	@After
	public void tearDown() {
		
	}
	
	// / To override for implementation
	// /------------------------------------------------------
	public MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}
	
	// @Test(expected = Exception.class)
	// public void commonSetupTest()throws Exception {
	// MetaTable metaTable =new JStruct_MetaTable();
	// jStruct_MetaObject.commonSetup(metaTable, null, null, false);
	// }
	
	@Test
	public void putTest() {
		Map<String, Object> deltaDataMap = new HashMap<String, Object>();
		Object object = null;
		deltaDataMap.put("key4", ObjectTokens.NULL);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNull(jStruct_MetaObject.put("key4", ObjectTokens.NULL));
		deltaDataMap = new HashMap<String, Object>();
		
		deltaDataMap.put("key5", object = new Object());
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", object));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", "test");
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", "test"));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", null);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNull(jStruct_MetaObject.put("key5", null));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", "abc");
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", null));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", null);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNull(jStruct_MetaObject.put("key5", "abc"));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", new String("hello"));
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", new String("test")));
		
		deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key5", new String("hello"));
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", new Integer(123)));
		
		jStruct_MetaObject.isCompleteRemoteDataMap = false;
		assertNotNull(jStruct_MetaObject.get("key5"));
	}
	
	@Test
	public void removeTest() {
		Map<String, Object> deltaDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key", "value");
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.remove("key"));
	}
	
	@Test
	public void keySetTest() {
		Map<String, Object> deltaDataMap = new HashMap<String, Object>();
		Map<String, Object> remoteDataMap = new HashMap<String, Object>();
		deltaDataMap.put("key", "value");
		deltaDataMap.put("key1", null);
		deltaDataMap.put("key2", ObjectTokens.NULL);
		remoteDataMap.put("key", "value");
		remoteDataMap.put("key1", null);
		remoteDataMap.put("key2", ObjectTokens.NULL);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		jStruct_MetaObject.remoteDataMap = remoteDataMap;
		assertNotNull(jStruct_MetaObject.keySet());
		jStruct_MetaObject.collapseDeltaToRemoteMap();
	}
	
	@Test
	public void toStringTest() {
		assertNotNull(jStruct_MetaObject.toString());
		assertNotNull(jStruct_MetaObject.agressiveNumericConversion("test"));
		assertNotNull(jStruct_MetaObject.agressiveNumericConversion("10"));
		assertNotNull(jStruct_MetaObject.agressiveNumericConversion("9648512236521"));
		assertNotNull(jStruct_MetaObject.agressiveNumericConversion("96485.12236521"));
	}
}
