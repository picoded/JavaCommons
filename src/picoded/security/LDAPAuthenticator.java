package picoded.security;

// Java dependencies
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

// Javax dependencies
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
// LDAP dependencies

///
/// Simple LDAP Authenticator utility.
///
/// One thing to note strongly, is that it caches the valid login context,
/// for the usage of more "complex" features. As such, try to limit a single
/// login session to an object.
///
public class LDAPAuthenticator {
	
	//////////////////////////////////////////
	//
	//  Static vars
	//
	//////////////////////////////////////////
	
	/// User Info attributes inside LDAP
	private static String[] userInfoAttributes = { //
	"distinguishedName", "cn", "name", //
		"uid", "sn", "givenname", "memberOf", //
		"samaccountname", "userPrincipalName" //
	}; //
	
	//////////////////////////////////////////
	//
	//  Internal vars
	//
	//////////////////////////////////////////
	
	//
	// Server config
	//
	
	/// Default domain name
	private String defaultDomain = null;
	
	/// Server name to connect to
	private String serverName = null;
	
	/// Server port number to connect to
	private int port = 389; //default non secure port
	
	//
	// Cached context
	//
	
	/// The cached context
	private LdapContext cachedContext = null;
	
	/// The cached username in context
	private String cachedUser = null;
	
	/// The cached domain in context
	private String cachedDomain = null;
	
	//////////////////////////////////////////
	//
	//  Constructor
	//
	//////////////////////////////////////////
	
	/// Blank constructor (will rely on user, and server info for domain, etc)
	public LDAPAuthenticator() {
	}
	
	/// Constructor which setsup the various base config
	public LDAPAuthenticator(String serverName, int port, String domainName) {
		this.serverName = serverName;
		this.port = port;
		this.defaultDomain = domainName;
	}
	
	/// Closes the current cached context if any
	public void close() throws NamingException {
		cachedUser = null;
		cachedDomain = null;
		
		closeContext(cachedContext);
		cachedContext = null;
	}
	
	//////////////////////////////////////////
	//
	//  Utility function
	//
	//////////////////////////////////////////
	
	///
	/// Closes the ldap context silently
	///
	/// @param LdapContext to close
	///
	protected void closeContext(LdapContext context) throws NamingException {
		if (context != null) {
			//			try {
			context.close();
			//			} catch (Exception e) {
			// do nothing
			//			}
		}
	}
	
	///
	/// Converts the domain name, to the LDAP DC format
	///
	/// @param domainname used
	///
	/// @returns LDAP DC
	///
	private static String domainNameToLdapDC(String domainName) {
		StringBuilder buf = new StringBuilder();
		for (String token : domainName.split("\\.")) {
			if (token.length() == 0) {
				continue; // defensive check
			}
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append("DC=").append(token);
		}
		return buf.toString();
	}
	
	//////////////////////////////////////////
	//
	//  Authentication function
	//
	//////////////////////////////////////////
	
	///
	/// Used to authenticate a user given a username/password and domain name.
	///
	/// @returns  The expected error message, if any. Else simply null for success
	///
	@SuppressWarnings("unused")
	public String login(String username, String password) {
		
		//
		// Sanity checks
		//
		if (username == null) {
			return "Invalid blank username (null)";
		}
		
		if ((username = username.trim()).length() <= 0) {
			return "Invalid blank username (length=0)";
		}
		
		//
		// Domain names overwrite
		//
		String usedUsername = username; //username actually used
		String domainName = defaultDomain; //domain name actually used
		if (usedUsername.indexOf('@') >= 1) {
			String[] splitNames = usedUsername.split("@");
			
			if (splitNames.length != 2) {
				return "Unexpected username with improper domain (" + usedUsername + ")";
			}
			
			usedUsername = splitNames[0];
			domainName = splitNames[1];
		}
		
		//
		// Domain names validation check
		//
		
		// Possibly invalid domain name
		if ((domainName = domainName.trim()).length() <= 0) {
			domainName = null;
		}
		
		// Fallback to server domain
		if (domainName == null) {
			try {
				String fqdn = java.net.InetAddress.getLocalHost().getCanonicalHostName();
				if (fqdn.split("\\.").length > 1) {
					domainName = fqdn.substring(fqdn.indexOf('.') + 1);
				}
			} catch (java.net.UnknownHostException e) {
				// Does nothing
			}
		}
		
		// Missing domain name check
		if (domainName == null || (domainName = domainName.trim()).length() <= 0) {
			return "Missing domain name parameter for user (" + usedUsername + ")";
		}
		
		//
		// password length 0, is considered null (no password
		//
		if (password != null && (password = password.trim()).length() == 0) {
			password = null;
		}
		
		//
		// Create property for context
		//
		Hashtable<String, String> props = new Hashtable<String, String>();
		
		// Context builder
		props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		
		// User, domain and pass
		props.put(Context.SECURITY_PRINCIPAL, usedUsername + "@" + domainName);
		if (password != null) {
			props.put(Context.SECURITY_CREDENTIALS, password);
		}
		
		// LDAP Server connection
		String ldapURL = "ldap://" + ((serverName == null) ? domainName : serverName + ":" + port);
		props.put(Context.PROVIDER_URL, ldapURL);
		
		/// The login context
		/// Try and catch the errors
		try {
			LdapContext loginContext = new InitialLdapContext(props, null);
			if (loginContext == null) {
				return "Failed to acquire login context: " + username;
			}
			cachedDomain = domainName;
			cachedUser = usedUsername;
			cachedContext = loginContext;
		} catch (javax.naming.CommunicationException e) {
			return "Failed to connect to server, possible network error";
		} catch (NamingException e) {
			return "Failed to authenticate: " + username;
		}
		return null;
	}
	
	///
	/// Get some basic user information. Containing the following (userInfoAttributes)
	///
	/// Requires a previously valid authentication()
	///
	/// "distinguishedName", "cn", "name",
	/// "uid", "sn", "givenname", "memberOf",
	/// "samaccountname", "userPrincipalName"
	///
	/// @returns Map<String,String> of the user info mentioned above.
	///
	public Map<String, String> userInfo() {
		
		//
		// Basic setup and checks
		//
		Map<String, String> ret = new HashMap<String, String>();
		String usedDomain = cachedDomain;
		String usedUsername = cachedUser;
		String principalName = usedUsername + "@" + usedDomain;
		LdapContext usedContext = cachedContext;
		
		if (usedContext == null) {
			throw new RuntimeException("Missing user context, call authenticate() first");
		}
		
		//
		// Try to search user
		//
		try {
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SUBTREE_SCOPE);
			controls.setReturningAttributes(userInfoAttributes);
			NamingEnumeration<SearchResult> answer = usedContext.search(
				domainNameToLdapDC(usedDomain), "(& (userPrincipalName=" + principalName
					+ ")(objectClass=user))", controls);
			
			if (answer.hasMore()) {
				Attributes attr = answer.next().getAttributes();
				if (attr != null && attr.get("userPrincipalName") != null) {
					//
					// Found it, prepare return values
					//
					for (String info : userInfoAttributes) {
						Object val = null;
						if (attr.get(info) != null) {
							val = attr.get(info).get();
						}
						
						if (val != null) {
							ret.put(info, val.toString());
						} else {
							ret.put(info, null);
						}
					}
					//
					// And returns
					//
					return ret;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch userInfo (" + usedUsername + ")", e);
		}
		
		throw new RuntimeException("Failed to fetch userInfo (" + usedUsername + ")");
		//return null; // Failed to get
	}
}
