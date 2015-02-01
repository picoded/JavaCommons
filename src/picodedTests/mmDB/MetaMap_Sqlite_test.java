package picodedTests.mmDB;

// Target test class
import picoded.mmDB.MetaMap;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class MetaMap_Sqlite_test {

	protected MetaMap mmObj = null;

	@Before
	public void setUp() {
		mmObj = new MetaMap();
	}

	@After
	public void tearDown() {
	}

	/// Test the object constructor
	@Test
	public void constructor() {
		assertNotNull(mmObj);
	}
}