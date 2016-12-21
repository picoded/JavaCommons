package picoded.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.TestConfig;

///
/// Basic LDAP testing
///
public class LDAPAuthenticator_test {
	
	protected LDAPAuthenticator authObj = null;
	
	@Before
	public void setUp() {
		authObj = new LDAPAuthenticator(TestConfig.LDAP_HOST(), TestConfig.LDAP_PORT(),
			TestConfig.LDAP_DOMAIN());
	}
	
	@After
	public void tearDown() {
		if (authObj != null) {
			authObj.close();
		}
		authObj = null;
	}
	
	@Test
	public void constructorTest() {
		assertNotNull(authObj);
	}
	
	@Test
	public void failedLogin() {
		assertEquals("Failed to authenticate: wrong", authObj.login("wrong", "wrong"));
	}
	
	@Test
	public void correctLogin() {
		assertNull(authObj.login("dummyuser", "P@ssw0rd!"));
	}
	
	@Test
	public void basicLoginInfo() {
		assertNull(authObj.login("dummyuser", "P@ssw0rd!"));
		assertNotNull(authObj.userInfo());
	}
}