package picoded.dstack.struct.simple;

// Test system include
import static org.junit.Assert.*;
import org.junit.*;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions; 
import com.carrotsearch.junitbenchmarks.BenchmarkRule; 

// Java includes
import java.util.*;

// External lib includes
import org.apache.commons.lang3.RandomUtils;

// Test depends
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;

// MetaTable base test class
public class StructSimple_MetaTable_perf extends AbstractBenchmark {
	
	/// Test object
	public MetaTable mtObj = null;
	
	// To override for implementation
	//-----------------------------------------------------
	public MetaTable implementationConstructor() {
		return new StructSimple_MetaTable();
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void setUp() {
		mtObj = implementationConstructor();
		mtObj.systemSetup();

		prepareTestObjects();
	}
	
	@After
	public void tearDown() {
		if (mtObj != null) {
			mtObj.systemDestroy();
		}
		mtObj = null;
	}
	
	// Performance benchmark setup
	//-----------------------------------------------------

	/// Small map of 10 string, and 10 numeric properties
	Map<String,Object> smallMap = null;

	/// Medium map of 200 string, and 250 numeric properties
	Map<String,Object> mediumMap = null;

	/// Large map of 1000 string, and 1000 numeric properties
	Map<String,Object> largeMap = null;

	/// Small map of 10 string, and 10 numeric properties
	Map<String,Object> smallMap2 = null;

	/// Medium map of 200 string, and 250 numeric properties
	Map<String,Object> mediumMap2 = null;

	/// Large map of 1000 string, and 1000 numeric properties
	Map<String,Object> largeMap2 = null;

	/// Iterate and setup a test map, to a given size
	public Map<String,Object> setupTestMap(int max) {
		HashMap<String, Object> ret = new HashMap<String, Object>();

		for(int i=0; i<max; ++i) {
			ret.put( GUID.base58(), GUID.base58() );
			ret.put( GUID.base58(), new Float(RandomUtils.nextFloat(0, 1000000000.0f)) );
		}

		return ret;
	}

	/// Prepare several test objects for performance testing alter
	public void prepareTestObjects() {
		smallMap = setupTestMap(10);
		mediumMap = setupTestMap(250);
		largeMap = setupTestMap(1000);
		smallMap2 = setupTestMap(10);
		mediumMap2 = setupTestMap(250);
		largeMap2 = setupTestMap(1000);
	}

	/// Configurable iteration sets count
	public int iterationCount = 100;

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void largeMapPerf() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			mtObj.newObject(largeMap);
			mtObj.newObject(largeMap2);
		}
	}

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void mediumMapPerf() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			mtObj.newObject(mediumMap);
			mtObj.newObject(mediumMap2);
		}
	}

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void smallMapPerf() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			mtObj.newObject(smallMap);
			mtObj.newObject(smallMap2);
		}
	}

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void largeMapPerf_insertAndUpdate() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			MetaObject mo = mtObj.newObject(largeMap);
			mo.saveDelta();

			mo.putAll(largeMap2);
			mo.saveDelta();
		}
	}

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void mediumMapPerf_insertAndUpdate() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			MetaObject mo = mtObj.newObject(mediumMap);
			mo.saveDelta();

			mo.putAll(mediumMap2);	
			mo.saveDelta();
		}
	}

	@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
	@Test
	public void smallMapPerf_insertAndUpdate() throws Exception {
		for(int i=0; i<iterationCount; ++i) {
			MetaObject mo = mtObj.newObject(smallMap);
			mo.saveDelta();

			mo.putAll(smallMap2);
			mo.saveDelta();
		}
	}

}