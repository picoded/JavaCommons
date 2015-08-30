package picoded.RESTBuilder.templates;

import java.util.*;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;

import picoded.enums.HttpRequestType;

/// Account login template API
public class AccountLogin extends BasePage {
	
	/// Default api set prefix
	protected static String _apiSetPrefix_prefix = "account.";
	
	/// Internal prefix set for the API
	protected String _apiSetPrefix = _apiSetPrefix_prefix;
	
	/// The prefix for the api
	public String apiSetPrefix() {
		return _apiSetPrefix;
	}
		
	/// The prefix for the api
	public void setApiSetPrefix(String api) {
		_apiSetPrefix = api;
	}
	
	/// Flags as JSON request
	public boolean isJsonRequest() {
		return true;
	}
	
	/// Does the default setup
	public void doSetup() throws Exception {
		super.doSetup();
		setupRESTBuilder( this.restBuilder(), this.accountAuthTable(), _apiSetPrefix );
	}
	
	/// Process the request, not the authentication layer
	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return restBuilder().servletCall( _apiSetPrefix, this, outputData );
	}
	
	///
	/// Generates the default REST function for the following, 
	/// note that they do not have ANY authentication layer enforced.
	///
	/// + login 
	///    + GET  : Returns the current accountID, and accountNAME array
	///    + POST : Login with accountNAME / accountID and accountPASS
	/// + logout
	///    + GET  : Logs out the current login ID
	/// + password (only works for its own user)
	///    + POST : accountID, oldPassword, newPassword
	/// + groups_id
	///    + GET  : Returns the list of groups found in system
	/// 
	///
	///
	/// # Note the following default has yet to be implmented
	/// # And SHOULD be rewritten / overwritten to fit the app specific security model
	///    + name
	///        + POST : accountID, oldName, newName, appendName
	/// + list
	///    + GET : List all the accounts and their ID, or use a search criteria
	/// + meta/delta/$(loginID)/
	///    + GET : 
	/// 
	/// + info/$(loginID)
	///    + GET  : Gets the user meta info, and etc
	///         + names : Array of login names (not login ID)
	///         + accountID : The accountID
	///         + meta : The meta info representing the account
	///         + 
	///    + POST : Update the user meta info, and etc
	///         + isDelta : true / false, indicate if only update changes
	///         + meta : Object representing the meta info to update
	///    + DELETE : Deletes the account with the ID
	/// 
	public static RESTFunction generateDefaultFunction(AccountTable at, String apiSuffix, HttpRequestType apiType ) {
		RESTFunction ret = null;
		
		if( apiSuffix.equals("login") ) {
			
			//
			// Login functions
			//
			if( apiType == HttpRequestType.GET ) {
				ret = (req, res) -> {
					res.put("accountID", null);
					res.put("accountNAME", null);
					
					if(req.requestPage() != null) {
						BasePage bp = (BasePage)(req.requestPage());
						
						AccountObject ao = at.getRequestUser( bp.getHttpServletRequest() );
						if( ao != null ) {
							res.put("accountID", ao._oid());
							
							String[] names = ao.getNames().toArray(new String[0]);
							res.put("accountNAME", Arrays.asList( (names == null)? new String[] {} : names) );
						}
					}
					return res;
				};
			} else if( apiType == HttpRequestType.POST ) {
				ret = (req, res) -> {
					res.put("accountID", null);
					res.put("accountNAME", null);
					
					if( req.requestPage() != null ) {
						BasePage bp = (BasePage)(req.requestPage());
						AccountObject ao = null;
						
						String tStr = req.getString("accountNAME");
						if( tStr != null ) {
							ao = at.getFromName(tStr);
						}
						
						if( ao == null && (tStr = req.getString("accountID")) != null ) {
							ao = at.getFromID(tStr);
						}
						
						// AccountName or AccountID is valid
						if( ao != null ) {
							ao = at.loginAccount( bp.getHttpServletRequest(), bp.getHttpServletResponse(), ao, req.getString("accountPASS", ""), false );
							
							// Login is valid
							if( ao != null ) {
								res.put("accountID", ao._oid());
								String[] names = ao.getNames().toArray(new String[0]);
								res.put("accountNAME", Arrays.asList( (names == null)? new String[] {} : names) );
							}
						}
					}
					return res;
				};
			}
			
		} else if( apiSuffix.equals("logout") ) {
			
			//
			// Logout functions
			//
			ret = (req, res) -> {
				res.put("logout", "false");
				
				if(req.requestPage() != null) {
					BasePage bp = (BasePage)(req.requestPage());
					at.logoutAccount( bp.getHttpServletRequest(), bp.getHttpServletResponse() );
					res.put("logout", "true");
				}
				return res;
			};
			
		} else if( apiSuffix.equals("password") ) {
			
			//
			// Password change
			//
			ret = (req, res) -> {
				res.put("accountID", null);
				res.put("passwordChanged", false);
				
				if(req.requestPage() != null) {
					BasePage bp = (BasePage)(req.requestPage());
					
					// Allow change password only if it is current user
					AccountObject ao = at.getRequestUser( bp.getHttpServletRequest() );
					if( ao != null ) {
						String accID = req.getString("accountID");
						if( ao._oid().equals(accID) ) {
							
							if( ao.setPassword( req.getString("newPassword"), req.getString("oldPassword") ) ) {
								res.put("accountID", accID);
								res.put("passwordChanged", true);
							} else {
								res.put("accountID", accID);
								res.put("error", "Original password is wrong");
							}
						}
						
					}
				}
				return res;
			};
			
		}
		
		return ret;
	}
	
	///
	/// Takes the restbuilder and the account table object and implements its respective default API
	///
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at, String setPrefix ) {
		String[] apiSuffix = new String[] { "login", "logout", "password", "list" };
		
		for( int a=0; a<apiSuffix.length; ++a ) {
			for( HttpRequestType t : HttpRequestType.values() ) {
				RESTFunction r = generateDefaultFunction(at, apiSuffix[a], t);
				if( r != null ) {
					rb.getNamespace( setPrefix + apiSuffix[a] ).put( t, r );
				}
			}
		}
		return rb;
	}
	
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at) {
		return setupRESTBuilder(rb, at, _apiSetPrefix_prefix);
	}
	
}
