package picoded.JStruct.internal;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JStruct_KeyValueMap_test {
	JStruct_KeyValueMap jStruct_KeyValueMap = null;
	
	@Before
	public void setUp() {
		jStruct_KeyValueMap = new JStruct_KeyValueMap();
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	// / Invalid constructor test
	@Test
	public void invalidConstructor() {
		new JStruct_KeyValueMap();
	}
	
	@Test
	public void getTempHintTest() {
		assertFalse(jStruct_KeyValueMap.getTempHint());
	}
	
	@Test
	public void maintenanceTest() {
		ConcurrentHashMap<String, Long> expireMap = new ConcurrentHashMap<String, Long>();
		expireMap.put("key3", 0l);
		expireMap.put("key5", 160l);
		expireMap.put("key6", 169898998260l);
		jStruct_KeyValueMap.expireMap = expireMap;
		jStruct_KeyValueMap.maintenance();
	}
	
	@Test
	public void setExpiryRaw() {
		ConcurrentHashMap<String, String> valueMap = new ConcurrentHashMap<String, String>();
		valueMap.put("key3", "0l");
		valueMap.put("key5", "160l");
		jStruct_KeyValueMap.valueMap = valueMap;
		jStruct_KeyValueMap.setExpiryRaw("key5", 160l);
	}
}
