package picoded.struct;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GenericConvertMap_test {
	
	private GenericConvertMap<String, String> genericConvertMap = Mockito.mock(GenericConvertMap.class, Mockito.CALLS_REAL_METHODS);

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
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringTest() {
		assertEquals("", genericConvertMap.getString("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringOverloadTest() {
		assertEquals("", genericConvertMap.getString("my_key", "my_object"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getBooleanTest() {
		assertEquals("", genericConvertMap.getBoolean("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getBooleanOverloadTest() {
		assertEquals("", genericConvertMap.getBoolean("my_key", true));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getNumberTest() {
		assertEquals("", genericConvertMap.getNumber("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getNumberOverloadTest() {
		assertEquals("", genericConvertMap.getNumber("my_key", 1));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getIntTest() {
		assertEquals("", genericConvertMap.getNumber("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getIntOverloadTest() {
		assertEquals("", genericConvertMap.getNumber("my_key", 1));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getLongTest() {
		assertEquals("", genericConvertMap.getLong("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getLongOverloadTest() {
		assertEquals("", genericConvertMap.getLong("my_key", 1l));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getFloatTest() {
		assertEquals("", genericConvertMap.getFloat("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getFloatOverloadTest() {
		assertEquals("", genericConvertMap.getFloat("my_key", 1f));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getDoubleTest() {
		assertEquals("", genericConvertMap.getDouble("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getDoubleOverloadTest() {
		assertEquals("", genericConvertMap.getDouble("my_key", 1d));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getByteTest() {
		assertEquals("", genericConvertMap.getByte("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getByteOverloadTest() {
		assertEquals("", genericConvertMap.getByte("my_key", (byte)'a'));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getShortTest() {
		assertEquals("", genericConvertMap.getShort("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getShortOverloadTest() {
		assertEquals("", genericConvertMap.getShort("my_key", (short)'a'));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getUUIDTest() {
		assertEquals("", genericConvertMap.getUUID("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getUUIDOverloadTest() {
		assertEquals("", genericConvertMap.getUUID("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGUIDTest() {
		assertEquals("", genericConvertMap.getGUID("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGUIDOverloadTest() {
		assertEquals("", genericConvertMap.getGUID("my_key", "ok"));
	}
	
	@Test 
	public void getObjectListTest() {
		assertNull(genericConvertMap.getObjectList("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getObjectListOverloadTest() {
		assertEquals("", genericConvertMap.getObjectList("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringMapTest() {
		assertEquals("", genericConvertMap.getStringMap("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringMapOverloadTest() {
		assertEquals("", genericConvertMap.getStringMap("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGenericConvertStringMapTest() {
		assertEquals("", genericConvertMap.getGenericConvertStringMap("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGenericConvertStringMapOverloadTest() {
		assertEquals("", genericConvertMap.getGenericConvertStringMap("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGenericConvertListTest() {
		assertEquals("", genericConvertMap.getGenericConvertList("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getGenericConvertListOverloadTest() {
		assertEquals("", genericConvertMap.getGenericConvertList("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringArrayTest() {
		assertEquals("", genericConvertMap.getStringArray("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getStringArrayOverloadTest() {
		assertEquals("", genericConvertMap.getStringArray("my_key", "ok"));
	}
	
	@Test 
	public void getObjectArrayTest() {
		assertNull(genericConvertMap.getObjectArray("my_key"));
	}
	
	@Test 
	public void getObjectArrayOverloadTest() {
		assertNull(genericConvertMap.getObjectArray("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getNestedObjectTest() {
		assertEquals("", genericConvertMap.getNestedObject("my_key"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getNestedObjectOverloadTest() {
		assertEquals("", genericConvertMap.getNestedObject("my_key", "ok"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void typecastPutTest() {
		assertEquals("", genericConvertMap.typecastPut("my_key", "my_value"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void convertPutTest() {
		assertEquals("", genericConvertMap.convertPut("my_key", "my_value"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void convertPutOverloadTest() {
		assertEquals("", genericConvertMap.convertPut("my_key", "my_value", String.class));
	}
}
