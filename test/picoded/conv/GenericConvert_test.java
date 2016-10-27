package picoded.conv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.struct.GenericConvertList;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertList;
import picoded.struct.ProxyGenericConvertMap;
//Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static picoded.conv.GenericConvert.*;

public class GenericConvert_test {
	
	private static final double DELTA = 1e-15;
	
	/// Setup the temp vars
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new GenericConvert();
	}
	
	@Test
	public void toStringTest() {
		assertNotNull(GenericConvert.toString(-1, null));
		assertNull("", GenericConvert.toString(null, null));
		assertEquals("fallback", GenericConvert.toString(null, "fallback"));
		assertEquals("inout", GenericConvert.toString("inout", null));
		assertEquals("1", GenericConvert.toString(new Integer(1), null));
		assertEquals("1", GenericConvert.toString(new Integer(1)));
	}
	
	@Test
	public void toBooleanTest() {
		List<String> list = new ArrayList<String>();
		assertFalse(GenericConvert.toBoolean(list, false));
		/// for boolean true false test case
		Boolean boolean1 = new Boolean(true);
		assertTrue(GenericConvert.toBoolean(boolean1, false));
		boolean1 = new Boolean(false);
		assertFalse(GenericConvert.toBoolean(boolean1, true));
		/// for boolean number test case
		assertTrue(GenericConvert.toBoolean(1, true));
		assertFalse(toBoolean(new Boolean("1"), false));
		assertFalse(GenericConvert.toBoolean(0, true));
		/// for string test case
		assertFalse(GenericConvert.toBoolean(null, false));
		assertTrue(toBoolean(null, true));
		assertFalse(GenericConvert.toBoolean("", false));
		assertTrue(GenericConvert.toBoolean("+", false));
		assertTrue(GenericConvert.toBoolean("t", false));
		assertTrue(GenericConvert.toBoolean("T", false));
		assertTrue(GenericConvert.toBoolean("y", false));
		assertTrue(GenericConvert.toBoolean("Y", false));
		
		assertFalse(GenericConvert.toBoolean("-", false));
		assertFalse(GenericConvert.toBoolean("f", false));
		assertFalse(GenericConvert.toBoolean("F", false));
		assertFalse(GenericConvert.toBoolean("n", false));
		assertFalse(GenericConvert.toBoolean("N", false));
		
		assertTrue(GenericConvert.toBoolean("123", false));
		assertTrue(GenericConvert.toBoolean("12", false));
		assertFalse(GenericConvert.toBoolean("-1", false));
		assertFalse(GenericConvert.toBoolean("$%", false));
		
		assertTrue(toBoolean(999, false));
		assertFalse(toBoolean("000", false));
	}
	
	@Test
	public void toBooleanSingleParameterTest() {
		assertTrue(toBoolean("true"));
	}
	
	@Test
	public void toNumberTest() {
		List<String> list = new ArrayList<String>();
		assertNotEquals(list, GenericConvert.toNumber(list, 0).intValue());
		assertNotEquals("$%", GenericConvert.toNumber("$%", 0).intValue());
		assertEquals(10, GenericConvert.toNumber(10, 0).intValue());
		assertNotEquals("", GenericConvert.toNumber("", 0).intValue());
		assertEquals(new BigDecimal("01111111111111111"), GenericConvert.toNumber("01111111111111111", 0));
		assertEquals(new BigDecimal("2.1"), GenericConvert.toNumber("2.1", null));
		assertEquals(new BigDecimal("2.2"), GenericConvert.toNumber("2.2"));
	}
	
	@Test
	public void toNumberSingleTest() {
		assertEquals(new BigDecimal("1"), GenericConvert.toNumber("1"));
	}
	
	@Test
	public void toIntTest() {
		assertEquals(1, GenericConvert.toInt(null, 1));
		assertEquals(2, GenericConvert.toInt(2, 1));
		assertEquals(3, GenericConvert.toInt(3));
	}
	
	@Test
	public void toLongTest() {
		assertEquals(1l, GenericConvert.toLong(null, 1l));
		assertEquals(2l, GenericConvert.toLong(2l, 1l));
		assertEquals(3l, GenericConvert.toLong(3l));
	}
	
	@Test
	public void toFloatTest() {
		assertEquals(1.0f, GenericConvert.toFloat(null, 1.0f), DELTA);
		assertEquals(2.0f, GenericConvert.toFloat(2.0f, 1.0f), DELTA);
		assertEquals(3.0f, GenericConvert.toFloat(3.0f), DELTA);
	}
	
	@Test
	public void toDoubleTest() {
		assertEquals(1.0, GenericConvert.toDouble(null, 1.0), DELTA);
		assertEquals(2.0, GenericConvert.toDouble("2.0", 1.0), DELTA);
		assertEquals(3.0, GenericConvert.toDouble(3.0), DELTA);
		assertEquals(4.0, GenericConvert.toDouble(4.0, 1.0), DELTA);
	}
	
	@Test
	public void toByteTest() {
		assertEquals((byte) 'a', GenericConvert.toByte(null, (byte) 'a'));
		assertEquals((byte) 'b', GenericConvert.toByte("a", (byte) 'b'));
		assertEquals((byte) 'c', GenericConvert.toByte((byte) 'c'));
		assertEquals((byte) 4.0, GenericConvert.toByte(4.0, (byte) 'd'));
	}
	
	@Test
	public void toShortTest() {
		assertEquals((short) 'a', GenericConvert.toShort(null, (short) 'a'));
		assertEquals((short) 'b', GenericConvert.toShort("a", (short) 'b'));
		assertEquals((short) 'c', GenericConvert.toShort((short) 'c'));
		assertEquals((short) 4.0, GenericConvert.toShort(4.0, (short) 'd'));
	}
	
	@Test
	public void toUUIDTest() {
		assertNull(GenericConvert.toUUID("hello-world"));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertEquals(GUID.fromBase58("123456789o123456789o12"), GenericConvert.toUUID("123456789o123456789o12", null));
		assertNull(GenericConvert.toUUID("123456789o123456789o1o2", null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(null, GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertNull(GenericConvert.toUUID(null, null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), "hello world"));
		assertNull(GenericConvert.toUUID(1234567, "default"));
		assertNull(GenericConvert.toUUID("123456789o123456789o12345", "default"));
	}
	
	@Test
	public void toGUIDTest() {
		assertNull(GenericConvert.toGUID("hello-world"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o"));
		assertNull(GenericConvert.toGUID(100));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(null, "ADAukG8u3ryYrm6pHFDB6o"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", null));
		assertNull(GenericConvert.toGUID(null, null));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", "hello world"));
		List<String> list = new ArrayList<String>();
		assertNotEquals(list, GenericConvert.toNumber(list, 0).intValue());
		assertNotEquals("$%", GenericConvert.toNumber("$%", 0).intValue());
		assertEquals(10, GenericConvert.toNumber(10, 0).intValue());
		assertNotEquals("", GenericConvert.toNumber("", 0).intValue());
		assertEquals(new BigDecimal("01111111111111111"), GenericConvert.toNumber("01111111111111111", 0));
		UUID uuid = UUID.randomUUID();
		assertNotNull(toGUID(uuid, null));
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchObjectTest() {
		assertNull(fetchObject(null, null, null));
		assertEquals("default", fetchObject(null, null, "default"));
		Map map = new HashMap();
		assertNull(fetchObject(map, null, null));
		map = new HashMap();
		map.put("key", "value");
		assertNull(fetchObject(map, null, null));
		assertEquals("value", fetchObject(map, "key", null));
		
		List list = new ArrayList();
		assertNull(fetchObject(list, null, null));
		list = new ArrayList();
		list.add("value");
		assertNull(fetchObject(list, null, null));
		assertNull("value", fetchObject(list, "key", null));
		assertEquals("value", fetchObject(list, "0", null));
		
		assertEquals("default", fetchObject("value", "0", "default"));
	}
	
	@Test
	public void biFunctionMapTest() {
		assertNotNull(biFunctionMap());
		Map<Class<?>, BiFunction<Object, Object, ?>> myBiFunctionMap = new HashMap<Class<?>, BiFunction<Object, Object, ?>>();
		biFunctionMap = myBiFunctionMap;
		assertEquals(myBiFunctionMap, biFunctionMap);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchObjectWithTwoParametersTest() {
		assertNull(fetchObject(null, null));
		Map map = new HashMap();
		assertNull(fetchObject(map, null));
		map = new HashMap();
		map.put("key", "value");
		assertNull(fetchObject(map, null));
		assertEquals("value", fetchObject(map, "key"));
		
		List list = new ArrayList();
		assertNull(fetchObject(list, null));
		list = new ArrayList();
		list.add("value");
		assertNull(fetchObject(list, null));
		assertNull("value", fetchObject(list, "key"));
		assertEquals("value", fetchObject(list, "0"));
		
		assertNull(fetchObject("value", "0"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchNestedObjectTest() {
		assertNull(fetchNestedObject(null, null));
		
		assertNull(fetchNestedObject(null, null, null));
		assertEquals("default", fetchNestedObject(null, null, "default"));
		assertEquals("default", fetchNestedObject("string", null, "default"));
		Map map = new HashMap();
		assertEquals("default", fetchNestedObject(map, null, "default"));
		assertEquals("default", fetchNestedObject(map, "", "default"));
		map.put("key", "value");
		assertEquals("value", fetchNestedObject(map, "key", "default"));
		assertEquals("value", fetchNestedObject(map, " key ", "default"));
		assertEquals("value", fetchNestedObject(map, ".key", "default"));
		map.put("key1", "value1");
		assertEquals("value1", fetchNestedObject(map, "[key1]", "default"));
		assertEquals("default", fetchNestedObject(map, "[key2]", "default"));
		assertEquals("default", fetchNestedObject(map, "key.[key2]", "default"));
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchNestedObjectRecursiveTest() {
		Map map = new HashMap();
		map.put("key", "value");
		Map subMap = new HashMap();
		subMap.put("sub_key", "sub_value");
		map.put("key1", subMap);
		assertEquals("sub_value", fetchNestedObject(map, "key1.sub_key", "default"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void fetchNestedObjectRecursiveArrayTest() {
		Map map = new HashMap();
		map.put("key", "value");
		Map subMap = new HashMap();
		subMap.put("sub_key", "sub_value");
		map.put("key1", subMap);
		assertEquals("sub_value", fetchNestedObject(map, "[key1][sub_key]", "default"));
		assertEquals("default", fetchNestedObject(map, "key[key1]", "default"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(expected = RuntimeException.class)
	public void fetchNestedObjectInvalidTest() {
		Map map = new HashMap();
		map.put("key", "value");
		assertEquals("value1", fetchNestedObject(map, "[key1", "default"));
	}
	
	@Test
	public void toStringArrayTest() {
		assertNull(toStringArray(null));
		
		assertNull(toStringArray(null, null));
		
		assertNull(toStringArray(null, "default"));
		assertArrayEquals(new String[] { "key1", "key2" }, toStringArray(new String[] { "key1", "key2" }, "default"));
		assertArrayEquals(new String[] { "1", "2.2" }, toStringArray(new Object[] { "1", 2.2 }, "default"));
		assertArrayEquals(new String[] { "key1", "key2", "key3" },
			toStringArray("[\"key1\",\"key2\",\"key3\"]", "default"));
		List<String> list = new ArrayList<>();
		list.add("key1");
		list.add("key2");
		assertArrayEquals(new String[] { "key1", "key2" }, toStringArray(list, "default"));
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		//assertArrayEquals(new String[]{"key1", "key2"}, toStringArray(2.2, "default"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void toStringArrayExceptionTest() {
		List list = new ArrayList<>();
		list.add(2.23);
		list.add("3.4");
		list.add("key1");
		assertArrayEquals(new String[] { "2.23", "3.4", "key1" }, toStringArray(list, "default"));
	}
	
	@Test
	public void toObjectArrayTest() {
		assertNull(toObjectArray(null));
		
		assertNull(toObjectArray(null, null));
		assertNull(toObjectArray(null, "default"));
		assertArrayEquals(new Object[] { "key1", "key2" }, toObjectArray(new String[] { "key1", "key2" }, "default"));
		assertArrayEquals(new Object[] { "1", "2.2" }, toObjectArray(new String[] { "1", "2.2" }, "default"));
		assertArrayEquals(new Object[] { "key1", "key2", "key3" },
			toObjectArray("[\"key1\",\"key2\",\"key3\"]", "default"));
		List<String> list = new ArrayList<>();
		list.add("key1");
		list.add("key2");
		assertArrayEquals(new String[] { "key1", "key2" }, toObjectArray(list, "default"));
	}
	
	@Test
	public void toObjectListTest() {
		assertNull(toObjectList(null));
		
		assertNull(toObjectList(null, null));
		
		assertNull(toObjectList(null, "default"));
		assertEquals(Arrays.asList(new Object[] { "key1", "key2" }),
			toObjectList(new String[] { "key1", "key2" }, "default"));
		assertEquals(Arrays.asList(new Object[] { "1", "2.2" }), toObjectList(new String[] { "1", "2.2" }, "default"));
		assertEquals(Arrays.asList(new Object[] { "key1", "key2", "key3" }),
			toObjectList("[\"key1\",\"key2\",\"key3\"]", "default"));
		List<String> list = new ArrayList<>();
		list.add("key1");
		list.add("key2");
		assertEquals(Arrays.asList(new String[] { "key1", "key2" }), toObjectList(list, "default"));
		assertEquals(Arrays.asList(new Object[] { "key1", "key2", "key3" }),
			toObjectList(new StringBuilder("[\"key1\",\"key2\",\"key3\"]"), "default"));
	}
	
	@Test
	public void toStringMapTest() {
		assertNull(toStringMap(null));
		
		assertNull(toStringMap(null, null));
		assertNull("default", toStringMap(null, "default"));
		Map<String, String> map = new HashMap<>();
		assertEquals(map, toStringMap(map, null));
		map = new HashMap<String, String>();
		map.put("Hello", "WORLD");
		map.put("WORLD", "Hello");
		String str = ConvertJSON.fromMap(map);
		assertEquals(map, toStringMap(str, null));
	}
	
	@Test
	public void normalizeObjectPathTest() {
		assertEquals("", normalizeObjectPath(null, null));
		
		assertEquals("", normalizeObjectPath(null, null, null).toString());
		StringBuilder sb = new StringBuilder();
		assertEquals("", normalizeObjectPath(null, null, sb).toString());
		List<String> list = new ArrayList<String>();
		assertEquals("", normalizeObjectPath(null, list, sb).toString());
		list.add("key");
		List<String> base = new ArrayList<String>();
		assertEquals("", normalizeObjectPath(base, list, sb).toString());
		base.add("my_path");
		assertEquals("", normalizeObjectPath(base, list, sb).toString());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "my_path");
		assertEquals("key", normalizeObjectPath(map, list, sb).toString());
		// Attempt to correct the case insensitivty issue
		list = new ArrayList<String>();
		list.add("KEY");
		sb = new StringBuilder();
		map = new HashMap<String, Object>();
		map.put("key", "my_path");
		assertEquals("key", normalizeObjectPath(map, list, sb).toString());
		// Attempt to correct the case insensitivty issue
		list = new ArrayList<String>();
		list.add("KEY");
		sb = new StringBuilder();
		map = new HashMap<String, Object>();
		map.put("key", "my_path");
		assertEquals("key", normalizeObjectPath(map, list, sb).toString());
		// Attempt to correct the case insensitivty issue
		list = new ArrayList<String>();
		list.add("KEEY");
		sb = new StringBuilder();
		map = new HashMap<String, Object>();
		map.put("key", "my_path");
		assertEquals("", normalizeObjectPath(map, list, sb).toString());
		list = new ArrayList<String>();
		list.add("0");
		sb = new StringBuilder();
		assertEquals("[0]", normalizeObjectPath(base, list, sb).toString());
		// Attempt to correct the case insensitivty issue
		list = new ArrayList<String>();
		list.add("KEY");
		sb = new StringBuilder();
		map = new HashMap<String, Object>();
		map.put("key", null);
		assertEquals("", normalizeObjectPath(map, list, sb).toString());
		// Attempt to correct the case insensitivty issue
		list = new ArrayList<String>();
		list.add("KEY");
		//sb = new StringBuilder();
		map = new HashMap<String, Object>();
		map.put("key", "my_path");
		sb.append("previous");
		assertEquals("previous.key", normalizeObjectPath(map, list, sb).toString());
	}
	
	@Test(expected = RuntimeException.class)
	public void normalizeObjectPathRecursiveexceptionTest() {
		// Else recursive fetch
		List<String> list = new ArrayList<String>();
		list.add("KEY");
		list.add("key1");
		StringBuilder sb = new StringBuilder();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "my_path");
		map.put("key1", "my_path1");
		assertEquals("previous.key", normalizeObjectPath(map, list, sb).toString());
	}
	
	@Test(expected = RuntimeException.class)
	public void normalizeObjectPathRecursiveTest() {
		// Else recursive fetch
		List<String> list = new ArrayList<String>();
		list.add("key1");
		list.add("key2");
		StringBuilder sb = new StringBuilder();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key1", "my_path1");
		map.put("key2", "my_path2");
		assertEquals("previous.key", normalizeObjectPath(map, list, sb).toString());
	}
	
	@Test(expected = RuntimeException.class)
	public void normalizeObjectPathExceptionTest() {
		StringBuilder sb = new StringBuilder();
		List<String> list = new ArrayList<String>();
		list.add("key");
		assertEquals("", normalizeObjectPath(null, list, sb).toString());
	}
	
	@Test(expected = RuntimeException.class)
	public void normalizeObjectPathRecursiveExceptionTest() {
		StringBuilder sb = new StringBuilder();
		List<String> list = new ArrayList<String>();
		list.add("0");
		list.add("1");
		List<String> base = new ArrayList<String>();
		base.add("my_path");
		base.add("my_second_path");
		sb = new StringBuilder();
		assertEquals("[0]", normalizeObjectPath(base, list, sb).toString());
	}
	
	@Test
	public void splitObjectPathTest() {
		assertArrayEquals(new String[] {}, splitObjectPath(null));
		
		assertEquals(new ArrayList<String>(), splitObjectPath(null, null));
		List<String> ret = new ArrayList<>();
		assertEquals(new ArrayList<String>(), splitObjectPath(null, ret));
		assertEquals(new ArrayList<String>(), splitObjectPath("", ret));
		List<String> key = new ArrayList<String>();
		key.add("my_key");
		assertEquals(key, splitObjectPath(" my_key ", ret));
		ret = new ArrayList<>();
		assertEquals(key, splitObjectPath(".my_key", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("key");
		key.add("my_key");
		assertEquals(key, splitObjectPath("key.my_key", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("my_key");
		assertEquals(key, splitObjectPath("[my_key]", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("\\my_key");
		List<String> expected = new ArrayList<>();
		expected.add("my_key");
		assertEquals(key, splitObjectPath("[\\my_key]", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("\\my_key\\");
		expected = new ArrayList<>();
		expected.add("my_key");
		assertEquals(expected, splitObjectPath("[\"" + "my_key" + "\"]", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("\\my_key\\");
		expected = new ArrayList<>();
		expected.add("key");
		expected.add("my_key");
		assertEquals(expected, splitObjectPath("key[my_key]", ret));
		ret = new ArrayList<>();
		expected = new ArrayList<>();
		expected.add("key");
		assertEquals(expected, splitObjectPath("[ key ]", ret));
		ret = new ArrayList<>();
		key = new ArrayList<>();
		key.add("\\my_key\\");
		expected = new ArrayList<>();
		expected.add("my_key");
		assertEquals(expected, splitObjectPath("[\'" + "my_key" + "\']", ret));
	}
	
	@Test(expected = RuntimeException.class)
	public void splitObjectPathExceptionTest() {
		List<String> ret = new ArrayList<>();
		List<String> key = new ArrayList<String>();
		assertEquals(key, splitObjectPath("[my_key", ret));
	}
	
	//@Test (expected = RuntimeException.class)
	public void splitObjectPathExceptionUnexpectedKeyTest() {
		List<String> ret = new ArrayList<>();
		List<String> key = new ArrayList<String>();
		assertEquals(key, splitObjectPath("key.my_key]", ret));
	}
	
	@Test(expected = RuntimeException.class)
	public void getBiFunction_noisySelfTest() {
		assertNotNull(getBiFunction_noisy(getClass()));
	}
	
	@Test
	public void getBiFunction_noisyTest() {
		biFunctionMap = null;
		assertNotNull(getBiFunction_noisy(String.class));
	}
	
	@Test
	public void toStringArrayForceToStringTest() {
		assertNull(toArrayHelper(null));
		StringBuilder sb = new StringBuilder("[\"key1\",\"key2\",\"key3\"]");
		assertArrayEquals(new String[] { "key1", "key2", "key3" }, toStringArray(sb, null));
	}
	
	@Test
	public void toGenericConvertListTest() {
		assertNull(toGenericConvertList(null));
		
		assertNull(toGenericConvertList(null, null));
		
		List<String> list = new ArrayList<String>();
		GenericConvertList<String> gcList = new ProxyGenericConvertList<String>(list);
		assertEquals(list, toGenericConvertList(gcList, null));
		assertEquals(null, toGenericConvertList(list, null));
		
	}
	
	@Test
	public void toGenericConvertStringMapTest() {
		assertNull(toGenericConvertStringMap(null));
		
		assertNull(toGenericConvertStringMap(null, null));
		assertNull(toGenericConvertStringMap(null, "default"));
		
		assertNull(toGenericConvertStringMap("true", "default"));
		
		Map<String, String> defMap = new HashMap<String, String>();
		defMap.put("key", "value");
		assertEquals(defMap, toGenericConvertStringMap(null, defMap));
		
		GenericConvertMap<String, String> gcMap = new ProxyGenericConvertMap<String, String>(defMap);
		assertEquals(defMap, toGenericConvertStringMap(gcMap, defMap));
		
		Map<String, String> empty = new ProxyGenericConvertMap<String, String>();
		assertEquals(defMap, toGenericConvertStringMap(defMap, empty));
		
		assertEquals(defMap, toGenericConvertStringMap(ConvertJSON.fromMap(defMap), empty));
		assertNotNull(toGenericConvertStringMap("{}", empty));
		
		assertNotNull(toGenericConvertStringMap(123456, empty));
		
	}
}
