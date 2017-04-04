package picoded.struct;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenericConvertMap_test extends StandardHashMap_test {
	
	class GenericConvertTest<K, V> implements GenericConvertMap<K, V> {
		
	}
	
	class ProxyTest<K, V> implements GenericConvertMap<K, V> {
		HashMap<K, V> base = new HashMap<>();
		
		@Override
		public V put(K key, V value) {
			return base.put(key, value);
		}
		
		@Override
		public V get(Object key) {
			return base.get(key);
		}
		
		@Override
		public Set<K> keySet() {
			return base.keySet();
		}
		
		@Override
		public V remove(Object key) {
			return base.remove(key);
		}
	}
	
	GenericConvertMap<String, Object> unsupported = null;
	ProxyTest<String, Object> proxyMap = null;
	
	@Override
	@Before
	public void setUp() {
		unsupported = new GenericConvertTest<>();
		map = new ProxyTest<String, Object>();
		proxyMap = new ProxyTest<String, Object>();
	}
	
	@Override
	@After
	public void tearDown() {
		unsupported = null;
		map = null;
		proxyMap = null;
	}
	
	@Test
	public void buildTest() {
		assertNotNull(GenericConvertMap.build(new HashMap<String, String>()));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringTest() {
		assertEquals("", unsupported.getString("my_key"));
	}
	
	@Test
	public void getStringValidTest() {
		proxyMap.put("my_key", "key");
		assertEquals("key", proxyMap.getString("my_key"));
	}
	
	@Test
	public void getStringInvalidTest() {
		proxyMap.put("my_key", "key");
		assertEquals(null, proxyMap.getString("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringOverloadTest() {
		assertEquals("", unsupported.getString("my_key", "my_object"));
	}
	
	@Test
	public void getStringOverloadValidTest() {
		proxyMap.put("my_key", "key");
		assertEquals("key", proxyMap.getString("my_key", "my_object"));
	}
	
	@Test
	public void getStringOverloadValidAlternateTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals("my_object", proxyMap.getString("my_key", "my_object"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getBooleanTest() {
		assertEquals("", unsupported.getBoolean("my_key"));
	}
	
	@Test
	public void getBooleanValidTest() {
		proxyMap.put("my_key", "true");
		assertTrue(proxyMap.getBoolean("my_key"));
	}
	
	@Test
	public void getBooleanValidFalseTest() {
		proxyMap.put("my_key", "my_object");
		assertFalse(proxyMap.getBoolean("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getBooleanOverloadTest() {
		assertEquals("", unsupported.getBoolean("my_key", true));
	}
	
	@Test
	public void getBooleanOverloadValidTest() {
		proxyMap.put("my_key", "true");
		assertTrue(proxyMap.getBoolean("my_key", true));
	}
	
	@Test
	public void getBooleanOverloadValidTrueTest() {
		proxyMap.put("my_key", "true");
		assertTrue(proxyMap.getBoolean("my_key", true));
	}
	
	@Test
	public void getBooleanOverloadValidFalseTest() {
		proxyMap.put("my_key", "my_object");
		assertFalse(proxyMap.getBoolean("my_key", false));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getNumberTest() {
		assertEquals("", unsupported.getNumber("my_key"));
	}
	
	@Test
	public void getNumberValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(BigDecimal.valueOf(1), proxyMap.getNumber("my_key"));
	}
	
	@Test
	public void getNumberNullTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getNumber("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getNumberOverloadTest() {
		assertEquals("", unsupported.getNumber("my_key", 1));
	}
	
	@Test
	public void getNumberOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(BigDecimal.valueOf(1), proxyMap.getNumber("my_key", 5));
	}
	
	@Test
	public void getNumberOverloadValidAlternateTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(5, proxyMap.getNumber("my_key1", 5));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getIntTest() {
		assertEquals("", unsupported.getInt("my_key"));
	}
	
	@Test
	public void getIntValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1, proxyMap.getInt("my_key"));
	}
	
	@Test
	public void getIntInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(0, proxyMap.getInt("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getIntOverloadTest() {
		assertEquals("", unsupported.getInt("my_key", 1));
	}
	
	@Test
	public void getIntOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1, proxyMap.getInt("my_key", 5));
	}
	
	@Test
	public void getIntOverloadInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(5, proxyMap.getInt("my_key1", 5));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getLongTest() {
		assertEquals("", unsupported.getLong("my_key"));
	}
	
	@Test
	public void getLongValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1l, proxyMap.getLong("my_key"));
	}
	
	@Test
	public void getLongInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(0l, proxyMap.getLong("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getLongOverloadTest() {
		assertEquals("", unsupported.getLong("my_key", 1l));
	}
	
	@Test
	public void getLongOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1l, proxyMap.getLong("my_key", 5l));
	}
	
	@Test
	public void getLongOverloadInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(5l, proxyMap.getLong("my_key1", 5l));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getFloatTest() {
		assertEquals("", unsupported.getFloat("my_key"));
	}
	
	@Test
	public void getFloatValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1f, proxyMap.getFloat("my_key"), 0.01);
	}
	
	@Test
	public void getFloatInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(0f, proxyMap.getFloat("my_key"), 0.01);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getFloatOverloadTest() {
		assertEquals("", unsupported.getFloat("my_key", 1f));
	}
	
	@Test
	public void getFloatOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1f, proxyMap.getFloat("my_key", 1f), 0.01);
	}
	
	@Test
	public void getFloatOverloadInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(5f, proxyMap.getFloat("my_key1", 5f), 0.01);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getDoubleTest() {
		assertEquals("", unsupported.getDouble("my_key"));
	}
	
	@Test
	public void getDoubleValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1f, proxyMap.getDouble("my_key"), 0.01);
	}
	
	@Test
	public void getDoubleInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(0f, proxyMap.getDouble("my_key1"), 0.01);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getDoubleOverloadTest() {
		assertEquals("", unsupported.getDouble("my_key", 1d));
	}
	
	@Test
	public void getDoubleOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1d, proxyMap.getDouble("my_key", 5d), 0.01);
	}
	
	@Test
	public void getDoubleOverloadInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(5d, proxyMap.getDouble("my_key1", 5d), 0.01);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getByteTest() {
		assertEquals("", unsupported.getByte("my_key"));
	}
	
	@Test
	public void getByteValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals((byte) 1, proxyMap.getByte("my_key"));
	}
	
	@Test
	public void getByteInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals((byte) 0, proxyMap.getByte("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getByteOverloadTest() {
		assertEquals("", unsupported.getByte("my_key", (byte) 'a'));
	}
	
	@Test
	public void getByteOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals((byte) 1, proxyMap.getByte("my_key", (byte) 'a'));
	}
	
	@Test
	public void getByteOverloadInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals((byte) 'a', proxyMap.getByte("my_key1", (byte) 'a'));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getShortTest() {
		assertEquals("", unsupported.getShort("my_key"));
	}
	
	@Test
	public void getShortValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1, proxyMap.getShort("my_key"));
	}
	
	@Test
	public void getShortInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals(0, proxyMap.getShort("my_key1"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getShortOverloadTest() {
		assertEquals("", unsupported.getShort("my_key", (short) 'a'));
	}
	
	@Test
	public void getShortOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals(1, proxyMap.getShort("my_key", (short) 'a'));
	}
	
	@Test
	public void getShortOverloadInvalidTest() {
		proxyMap.put("my_key", "my_object");
		assertEquals((short) 'a', proxyMap.getShort("my_key1", (short) 'a'));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getUUIDTest() {
		assertEquals("", unsupported.getUUID("my_key"));
	}
	
	@Test
	public void getUUIDValidTest() {
		proxyMap.put("my_key", "my_object");
		assertNull(proxyMap.getUUID("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getUUIDOverloadTest() {
		assertEquals("", unsupported.getUUID("my_key", "ok"));
	}
	
	@Test
	public void getUUIDOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getUUID("my_key", "ok"));
	}
	
	@Test
	public void getUUIDOverloadInvalidTest() {
		proxyMap.put("my_key", "1");
		assertNotNull(proxyMap.getUUID("my_key", "o123456789o123456789ok"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGUIDTest() {
		assertEquals("", unsupported.getGUID("my_key"));
	}
	
	@Test
	public void getGUIDValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGUID("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGUIDOverloadTest() {
		assertEquals("", unsupported.getGUID("my_key", "ok"));
	}
	
	@Test
	public void getGUIDOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGUID("my_key", "ok"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getObjectListTest() {
		assertNull(unsupported.getObjectList("my_key"));
	}
	
	@Test
	public void getObjectListValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getObjectList("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getObjectListOverloadTest() {
		assertEquals("", unsupported.getObjectList("my_key", "ok"));
	}
	
	@Test
	public void getObjectListOverloadValidTest() {
		List<String> list = new ArrayList<String>();
		list.add("me");
		proxyMap.put("my_key", "1");
		assertEquals(list, proxyMap.getObjectList("my_key", list));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringMapTest() {
		assertEquals("", unsupported.getStringMap("my_key"));
	}
	
	@Test
	public void getStringMapValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getStringMap("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringMapOverloadTest() {
		assertEquals("", unsupported.getStringMap("my_key", "ok"));
	}
	
	@Test
	public void getStringMapOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getStringMap("my_key", "ok"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGenericConvertStringMapTest() {
		assertEquals("", unsupported.getGenericConvertStringMap("my_key"));
	}
	
	@Test
	public void getGenericConvertStringMapValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGenericConvertStringMap("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGenericConvertStringMapOverloadTest() {
		assertEquals("", unsupported.getGenericConvertStringMap("my_key", "ok"));
	}
	
	@Test
	public void getGenericConvertStringMapOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGenericConvertStringMap("my_key", "ok"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGenericConvertListTest() {
		assertEquals("", unsupported.getGenericConvertList("my_key"));
	}
	
	@Test
	public void getGenericConvertListValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGenericConvertList("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getGenericConvertListOverloadTest() {
		assertEquals("", unsupported.getGenericConvertList("my_key", "ok"));
	}
	
	@Test
	public void getGenericConvertListOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getGenericConvertList("my_key", "ok"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringArrayTest() {
		assertEquals("", unsupported.getStringArray("my_key"));
	}
	
	@Test
	public void getStringArrayValidTest() {
		proxyMap.put("my_key", "1");
		assertNull(proxyMap.getStringArray("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getStringArrayOverloadTest() {
		assertEquals("", unsupported.getStringArray("my_key", "ok"));
	}
	
	@Test
	public void getStringArrayOverloadValidTest() {
		String[] strArr = new String[] { "1", "2" };
		proxyMap.put("my_key", strArr);
		assertArrayEquals(strArr, proxyMap.getStringArray("my_key", strArr));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getObjectArrayTest() {
		assertNull(unsupported.getObjectArray("my_key"));
	}
	
	@Test
	public void getObjectArrayValidTest() {
		proxyMap.put("my_key", "1, 2");
		assertNull(proxyMap.getObjectArray("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getObjectArrayOverloadTest() {
		assertNull(unsupported.getObjectArray("my_key", "ok"));
	}
	
	@Test
	public void getObjectArrayOverloadValidTest() {
		Object[] strArr = new Object[] { "1", 2 };
		proxyMap.put("my_key", strArr);
		assertArrayEquals(strArr, proxyMap.getObjectArray("my_key", strArr));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getNestedObjectTest() {
		assertEquals("", unsupported.getNestedObject("my_key"));
	}
	
	@Test
	public void getNestedObjectValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals("1", proxyMap.getNestedObject("my_key"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void getNestedObjectOverloadTest() {
		assertEquals("", unsupported.getNestedObject("my_key", "ok"));
	}
	
	@Test
	public void getNestedObjectOverloadValidTest() {
		proxyMap.put("my_key", "1");
		assertEquals("1", proxyMap.getNestedObject("my_key", "ok"));
	}
	
	@Test
	public void getNestedObjectOverloadInvalidTest() {
		proxyMap.put("my_key", "1");
		assertEquals("ok", proxyMap.getNestedObject("my_key1", "ok"));
	}
	
	//	 @Test(expected = UnsupportedOperationException.class)
	//	 public void typecastPutTest() {
	//	 	assertEquals("", genericConvertMap.typecastPut("my_key", "my_value"));
	//	 }
	
	// @Test
	// public void typecastPutValidTest() {
	// 	when(proxyMap.typecastPut("my_key", "my_value")).thenCallRealMethod();
	// 	when(proxyMap.get("my_key")).thenReturn("1");
	// 	when(proxyMap.put("my_key", "my_value")).thenReturn("my_value");
	// 	assertEquals("my_value", proxyMap.typecastPut("my_key", "my_value"));
	// }
	// 
	//	 @Test(expected = UnsupportedOperationException.class)
	//	 public void convertPutTest() {
	//	 	assertEquals("", unsupported.convertPut("my_key", "my_value"));
	//	 }
	
	// @Test(expected = UnsupportedOperationException.class)
	// public void convertPutOverloadTest() {
	// 	assertEquals("", genericConvertMap.convertPut("my_key", "my_value", String.class));
	// }
	// 
	// @Test
	// public void convertPutOverloadValidTest() {
	// 	when(proxyMap.convertPut("my_key", "my_value", String.class)).thenCallRealMethod();
	// 	when(proxyMap.get("my_key")).thenReturn("my_value");
	// 	when(proxyMap.put("my_key", "my_key")).thenReturn("my_key");
	// 	assertEquals("my_key", proxyMap.convertPut("my_key", "my_value", String.class));
	// }
}
