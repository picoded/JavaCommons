package picodedTests.objectSetDB;

// Target test class
import picoded.objectSetDB.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

// Various utility includes
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

///
/// Test Case for picoded.objectSetDB.ObjectSetDB
///
public class ObjectSetDB_sqlite_test {
	
	// The ObjectSetDB
	public ObjectSetDB OSDB = null;
	
	// Setsup the object set DB with JSqlite
	public void setupObjectSetDB() {
		OSDB = new ObjectSetDB();
	}
	
	// Common setup calls
	public void commonSetup() {
		
	}
	
	@Before
	public void setupTestCase() {
		setupObjectSetDB();
		commonSetup();
	}
	
	/// Test the object constructor
	@Test
	public void constructor() {
		assertNotNull(OSDB);
	}
	
	/// Test the data structure handling
	@Test
	public void structureSetup() {
		assertEquals(ObjectSetDB.BlankObjectStructure, OSDB.getStructure("blank"));
		assertEquals(ObjectSetDB.BlankObjectStructure, OSDB.getStructure("structSetupTest"));
		
		Map<String, Object> nStruct = null;
		assertNotNull(nStruct = OSDB.getWritableStructure("structSetupTest"));
		nStruct.put("int", Integer.class);
		nStruct.put("double", "DOUBLE"); //test structure conversion
		
		assertNotEquals(ObjectSetDB.BlankObjectStructure, nStruct);
		assertNotNull(OSDB.setStructure("structSetupTest", nStruct));
		
		Map<String, Object> tStruct = new HashMap<String, Object>();
		tStruct.putAll(nStruct);
		tStruct.put("double", Double.class); //Actual converted
		
		assertNotEquals(nStruct, tStruct);
		
		assertNotEquals(ObjectSetDB.BlankObjectStructure, tStruct);
		assertNotEquals(ObjectSetDB.BlankObjectStructure, OSDB.getStructure("structSetupTest"));
		
		assertNotEquals(nStruct, OSDB.getStructure("structSetupTest"));
		assertEquals(tStruct, OSDB.getStructure("structSetupTest"));
		
		assertNotNull(OSDB.resetStructure("structSetupTest"));
		assertEquals(ObjectSetDB.BlankObjectStructure, OSDB.getStructure("structSetupTest"));
	}
	
	/// Getting object set
	@Test
	public void objSet() {
		assertNotNull(OSDB.get("test-sub-set"));
	}
	
	/// Testing object map
	@Test
	public void objMapBasic() throws ObjectSetException {
		ObjectMap m;
		assertNotNull(m = OSDB.get("test-sub-set").get("obj1"));
		m.parentSet().tableSetup();
		
		assertNull(m.get("blank"));
		assertNull(m.get("hello"));
		
		m.put("hello", "world");
		assertNotNull(m.get("hello"));
		assertEquals("world", m.get("hello"));
	}
	
	/// Iteration count
	static int basicTestIterations = 15;
	
	/// Testing object standard java types in a loop
	public void objMapStdJava() throws ObjectSetException {
		Random rObj = new Random();
		
		int tmp_int;
		long tmp_long;
		double tmp_double;
		float tmp_float;
		
		double accuracy = 0.000000001;
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_int = rObj.nextInt();
			
			OSDB.get("test-sub-set").get("hello").put("world", "s" + tmp_int);
			assertEquals("String value test", "s" + tmp_int, OSDB.get("test-sub-set").get("hello").get("world"));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_int = rObj.nextInt();
			
			OSDB.get("test-sub-set").get("hello").put("world", tmp_int);
			assertEquals("Int value test", (long) tmp_int, OSDB.get("test-sub-set").get("hello").get("world"));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_long = rObj.nextLong();
			
			OSDB.get("test-sub-set").get("hello").put("world", tmp_long);
			assertEquals("Long value test", tmp_long, OSDB.get("test-sub-set").get("hello").get("world"));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_double = rObj.nextDouble() + rObj.nextInt();
			
			OSDB.get("test-sub-set").get("hello").put("world", tmp_double);
			assertEquals("Double value test", tmp_double, OSDB.get("test-sub-set").get("hello").get("world"));
		}
		
		for (int i = 0; i < basicTestIterations; ++i) {
			tmp_float = rObj.nextFloat() + rObj.nextInt();
			
			OSDB.get("test-sub-set").get("hello").put("world", tmp_float);
			assertEquals("Double value test", tmp_float, OSDB.get("test-sub-set").get("hello").get("world"));
		}
		
	}
}
