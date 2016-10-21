package picoded.conv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class GenericConvert_test {
	
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
		assertFalse(GenericConvert.toBoolean(0, true));
		/// for string test case
		assertFalse(GenericConvert.toBoolean(null, false));
		assertFalse(GenericConvert.toBoolean("", false));
		assertTrue(GenericConvert.toBoolean("123", false));
		assertTrue(GenericConvert.toBoolean("12", false));
		assertFalse(GenericConvert.toBoolean("-1", false));
		assertFalse(GenericConvert.toBoolean("$%", false));
		
	}
	
	@Test
	public void toNumberTest() {
		List<String> list = new ArrayList<String>();
		assertNotEquals(list, GenericConvert.toNumber(list, 0).intValue());
		assertNotEquals("$%", GenericConvert.toNumber("$%", 0).intValue());
		assertEquals(10, GenericConvert.toNumber(10, 0).intValue());
		assertNotEquals("", GenericConvert.toNumber("", 0).intValue());
		assertEquals(new BigDecimal("01111111111111111"), GenericConvert.toNumber("01111111111111111", 0));
		
	}
}
