package picodedTests.security;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import picoded.security.*;
import picodedTests.TestConfig;

public class LDAPAuthenticator_test {
	
	protected LDAPAuthenticator authObj = null;
	
	@Before
	public void setUp() {
		authObj = new LDAPAuthenticator( TestConfig.LDAP_HOST(), TestConfig.LDAP_PORT(), TestConfig.LDAP_DOMAIN() );
	}
	
	@After
	public void tearDown() {
		if(authObj != null) {
			authObj.close();
		}
		authObj = null;
	}
	
	@Test
	public void constructorTest() {
		assertNotNull( authObj );
	}
	
	
}