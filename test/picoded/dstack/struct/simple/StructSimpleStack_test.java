package picoded.dstack.struct.simple;

// Test system include
import static org.junit.Assert.*;
import org.junit.*;

// Java includes
import java.util.*;

// External lib includes
import org.apache.commons.lang3.RandomUtils;

// Test depends
import picoded.core.conv.GUID;
import picoded.core.struct.CaseInsensitiveHashMap;
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

// Setup the test stack
public class StructSimpleStack_test {
	
	// The testing stack to use
	CommonStack testStack = null;
	
	// To override for implementation
	//-----------------------------------------------------
	public CommonStack implementationConstructor() {
		return new StructSimpleStack();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void setUp() {
		testStack = implementationConstructor();
		testStack.systemSetup();
	}
	
	@After
	public void tearDown() {
		if (testStack != null) {
			testStack.systemDestroy();
		}
		testStack = null;
	}
	
	@Test
	public void constructorTest() {
		// not null check
		assertNotNull(testStack);
		
		// Incremental maintenance
		testStack.incrementalMaintenance();
		
		// run maintaince, no exception?
		testStack.maintenance();
	}
	
	// Does the various get calls
	//-----------------------------------------------------
	
}