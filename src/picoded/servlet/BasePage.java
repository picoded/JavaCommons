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
		JStackUtils.setupRESTBuilderStruct(rbObj, JStackObj, JConfig().getStringMap("sys.JStack.struct", null));
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
		at.systemSetup();

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

		_pageBuilderObj = new PageBuilder(getPageTemplatePath(), getPageOutputPath());
		_pageBuilderObj.setJMTE(JMTE());
		_pageBuilderObj.setUriRootPrefix(getContextURI());

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

		_formSetObj = new JSMLFormSet(getJsmlTemplatePath(), getContextURI());
		return _formSetObj;
	}

	/////////////////////////////////////////////
	//
	// Servlet context handling
	//
	/////////////////////////////////////////////

	/// BasePage initializeContext to be extended / build on
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		boolean skipAccountAuthTableSetup = JConfig().getBoolean("sys.JStack.skipAccountAuthTableSetup", false);
		if(!skipAccountAuthTableSetup){
			accountAuthTableSetup();
		}else{
			System.out.println("Skipping skipAccountAuthTableSetup in BasePage");
		}
	}

}
