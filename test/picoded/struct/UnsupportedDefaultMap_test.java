package picoded.struct;


import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UnsupportedDefaultMap_test {
	
	@SuppressWarnings("unchecked")
	UnsupportedDefaultMap<String, String> unsupportedDefaultMap = Mockito.mock(UnsupportedDefaultMap.class, Mockito.CALLS_REAL_METHODS);
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getTest() {
		Mockito.when(unsupportedDefaultMap.get("key")).thenCallRealMethod();
		unsupportedDefaultMap.get("key");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void putTest() {
		Mockito.when(unsupportedDefaultMap.put("key", "value")).thenCallRealMethod();
		unsupportedDefaultMap.put("key", "value");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void removeTest() {
		Mockito.when(unsupportedDefaultMap.remove("key")).thenCallRealMethod();
		unsupportedDefaultMap.remove("key");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void keySetTest() {
		Mockito.when(unsupportedDefaultMap.keySet()).thenCallRealMethod();
		unsupportedDefaultMap.keySet();
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void clearTest() {
		unsupportedDefaultMap.clear();
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void containsKeyTest() {
		unsupportedDefaultMap.containsKey("key");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void containsValueTest() {
		unsupportedDefaultMap.containsValue("value");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void entrySetTest() {
		Mockito.when(unsupportedDefaultMap.entrySet()).thenCallRealMethod();
		unsupportedDefaultMap.entrySet();
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void isEmptyTest() {
		unsupportedDefaultMap.isEmpty();
	}
	
	@Test (expected = java.lang.UnsupportedOperationException.class)
	public void putAllTest() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("my_key", "my_value");
		unsupportedDefaultMap.putAll(map);
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void sizeTest() {
		unsupportedDefaultMap.size();
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void valuesTest() {
		Mockito.when(unsupportedDefaultMap.values()).thenCallRealMethod();
		unsupportedDefaultMap.values();
	}
}
