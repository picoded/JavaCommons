package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URL;
import java.lang.String;
import java.io.File;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
import java.util.*;

import java.lang.Number;
import java.lang.System;

// Exceptions used
import javax.servlet.ServletException;
import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;

// Objects used
import java.util.HashMap;
import java.io.PrintWriter;

import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.JStack.*;

/**
 * Extends the corePage/jSqlPage functionality, and implements basic UI templating, and accountManagement
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * @TODO
 * + unit tests
 * + logger
 * + API module
 */
public class BasePage extends JStackPage {
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Static variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// Common not so impt stuff
	//-------------------------------------------
	
	// Serialize version ID
	static final long serialVersionUID = 1L;
	
	/////////////////////////////////////////////
	//
	// JStack related convinence function
	//
	/////////////////////////////////////////////
	
	protected String _JStackAppPrefix = "picoded_";
	protected String _accountTableSuffix = "account"; 
	
	protected AccountTable _accountAuthObj = null;
	
	public AccountTable accountAuthTable() {
		if( _accountAuthObj != null ) {
			return _accountAuthObj;
		}
		
		// @TODO cookiePrefix to be loaded from configurable
		// @TODO Config loading
		// 
		// httpUserAuthObj.loginLifetime = cStack.getInt( "userAuthCookie.loginLifetime", httpUserAuthObj.loginLifetime);
		// httpUserAuthObj.loginRenewal = cStack.getInt( "userAuthCookie.loginRenewal", httpUserAuthObj.loginRenewal);
		// httpUserAuthObj.rmberMeLifetime = cStack.getInt( "userAuthCookie.rememberMeLifetime", httpUserAuthObj.rmberMeLifetime);
		// 
		// httpUserAuthObj.isHttpOnly = cStack.getBoolean( "userAuthCookie.isHttpOnly", httpUserAuthObj.isHttpOnly);
		// httpUserAuthObj.isSecureOnly = cStack.getBoolean( "userAuthCookie.isSecureOnly", httpUserAuthObj.isSecureOnly);
		
		_accountAuthObj = JStack().getAccountTable( _JStackAppPrefix + _accountTableSuffix );
		
		return _accountAuthObj;
	}
	
	/// [Protected] cached current account object
	public AccountObject _currentAccount = null;
	
	/// Current account that is logged in
	public AccountObject currentAccount() {
		
		if( _currentAccount != null ) {
			return _currentAccount;
		}
		
		_currentAccount = accountAuthTable().getRequestUser(httpRequest, httpResponse);
		
		return _currentAccount;
	}
	
	/// Diverts the user if not logged in, and returns true.
	/// Else stored the logged in user name, does setup(), and returns false.
	public boolean divertInvalidUser(String redirectTo) throws IOException, JStackException {
		if( currentAccount() == null ) {
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
	
	/// Gets the user meta info from the specific user
	public String accountMetaInfo(String name, String meta) throws JStackException {
		return accountAuthTable().getFromName(name).getString(meta);
	}
	
	/*
	
	//////////////////////////////////////////////////
	// GroupMeta module is intentionally leftout
	// As this tends to be highly adjusted according
	// to the project specific requirments.
	//////////////////////////////////////////////////
	
	/////////////////////////////////////////////
	// HTML fetch and JMTE templating
	/////////////////////////////////////////////
	protected class currentUserMetaInfo_nr implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "currentUserMetaInfo";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { (new Object()).getClass() };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			if(format == null || format.length() <= 0) {
				return null;
			}
			
			try {
				return currentUserMetaInfo(format);
			} catch(jSqlException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/// [Protected] jmte object used
	protected jmte jmteObj = null;
	
	/// Loads and setup the jmte object with the "contextPath" parameter, and htmlPartsFolder directory and returns the jmte object
	public jmte jmteObj() {
		if(jmteObj != null) { return jmteObj; }
		
		jmteObj = new jmte( htmlPartsFolder );
		jmteObj.baseDataModel.put( "ContextPath", getContextPath() );
		jmteObj.registerNamedRenderer( new currentUserMetaInfo_nr() );
		
		return jmteObj;
	}
	
	////////////////////////////////////////////////////
	// tableSetup calls for various jSql based modules
	////////////////////////////////////////////////////
	@Override
	public void jSqlTableSetups() throws jSqlException {
		super.jSqlTableSetups();
		userAuth().tableSetup();
		userMetaObj().tableSetup();
		adminLogger().tableSetup();
	}
	
	/// Called once when initialized, to purge all existing data.
	@Override
	public void doPurge() {
		super.doPurge();
		
		validatedUser = false;
		currentUser = null;
	}
	*/
}
