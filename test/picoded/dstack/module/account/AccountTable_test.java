package picoded.dstack.module.account;

// Target test class
import static org.junit.Assert.*;
import org.junit.*;

// Test depends
import java.util.*;
import picoded.dstack.*;
import picoded.dstack.struct.simple.*;
import picoded.TestConfig;

public class AccountTable_test {

	// Test object for reuse
	public AccountTable testAT = null;

	// To override for implementation
	//-----------------------------------------------------

	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public CommonStack stackProvider() {
		return new StructSimpleStack();
	}

	// Setup and sanity test
	//-----------------------------------------------------

	/// Caching of stack provider
	public CommonStack stackProviderCache = null;
	/// Does the actual account table setup
	public AccountTable implementationConstructor() {
		// Setup the stack provider, and cache it
		if(stackProviderCache == null) {
			stackProviderCache = stackProvider();
		}
		return new AccountTable(stackProviderCache, TestConfig.randomTablePrefix().toUpperCase());
	}

	@Before
	public void systemSetup() {
		testAT = implementationConstructor();
		testAT.systemSetup();
	}

	@After
	public void systemDestroy() {
		if (testAT != null) {
			testAT.systemDestroy();
			testAT = null;
		}
	}

	@Test
	public void constructorTest() {
		assertNotNull(testAT);
	}

	@Test
	public void blankHelloWorld() {
		assertFalse(testAT.containsKey("hello-world"));
		assertNull(testAT.get("hello-world"));
	}

	@Test
	public void createCycleTest() {
		AccountObject testAO = null;
		assertNotNull( testAO = testAT.newObject("hello-world") );
		assertTrue( testAT.hasLoginID("hello-world") );

		// Validating seperately collected accountObjects
		assertNotNull( testAO._oid() );
		assertNotNull(testAT.get(testAO._oid()));
		assertEquals( testAO._oid(), testAT.get( testAO._oid() )._oid() );
		assertEquals( testAO._oid(), testAT.getFromLoginID( "hello-world" )._oid() );

		// Removal test
		testAO.remove( testAO );
		blankHelloWorld();
	}
}
