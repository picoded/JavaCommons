package picoded.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/// Test the java standard HashMap
/// This is used to ensure code coverage, and implmentation is consistent,
/// between standard Map, and UnsupportedDefaultMap
public class StandardHashMap_test {
	
	// Test list 
	Map<String, Object> map = null;
	
	@Before
	public void setUp() {
		map = new HashMap<String, Object>();
	}
	
	@After
	public void tearDown() {
		map = null;
	}
	
	//
	// Map implementation testing
	//
	
	@Test
	public void constructorTest() {
		assertNotNull(map);
	}
	
	@Test
	public void putGetSizeClear() {
		map.put("key1", "1");
		map.put("key2", "2");
		
		assertEquals("1", map.get("key1"));
		assertEquals("2", map.get("key2"));
		assertEquals(2, map.size());
		
		map.clear();
		assertEquals(0, map.size());
	}
	
	@Test
	public void containsKeyTest() {
		assertFalse(map.containsKey("key1"));
		map.put("key1", "1");
		assertTrue(map.containsKey("key1"));
	}
	
	@Test
	public void containsValueForNullTest() {
		assertFalse(map.containsValue("value"));
		assertFalse(map.containsValue(null));
		map.put("key1", null);
		assertFalse(map.containsValue("value"));
		assertTrue(map.containsValue(null));
	}
	
	@Test
	public void containsValueForValueTest() {
		assertFalse(map.containsValue(null));
		assertFalse(map.containsValue("V2"));
		map.put("key5", "V2");
		assertFalse(map.containsValue(null));
		assertTrue(map.containsValue("V2"));
	}
	
	public void isEmptyTest() {
		assertTrue(map.isEmpty());
		map.put("key5", "V2");
		assertFalse(map.isEmpty());
	}
	
	@Test
	public void putAllValidTest() {
		Map<String, String> submap = new HashMap<String, String>();
		submap.put("my_key", "my_value");
		map.putAll(submap);
		assertEquals("my_value", map.get("my_key"));
	}
	
	@Test
	public void valuesValidTest() {
		map.put("key1", "1");
		
		Set<Map.Entry<String, String>> set = new HashSet<>();
		Map<String, String> submap = new HashMap<>();
		submap.put("key1", "1");
		set.addAll(submap.entrySet());
		Collection<Object> col = map.values();
		assertNotNull(col);
		
		// We do a toString conversion with 1 entry,
		// As UnsupportedDefaultMap uses the DeferredMapEntry, which 
		// is not directly equatible, but functions the same.
		assertEquals(set.toString(), map.entrySet().toString());
	}
}
