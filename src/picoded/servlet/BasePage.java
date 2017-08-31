package picoded.servlet;

import java.util.*;
import java.io.IOException;
import picoded.dstack.module.account.*;
import picoded.dstack.*;

/**
 * Extends DStackPage and cater the initialization of the AccountTable
 */
public class BasePage extends DStackPage {
	
	protected AccountTable _accountTable = null;
	
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
		return DConfig().getString("sys.DStack.baseAccount.name",
			DConfig().getString("sys.account.tableConfig.tablePrefix", "account"));
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
	private AccountTable getAccountTable() {
		if (_accountTable != null) {
			return _accountTable;
		}
		_accountTable = DStack().getAccountTable(getAccountTablePrefix());
		
		return _accountTable;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Current Account Methods
	//
	////////////////////////////////////////////////////////////////////////////
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
		Map<String, Object> meta = dc.getStringMap("sys.account.superUsers.meta",
			new HashMap<String, Object>());
		boolean resetMeta = dc.getBoolean("sys.account.superUsers.resetMeta", true);
		List<String> superGrpMemberRoles = dc.getList("sys.account.superUsers.memberRoles",
			at.defaultMembershipRoles());
		
		// Set up super user
		AccountObject superUser = at.getFromLoginID(adminUser);
		if (superUser == null) {
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
		AccountObject superGrp = at.getFromLoginID(getSuperUserGroupName());
		if (superGrp == null) {
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
