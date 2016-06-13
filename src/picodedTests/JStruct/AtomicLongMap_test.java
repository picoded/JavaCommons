package picodedTests.JStruct;

// Target test class
import picoded.JStruct.*;
import picoded.JStruct.internal.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class AtomicLongMap_test {

	/// Test object
	public AtomicLongMap almObj = null;

	/// To override for implementation
	///------------------------------------------------------
	public AtomicLongMap implementationConstructor() {
		//JStruct jsObj = new JStruct();
		//return (new JStruct()).getKeyValueMap("test");
		return new JStruct_AtomicLongMap();
	}

	/// Setup and sanity test
	///------------------------------------------------------
	@Before
	public void setUp() {
		almObj = implementationConstructor();
		almObj.systemSetup();
	}

	@After
	public void tearDown() {
		if (almObj != null) {
			almObj.systemTeardown();
		}
		almObj = null;
	}

	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(almObj);

		//run maintaince, no exception?
		almObj.maintenance();
	}

	/// utility
	///------------------------------------------------------
	public long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis() / 1000L);
	}

	/// basic test
	///------------------------------------------------------

	@Test
	public void simpleHasPutHasGet() throws Exception {
		// assertNull(almObj.put("hello", 1));
		assertEquals(1, almObj.getLong("hello"));
	}

}
