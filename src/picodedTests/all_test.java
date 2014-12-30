package picodedTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import picodedTests.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	picodedTests.fileUtils.DeleteFilesByAge_test.class
})

public class all_test {
}