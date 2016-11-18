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
	
	@Test
	public void putTest() {
		Map<String, Object> deltaDataMap = new HashMap<String, Object>();
		Object object = null;
		deltaDataMap.put("key4", ObjectTokens.NULL);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNull(jStruct_MetaObject.put("key4", ObjectTokens.NULL));
		deltaDataMap = new HashMap<String, Object>();
		object = new Object();
		deltaDataMap.put("key5", object);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", object));
		
		deltaDataMap = new HashMap<String, Object>();
		object = null;
		deltaDataMap.put("key5", object);
		jStruct_MetaObject.deltaDataMap = deltaDataMap;
		assertNotNull(jStruct_MetaObject.put("key5", null));
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
	}
	
	@Test
	public void toStringTest() {
		assertNotNull(jStruct_MetaObject.toString());
	}
	
}
