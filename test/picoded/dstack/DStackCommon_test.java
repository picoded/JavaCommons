package picoded.dstack;

// Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

// Test depends
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

public class DStackCommon_test {
	
	// Test object for reuse
	public DStackCommon<String, String> testObj = null;
	
	// To override for implementation
	//-----------------------------------------------------
	
	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public DStackCommon<String, String> implementationConstructor() {
		return new StructSimple_KeyValue();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void systemSetup() {
		testObj = implementationConstructor();
		testObj.systemSetup();
	}
	
	@After
	public void systemDestroy() {
		if (testObj != null) {
			testObj.systemDestroy();
		}
		testObj = null;
	}
	
	@Test
	public void constructorSetupAndMaintenance() {
		// not null check
		assertNotNull(testObj);
		
		// run maintaince, no exception?
		testObj.maintenance();
		
		// run incremental maintaince, no exception?
		testObj.incrementalMaintenance();
	}
	
	///
	/// This specifically test incrementalMaintenance over a very large test run (10,000,000?)
	/// So as to ensure that the default random 1% triggering, actually happens.
	////
	/// And without error
	///
	@Test
	public void incrementalMaintenance() {
		for (int i = 0; i < 10000000; ++i) {
			testObj.incrementalMaintenance();
		}
	}
}
