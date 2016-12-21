package picoded.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
	public void tearDown() throws NamingException {
		if (authObj != null) {
			authObj.close();
		}
		authObj = null;
	}
	
	@Test
	public void constructorTest() {
		assertNotNull(authObj);
	}
	
	/// default constructor test
	@Test
	public void invalidConstructor() {
		new LDAPAuthenticator();
		
	}
	
	@Test
	public void failedLogin() {
		assertEquals("Invalid blank username (null)", authObj.login(null, null));
		assertEquals("Invalid blank username (length=0)", authObj.login("", null));
		assertEquals("Failed to authenticate: wrong", authObj.login("wrong", "wrong"));
		assertNotNull(authObj.login("test@ @", "P@ssw0rd!"));
		assertNotNull(authObj.login("test@123", "P@ssw0rd!"));
		assertNotNull(authObj.login("test@123@12", "P@ssw0rd!"));
		assertNull(authObj.login("test@gmail.com", ""));
		authObj = new LDAPAuthenticator(null, TestConfig.LDAP_PORT(), TestConfig.LDAP_DOMAIN());
		assertNotNull(authObj.login("test@gmail.com", "P@ssw0rd!"));
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
	
	@Test
	public void closeContextTest() throws NamingException {
		LdapContext context = Mockito.mock(LdapContext.class);
		context.addToEnvironment(null, null);
		authObj.closeContext(context);
	}
}