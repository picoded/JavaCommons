package picodedTests.servlet;

// Target test class
import picoded.servlet.CorePage;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class CorePage_test {
	
	public CorePage testPage = null;
	
	@Before
	public void setUp() {
		testPage = new CorePage();
	}
	
	@After
	public void tearDown() {
		testPage = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(testPage);
	}
}