package picoded.servlet;

// Java Serlvet requirments
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

// Net, io, NIO
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;

// Exceptions used
import javax.servlet.ServletException;
import java.io.IOException;

// Objects used
import java.util.*;
import java.io.PrintWriter;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Apache library used
import org.apache.commons.io.FilenameUtils;

// Sub modules useds
import picoded.conv.JMTE;
import picoded.enums.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.template.core.*;
import picoded.RESTBuilder.template.server.*;
import picoded.webTemplateEngines.JSML.*;
import picoded.page.builder.*;

/**
 * Extends the corePage/jSqlPage functionality, and implements basic UI templating, lifecycle handling,
 * and accountsManagement
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * @TODO
 * + unit tests
 * + logger
 * + API module
 */
public class BasePage extends JStackPage implements ServletContextListener {
	
	/////////////////////////////////////////////
	//
	// Static variables
	//
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	//
	// RESTBuilder related convinence
	//
	/////////////////////////////////////////////
	
	/// Cached restbuilder object
	protected RESTBuilder _restBuilderObj = null;
	
	/// REST API builder
	public RESTBuilder restBuilder() {
		if (_restBuilderObj != null) {
			return _restBuilderObj;
		}
		
		_restBuilderObj = new RESTBuilder();
		restBuilderSetup(_restBuilderObj);
		
		return _restBuilderObj;
	}
	
	/// !To Override
	/// to configure the restBuilderSetup steps
	public void restBuilderSetup(RESTBuilder rbObj) {
		// The server timestamp fetching
		rbObj.getNamespace("server.now").put(HttpRequestType.GET, ServerTime.now);
		
		// Preload RESBUilder stack
		JStackUtils.setupRESTBuilderStruct(rbObj, JStackObj,
			JConfig().getStringMap("sys.JStack.struct", null));
	}
	
	/////////////////////////////////////////////
	//
	// JStack related convinence function
	//
	/////////////////////////////////////////////
	
	protected AccountTable _accountAuthObj = null;
	
	/// The default setup process of accountAuthTable
	public void accountAuthTableSetup() throws JStackException {
		// Gets the configuration setup
		JConfig jc = JConfig();
		
		// Setup the tables
		AccountTable at = accountAuthTable();
		
		// Setup table
		boolean skipAccountAuthTableSetup = JConfig().getBoolean(
			"sys.JStack.skipAccountAuthTableSetup", false);
		if (!skipAccountAuthTableSetup) {
			at.systemSetup();
		} else {
			System.out.println("Skipping systemSetup in BasePage");
		}
		
		// Gets the superuser group
		String superGroup = jc.getString("sys.account.superUsers.groupName", "SuperUsers");
		String adminUser = jc.getString("sys.account.superUsers.rootUsername", "admin");
		String adminPass = jc.getString("sys.account.superUsers.rootPassword", "P@ssw0rd!");
		boolean resetPass = jc.getBoolean("sys.account.superUsers.rootPasswordReset", false);
		
		// Gets and setup the objects if needed
		AccountObject grpObject = at.getFromName(superGroup);
		if (grpObject == null) {
			grpObject = at.newObject(superGroup);
		}
		
		// Remove password for super group
		grpObject.removePassword();
		
		// Setup the default admin
		AccountObject userObject = at.getFromName(adminUser);
		if (userObject == null) {
			userObject = at.newObject(adminUser);
			userObject.setPassword(adminPass);
		} else if (resetPass) {
			userObject.setPassword(adminPass);
		}
		
		// Ensure its role
		String role = grpObject.getMemberRole(userObject);
		if (role == null || !(role.equals("admin"))) {
			grpObject.setMember(userObject, "admin");
		}
		
		// Apply changes
		userObject.saveDelta();
		grpObject.saveDelta();
	}
	
	public String getSuperUserGroupName() {
		return JConfig().getString("sys.account.superUsers.groupName", "SuperUsers");
	}
	
