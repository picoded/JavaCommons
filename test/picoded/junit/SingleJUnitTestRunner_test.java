package picoded.junit;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

/// The actual test suite
public class SingleJUnitTestRunner_test {
	
	/// A blank, assertTrue test case to call internally
	@Test
	public void isTrue() {
		assertTrue(true);
	}
	
	/// Boolean control for "shouldFailWhenSet"
	public static volatile boolean failureFlag = false;
	
	//
	// Call the sub test within itself : For code coverage !
	//
	
	@Test
	public void mainIsTrue() {
		SingleJUnitTestRunner.main("picoded.junit.SingleJUnitTestRunner_test#isTrue");
		assertEquals(1, SingleJUnitTestRunner.runTestMethod("picoded.junit.SingleJUnitTestRunner_test#isTrue"));
	}
	
	@Test
	public void checkFailure() {
		assertEquals(-1, SingleJUnitTestRunner.runTestMethod("picoded.junit.SingleJUnitTestRunner#_thisThrowsException"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void classNotFoundTest() {
		SingleJUnitTestRunner.main("picoded.junit.ThisClassDoesNotExsits#methodName");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalidFormat() {
		SingleJUnitTestRunner.runTestMethod("picoded.junit.SingleJUnitTestRunner_test");
	}
	
	@Test
	public void callAssertFalseForCodeCoverage() {
		try {
			(new SingleJUnitTestRunner())._thisThrowsException();
		} catch (RuntimeException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
}
