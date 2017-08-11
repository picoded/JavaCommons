package picoded.servlet.api.module.account;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.mail.internet.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.servlet.api.internal.HaltException;

import picoded.dstack.module.account.*;
import picoded.dstack.*;
import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtil;
import picoded.conv.GenericConvert;
import java.util.function.BiFunction;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

import static picoded.servlet.api.module.account.Account_Strings.*;

/**
* Account table API builder
**/
public class AccountFilterApi extends AccountTableApi implements ApiModule {

	/**
	* The AccountTable reference
	**/
	protected AccountTable table = null;

	/**
	* Static ERROR MESSAGES
	**/
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";

	/**
	* Setup the account table api class
	*
	* @param  The input AccountTable to use
	**/
	public AccountFilterApi(AccountTable inTable) {
    super(inTable);
		table = super.table;
	}
	// Global Variables
	ApiResponse apr = null;

	////////////////////////////////////////////////////////////////////////////
	/// Account Admin Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction admin_bundle_check = (req, res) -> {
		apr = this.isLoggedIn.apply(req, res);
		if ( apr != null )
			return apr;
		apr = this.check_is_super_user.apply(req, res);
		if ( apr != null )
			return apr;
		return null;
	};
	// Check if it is admin
	protected ApiFunction check_is_super_user = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( !currentUser.isSuperUser() ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
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
		apr = this.check_email.apply(req, res);
		if ( apr != null )
			return apr;
		apr = this.check_password.apply(req, res);
		if ( apr != null )
			return apr;
		return null;
	};
	// Check password complexity
	protected ApiFunction check_password = (req, res) -> {
		String passwordRegex = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,})", password = "";
		Pattern p = Pattern.compile(passwordRegex);
		String[] passwordList = new String[]{REQ_PASSWORD, REQ_NEW_PASSWORD, REQ_REPEAT_PASSWORD};
		for ( String password_string : passwordList ) {
			password = req.getString(password_string, "");
			Matcher m = p.matcher(password);
			if ( !password.isEmpty() && !m.matches()) {
				res.put(RES_ERROR, ERROR_PASSWORD_COMPLEXITY);
				return res;
			}
		}
		return null;
	};

	// Check email format
	protected ApiFunction check_email = (req, res) -> {
		String[] emailList = new String[]{REQ_EMAIL};
		for ( String email_string : emailList ) {
			String email = req.getString(email_string, "");
			if ( !email.isEmpty() && !isEmailFormat(email) ) {
				res.put(RES_ERROR, email_string+" is not a valid email.");
				return res;
			}
		}
		return null;
	};
	////////////////////////////////////////////////////////////////////////////
	/// Group Admin Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction group_admin_bundle_check = (req, res) -> {
		apr = this.check_is_super_user.apply(req, res);
		if ( apr != null )
			return apr;
		apr = this.check_is_current_user_group_admin.apply(req, res);
		if ( apr != null )
			return apr;
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
		apr = this.isLoggedIn.apply(req, res);
		if ( apr != null )
			return apr;
		apr = this.check_is_super_user.apply(req, res);
		if ( apr == null )
			return null;
		apr = this.check_is_current_user_group_admin.apply(req, res);
		if ( apr == null )
			return null;
		apr = this.check_is_current_user_member.apply(req, res);
		if ( apr != null )
			return apr;
		apr = this.check_is_editing_self.apply(req, res);
		if (apr != null )
			return apr;
		return null;
	};

	// Check if user is a member of the group
	protected ApiFunction check_is_current_user_member = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String groupID = req.getString(REQ_GROUP_ID, "");
		if ( groupID.isEmpty() ) {
			res.put(RES_ERROR, ERROR_NO_GROUP_ID);
			return res;
		}
		req.put(REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		return null;
	};

	// Check if current user is a group admin
	protected ApiFunction check_is_current_user_group_admin = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String groupID = req.getString(REQ_GROUP_ID, "");
		if ( groupID.isEmpty() ) {
			res.put(RES_ERROR, ERROR_NO_GROUP_ID);
			return res;
		}
		req.put(REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String role = res.getString(RES_SINGLE_RETURN_VALUE);
		if ( !role.equalsIgnoreCase("admin") ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
			return res;
		}
		return null;
	};

	// Check if current user is editing self
	protected ApiFunction check_is_editing_self = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String userID = req.getString(REQ_USER_ID, "");
		if ( !userID.equalsIgnoreCase(currentUser._oid()) ){
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
			return res;
		}
		return null;
	};

	/**
	* Does the actual setup for the API
	* Given the API Builder, and the namespace prefix
	*
	* @param  API builder to add the required functions
	* @param  Path to assume
	**/
	public void setupApiBuilder(ApiBuilder builder, String path) {
    super.setupApiBuilder(builder, path);
		builder.filter(path+"account/*", account_bundle_check);
		builder.filter(path+"account/login/*", complexity_bundle_check);
		builder.filter(path+"account/rancherRegister/*", complexity_bundle_check);
		builder.filter(path+"account/admin/*", admin_bundle_check);

		builder.filter(path+"account/group/*", group_bundle_check);
		builder.filter(path+"account/group/admin/*", group_admin_bundle_check);

	}


	// Private Methods

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
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		return null;
	};
}
