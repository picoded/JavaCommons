package picoded.struct;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GenericConvertList_test {
	
	private GenericConvertList<String> genericConvertList = Mockito.mock(GenericConvertList.class, Mockito.CALLS_REAL_METHODS);
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test 
	public void buildTest() {
		assertNotNull(GenericConvertList.build(new ArrayList<String>()));
	}
	
	@Test 
	public void getSilentTest() {
		assertNull(genericConvertList.getSilent(0));
	}
	
	@Test 
	public void getSilentNonZeroTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertEquals("me", genericConvertList.getSilent(1));
	}
	
	@Test 
	public void getStringTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertEquals("me", genericConvertList.getString(1));
	}
	
	@Test 
	public void getString2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals("1", genericConvertList.getString(1, "ok"));
	}
	
	@Test 
	public void getBooleanTest() {
		assertEquals(false, genericConvertList.getBoolean(1));
	}
	
	@Test 
	public void getBoolean2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("false");
		assertEquals(false, genericConvertList.getBoolean(1, true));
	}
	
	@Test 
	public void getNumberTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(BigDecimal.valueOf(1), genericConvertList.getNumber(1));
	}
	
	@Test 
	public void getNumber2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(BigDecimal.valueOf(1), genericConvertList.getNumber(1, -1));
	}
	
	@Test 
	public void getIntTest() {
		assertEquals(0, genericConvertList.getInt(1));
	}
	
	@Test 
	public void getInt2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(1, genericConvertList.getInt(1, -1));
	}
	
	@Test 
	public void getLongTest() {
		assertEquals(0, genericConvertList.getLong(1));
	}
	
	@Test 
	public void getFloatTest() {
		assertEquals(0, genericConvertList.getFloat(1), 0.01);
	}
	
	@Test 
	public void getDoubleTest() {
		assertEquals(0, genericConvertList.getDouble(1), 0.01);
	}
	
	@Test 
	public void getDouble2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(1.0, genericConvertList.getDouble(1, -1l), 0.01);
	}
	
	@Test 
	public void getByteTest() {
		assertEquals(0, genericConvertList.getByte(1));
	}
	
	@Test 
	public void getByte2ParamTest() {
		byte temp = 2;
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(1, genericConvertList.getByte(1, temp));
	}
	
	@Test 
	public void getShortTest() {
		assertEquals(0, genericConvertList.getShort(1));
	}
	
	@Test 
	public void getShort2ParamTest() {
		short temp = 2;
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertEquals(1, genericConvertList.getShort(1, temp));
	}
	
	@Test 
	public void getUUIDTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertNull(genericConvertList.getUUID(1));
	}
	
	@Test 
	public void getUUID2ParamTest() {
		assertNull(genericConvertList.getUUID(1, "ok"));
	}
	
	@Test 
	public void getGUIDTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("123456789o123456789o12");
		assertEquals("123456789o123456789o12", genericConvertList.getGUID(1));
	}
	
	
	@Test 
	public void getObjectListTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("1");
		assertNull(genericConvertList.getObjectList(1));
	}
	
	
	@Test 
	public void getStringArray2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertNull(genericConvertList.getStringArray(1, "ok"));
	}
	
	@Test 
	public void getObjectArrayTest() {
		assertNull(genericConvertList.getObjectArray(1));
	}
	
	@Test 
	public void getObjectArray2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertNull(genericConvertList.getObjectArray(1, "ok"));
	}
	
	
	@Test 
	public void getNestedObject2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertEquals("me", genericConvertList.getNestedObject("1", "ok"));
	}
	
	@Test 
	public void getGenericConvertStringMapTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertNull(genericConvertList.getGenericConvertStringMap(1));
	}
	
	@Test 
	public void getGenericConvertStringMap2ParamTest() {
		when(genericConvertList.size()).thenReturn(2);
		when(genericConvertList.get(1)).thenReturn("me");
		assertNull(genericConvertList.getGenericConvertStringMap(1, "ok"));
	}
	
	@Test 
	public void getGenericConvertListTest() {
		when(genericConvertList.size()).thenReturn(1);
		assertNull(genericConvertList.getGenericConvertList(0));
	}
	
	@Test 
	public void getGenericConvertListSecondTest() {
		when(genericConvertList.size()).thenReturn(1);
		assertNull(genericConvertList.getGenericConvertList(1));
	}
	
	@Test 
	public void getGenericConvertList2ParamTest() {
		assertNull(genericConvertList.getGenericConvertList(1, "ok"));
	}
}
