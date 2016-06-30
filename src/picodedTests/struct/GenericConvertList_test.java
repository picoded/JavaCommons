package picodedTests.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.conv.GUID;
import picoded.struct.GenericConvertArrayList;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GenericConvertArrayList_test {

	private GenericConvertArrayList convertArrayList = null;

	@Before
	public void setUp() {
		
	}

	@After
	public void tearDown() {
	}

	// / The following, test severals assumptions regarding Object[] instanceof
	// tests
	// / done in java, this is required for the generic toXArray functions.
	@Test
	public void arrayInstanceOfTest() {
		String sample = "a";
		assertFalse(((Object) sample instanceof Object[]));

		String[] strArr = new String[] { "1", "2", "3" };
		Object[] objArr = new Object[] { "1", "2", "3" };
		Integer[] intArr = new Integer[] { 1, 2, 3 };

		// / String array tests
		assertTrue((strArr instanceof Object[]));
		assertTrue(((Object) strArr instanceof Object));

		assertTrue(((Object) strArr instanceof String[]));
		assertFalse(((Object) strArr instanceof Integer[]));
		assertFalse(((Object) strArr instanceof String));
		assertFalse(((Object) strArr instanceof Integer));

		// / Integer array tests
		assertTrue((intArr instanceof Object[]));
		assertTrue(((Object) intArr instanceof Object));

		assertFalse(((Object) intArr instanceof String[]));
		assertTrue(((Object) intArr instanceof Integer[]));
		assertFalse(((Object) intArr instanceof String));
		assertFalse(((Object) intArr instanceof Integer));

		// / Object array tests
		assertTrue((objArr instanceof Object[]));
		assertTrue(((Object) objArr instanceof Object));

		assertFalse(((Object) objArr instanceof String[]));
		assertFalse(((Object) objArr instanceof Integer[]));
		assertFalse(((Object) objArr instanceof String));
		assertFalse(((Object) objArr instanceof Integer));
	}

	@Test
	public void classLoockUpTest() {
		assertEquals(String.class, String.class);
		assertEquals(String[].class, String[].class);
		assertNotEquals(String.class, String[].class);
	}

	@Test
	public void intTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertTrue(convertArrayList.isEmpty());
		convertArrayList.add("itemA");
		assertEquals(1, convertArrayList.size());
		convertArrayList.add("itemB");
        assertEquals(2, convertArrayList.size());
	}

	@Test
	public void testAddAndGet() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		convertArrayList.add(-3);
		convertArrayList.add(17);
		convertArrayList.add(99);
		assertEquals(42, convertArrayList.get(0));
		assertEquals(-3, convertArrayList.get(1));
		assertEquals(17, convertArrayList.get(2));
		assertEquals(99, convertArrayList.get(3));
		assertEquals("second attempt", 42, convertArrayList.get(0));
		assertEquals("second attempt", 99, convertArrayList.get(3));
	}

	@Test
	public void testSize() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertEquals(0, convertArrayList.size());
		convertArrayList.add(42);
		assertEquals(1, convertArrayList.size());
		convertArrayList.add(-3);
		assertEquals(2, convertArrayList.size());
		convertArrayList.add(17);
		assertEquals(3, convertArrayList.size());
		convertArrayList.add(99);
		assertEquals(4, convertArrayList.size());
		assertEquals("second attempt", 4, convertArrayList.size());
	}

	@Test
	public void testIsEmpty() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertTrue(convertArrayList.isEmpty());
		convertArrayList.add(42);
		assertFalse("should have one element", convertArrayList.isEmpty());
		convertArrayList.add(-3);
		assertFalse("should have two elements", convertArrayList.isEmpty());
	}

	@Test
	public void testIsEmpty1() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		convertArrayList.add(-3);
		assertFalse("should have two elements", convertArrayList.isEmpty());
		convertArrayList.remove(1);
		convertArrayList.remove(0);
		assertTrue("after removing all elements", convertArrayList.isEmpty());
		convertArrayList.add(42);
		assertFalse("should have one element", convertArrayList.isEmpty());
	}
	
	@Test
	public void getStringTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		assertEquals("42", convertArrayList.getString(0));
	}
	
	@Test
	public void getString2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		assertEquals("42", convertArrayList.getString(0, "43"));
	}
	
	@Test
	public void getBooleanTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		assertEquals(true, convertArrayList.getBoolean(0));
	}
	
	@Test
	public void getBoolean2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(-42);
		assertEquals(false, convertArrayList.getBoolean(0, false));
	}
	
	@Test
	public void getNumberTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		assertEquals(42, convertArrayList.getNumber(0));
	}
	
	@Test
	public void getNumber2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("a");
		assertEquals(24, convertArrayList.getNumber(0, 24));
	}
	
	@Test (expected = java.lang.UnsupportedOperationException.class)
	public void getIntTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		assertEquals(42, convertArrayList.getInt("24"));
	}
	
	@Test
	public void getInt2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("a");
		assertEquals(24, convertArrayList.getInt(0, 24));
	}
	
	@Test
	public void getLongTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42l);
		assertEquals(42, convertArrayList.getLong(0));
	}
	
	@Test
	public void getLong2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		assertEquals(24, convertArrayList.getLong(0, 24l));
	}
	
	@Test
	public void getFloatTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42.09);
		assertEquals(42.09, convertArrayList.getFloat(0), 0.0001);
	}
	
	@Test
	public void getFloat2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		assertEquals(24.9, convertArrayList.getFloat(0, 24.9f), 0.00001);
	}
	
	@Test
	public void getDoubleTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42.09);
		assertEquals(42.09, convertArrayList.getDouble(0), 0.0001);
	}
	
	@Test
	public void getDouble2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		assertEquals(24.9, convertArrayList.getDouble(0, 24.9f), 0.00001);
	}
	
	@Test
	public void getByteTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("a");
		assertEquals(0, convertArrayList.getByte(0));
	}
	
	@Test
	public void getByte2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		assertEquals(97, convertArrayList.getByte(0, (byte)'a'));
	}
	
	@Test
	public void getShortTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("aa");
		assertEquals(0, convertArrayList.getShort(0));
	}
	
	@Test
	public void getShort2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		assertEquals(97, convertArrayList.getShort(0, (short)'a'));
	}
	
	@Test
	public void getUUIDTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		UUID uuid = GUID.randomUUID();
		convertArrayList.add(uuid);
		assertEquals(uuid, convertArrayList.getUUID(0));
	}
	
	@Test
	public void getUUID2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("UID");
		UUID uuid = GUID.randomUUID();
		assertEquals(uuid, convertArrayList.getUUID(0, uuid));
	}
	
	@Test
	public void getGUIDTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		UUID uuid = GUID.randomUUID();
		convertArrayList.add(uuid);
		assertEquals(GUID.base58(uuid), convertArrayList.getGUID(0));
	}
	
	@Test
	public void getGUID2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add("");
		UUID uuid = GUID.randomUUID();
		assertEquals(GUID.base58(uuid), convertArrayList.getGUID(0, uuid));
	}
	
	@Test
	public void getObjectListNullTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		convertArrayList.add(list);
		assertNull(convertArrayList.getObjectList(0));
	}
	
	@Test
	public void getObjectListTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		list.add("aa");
		list.add("bb");
		convertArrayList.add(list);
		assertEquals(list, convertArrayList.getObjectList(0));
	}
	
	@Test
	public void getObjectList2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		list.add("aa");
		list.add("bb");
		convertArrayList.add(list);
		assertEquals(list, convertArrayList.getObjectList(0, list));
	}
	
	@Test (expected = java.lang.UnsupportedOperationException.class)
	public void getStringArrayTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		convertArrayList.add(list);
		assertEquals(list, convertArrayList.getStringArray("a,b,c,d"));
	}
	
	@Test
	public void getStringArray2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		list.add("aa");
		list.add("bb");
		convertArrayList.add(list);
		assertEquals(list.toArray(), convertArrayList.getStringArray(0, list));
	}
	
	@Test 
	public void getObjectArrayTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		convertArrayList.add(list);
		assertNull(convertArrayList.getObjectArray(0));
	}
	
	@Test
	public void getObjectArray2ParamTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		List<String> list = new ArrayList<>();
		list.add("aa");
		list.add("bb");
		convertArrayList.add(list);
		assertEquals(list.toArray(), convertArrayList.getStringArray(0, list));
	}
}
