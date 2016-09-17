package picoded.junit;

// Junit includes
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;

///
/// Single JUnit test case runner
///
/// Processes all argument, which splits along #, for class, followed by function. This is actually used by runTest.sh
///
/// # Example
/// `./runTest.sh  package.namespace.classname#testFunction`
///
public class SingleJUnitTestRunner {
	
	/// System out print ln handling
	private static void println(String in) {
		// Note: this is intentionally not using logger, to ensure same output format with JUnit.
		System.out.println(in);
	}
	
	/// Calls a method name in a test class
	///
	/// @param  Class name to run test
	/// @param  Method/Function name to run test
	///
	/// @return  Number of successful test cases ( 1 ) or failure ( -1 )
	public static int runTestMethod(String className, String methodName) {
		try {
			// Note: Dynamic loading of class is intentional. Exempted from vulnerability check.
			Request request = Request.method(Class.forName(className), methodName);
			Result result = new JUnitCore().run(request);
			
			// This is following the standard JUnit output format
			//------------------------------------------------------------
			println("Total Time: " + ((result.getRunTime()) / 1000.0f));
			println("");
			
			if (result.wasSuccessful()) {
				println("OK (" + result.getRunCount() + " tests)");
				println("");
				
				return result.getRunCount();
			} else {
				println("FAIL (" + result.getRunCount() + " tests)");
				
				println("");
				println("There was " + result.getFailureCount() + " failure:");
				
				Failure fail = result.getFailures().get(0);
				println("1) " + fail.getTestHeader());
				println(fail.getTrace());
				
				return -result.getFailureCount();
			}
		} catch(ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found for : "+className, e);
		}
	}
	
	/// Calls a method name in a test class
	///
	/// @param  ClassName#MethodName format string
	///
	/// @return  Number of successful test cases ( 1 ) or failure ( -1 )
	public static int runTestMethod(String classAndMethod) {
		String[] formatPair = classAndMethod.split("#");
		
		if(formatPair.length != 2) {
			throw new IllegalArgumentException("Unknown class and method format : "+formatPair);
		}
		return runTestMethod(formatPair[0],formatPair[1]);
	}
	
	/// The main command line test runner, see example in class above
	///
	/// @param  Arguments of class#method name pairs
	public static void main(String... args) {
		for(String pair : args) {
			runTestMethod(pair);
		}
	}
	
	///
	/// Used internally to help provide code coverage testing of failure conditons. Do not use
	/// 
	/// Note: Intentionally breaking convention, to indicate with an _underscore, that this *should not be used*.
	///
	@Test
	public void _thisAssertsFailure() {
		assertTrue(false);
	}
	
}
