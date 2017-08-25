package picoded.core.struct.query;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.query.CompareUtils;

///
/// Test Case for picoded.core.struct.query.condition.*
///
public class CompareUtils_test {
	
	//
	// Test Setup
	//--------------------------------------------------------------------
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	/// Used to pass an empty test
	@Test
	public void blankTest() {
		assertNotNull("");
	}
	
	//
	// Query test
	//--------------------------------------------------------------------
	
	// Simple equality test
	@Test
	public void simpleDynamicCompare() {
		assertEquals(0, CompareUtils.dynamicCompare(null, null));
		
		assertEquals(0, CompareUtils.dynamicCompare(new Integer(1), new Integer(1)));
		assertEquals(1, CompareUtils.dynamicCompare(new Integer(2), new Integer(1)));
		assertEquals(-1, CompareUtils.dynamicCompare(new Integer(1), new Integer(2)));
		
		assertEquals(0, CompareUtils.dynamicCompare(new Integer(1), "1"));
		assertEquals(1, CompareUtils.dynamicCompare(new Integer(2), "1"));
		assertEquals(0, CompareUtils.dynamicCompare("1", "1"));
		assertEquals(-1, CompareUtils.dynamicCompare(new Integer(0), "1"));
		
		assertEquals(1, CompareUtils.dynamicCompare("c", "b"));
		assertEquals(0, CompareUtils.dynamicCompare("b", "b"));
		assertEquals(-1, CompareUtils.dynamicCompare("a", "b"));
		
		assertEquals(1, CompareUtils.dynamicCompare("b", null));
		assertEquals(1, CompareUtils.dynamicCompare(new Integer(1), null));
		assertEquals(-1, CompareUtils.dynamicCompare(null, new Integer(1)));
		assertEquals(-1, CompareUtils.dynamicCompare(null, "b"));
		
		// Double compare
		assertEquals(0, CompareUtils.dynamicCompare(new Double(1.2), new Double(1.2)));
		assertEquals(1, CompareUtils.dynamicCompare(new Double(2.5), new Double(1.0)));
		assertEquals(1, CompareUtils.dynamicCompare(new Double(2.5), new Double(2.2)));
		assertEquals(-1, CompareUtils.dynamicCompare(new Double(1.0), new Double(2.5)));
		assertEquals(1, CompareUtils.dynamicCompare(new Double(2.5), "2.2"));
		
	}
	
	@Test(expected = IllegalAccessError.class)
	public void compareUtilsTest() {
		new CompareUtils();
	}
	
	@Test
	public void stringMatchesTest() {
		assertEquals(0, CompareUtils.stringMatches(null, null));
		assertEquals(1, CompareUtils.stringMatches("b", null));
		assertEquals(-1, CompareUtils.stringMatches(null, "b"));
		assertEquals(0, CompareUtils.stringMatches("b", "b"));
		assertEquals(-1, CompareUtils.stringMatches("b", "c"));
	}
	
	@Test
	public void stringLikeCompareTest() {
		assertEquals(0, CompareUtils.stringLikeCompare(null, null));
		assertEquals(-1, CompareUtils.stringLikeCompare(null, "a"));
		assertEquals(1, CompareUtils.stringLikeCompare("A", null));
		assertEquals(1, CompareUtils.stringLikeCompare("A", "a"));
		assertEquals(0, CompareUtils.stringLikeCompare("A", "A"));
	}
	
	@Test
	public void objectToNumberIfPossibleTest() {
		assertNull(CompareUtils.objectToNumberIfPossible("a"));
		assertNull(CompareUtils.objectToNumberIfPossible(null));
	}
	
	@Test
	public void numericCompareTest() {
		assertEquals(0, CompareUtils.numericCompare(null, null));
		assertEquals(1, CompareUtils.numericCompare(2, 1));
		assertEquals(1, CompareUtils.numericCompare(2.2, 1));
	}
	
	@Test
	public void dynamicCompareTest() {
		assertEquals(0, CompareUtils.dynamicCompare(null, null));
		assertEquals(0, CompareUtils.dynamicCompare("a", "a"));
	}
}
