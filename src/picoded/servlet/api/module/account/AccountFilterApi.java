package picoded.servlet.api.module.account;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.mail.internet.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.servlet.api.exception.HaltException;

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

	////////////////////////////////////////////////////////////////////////////
	/// Account Admin Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction admin_bundle_check = (req, res) -> {
		res = this.isLoggedIn.apply(req, res);
		if ( res != null )
			return res;
		res = this.check_is_super_user.apply(req, res);
		if ( res != null )
			return res;
		return null;
	};
	// Check if it is admin
	protected ApiFunction check_is_super_user = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( !currentUser.isSuperUser() ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_PRIVILEGES);
			return res;
		}
		return null;
	};

	////////////////////////////////////////////////////////////////////////////
	/// Account Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction account_bundle_check = (req, res) -> {
		return null;
	};

	// checking of password and email format
	protected ApiFunction complexity_bundle_check = (req, res) -> {
		res = this.check_password.apply(req, res);
		if ( res != null )
			return res;
		res = this.check_email.apply(req, res);
		if ( res != null )
			return res;
		return null;
	};
	// Check password complexity
	protected ApiFunction check_password = (req, res) -> {
		String passwordRegex = "(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])", password = "";
		Pattern p = Pattern.compile(passwordRegex);
		String[] passwordList = new String[]{Account_Strings.REQ_PASSWORD, Account_Strings.REQ_NEW_PASSWORD, Account_Strings.REQ_REPEAT_PASSWORD};
		for ( String password_string : passwordList ) {
			Matcher m = p.matcher(password);
			password = req.getString(password_string, "");
			if ( !password.isEmpty() && !m.matches()) {
				res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_PASSWORD_COMPLEXITY);
				return res;
			}
		}
		return null;
	};

	// Check email format
	protected ApiFunction check_email = (req, res) -> {
		String email = req.getString(Account_Strings.REQ_USERNAME);
		if ( !isEmailFormat(email) ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_INVALID_FORMAT_EMAIL);
			return res;
		}
		return null;
	};
	////////////////////////////////////////////////////////////////////////////
	/// Group Admin Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction group_admin_bundle_check = (req, res) -> {
		res = this.check_is_super_user.apply(req, res);
		if ( res != null )
			return res;
		res = this.check_is_current_user_group_admin.apply(req, res);
		if ( res != null )
			return res;
		return null;
	};

	// Check if user has rights to CRUD group (user must be admin of the group)
	// protected ApiFunction check_crud = (req, res) -> {
	// };

	////////////////////////////////////////////////////////////////////////////
	/// Group Member Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction group_bundle_check = (req, res) -> {
		res = this.isLoggedIn.apply(req, res);
		if ( res != null )
			return res;
		res = this.check_is_super_user.apply(req, res);
		if ( res == null )
			return null;
		res = this.check_is_current_user_group_admin.apply(req, res);
		if ( res == null )
			return null;
		res = this.check_is_current_user_member.apply(req, res);
		if ( res != null )
			return res;
		res = this.check_is_editing_self.apply(req, res);
		if (res != null )
			return res;
		return null;
	};

	// Check if user is a member of the group
	protected ApiFunction check_is_current_user_member = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String groupID = req.getString(Account_Strings.REQ_GROUP_ID, "");
		if ( groupID.isEmpty() ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP_ID);
			return res;
		}
		req.put(Account_Strings.REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		if ( res.get(Account_Strings.RES_ERROR) != null )
			return res;
		return null;
	};

	// Check if current user is a group admin
	protected ApiFunction check_is_current_user_group_admin = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String groupID = req.getString(Account_Strings.REQ_GROUP_ID, "");
		if ( groupID.isEmpty() ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP_ID);
			return res;
		}
		req.put(Account_Strings.REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		if ( res.get(Account_Strings.RES_ERROR) != null )
			return res;
		String role = res.getString(Account_Strings.RES_SINGLE_RETURN_VALUE);
		if ( !role.equalsIgnoreCase("admin") ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_PRIVILEGES);
			return res;
		}
		return null;
	};

	// Check if current user is editing self
	protected ApiFunction check_is_editing_self = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String userID = req.getString(Account_Strings.REQ_USER_ID, "");
		if ( !userID.equalsIgnoreCase(currentUser._oid()) ){
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_PRIVILEGES);
			return res;
		}
		return null;
	};


	/// Does the actual setup for the API
	/// Given the API Builder, and the namespace prefix
	///
	/// @param  API builder to add the required functions
	/// @param  Path to assume
	public void setupApiBuilder(ApiBuilder builder, String path) {
    super.setupApiBuilder(builder, path);
		builder.filter(path+"account/*", account_bundle_check);
		builder.filter(path+"account/login/*", complexity_bundle_check);
		builder.filter(path+"account/admin/*", admin_bundle_check);

		builder.filter(path+"account/group/*", group_bundle_check);
		builder.filter(path+"account/group/admin/*", group_admin_bundle_check);

	}

	/// Private Methods

	// with help from http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
	private boolean isEmailFormat(String inEmail){
		boolean result = true;
		try{
			InternetAddress emailAddr = new InternetAddress(inEmail);
			emailAddr.validate();
		}catch(AddressException ex){
			result = false;
		}
		return result;
	}


	private ApiFunction isLoggedIn = (req, res) -> {
		AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
			return res;
		}
		return null;
	};
}