	/// The primary accountAuthTable used for user authentication
	public AccountTable accountAuthTable() {
		if (_accountAuthObj != null) {
			return _accountAuthObj;
		}
		
		// @TODO cookiePrefix to be loaded from configurable
		// @TODO Config loading
		
		// Gets the configuration setup
		JConfig jc = JConfig();
		String tablePrefix = jc.getString("sys.JStack.baseAccount.name",
			jc.getString("sys.account.tableConfig.tablePrefix", "picoded_account"));
		
		// httpUserAuthObj.loginLifetime = cStack.getInt( "userAuthCookie.loginLifetime", httpUserAuthObj.loginLifetime);
		// httpUserAuthObj.loginRenewal = cStack.getInt( "userAuthCookie.loginRenewal", httpUserAuthObj.loginRenewal);
		// httpUserAuthObj.rmberMeLifetime = cStack.getInt( "userAuthCookie.rememberMeLifetime", httpUserAuthObj.rmberMeLifetime);
		//
		// httpUserAuthObj.isHttpOnly = cStack.getBoolean( "userAuthCookie.isHttpOnly", httpUserAuthObj.isHttpOnly);
		// httpUserAuthObj.isSecureOnly = cStack.getBoolean( "userAuthCookie.isSecureOnly", httpUserAuthObj.isSecureOnly);
		
		_accountAuthObj = JStack().getAccountTable(tablePrefix);
		_accountAuthObj.setSuperUserGroupName(getSuperUserGroupName());
		
		return _accountAuthObj;
	}
	
	/// [Protected] cached current account object
	public AccountObject _currentAccount = null;
	
	/// Current account that is logged in
	public AccountObject currentAccount() {
		
		if (_currentAccount != null) {
			return _currentAccount;
		}
		
		_currentAccount = accountAuthTable().getRequestUser(httpRequest, httpResponse);
		
		return _currentAccount;
	}
	
	/// Diverts the user if not logged in, and returns true.
	/// Else stored the logged in user name, does setup(), and returns false.
	public boolean divertInvalidUser(String redirectTo) throws IOException, JStackException {
		if (currentAccount() == null) {
			httpResponse.sendRedirect(redirectTo); // wherever you wanna redirect this page.
			return true;
		}
		return false;
	}
	
