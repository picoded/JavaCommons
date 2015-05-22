package picodedTests.conv;

// Target test class
import picoded.conv.GenericConvert;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case

///
/// Test Case for picoded.conv.GenericConvert
///
public class GenericConvert_test {
	
	@Test
	public void toStringTest() {
		assertEquals("hello world", GenericConvert.toString("hello world"));
		assertEquals("100", GenericConvert.toString(100));
		assertEquals("hello world", GenericConvert.toString(null, "hello world"));
	}
	
	@Test
	public void toBooleanTest() {
		assertEquals(true, GenericConvert.toBoolean("hello world", true));
		assertEquals(false, GenericConvert.toBoolean("hello world", false));
		
		assertEquals(true, GenericConvert.toBoolean("1"));
		assertEquals(false, GenericConvert.toBoolean("-"));
	}
	
}