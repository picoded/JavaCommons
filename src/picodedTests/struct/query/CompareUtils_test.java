package picodedTests.struct.query;

// Target test class
import picoded.struct.query.condition.*;
import picoded.struct.query.internal.*;
import picoded.struct.query.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import picoded.struct.*;
import picoded.struct.query.*;
import picoded.struct.query.internal.*;

///
/// Test Case for picoded.struct.query.condition.*
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
		assertEquals(0, CompareUtils.dynamicCompare( null, null ));
		
		assertEquals(0, CompareUtils.dynamicCompare( new Integer(1), new Integer(1) ));
		assertEquals(0, CompareUtils.dynamicCompare( new Integer(1), "1" ));
		
		assertEquals(1, CompareUtils.dynamicCompare( new Integer(2), "1" ));
		assertEquals(0, CompareUtils.dynamicCompare( "1", "1" ));
		assertEquals(-1, CompareUtils.dynamicCompare( new Integer(0), "1" ));
		
		assertEquals(1, CompareUtils.dynamicCompare( "c", "b" ));
		assertEquals(0, CompareUtils.dynamicCompare( "b", "b" ));
		assertEquals(-1, CompareUtils.dynamicCompare( "a", "b" ));
		
		assertEquals(1, CompareUtils.dynamicCompare( "b", null ));
		assertEquals(1, CompareUtils.dynamicCompare( new Integer(1), null ));
		assertEquals(-1, CompareUtils.dynamicCompare( null, new Integer(1) ));
		assertEquals(-1, CompareUtils.dynamicCompare( null, "b" ));
	}
	
	
}
