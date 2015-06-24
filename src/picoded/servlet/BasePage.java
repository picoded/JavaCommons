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
 * Extends the corePage/jSqlPage functionality, and implements basic UI templating, and personaManagement
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
	// JStack convinence function
	//
	/////////////////////////////////////////////
	
	protected String _JStackAppPrefix = "picoded_";
	protected String _personaTableSuffix = "persona"; 
	
	protected PersonaTable _personaAuthObj = null;
	
	public PersonaTable personaAuth() {
		if( _personaAuthObj != null ) {
			return _personaAuthObj;
		}
		
		_personaAuthObj = JStack().getPersonaTable( _JStackAppPrefix + _personaTableSuffix );
		
		return _personaAuthObj;
	}
	
	
	
	/*
	/// [Protected] internal httpUserAuth object
	protected httpUserAuth httpUserAuthObj = null;
	
	/// Loads and setup the httpUserAuth module
	public httpUserAuth userAuth() {
		if(httpUserAuthObj != null) {
			return httpUserAuthObj;
		}
		jConfig cStack = configStack();
		
		httpUserAuthObj = new httpUserAuth( jSqlConnect(), "", "" );
		
		httpUserAuthObj.loginLifetime = cStack.getInt( "userAuthCookie.loginLifetime", httpUserAuthObj.loginLifetime);
		httpUserAuthObj.loginRenewal = cStack.getInt( "userAuthCookie.loginRenewal", httpUserAuthObj.loginRenewal);
		httpUserAuthObj.rmberMeLifetime = cStack.getInt( "userAuthCookie.rememberMeLifetime", httpUserAuthObj.rmberMeLifetime);
		
		httpUserAuthObj.isHttpOnly = cStack.getBoolean( "userAuthCookie.isHttpOnly", httpUserAuthObj.isHttpOnly);
		httpUserAuthObj.isSecureOnly = cStack.getBoolean( "userAuthCookie.isSecureOnly", httpUserAuthObj.isSecureOnly);
		
		return httpUserAuthObj;
	}
	
	/// [Protected] cached current user string
	protected String currentUser = null;
	/// [Protected] boolean indicate if the validate step has occured
	protected boolean validatedUser = false;
	
	/// Returns the current user, after validating
	public String currentUser() {
		if(!validatedUser) {
			currentUser = userAuth().validateUser(httpRequest, httpResponse);
			validatedUser = true;
		}
		return currentUser;
	}
	
	/// Diverts the user if not logged in, and returns true.
	/// Else stored the logged in user name, does setup(), and returns false.
	public boolean divertInvalidUser(String redirectTo) throws IOException, jSqlException {
		if( currentUser() == null ) {
			httpResponse.sendRedirect(redirectTo); // wherever you wanna redirect this page.
			return true;
		}
		return false;
	}
	
	/////////////////////////////////////////////
	// Admin logs setup
	/////////////////////////////////////////////
	
	/// [Protected] internal adminLogs object
	protected adminLogs adminLogsObj = null;
	
	/// Loads and setup the adminLogs object
	public adminLogs adminLogger() {
		if(adminLogsObj != null) {
			return adminLogsObj;
		}
		adminLogsObj = new adminLogs( jSqlConnect(), "" );
		return adminLogsObj;
	}
	
	/// Logging ETC data, with the current logged in user
	public void adminLogger_etc( String zone, String action ) {
		adminLogger().writeLog( ((currentUser != null)? currentUser : "system"), zone, action, adminLogs.logLevel(1) );
	}
	
	/// Logging LOG data, with the current logged in user
	public void adminLogger_log( String zone, String action ) {
		adminLogger().writeLog( ((currentUser != null)? currentUser : "system"), zone, action, adminLogs.logLevel(2) );
	}
	
	/// Logging IMPORTANT data, with the current logged in user
	public void adminLogger_imp( String zone, String action ) {
		adminLogger().writeLog( ((currentUser != null)? currentUser : "system"), zone, action, adminLogs.logLevel(3) );
	}
	
	/// Logging WARNING data, with the current logged in user
	public void adminLogger_wrn( String zone, String action ) {
		adminLogger().writeLog( ((currentUser != null)? currentUser : "system"), zone, action, adminLogs.logLevel(4) );
	}
	
	/// Logging ERROR data, with the current logged in user
	public void adminLogger_err( String zone, String action ) {
		adminLogger_err( zone, action, null );
	}
   
	public void adminLogger_err( String zone, String action, String reqparams ) {
		adminLogger().writeError( ((currentUser != null)? currentUser : "system"), zone, action, reqparams );
	}
	
	/////////////////////////////////////////////
	// UserMeta module setup
	/////////////////////////////////////////////
	
	/// [Protected] internal adminLogs object
	protected userMeta userMetaObj = null;
	
	public userMeta userMetaObj() {
		if(userMetaObj != null) {
			return userMetaObj;
		}
		userMetaObj = new userMeta( jSqlConnect(), "" );
		
		return userMetaObj;
	}
	
	/// Get the user meta from the current user, this can also be called via JMTE
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.html}
	/// {currentUserMetaInfo(metaName)}
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String currentUserMetaInfo(String meta) throws jSqlException {
		return userMetaObj().get(currentUser, meta);
	}
	
	/// Gets the user meta info from the specific user
	public String userMetaInfo(String user, String meta) throws jSqlException {
		return userMetaObj().get(user, meta);
	}
	
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
