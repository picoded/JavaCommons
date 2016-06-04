package picodedTests.conv;

// Target test class
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

// Test Case include
import org.junit.Test;

import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.conv.GenericConvert;

// Classes used in test case

///
/// Test Case for picoded.conv.GenericConvert
///
public class GenericConvert_test {
	
	private static final double DELTA = 1e-15;
	private static final String jsonString = "[{\"balance\":1000.21, \"num\":100, \"nickname\":\"tommy\", \"is_vip\":true, \"name\":\"foo\"}]";
	String output = "{balance:1000.21,num:100,nickname:tommy,is_vip:true,name:foo}";
	
	@Test
	public void toStringTest() {
		assertEquals("hello world", GenericConvert.toString("hello world"));
		assertEquals("100", GenericConvert.toString(100));
		assertEquals("hello world", GenericConvert.toString(null, "hello world"));
		assertNull(GenericConvert.toString(null, null));
		assertEquals("hello", GenericConvert.toString("hello", "hello world"));
	}
	
	@Test
	public void toBooleanTest() {
		assertEquals(true, GenericConvert.toBoolean("hello world", true));
		assertEquals(false, GenericConvert.toBoolean("hello world", false));
		
		assertEquals(true, GenericConvert.toBoolean("1"));
		assertEquals(false, GenericConvert.toBoolean("-"));
		assertEquals(true, GenericConvert.toBoolean(null, true));
		assertEquals(true, GenericConvert.toBoolean(1, true));
	}
	
	@Test
	public void toNumberTest() {
		assertEquals(1, GenericConvert.toNumber(null, 1));
		assertEquals(1.0, GenericConvert.toNumber(1.0, null));
		assertEquals(new BigDecimal("2.1"), GenericConvert.toNumber("2.1", null));
		assertEquals(new BigDecimal("2.2"), GenericConvert.toNumber("2.2"));
	}
	
	@Test
	public void toIntTest() {
		assertEquals(1, GenericConvert.toInt(null, 1));
		assertEquals(2, GenericConvert.toInt(2, 1));
		assertEquals(3, GenericConvert.toInt(3));
	}
	
	@Test
	public void toLongTest() {
		System.out.println(GenericConvert.toLong(null, 1l));
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
		//assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"), GenericConvert.toUUID("4a9547b2-c9d4-a87d-a3fc", null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(null, GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertNull(GenericConvert.toUUID(null, null));
		assertEquals(GUID.fromBase58("heijoworjdabcdefghijabc"),
			GenericConvert.toUUID(GUID.fromBase58("heijoworjdabcdefghijabc"), "hello world"));
	}
	
	@Test
	public void toGUIDTest() {
		assertNull(GenericConvert.toGUID("hello-world"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(GUID.fromBase58("heijoworjdabcdefghijabc")));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o"));
		//assertEquals("100", GenericConvert.toGUID(100));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID(null, "ADAukG8u3ryYrm6pHFDB6o"));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", null));
		assertNull(GenericConvert.toGUID(null, null));
		assertEquals("ADAukG8u3ryYrm6pHFDB6o", GenericConvert.toGUID("ADAukG8u3ryYrm6pHFDB6o", "hello world"));
	}
	
	@Test
	public void toStringArrayTest() {
		assertNull(GenericConvert.toStringArray(null, null));
		assertArrayEquals(new String[] { "hello world" },
			GenericConvert.toStringArray(new String[] { "hello world" }, null));
		assertArrayEquals(new String[] { "1", "2", "3" }, GenericConvert.toStringArray(new Integer[] { 1, 2, 3 }, null));
		assertArrayEquals(new String[] { "100" }, GenericConvert.toStringArray(new Integer[] { 100 }));
		assertArrayEquals(new String[] { "1.0" }, GenericConvert.toStringArray(new Double[] { 1.0 }, "hello world"));
		String[] inputArray = GenericConvert.toStringArray(jsonString);
		String input = inputArray[0];
		input = input.replaceAll("\"", "");
		assertEquals(output, input);
		
		inputArray = GenericConvert.toStringArray(ConvertJSON.toList(jsonString), null);
		input = inputArray[0];
		input = input.replaceAll("\"", "");
		assertEquals(output, input);
	}
	
	@Test
	public void toObjectArrayTest() {
		assertNull(GenericConvert.toObjectArray(null, null));
		assertArrayEquals(new String[] { "hello world" },
			GenericConvert.toObjectArray(new String[] { "hello world" }, null));
		assertArrayEquals(new Integer[] { 1, 2, 3 }, GenericConvert.toObjectArray(new Object[] { 1, 2, 3 }, null));
		assertArrayEquals(new Integer[] { 100 }, GenericConvert.toObjectArray(new Object[] { 100 }));
		assertArrayEquals(new Double[] { 1.0 }, GenericConvert.toObjectArray(new Object[] { 1.0 }, "hello world"));
		
		assertArrayEquals(ConvertJSON.toList(jsonString).toArray(), GenericConvert.toObjectArray(jsonString));
		assertArrayEquals(ConvertJSON.toList(jsonString).toArray(),
			GenericConvert.toObjectArray(ConvertJSON.toList(jsonString), null));
	}
	
}