package picoded.dstack.module.account;

// Target test class
import static org.junit.Assert.*;
import org.junit.*;

// Test depends
import java.util.*;
import picoded.core.conv.*;
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
		return new StructSimpleStack(null);
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	
	/// Caching of stack provider
	public CommonStack stackProviderCache = null;
	
	/// Does the actual account table setup
	public AccountTable implementationConstructor() {
		// Setup the stack provider, and cache it
		if (stackProviderCache == null) {
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
		assertNotNull(testAO = testAT.newEntry("hello-world"));
		assertTrue(testAT.hasLoginName("hello-world"));
		
		// Validating seperately collected accountObjects
		assertNotNull(testAO._oid());
		assertNotNull(testAT.get(testAO._oid()));
		assertEquals(testAO._oid(), testAT.get(testAO._oid())._oid());
		assertEquals(testAO._oid(), testAT.getFromLoginName("hello-world")._oid());
		
		// Removal test
		testAO.remove(testAO);
		blankHelloWorld();
	}
	
	@Test
	public void passwordTest() {
		AccountObject testAO = testAT.newEntry("hello-world");
		
		assertFalse(testAO.hasPassword());
		assertFalse(testAO.validatePassword("bad-pass"));
		
		testAO.setPassword("P@ssw0rd!");
		assertTrue(testAO.hasPassword());
		assertTrue(testAO.validatePassword("P@ssw0rd!"));
		assertFalse(testAO.validatePassword("bad-pass"));
	}
	
	@Test
	public void sessionAndTokenManagement() {
		AccountObject testAO = testAT.newEntry("hello-world");
		testAO.setPassword("P@ssw0rd!");
		
		// The existing session ID sets
		assertFalse(testAO.hasSession("bad-session"));
		assertEquals(0, testAO.getSessionSet().size());
		
		// Session info map
		Map<String, Object> helloInfo = new HashMap<String, Object>();
		helloInfo.put("hello", "motto");
		
		// Time to generate the ession
		String sessionID = null;
		assertNotNull(sessionID = testAO.newSession(helloInfo));
		
		// And validate it
		assertTrue(testAO.hasSession(sessionID));
		assertEquals(1, testAO.getSessionSet().size());
		assertEquals(sessionID, testAO.getSessionSet().toArray()[0]);
		assertEquals(ConvertJSON.fromMap(helloInfo),
			ConvertJSON.fromMap(testAO.getSessionInfo(sessionID)));
		
		// TOKEN managment time
		//---------------------------
		
		// The existing token checks
		assertFalse(testAO.hasToken(sessionID, "bad-bad-token"));
		assertEquals(0, testAO.getTokenSet(sessionID).size());
		
		// Generate a token
		String tokenID = null;
		assertNotNull(tokenID = testAO.newToken(sessionID, (System.currentTimeMillis()) / 1000L + 10));
		
		// And validate it
		assertTrue(testAO.hasToken(sessionID, tokenID));
		assertEquals(1, testAO.getTokenSet(sessionID).size());
		assertEquals(tokenID, testAO.getTokenSet(sessionID).toArray()[0]);
	}
}
