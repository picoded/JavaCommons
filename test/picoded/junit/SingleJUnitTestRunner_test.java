package picoded.junit;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;

// Classes used in test case
import picoded.junit.SingleJUnitTestRunner;

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
		assertEquals(-1, SingleJUnitTestRunner.runTestMethod("picoded.junit.SingleJUnitTestRunner#_thisAssertsFailure"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void classNotFoundTest() {
		SingleJUnitTestRunner.main("picoded.junit.ThisClassDoesNotExsits#methodName");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void invalidFormat() {
		SingleJUnitTestRunner.runTestMethod("picoded.junit.SingleJUnitTestRunner_test");
	}
	
	@Test(expected = AssertionError.class)
	public void callAssertFalseForCodeCoverage() {
		(new SingleJUnitTestRunner())._thisAssertsFailure();
	}
}
