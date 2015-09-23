package picodedTests.struct;

// Target test class
import picoded.struct.GenericConvertMap;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.ProxyGenericConvertMap;

import java.util.function.BiFunction;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class GenericConvertMap_test {
	
	class GMap<K extends String, V> extends CaseInsensitiveHashMap<K, V> implements GenericConvertMap<K, V> {
		/// Java serialversion uid: http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
		private static final long serialVersionUID = 1L;
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	/// The following, test severals assumptions regarding Object[] instanceof tests
	/// done in java, this is required for the generic toXArray functions.
	@Test
	public void arrayInstanceOfTest() {
		String sample = "a";
		assertFalse(((Object) sample instanceof Object[]));
		
		String[] strArr = new String[] { "1", "2", "3" };
		Object[] objArr = new Object[] { "1", "2", "3" };
		Integer[] intArr = new Integer[] { 1, 2, 3 };
		
		/// String array tests
		assertTrue((strArr instanceof Object[]));
		assertTrue(((Object) strArr instanceof Object));
		
		assertTrue(((Object) strArr instanceof String[]));
		assertFalse(((Object) strArr instanceof Integer[]));
		assertFalse(((Object) strArr instanceof String));
		assertFalse(((Object) strArr instanceof Integer));
		
		/// Integer array tests
		assertTrue((intArr instanceof Object[]));
		assertTrue(((Object) intArr instanceof Object));
		
		assertFalse(((Object) intArr instanceof String[]));
		assertTrue(((Object) intArr instanceof Integer[]));
		assertFalse(((Object) intArr instanceof String));
		assertFalse(((Object) intArr instanceof Integer));
		
		/// Object array tests
		assertTrue((objArr instanceof Object[]));
		assertTrue(((Object) objArr instanceof Object));
		
		assertFalse(((Object) objArr instanceof String[]));
		assertFalse(((Object) objArr instanceof Integer[]));
		assertFalse(((Object) objArr instanceof String));
		assertFalse(((Object) objArr instanceof Integer));
	}
	
	/// The following, test the class uniqueness of BiFunction types
	@Test
	public void classLoockUpTest() {
		assertEquals(String.class, String.class);
		assertEquals(String[].class, String[].class);
		assertNotEquals(String.class, String[].class);
	}
	
	@Test
	public void intTest() {
		GenericConvertMap<String, String> tObj = new GMap<String, String>();
		
		assertNull(tObj.put("year", "1965"));
		
		assertNull(tObj.getStringArray("year"));
		assertEquals(1965, tObj.getInt("year"));
	}
	
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
	///
	/// tObj.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
	/// tObj.put("nor", new String[] { "this", "is", "the", "end" });
	///
	/// assertEquals( new String[] { "is", "not", "the", "beginning" }, tObj.getStringArray("this") );
	/// assertEquals( "[\"this\",\"is\",\"the\",\"end\"]", tObj.getString("nor") );
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Test
	public void stringArrayTest() {
		GenericConvertMap<String, Object> tObj = new GMap<String, Object>();
		
		tObj.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
		tObj.put("nor", new String[] { "this", "is", "the", "end" });
		
		assertArrayEquals(new String[] { "is", "not", "the", "beginning" }, tObj.getStringArray("this"));
		assertEquals("[\"this\",\"is\",\"the\",\"end\"]", tObj.getString("nor"));
	}
	
	@Test
	public void proxy_intTest() {
		GenericConvertMap<String, Object> tObj = ProxyGenericConvertMap
			.ensureGenericConvertMap(new CaseInsensitiveHashMap<String, Object>());
		
		assertNull(tObj.put("year", "1965"));
		
		assertNull(tObj.getStringArray("year"));
		assertEquals(1965, tObj.getInt("year"));
	}
	
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
	///
	/// tObj.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
	/// tObj.put("nor", new String[] { "this", "is", "the", "end" });
	///
	/// assertEquals( new String[] { "is", "not", "the", "beginning" }, tObj.getStringArray("this") );
	/// assertEquals( "[\"this\",\"is\",\"the\",\"end\"]", tObj.getString("nor") );
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Test
	public void proxy_stringArrayTest() {
		GenericConvertMap<String, Object> tObj = ProxyGenericConvertMap
			.ensureGenericConvertMap(new CaseInsensitiveHashMap<String, Object>());
		
		tObj.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
		tObj.put("nor", new String[] { "this", "is", "the", "end" });
		
		assertArrayEquals(new String[] { "is", "not", "the", "beginning" }, tObj.getStringArray("this"));
		assertEquals("[\"this\",\"is\",\"the\",\"end\"]", tObj.getString("nor"));
	}
}
