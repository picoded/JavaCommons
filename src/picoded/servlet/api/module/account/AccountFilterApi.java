package picoded.servlet.api.module.account;

import java.util.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.servlet.api.exceptions.HaltException;

import picoded.dstack.module.account.*;
import picoded.dstack.*;
import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtil;
import picoded.conv.GenericConvert;
import java.util.function.BiFunction;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;



///
/// Account table API builder
///
public class AccountFilterApi extends AccountTableApi implements ApiModule {

	/// The AccountTable reference
	protected AccountTable table = null;

	/// Static ERROR MESSAGES
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";


	/// Setup the account table api class
	///
	/// @param  The input AccountTable to use
	public AccountFilterApi(AccountTable inTable) {
    super(inTable);
		table = super.table;
	}

  protected ApiFunction test = (req, res) -> {
    if ( res.get(Account_Strings.RES_ERROR) != null )
      throw new HaltException(Account_Strings.ERROR_NO_USER);
    else
      return res;
  };

	protected ApiFunction check_is_super_user = (req, res) -> {
		AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
    if ( ao == null ) {
      res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
      return res;
    }
    if ( !ao.isSuperUser() ) {
      // throw new HaltException(Account_Strings.ERROR_NO_USER);
      res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
      return res;
    }

		// Return result
		return res;
	};

	protected ApiFunction test2 = (req, res) -> {
		// Get the account object (if any)
		// AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
		// Return if a valid login object was found
		// res.put(Account_Strings.RES_RETURN, ao != null);
		System.out.println("test2 is ran <<<<<<<<<<<<<<<<<<<<<<<");
		// Return result
		return res;
	};

	/// Does the actual setup for the API
	/// Given the API Builder, and the namespace prefix
	///
	/// @param  API builder to add the required functions
	/// @param  Path to assume
	public void setupApiBuilder(ApiBuilder builder, String path) {
    super.setupApiBuilder(builder, path);
		builder.filter(path+"account/*/*", test2);
		builder.filter(path+"account/login/*", check_is_super_user);
    builder.filter(path+"test", test);
	}
	/// Private Methods
}
