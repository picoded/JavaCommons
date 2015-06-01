package picodedTests.struct;

// Target test class
import picoded.struct.GenericConvertMap;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.ProxyGenericConvertMap;

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
		GenericConvertMap<String, Object> tObj = ProxyGenericConvertMap.ensureGenericConvertMap( new CaseInsensitiveHashMap<String, Object>() );
		
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
		GenericConvertMap<String, Object> tObj = ProxyGenericConvertMap.ensureGenericConvertMap( new CaseInsensitiveHashMap<String, Object>() );
		
		tObj.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
		tObj.put("nor", new String[] { "this", "is", "the", "end" });
		
		assertArrayEquals(new String[] { "is", "not", "the", "beginning" }, tObj.getStringArray("this"));
		assertEquals("[\"this\",\"is\",\"the\",\"end\"]", tObj.getString("nor"));
	}
}