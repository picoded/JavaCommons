package picoded.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredMapEntry_test {
	private DeferredMapEntry<String, String> deferredMapEntry;
	private String key = "key1";
	private Map<String, String> map = null;
	
	@Before
	public void setUp() {
		map = new HashMap<String, String>();
		map.put(key, "value_one");
		deferredMapEntry = new DeferredMapEntry<String, String>(map, key);
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test 
	public void getKeyTest() {
		assertEquals("key1", deferredMapEntry.getKey());
	}
	
	@Test 
	public void getValueTest() {
		assertEquals("value_one", deferredMapEntry.getValue());
	}
	
	@Test 
	public void setValueTest() {
		deferredMapEntry.setValue("value_new");
		assertEquals("value_new", deferredMapEntry.getValue());
	}
	
	@Test 
	public void equalsTest() {
		
		assertTrue(deferredMapEntry.equals(deferredMapEntry));
		assertFalse(deferredMapEntry.equals((key = null)));
		assertFalse(deferredMapEntry.equals((key = "")));
		
		map = new HashMap<String, String>();
		map.put(null, null);
		DeferredMapEntry<String, String> temp = new DeferredMapEntry<String, String>(map, null);
		assertFalse(deferredMapEntry.equals(temp));
		
		map = new HashMap<String, String>();
		map.put("", "");
		temp = new DeferredMapEntry<String, String>(map, "");
		assertFalse(deferredMapEntry.equals(temp));
		
		map = new HashMap<String, String>();
		map.put("key", "val");
		temp = new DeferredMapEntry<String, String>(map, "key1");
		assertFalse(deferredMapEntry.equals(temp));
		
		
	}
	
	@Test 
	public void equalsWithNullTest() {
		assertFalse(deferredMapEntry.equals(null));
	}
	
	@Test 
	public void equalsWithDifferentClassObjectTest() {
		assertFalse(deferredMapEntry.equals(map));
	}
	
	@Test 
	public void equalsWithDifferentObjectTest() {
		DeferredMapEntry<String, String> temp = new DeferredMapEntry<String, String>(map, key);
		assertTrue(deferredMapEntry.equals(temp));
	}
	
	@Test 
	public void hashCodeTest() {
		deferredMapEntry.hashCode();
		map = new HashMap<String, String>();
		map.put(null, null);
		DeferredMapEntry<String, String> temp = new DeferredMapEntry<String, String>(map, null);
		temp.hashCode();
		
		map = new HashMap<String, String>();
		map.put("", "");
		temp = new DeferredMapEntry<String, String>(map, "");
		temp.hashCode();
		
		map = new HashMap<String, String>();
		map.put("key", "val");
		temp = new DeferredMapEntry<String, String>(map, "key1");
		temp.hashCode();
	}
}
