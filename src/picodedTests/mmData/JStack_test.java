package picodedTests.mmData;

// Target test class
import picoded.jSql.*;
import picoded.jCache.*;
import picoded.mmData.JStack;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test case config
import picodedTests.TestConfig;
import picodedTests.jCache.LocalCacheSetup;

// Various utility includes
import java.util.Random;
import org.redisson.*;

///
/// Test Case for picoded.mmData.JStack
///
public class JStack_test {
	
	//-----------------------------------//
	// Setup steps                       //
	//-----------------------------------//
	
	protected JCache[] jCacheArr = JStack.empty_jCacheArr;
	protected JSql[] jSqlArr = JStack.empty_jSqlArr;
	
	protected JStack JStackObj = null;
	
	protected void commonSetUp() throws JSqlException {
		JStackObj = new JStack(jCacheArr, jSqlArr);
		assertNotNull(JStackObj);
	}
	
	// Setsup the various JSql and JCache stack, that is used to setup the mmData stack
	protected void setupJStack() {
		jSqlArr = new JSql[] { JSql.sqlite() };
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
		LocalCacheSetup.teardownHazelcastServer();
		LocalCacheSetup.teardownRedisServer();
	}
	
	@Before
	public void setUp() throws JSqlException {
		setupJStack();
		commonSetUp();
	}
	
	@After
	public void tearDown() throws JSqlException {
		
	}
	
	/// Test the object constructor
	@Test
	public void constructor() throws JSqlException {
		assertNotNull(JStackObj);
	}
	
	/// Test JSql stack handling
	@Test
	public void JSqlStackTest() throws JSqlException {
		assertArrayEquals("Gets default stack", jSqlArr, JStackObj.getJSqlStack(null));
		
		JSql[] tStack = new JSql[2]; //{ (JSql.sqlite()), (JSql.sqlite()) };
		tStack[0] = JSql.sqlite();
		tStack[1] = JSql.sqlite();
		
		assertArrayEquals("Gets default stack", jSqlArr, JStackObj.getJSqlStack("stack-test"));
		assertArrayEquals("Sets custom stack", JStack.empty_jSqlArr, JStackObj.setJSqlStack("stack-test", tStack));
		assertArrayEquals("Gets custom stack", tStack, JStackObj.getJSqlStack("stack-test"));
		
		assertArrayEquals("Reset custom stack", tStack, JStackObj.resetJSqlStack("stack-test"));
		assertArrayEquals("Gets default stack", jSqlArr, JStackObj.getJSqlStack("stack-test"));
	}
	
	/// Test JCache stack handling
	@Test
	public void JCacheStackTest() throws JSqlException {
		String hcClusterName = LocalCacheSetup.setupHazelcastServer();
		
		JCache[] tStack = new JCache[2]; //{ (JSql.sqlite()), (JSql.sqlite()) };
		tStack[0] = JCache.hazelcast(hcClusterName);
		tStack[1] = JCache.hazelcast(hcClusterName);
		
		assertArrayEquals("Gets default stack", jCacheArr, JStackObj.getJCacheStack(null));
		
		assertArrayEquals("Gets default stack", jCacheArr, JStackObj.getJCacheStack("stack-test"));
		assertArrayEquals("Sets custom stack", JStack.empty_jSqlArr, JStackObj.setJCacheStack("stack-test", tStack));
		assertArrayEquals("Gets custom stack", tStack, JStackObj.getJCacheStack("stack-test"));
		
		assertArrayEquals("Reset custom stack", tStack, JStackObj.resetJCacheStack("stack-test"));
		assertArrayEquals("Gets default stack", jCacheArr, JStackObj.getJCacheStack("stack-test"));
	}
	
}
