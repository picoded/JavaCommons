package picodedTests.objectSetDB;

// Target test class
import picoded.objectSetDB.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

//
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
	public void getObjSet() {
		assertNotNull(OSDB.get("test-sub-set"));
		
		assertNotNull(OSDB.get("test-sub-set"));
	}
}
