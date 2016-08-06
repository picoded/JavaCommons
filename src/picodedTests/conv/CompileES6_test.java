package picodedTests.conv;

// Target test class
import java.util.Calendar;

import picoded.conv.*;

// Test Case include
import org.junit.*;

import static org.junit.Assert.*;

public class CompileES6_test {
	@Test
	public void simpleConvert() {
		
		String es6 = "class hello { echo() { return 'hello world'; } };";
		String es5 = "";
		
		assertNotNull(es5 = CompileES6.compile(es6));
		//assertEquals( es6, es5 );
	}
}