	/// Get the user meta from the current user, this can also be called via JMTE
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.html}
	/// {currentUserMetaInfo(metaName)}
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String currentAccountMetaInfo(String meta) throws JStackException {
		return currentAccount().getString(meta);
	}
	
	// Removed?: Use accountAuthTable() instead
	// Gets the user meta info from the specific user
	// public String accountMetaInfo(String name, String meta) throws JStackException {
	// 	return accountAuthTable().getFromName(name).getString(meta);
	// }
	
	/////////////////////////////////////////////
	//
	// HTML fetch and JMTE templating
	//
	/////////////////////////////////////////////
	protected class currentAccountMetaInfo_nr implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "currentAccountMetaInfo";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { (new Object()).getClass() };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			if (format == null || format.length() <= 0) {
				return null;
			}
			
			try {
				return currentAccountMetaInfo(format);
			} catch (JStackException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/// [Protected] jmte object used
	protected JMTE _jmteObj = null;
	
	/// Loads and setup the jmte object with the "contextPath" parameter, htmlPartsFolder directory if needed
	///
	/// @returns the jmte object
	public JMTE JMTE() {
		if (_jmteObj != null) {
			return _jmteObj;
		}
		
		_jmteObj = new JMTE(getPageTemplatePath());
		JMTE_initialSetup(_jmteObj);
		
		return _jmteObj;
	}
	
	/// Initial setup of the JMTE logic, and base data model.
	///
	/// this is the function to override for extended classes to add to JMTE base data model
	public void JMTE_initialSetup(JMTE setupObj) {
		setupObj.baseDataModel.put("ContextPath", getContextPath());
		setupObj.baseDataModel.put("ContextURI", getContextURI());
		
		// Pass the configuration settings in JConfig to JMTE, but filter system
		setupObj.baseDataModel.put("Config", JConfig().createSubMap(null, "sys"));
		
		// JSML integration
		setupObj.baseDataModel.put("JSML", JSMLFormSet());
		
		// The this data model self reference
		// setupObj.baseDataModel.put("this", setupObj.baseDataModel);
		
		setupObj.registerNamedRenderer(new currentAccountMetaInfo_nr());
	}
	
	/////////////////////////////////////////////
	//
	// PageBuilder handling
	//
	/////////////////////////////////////////////
	
	/// [Protected] PageBuilder object used
	protected PageBuilder _pageBuilderObj = null;
	
	/// Loads and setup the PageBuilder object if needed
	///
	/// @returns the PageBuilder object
	public PageBuilder PageBuilder() {
		if (_pageBuilderObj != null) {
			_pageBuilderObj.setUriRootPrefix(getContextURI());
			return _pageBuilderObj;
		}
		
		JConfig jc = JConfig();
		
		_pageBuilderObj = new PageBuilder(getPageTemplatePath(), getPageOutputPath());
		_pageBuilderObj.setJMTE(JMTE());
		_pageBuilderObj.setUriRootPrefix(getContextURI());
		
		// Page builder config
		_pageBuilderObj.enableRelativeURI = jc.getBoolean("sys.pageBuilder.enableRelativeURI", false);
		_pageBuilderObj.fullApiRootPath = jc.getString("sys.pageBuilder.fullApiRootPath", null);
		
		return _pageBuilderObj;
	}
	
	/////////////////////////////////////////////
	//
	// JSML handling
	//
	/////////////////////////////////////////////
	
	/// [Protected] JSMLFormSet object used
	protected JSMLFormSet _formSetObj = null;
	
	/// Loads and setup the JSMLFormSet object if needed
	///
	/// @returns the JSMLFormSet object
	public JSMLFormSet JSMLFormSet() {
		if (_formSetObj != null) {
			return _formSetObj;
		}
		
		_formSetObj = new JSMLFormSet(this);
		return _formSetObj;
	}
	
	///
	/// Checks and forces a redirection for closing slash on index page requests.
	/// If needed (returns false, on validation failure)
	///
	/// For example : https://picoded.com/JavaCommons , will redirect to https://picoded.com/JavaCommons/
	///
	/// This is a rather complicated topic. Regarding the ambiguity of the HTML
	/// redirection handling of. (T605 on phabricator)
	///
	/// But basically take the following as example. On how a redirect is handled
	/// for a relative "./index.html" within a webpage.
	///
	/// | Current URL              | Redirects to             |
	/// |--------------------------|--------------------------|
	/// | host/subpath             | host/index.html          |
	/// | host/subpath/            | host/subpath/index.html  |
	/// | host/subpath/index.html  | host/subpath/index.html  |
	/// 
	/// As a result of the ambiguity in redirect for html index pages loaded
	/// in "host/subpath". This function was created, so that when called.
	/// will do any redirect if needed if the request was found to be.
	///
	/// The reason for standardising to "host/subpath/" is that this will be consistent
	/// offline page loads (such as through cordova). Where the index.html will be loaded
	/// in full file path instead.
	///
	/// 1) A request path withoug the "/" ending
	///
	/// 2) Not a file request, a file request is assumed if there was a "." in the last name
	///    Example: host/subpath/file.js
	///
	/// 3) Not an API request with the "api" keyword. Example: host/subpath/api
	///
	/// This will also safely handle the forwarding of all GET request parameters.
	/// For example: "host/subpath?abc=xyz" will be redirected to "host/subpath/?abc=xyz"
	///
	/// Note: THIS will silently pass as true, if a httpRequest is not found. This is to facilitate
	///       possible function calls done on servlet setup. Without breaking them
	///
	/// Now that was ALOT of explaination for one simple function wasnt it >_>
	/// Well its one of the lesser understood "gotchas" in the HTTP specifications.
	/// Made more unknown by the JavaCommons user due to the common usage of ${PageRootURI}
	/// which basically resolves this issue. Unless its in relative path mode. Required for app exports.
	///
	public boolean enforceProperRequestPathEnding() throws IOException {
		if (httpRequest != null) {
			String fullURI = httpRequest.getRequestURI();
			
			// This does not validate blank / root requests
			//
			// Should we? : To fix if this is required (as of now no)
			if (fullURI == null || fullURI.equalsIgnoreCase("/")) {
				return true;
			}
			
			//
			// Already ends with a "/" ? : If so its considered valid
			//
			if (fullURI.endsWith("/")) {
				return true;
			}
			
			//
			// Checks if its a file request. Ends check if it is
			//
			String name = FilenameUtils.getName(fullURI);
			if (FilenameUtils.getExtension(name).length() > 0) {
				// There is a file extension. so we shall assume it is a file
				return true; // And end it
			}
			
			//
			// Check for the "api" keyword
			//
			if (fullURI.indexOf("/api/") >= 0 || fullURI.indexOf("/API/") >= 0) {
				// Found it, assume its valid then
				return true;
			}
			
			//
			// Get the query string to append (if needed)
			//
			String queryString = httpRequest.getQueryString();
			if (queryString == null) {
				queryString = "";
			} else if (!queryString.startsWith("?")) {
				queryString = "?" + queryString;
			}
			
			//	
			// Enforce proper URL handling
			//
			httpResponse.sendRedirect(fullURI + "/" + queryString);
			return false;
		}
		
		// Validation is valid.
		return true;
	}
	
	/////////////////////////////////////////////
	//
	// Do Auth and Do output overrides
	//
	/////////////////////////////////////////////
	
	/// Enforces request path handling
	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		return enforceProperRequestPathEnding() && super.doAuth(templateData);
	}
	
	/// BasePage initializeContext to be extended / build on
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		accountAuthTableSetup();
	}
	
}
