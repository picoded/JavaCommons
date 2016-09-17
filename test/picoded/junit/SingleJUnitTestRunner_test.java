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
	public void helloWorld() {
		assertTrue(true);
	}
	
	/// Call the hello world test case in itself.
	@Test 
	public void selfRunnerTest() throws ClassNotFoundException {
		SingleJUnitTestRunner.main("picoded.junit.SingleJUnitTestRunner_test#helloWorld");
	}
}
