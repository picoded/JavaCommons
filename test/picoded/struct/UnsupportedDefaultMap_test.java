package picoded.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class UnsupportedDefaultMap_test extends StandardHashMap_test {
	
	//
	// Class implementation of UnsupportedDefaultList
	//
	
	/// Blank implmentation of unsupported
	class UnsupportedTest<K, V> implements UnsupportedDefaultMap<K, V> {
	}
	
	/// Implementation of core default map function to a HashMap
	class ProxyTest<K, V> implements UnsupportedDefaultMap<K, V> {
		// Base list to implement
		HashMap<K, V> base = new HashMap<K, V>();
		
		// Implementation to actual base list
		public V get(Object key) {
			return base.get(key);
		}
		
		// Implementation to actual base list
		public V put(K key, V value) {
			return base.put(key, value);
		}
		
		// Implementation to actual base list
		public V remove(Object key) {
			return base.remove(key);
		}
		
		// Implementation to actual base list
		public Set<K> keySet() {
			return base.keySet();
		}
	}
	
	Map<String,Object> unsupported = null;
	
	@Before
	public void setUp() {
		unsupported = new UnsupportedTest<>();
		map = new ProxyTest<>();
	}
	
	@After
	public void tearDown() {
		unsupported = null;
		map = null;
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getTest() {
		unsupported.get("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void putTest() {
		unsupported.put("key", "value");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeTest() {
		unsupported.remove("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void keySetTest() {
		unsupported.keySet();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void clearTest() {
		unsupported.clear();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsKeyTest() {
		unsupported.containsKey("key");
	}
	
}
