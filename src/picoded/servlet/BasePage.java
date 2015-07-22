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
import picoded.conv.JMTE;
import picoded.JStack.*;
import picoded.RESTBuilder.*;

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
	// RESTBuilder related convinence 
	//
	/////////////////////////////////////////////
	protected RESTBuilder _restBuilderObj = null;
	
	public RESTBuilder restBuilder() {
		if( _restBuilderObj != null ) {
			return _restBuilderObj;
		}
		
		_restBuilderObj = new RESTBuilder();
		
		return _restBuilderObj;
	}
	
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
	
	// Removed?: Use accountAuthTable() instead
	// Gets the user meta info from the specific user
	// public String accountMetaInfo(String name, String meta) throws JStackException {
	// 	return accountAuthTable().getFromName(name).getString(meta);
	// }
	
	/////////////////////////////////////////////
	// HTML fetch and JMTE templating
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
			if(format == null || format.length() <= 0) {
				return null;
			}
			
			try {
				return currentAccountMetaInfo(format);
			} catch(JStackException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/// [Protected] jmte object used
	protected JMTE _jmteObj = null;
	
	/// Loads and setup the jmte object with the "contextPath" parameter, and htmlPartsFolder directory and returns the jmte object
	public JMTE JMTE() {
		if(_jmteObj != null) { 
			return _jmteObj; 
		}
		
		_jmteObj = new JMTE( getPagesTemplatePath() );
		_jmteObj.baseDataModel.put( "ContextPath", getContextPath() );
		_jmteObj.registerNamedRenderer( new currentAccountMetaInfo_nr() );
		
		return _jmteObj;
	}
	
	

}
