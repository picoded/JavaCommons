package picoded.struct;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenericConvertMap_test {
	
	//	@SuppressWarnings("unchecked")
	//	private GenericConvertMap<String, String> genericConvertMap = Mockito.mock(
	//		GenericConvertMap.class, Mockito.CALLS_REAL_METHODS);
	//	
	//	@SuppressWarnings("unchecked")
	//	private GenericConvertMap<String, String> genericConvertMapForValid = Mockito
	//		.mock(GenericConvertMap.class);
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void buildTest() {
		assertNotNull(GenericConvertMap.build(new HashMap<String, String>()));
	}
	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringTest() {
	//		assertEquals("", genericConvertMap.getString("my_key"));
	//	}
	//	
	//	@Test
	//	public void getStringValidTest() {
	//		when(genericConvertMapForValid.getString("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("key");
	//		assertEquals("key", genericConvertMapForValid.getString("my_key"));
	//	}
	//	
	//	@Test
	//	public void getStringInvalidTest() {
	//		when(genericConvertMapForValid.getString("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("key");
	//		assertEquals(null, genericConvertMapForValid.getString("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringOverloadTest() {
	//		assertEquals("", genericConvertMap.getString("my_key", "my_object"));
	//	}
	//	
	//	@Test
	//	public void getStringOverloadValidTest() {
	//		when(genericConvertMapForValid.getString("my_key", "my_object")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("key");
	//		assertEquals("key", genericConvertMapForValid.getString("my_key", "my_object"));
	//	}
	//	
	//	@Test
	//	public void getStringOverloadValidAlternateTest() {
	//		when(genericConvertMapForValid.getString("my_key", "my_object")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("key");
	//		assertEquals("my_object", genericConvertMapForValid.getString("my_key", "my_object"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getBooleanTest() {
	//		assertEquals("", genericConvertMap.getBoolean("my_key"));
	//	}
	//	
	//	@Test
	//	public void getBooleanValidTest() {
	//		Mockito.when(genericConvertMapForValid.getBoolean("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("true");
	//		assertTrue(genericConvertMapForValid.getBoolean("my_key"));
	//	}
	//	
	//	@Test
	//	public void getBooleanValidFalseTest() {
	//		Mockito.when(genericConvertMapForValid.getBoolean("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("true");
	//		assertFalse(genericConvertMapForValid.getBoolean("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getBooleanOverloadTest() {
	//		assertEquals("", genericConvertMap.getBoolean("my_key", true));
	//	}
	//	
	//	@Test
	//	public void getBooleanOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getBoolean("my_key", true)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("true");
	//		assertTrue(genericConvertMapForValid.getBoolean("my_key", true));
	//	}
	//	
	//	@Test
	//	public void getBooleanOverloadValidTrueTest() {
	//		Mockito.when(genericConvertMapForValid.getBoolean("my_key", true)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("true");
	//		assertTrue(genericConvertMapForValid.getBoolean("my_key", true));
	//	}
	//	
	//	@Test
	//	public void getBooleanOverloadValidFalseTest() {
	//		Mockito.when(genericConvertMapForValid.getBoolean("my_key", true)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("true");
	//		assertFalse(genericConvertMapForValid.getBoolean("my_key", false));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getNumberTest() {
	//		assertEquals("", genericConvertMap.getNumber("my_key"));
	//	}
	//	
	//	@Test
	//	public void getNumberValidTest() {
	//		Mockito.when(genericConvertMapForValid.getNumber("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(BigDecimal.valueOf(1), genericConvertMapForValid.getNumber("my_key"));
	//	}
	//	
	//	@Test
	//	public void getNumberNullTest() {
	//		Mockito.when(genericConvertMapForValid.getNumber("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getNumber("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getNumberOverloadTest() {
	//		assertEquals("", genericConvertMap.getNumber("my_key", 1));
	//	}
	//	
	//	@Test
	//	public void getNumberOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getNumber("my_key", 5)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(BigDecimal.valueOf(1), genericConvertMapForValid.getNumber("my_key", 5));
	//	}
	//	
	//	@Test
	//	public void getNumberOverloadValidAlternateTest() {
	//		Mockito.when(genericConvertMapForValid.getNumber("my_key", 5)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(5, genericConvertMapForValid.getNumber("my_key", 5));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getIntTest() {
	//		assertEquals("", genericConvertMap.getInt("my_key"));
	//	}
	//	
	//	@Test
	//	public void getIntValidTest() {
	//		Mockito.when(genericConvertMapForValid.getInt("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1, genericConvertMapForValid.getInt("my_key"));
	//	}
	//	
	//	@Test
	//	public void getIntInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getInt("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(0, genericConvertMapForValid.getInt("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getIntOverloadTest() {
	//		assertEquals("", genericConvertMap.getInt("my_key", 1));
	//	}
	//	
	//	@Test
	//	public void getIntOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getInt("my_key", 5)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1, genericConvertMapForValid.getInt("my_key", 5));
	//	}
	//	
	//	@Test
	//	public void getIntOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getInt("my_key", 5)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(5, genericConvertMapForValid.getInt("my_key", 5));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getLongTest() {
	//		assertEquals("", genericConvertMap.getLong("my_key"));
	//	}
	//	
	//	@Test
	//	public void getLongValidTest() {
	//		Mockito.when(genericConvertMapForValid.getLong("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1l, genericConvertMapForValid.getLong("my_key"));
	//	}
	//	
	//	@Test
	//	public void getLongInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getLong("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(0l, genericConvertMapForValid.getLong("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getLongOverloadTest() {
	//		assertEquals("", genericConvertMap.getLong("my_key", 1l));
	//	}
	//	
	//	@Test
	//	public void getLongOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getLong("my_key", 5l)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1l, genericConvertMapForValid.getLong("my_key", 5l));
	//	}
	//	
	//	@Test
	//	public void getLongOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getLong("my_key", 5l)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(5l, genericConvertMapForValid.getLong("my_key", 5l));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getFloatTest() {
	//		assertEquals("", genericConvertMap.getFloat("my_key"));
	//	}
	//	
	//	@Test
	//	public void getFloatValidTest() {
	//		Mockito.when(genericConvertMapForValid.getFloat("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1f, genericConvertMapForValid.getFloat("my_key"), 0.01);
	//	}
	//	
	//	@Test
	//	public void getFloatInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getFloat("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(0f, genericConvertMapForValid.getFloat("my_key"), 0.01);
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getFloatOverloadTest() {
	//		assertEquals("", genericConvertMap.getFloat("my_key", 1f));
	//	}
	//	
	//	@Test
	//	public void getFloatOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getFloat("my_key", 1f)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1f, genericConvertMapForValid.getFloat("my_key", 1f), 0.01);
	//	}
	//	
	//	@Test
	//	public void getFloatOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getFloat("my_key", 5f)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(5f, genericConvertMapForValid.getFloat("my_key", 5f), 0.01);
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getDoubleTest() {
	//		assertEquals("", genericConvertMap.getDouble("my_key"));
	//	}
	//	
	//	@Test
	//	public void getDoubleValidTest() {
	//		Mockito.when(genericConvertMapForValid.getDouble("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1f, genericConvertMapForValid.getDouble("my_key"), 0.01);
	//	}
	//	
	//	@Test
	//	public void getDoubleInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getDouble("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(0f, genericConvertMapForValid.getDouble("my_key"), 0.01);
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getDoubleOverloadTest() {
	//		assertEquals("", genericConvertMap.getDouble("my_key", 1d));
	//	}
	//	
	//	@Test
	//	public void getDoubleOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getDouble("my_key", 5d)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1d, genericConvertMapForValid.getDouble("my_key", 5d), 0.01);
	//	}
	//	
	//	@Test
	//	public void getDoubleOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getDouble("my_key", 5d)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(5d, genericConvertMapForValid.getDouble("my_key", 5d), 0.01);
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getByteTest() {
	//		assertEquals("", genericConvertMap.getByte("my_key"));
	//	}
	//	
	//	@Test
	//	public void getByteValidTest() {
	//		Mockito.when(genericConvertMapForValid.getByte("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals((byte) 1, genericConvertMapForValid.getByte("my_key"));
	//	}
	//	
	//	@Test
	//	public void getByteInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getByte("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals((byte) 0, genericConvertMapForValid.getByte("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getByteOverloadTest() {
	//		assertEquals("", genericConvertMap.getByte("my_key", (byte) 'a'));
	//	}
	//	
	//	@Test
	//	public void getByteOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getByte("my_key", (byte) 'a')).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals((byte) 1, genericConvertMapForValid.getByte("my_key", (byte) 'a'));
	//	}
	//	
	//	@Test
	//	public void getByteOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getByte("my_key", (byte) 'a')).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals((byte) 'a', genericConvertMapForValid.getByte("my_key", (byte) 'a'));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getShortTest() {
	//		assertEquals("", genericConvertMap.getShort("my_key"));
	//	}
	//	
	//	@Test
	//	public void getShortValidTest() {
	//		Mockito.when(genericConvertMapForValid.getShort("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1, genericConvertMapForValid.getShort("my_key"));
	//	}
	//	
	//	@Test
	//	public void getShortInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getShort("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(0, genericConvertMapForValid.getShort("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getShortOverloadTest() {
	//		assertEquals("", genericConvertMap.getShort("my_key", (short) 'a'));
	//	}
	//	
	//	@Test
	//	public void getShortOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getShort("my_key", (short) 'a')).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals(1, genericConvertMapForValid.getShort("my_key", (short) 'a'));
	//	}
	//	
	//	@Test
	//	public void getShortOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getShort("my_key", (short) 'a')).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals((short) 'a', genericConvertMapForValid.getShort("my_key", (short) 'a'));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getUUIDTest() {
	//		assertEquals("", genericConvertMap.getUUID("my_key"));
	//	}
	//	
	//	@Test
	//	public void getUUIDValidTest() {
	//		Mockito.when(genericConvertMapForValid.getUUID("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getUUID("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getUUIDOverloadTest() {
	//		assertEquals("", genericConvertMap.getUUID("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getUUIDOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getUUID("my_key", "ok")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getUUID("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getUUIDOverloadInvalidTest() {
	//		Mockito.when(genericConvertMapForValid.getUUID("my_key", "o123456789o123456789ok"))
	//			.thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertNotNull(genericConvertMapForValid.getUUID("my_key", "o123456789o123456789ok"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGUIDTest() {
	//		assertEquals("", genericConvertMap.getGUID("my_key"));
	//	}
	//	
	//	@Test
	//	public void getGUIDValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGUID("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGUID("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGUIDOverloadTest() {
	//		assertEquals("", genericConvertMap.getGUID("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getGUIDOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGUID("my_key", "ok")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGUID("my_key", "ok"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getObjectListTest() {
	//		assertNull(genericConvertMap.getObjectList("my_key"));
	//	}
	//	
	//	@Test
	//	public void getObjectListValidTest() {
	//		Mockito.when(genericConvertMapForValid.getObjectList("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getObjectList("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getObjectListOverloadTest() {
	//		assertEquals("", genericConvertMap.getObjectList("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getObjectListOverloadValidTest() {
	//		List<String> list = new ArrayList<>();
	//		list.add("me");
	//		Mockito.when(genericConvertMapForValid.getObjectList("my_key", list)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals(list, genericConvertMapForValid.getObjectList("my_key", list));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringMapTest() {
	//		assertEquals("", genericConvertMap.getStringMap("my_key"));
	//	}
	//	
	//	@Test
	//	public void getStringMapValidTest() {
	//		Mockito.when(genericConvertMapForValid.getStringMap("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getStringMap("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringMapOverloadTest() {
	//		assertEquals("", genericConvertMap.getStringMap("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getStringMapOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getStringMap("my_key", "ok")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getStringMap("my_key", "ok"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGenericConvertStringMapTest() {
	//		assertEquals("", genericConvertMap.getGenericConvertStringMap("my_key"));
	//	}
	//	
	//	@Test
	//	public void getGenericConvertStringMapValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGenericConvertStringMap("my_key"))
	//			.thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGenericConvertStringMap("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGenericConvertStringMapOverloadTest() {
	//		assertEquals("", genericConvertMap.getGenericConvertStringMap("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getGenericConvertStringMapOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGenericConvertStringMap("my_key", "ok"))
	//			.thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGenericConvertStringMap("my_key", "ok"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGenericConvertListTest() {
	//		assertEquals("", genericConvertMap.getGenericConvertList("my_key"));
	//	}
	//	
	//	@Test
	//	public void getGenericConvertListValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGenericConvertList("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGenericConvertList("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getGenericConvertListOverloadTest() {
	//		assertEquals("", genericConvertMap.getGenericConvertList("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getGenericConvertListOverloadValidTest() {
	//		Mockito.when(genericConvertMapForValid.getGenericConvertList("my_key", "ok"))
	//			.thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getGenericConvertList("my_key", "ok"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringArrayTest() {
	//		assertEquals("", genericConvertMap.getStringArray("my_key"));
	//	}
	//	
	//	@Test
	//	public void getStringArrayValidTest() {
	//		Mockito.when(genericConvertMapForValid.getStringArray("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertNull(genericConvertMapForValid.getStringArray("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getStringArrayOverloadTest() {
	//		assertEquals("", genericConvertMap.getStringArray("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getStringArrayOverloadValidTest() {
	//		String[] strArr = new String[] { "1", "2" };
	//		Mockito.when(genericConvertMapForValid.getStringArray("my_key", strArr)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1, 2");
	//		assertArrayEquals(strArr, genericConvertMapForValid.getStringArray("my_key", strArr));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getObjectArrayTest() {
	//		assertNull(genericConvertMap.getObjectArray("my_key"));
	//	}
	//	
	//	@Test
	//	public void getObjectArrayValidTest() {
	//		Mockito.when(genericConvertMapForValid.getObjectArray("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1, 2");
	//		assertNull(genericConvertMapForValid.getObjectArray("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getObjectArrayOverloadTest() {
	//		assertNull(genericConvertMap.getObjectArray("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getObjectArrayOverloadValidTest() {
	//		Object[] strArr = new Object[] { "1", 2 };
	//		when(genericConvertMapForValid.getObjectArray("my_key", strArr)).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1, 2");
	//		assertArrayEquals(strArr, genericConvertMapForValid.getObjectArray("my_key", strArr));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getNestedObjectTest() {
	//		assertEquals("", genericConvertMap.getNestedObject("my_key"));
	//	}
	//	
	//	@Test
	//	public void getNestedObjectValidTest() {
	//		when(genericConvertMapForValid.getNestedObject("my_key")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals("1", genericConvertMapForValid.getNestedObject("my_key"));
	//	}
	//	
	//	@Test(expected = UnsupportedOperationException.class)
	//	public void getNestedObjectOverloadTest() {
	//		assertEquals("", genericConvertMap.getNestedObject("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getNestedObjectOverloadValidTest() {
	//		when(genericConvertMapForValid.getNestedObject("my_key", "ok")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	//		assertEquals("1", genericConvertMapForValid.getNestedObject("my_key", "ok"));
	//	}
	//	
	//	@Test
	//	public void getNestedObjectOverloadInvalidTest() {
	//		when(genericConvertMapForValid.getNestedObject("my_key", "ok")).thenCallRealMethod();
	//		when(genericConvertMapForValid.get("key")).thenReturn("1");
	//		assertEquals("ok", genericConvertMapForValid.getNestedObject("my_key", "ok"));
	//	}
	
	// @Test(expected = UnsupportedOperationException.class)
	// public void typecastPutTest() {
	// 	assertEquals("", genericConvertMap.typecastPut("my_key", "my_value"));
	// }
	//
	// @Test
	// public void typecastPutValidTest() {
	// 	when(genericConvertMapForValid.typecastPut("my_key", "my_value")).thenCallRealMethod();
	// 	when(genericConvertMapForValid.get("my_key")).thenReturn("1");
	// 	when(genericConvertMapForValid.put("my_key", "my_value")).thenReturn("my_value");
	// 	assertEquals("my_value", genericConvertMapForValid.typecastPut("my_key", "my_value"));
	// }
	// 
	// @Test(expected = UnsupportedOperationException.class)
	// public void convertPutTest() {
	// 	assertEquals("", genericConvertMap.convertPut("my_key", "my_value"));
	// }
	// 
	// @Test(expected = UnsupportedOperationException.class)
	// public void convertPutOverloadTest() {
	// 	assertEquals("", genericConvertMap.convertPut("my_key", "my_value", String.class));
	// }
	// 
	// @Test
	// public void convertPutOverloadValidTest() {
	// 	when(genericConvertMapForValid.convertPut("my_key", "my_value", String.class)).thenCallRealMethod();
	// 	when(genericConvertMapForValid.get("my_key")).thenReturn("my_value");
	// 	when(genericConvertMapForValid.put("my_key", "my_key")).thenReturn("my_key");
	// 	assertEquals("my_key", genericConvertMapForValid.convertPut("my_key", "my_value", String.class));
	// }
}
