package picoded.servlet;

import java.util.*;
import java.util.logging.Logger;
import java.io.IOException;
import picoded.dstack.module.account.*;
import picoded.servlet.api.module.account.*;
import picoded.dstack.*;

/**
 * Extends DStackPage and cater the initialization of the AccountTable
 */
public class BasePage extends DStackPage {
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Reusable output logger
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Servlet logging interface
	 * 
	 * This is not a static class, so that the this object inherits
	 * any extensions if needed
	 **/
	public Logger log() {
		if (logObj != null) {
			return logObj;
		}
		logObj = Logger.getLogger(this.getClass().getName());
		return logObj;
	}
	
	// Memoizer for log() function
	protected Logger logObj = null;
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Internal data structures and their API
	//
	////////////////////////////////////////////////////////////////////////////
	
	// AccountTable
	protected AccountTable _accountTable = null;
	
	// AccountTableAPI
	protected AccountTableApi _accountTableAPi = null;
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Setup and auth
	//
	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void doSharedSetup() throws Exception {
		super.doSharedSetup();
		AccountTableApi ata = new AccountTableApi(getAccountTable());
		ata.apiSetup(this.apiBuilder(), "");
	}
	
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		setupAccountTable();
	}
	
	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		return super.doAuth(templateData);
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// GET Private Methods
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return the accountTable prefix
	 *
	 * @return String of the table prefix
	 */
	private String getAccountTablePrefix() {
		return DConfig().getString("sys.account.tableConfig.tablePrefix", "account");
	}
	
	/**
	 * Return the superUserGroup name
	 *
	 * @return String of the superUserGroup
	 */
	private String getSuperUserGroupName() {
		return DConfig().getString("sys.account.superUsers.groupName", "SuperUsers");
	}
	
	/**
	 * Return the account table, sets it if it does not exists
	 *
	 * @return AccountTable object
	 */
	public AccountTable getAccountTable() {
		if (_accountTable != null) {
			return _accountTable;
		}
		_accountTable = DStack().getAccountTable(getAccountTablePrefix());
		_accountTable.cookieDomain = DConfig().getString("sys.account.session.cookieDomain", null);
		return _accountTable;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Current Account Methods
	//
	////////////////////////////////////////////////////////////////////////////
	
	// Cache of the currentAccount, for quick reuse
	private AccountObject _currentAccount = null;
	
	/**
	 * Returns the current logged in user if any
	 *
	 * @return AccountObject else null
	 */
	public AccountObject currentAccount() {
		if (_currentAccount != null) {
			return _currentAccount;
		}
		
		_currentAccount = getAccountTable().getRequestUser(httpRequest, httpResponse);
		return _currentAccount;
	}
	
	/**
	 * Redirects non logged in user to specified path
	 *
	 * @return boolean value of processing redirection
	 */
	public boolean divertInvalidUser(String redirectPath) throws IOException {
		if (currentAccount() == null) {
			httpResponse.sendRedirect(redirectPath);
			return true;
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Account Table Setup and Configuration
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Set up account table with its configuration from the file
	 */
	public void setupAccountTable() {
		AccountTable at = getAccountTable();
		DConfig dc = DConfig();
		// Configure account table
		at.setSuperUserGroupName(getSuperUserGroupName());
		at.isHttpOnly = dc.getBoolean("sys.account.session.isHttpOnly", false /*default as false*/);
		at.isSecureOnly = dc
			.getBoolean("sys.account.session.isSecureOnly", false /*default as false*/);
		
		// Configure login life time from config
		int loginLifetime = dc.getInt("sys.account.session.loginlifetime", 3600);
		if (loginLifetime > -1) {
			at.loginLifetime = loginLifetime;
		}
		// Set up account table
		boolean skipAccountAuthTableSetup = dc.getBoolean("sys.account.skipAccountAuthTableSetup",
			false);
		if (!skipAccountAuthTableSetup) {
			at.systemSetup();
		}
		
		// Gets the superuser and group information
		String superGroup = getSuperUserGroupName();
		String adminUser = dc.getString("sys.account.superUsers.rootUsername", "admin");
		String adminPass = dc.getString("sys.account.superUsers.rootPassword", "P@ssw0rd!");
		boolean resetPass = dc.getBoolean("sys.account.superUsers.rootPasswordReset", false);
		Map<String, Object> meta = dc.getStringMap("sys.account.superUsers.data", "{}");
		boolean resetMeta = dc.getBoolean("sys.account.superUsers.resetData", false);
		List<String> superGrpMemberRoles = dc.getList("sys.account.superUsers.memberRoles",
			at.defaultMembershipRoles());
		
		// Set up super user
		AccountObject superUser = at.getFromLoginName(adminUser);
		if (superUser == null) {
			log().info("Missing superuser (possibly a first time setup)");
			log().info("Creating superuser : " + adminUser);
			
			superUser = at.newEntry(adminUser);
			superUser.setPassword(adminPass);
		} else if (resetPass) {
			superUser.setPassword(adminPass);
		}
		
		// Add in meta for user
		if (meta.size() > 0 && resetMeta) {
			superUser.putAll(meta);
		}
		
		// Set up super user group if does not exist (condition is to have an user
		// to be inside)
		AccountObject superGrp = at.getFromLoginName(getSuperUserGroupName());
		if (superGrp == null) {
			log().info("Missing superuser group (possibly a first time setup)");
			log().info("Creating superuser group : " + getSuperUserGroupName());
			
			superGrp = at.newEntry(getSuperUserGroupName());
			superGrp.setGroupStatus(true);
			superGrp.setMembershipRoles(superGrpMemberRoles);
			// Put superUser as the first admin of this group
			superGrp.addMember(superUser, "admin");
		}
		
		// Apply changes
		superUser.saveDelta();
		superGrp.saveDelta();
	}
	
}
