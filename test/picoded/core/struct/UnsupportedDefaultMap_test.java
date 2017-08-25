package picoded.core.struct;

import java.util.HashMap;
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
		@Override
		public V get(Object key) {
			return base.get(key);
		}
		
		// Implementation to actual base list
		@Override
		public V put(K key, V value) {
			return base.put(key, value);
		}
		
		// Implementation to actual base list
		@Override
		public V remove(Object key) {
			return base.remove(key);
		}
		
		// Implementation to actual base list
		@Override
		public Set<K> keySet() {
			return base.keySet();
		}
	}
	
	Map<String, Object> unsupported = null;
	
	@Override
	@Before
	public void setUp() {
		unsupported = new UnsupportedTest<>();
		map = new ProxyTest<>();
	}
	
	@Override
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
	
}
