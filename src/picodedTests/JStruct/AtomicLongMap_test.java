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
		// return new JStruct_AtomicLongMap()
		return new JStruct().getAtomicLongMap("test");
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

		// TEST CASE
		//public Long put(String key, Number value)
		Number i = 1;
		almObj.put("hello", i);
		assertEquals(new Long(1), almObj.get("hello"));

		// TEST CASE
		//public Long put(String key, long value)
		almObj.put("two", 2L);
		assertEquals(new Long(2), almObj.get("two"));

		// TEST CASE
		//public Long put(String key, Long value)
		almObj.put("a", new Long(3));
		almObj.put("b", new Long(4));
		almObj.put("c", new Long(5));

		// TEST CASE
		//public Long get(Object key)
		assertEquals(new Long(3), almObj.get("a"));
		assertEquals(new Long(4), almObj.get("b"));
		assertEquals(new Long(5), almObj.get("c"));

		// TEST CASE
		//public Long getAndAdd(Object key, Object delta)
		Object aObj = "a";
		assertEquals(new Long(3), almObj.get(aObj));

		// TEST CASE
		//public Long getAndIncrement(Object key)
		assertEquals(new Long(4), almObj.getAndIncrement("b"));
		assertEquals(new Long(5), almObj.getAndIncrement("b"));

		// TEST CASE
		//public Long incrementAndGet(Object key)
		assertEquals(new Long(6), almObj.incrementAndGet("c"));

		// TEST CASE
		//public boolean weakCompareAndSet(String key, Long expect, Long update)
		almObj.put("d", new Long(7));
		assertEquals(true, almObj.weakCompareAndSet("d", new Long(7), new Long(8)));
		assertEquals(new Long(8), almObj.get("d"));

		// almObj.put("e", new Long(10));
		// assertEquals(false, almObj.weakCompareAndSet("e", new Long(11), new Long(8)));
		// assertEquals(new Long(10), almObj.get("e"));
	}

	@Test
	public void nullGet() throws Exception {
		assertEquals(null, almObj.get("e"));
	}


}
