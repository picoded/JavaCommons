package picoded.conv;

import java.math.BigDecimal;

import org.junit.After;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static picoded.conv.GenericConvert.*;

import org.junit.Before;
import org.junit.Test;

public class GenericConvert_test {
	
	private static final double DELTA = 1e-15;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void toStringTest() {
		assertNull("", GenericConvert.toString(null, null));
	}
	
	@Test
	public void toStringFallbackNotNullTest() {
		assertEquals("fallback", GenericConvert.toString(null, "fallback"));
	}
	
	@Test
	public void toStringInputStringTest() {
		assertEquals("inout", GenericConvert.toString("inout", null));
	}
	
	@Test
	public void toStringInputObjectTest() {
		assertEquals("1", GenericConvert.toString(new Integer(1), null));
	}
	
	@Test
	public void toStringSingleParameterTest() {
		assertEquals("1", GenericConvert.toString(new Integer(1)));
	}
	
	@Test
	public void toBooleanTest() {
		assertTrue(toBoolean(null, true));
	}
	
	@Test
	public void toBooleanFallbackFalseTest() {
		assertFalse(toBoolean(null, false));
	}
	
	@Test
	public void toBooleanInputBooleanTest() {
		assertFalse(toBoolean(new Boolean("1"), false));
	}
	
	@Test
	public void toBooleanInputIntegerTest() {
		assertTrue(toBoolean(new Integer("1"), false));
	}
	
	@Test
	public void toBooleanInputStringTest() {
		assertTrue(toBoolean("", true));
	}
	
	@Test
	public void toBooleanInputStringForTrueTest() {
		assertTrue(toBoolean("+", false));
		assertTrue(toBoolean("t", false));
		assertTrue(toBoolean("T", false));
		assertTrue(toBoolean("y", false));
		assertTrue(toBoolean("Y", false));
	}
	
	@Test
	public void toBooleanInputStringForFalseTest() {
		assertFalse(toBoolean("-", false));
		assertFalse(toBoolean("f", false));
		assertFalse(toBoolean("F", false));
		assertFalse(toBoolean("n", false));
		assertFalse(toBoolean("N", false));
	}
	
	@Test
	public void toBooleanInputStringValidTest() {
		assertTrue(toBoolean("1", true));
	}
	
	@Test
	public void toBooleanInputStringNegativeTest() {
		assertFalse(toBoolean("-1", true));
	}
	
	@Test
	public void toBooleanSingleParameterTest() {
		assertTrue(toBoolean("true"));
	}
	
	@Test
	public void toNumberTest() {
		assertEquals(1, GenericConvert.toNumber(null, 1));
		assertEquals(1.0, GenericConvert.toNumber(1.0, null));
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
	}
	
}
