package picoded.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

///
/// Single JUnit test case runner
///
/// Uses the first argument, which splits along #, for class, followed by function
///
public class SingleJUnitTestRunner {
	public static void main(String... args) throws ClassNotFoundException {
		String[] classAndMethod = args[0].split("#");
		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
		Result result = new JUnitCore().run(request);
		//System.exit(result.wasSuccessful() ? 0 : 1);
		
		System.out.println("Total Time: " + ((result.getRunTime()) / 1000.0f));
		System.out.println("");
		
		if (result.wasSuccessful()) {
			System.out.println("OK (" + result.getRunCount() + " tests)");
			System.out.println("");
		} else {
			System.out.println("FAIL (" + result.getRunCount() + " tests)");
			
			System.out.println("");
			System.out.println("There was " + result.getFailureCount() + " failure:");
			
			Failure fail = result.getFailures().get(0);
			System.out.println("1) " + fail.getTestHeader());
			System.out.println(fail.getTrace());
		}
	}
}
