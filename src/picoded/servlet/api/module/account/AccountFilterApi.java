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

import static picoded.servlet.api.module.account.Account_Strings.*;


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
		res.put(SV_IS_SUPERUSER, this.is_current_user_superuser.apply(req, res));

		return res;
	};

	////////////////////////////////////////////////////////////////////////////
	/// Account Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction account_bundle_check = (req, res) -> {
		res.put(SV_IS_CREATE_GROUP, this.is_create_group.apply(req, res));
		res.put(SV_IS_LOGGED_IN, this.is_user_logged_in.apply(req, res));
		// CHECK: Non logged in users trying to create GROUP accounts
		if ( res.getBoolean(SV_IS_CREATE_GROUP) &&
				 !res.getBoolean(SV_IS_LOGGED_IN) ) {
			res.put(RES_ERROR, ERROR_USER_NOT_LOGIN);
			return res;
	  }
		res.put(SV_IS_SELF, this.is_current_user_self.apply(req, res));
		// Non Superusers editing accounts
		if ( res.getBoolean(SV_IS_LOGGED_IN) &&
				 !res.getBoolean(SV_IS_SELF) &&
				 !res.getBoolean(SV_IS_SUPERUSER) &&
				 res.getBoolean(SV_IS_META) ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
	  }
		return res;
	};

	protected ApiFunction account_info_check = (req, res) -> {
		res.put(SV_IS_SUPERUSER, this.is_current_user_superuser.apply(req, res));
		res.put(SV_IS_USER_ID, this.is_user_id_exist.apply(req, res));
		res.put(SV_IS_USER_NAME, this.is_user_name_exist.apply(req, res));
		res.put(SV_IS_SELF, this.is_current_user_self.apply(req, res));
		// CHECK: Non Superusers trying to list other account information
		if ( (res.getBoolean(SV_IS_USER_ID) ||
		 			res.getBoolean(SV_IS_USER_NAME)) &&
				 (!res.getBoolean(SV_IS_SUPERUSER) ||
				 !res.getBoolean(SV_IS_SELF)) ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
		}
		return res;
	};

	protected ApiFunction account_password_check = (req, res) -> {
		res.put(SV_IS_SUPERUSER, this.is_current_user_superuser.apply(req, res));
		res.put(SV_IS_USER_ID, this.is_user_id_exist.apply(req, res));
		res.put(SV_IS_SELF, this.is_current_user_self.apply(req, res));
		res.put(SV_IS_PASSWORD_SATISFIED, this.is_password_satisfied.apply(req, res));
		// CHECK: Users trying to reset other people users password
		if ( !res.getBoolean(SV_IS_SUPERUSER) &&
				 !res.getBoolean(SV_IS_SELF) ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
			return res;
		}
		// CHECK: User's new passwords do not satisfy the complexity
		if ( !res.getBoolean(SV_IS_PASSWORD_SATISFIED) ) {
			res.put(RES_ERROR, ERROR_PASSWORD_COMPLEXITY);
		}
		return res;
	};

	// checking of password and email format
	protected ApiFunction complexity_bundle_check = (req, res) -> {
		res.put(SV_IS_PASSWORD_SATISFIED, this.is_password_satisfied.apply(req, res));
		res.put(SV_IS_EMAIL_SATISFIED, this.is_email_satisfied.apply(req, res));

		// CHECK: Password complexity and Email format
		if ( !res.getBoolean(SV_IS_EMAIL_SATISFIED) )
			res.put(RES_ERROR, ERROR_INVALID_FORMAT_EMAIL);
		if ( !res.getBoolean(SV_IS_PASSWORD_SATISFIED) )
			res.put(RES_ERROR,
							res.getString(RES_ERROR,"|")+
							ERROR_PASSWORD_COMPLEXITY);
		return res;
	};

	////////////////////////////////////////////////////////////////////////////
	/// Group Admin Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction group_admin_bundle_check = (req, res) -> {
		return res;
	};

	// Check if user has rights to CRUD group (user must be admin of the group)
	// protected ApiFunction check_crud = (req, res) -> {
	// };

	////////////////////////////////////////////////////////////////////////////
	/// Group Member Filtering
	////////////////////////////////////////////////////////////////////////////
	// Pack all checks into one ApiFunction for ease of usage as well as set up
	protected ApiFunction group_bundle_check = (req, res) -> {
		res.put(SV_IS_LOGGED_IN, this.is_user_logged_in.apply(req, res));
		res.put(SV_IS_SELF, this.is_current_user_self.apply(req, res));
		res.put(SV_IS_ADMIN, this.is_current_user_group_admin.apply(req, res));
		res.put(SV_IS_SUPERUSER, this.is_current_user_superuser.apply(req, res));
		res.put(SV_IS_MEMBER, this.is_current_user_a_member.apply(req, res));
		res.put(SV_IS_SELF_GROUP, this.is_current_user_self_group.apply(req, res));
		// CHECK: User is not logged in
		if ( !res.getBoolean(SV_IS_LOGGED_IN) ) {
			res.put(RES_ERROR, ERROR_USER_NOT_LOGIN);
			return res;
		}
		// CHECK: Non Superusers who are not members trying to perform group functions not to itself
		if ( !res.getBoolean(SV_IS_MEMBER) &&
				 !res.getBoolean(SV_IS_SUPERUSER) &&
				 !res.getBoolean(SV_IS_SELF_GROUP) &&
				 !res.getBoolean(SV_IS_SELF) ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
			return res;
		}
		// CHECK: Members trying to perform functions on other members
		if ( !res.getBoolean(SV_IS_ADMIN) &&
				 !res.getBoolean(SV_IS_SELF) &&
				 !res.getBoolean(SV_IS_SELF_GROUP) &&
				 !res.getBoolean(SV_IS_SUPERUSER) ) {
			res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
		}
		return res;
	};

	protected ApiFunction group_get_info_check = (req, res) -> {
		// CHECK: Non Superusers and non admin trying to access other people information
		if ( !res.getBoolean(SV_IS_SELF) ) {
			if ( !res.getBoolean(SV_IS_SELF_GROUP) &&
					 !res.getBoolean(SV_IS_SUPERUSER) &&
					 !res.getBoolean(SV_IS_ADMIN) ){
				res.put(RES_ERROR, ERROR_NO_PRIVILEGES);
	  		return res;
		  }
		}
		return res;
	};

	protected ApiFunction group_first_role_check = (req, res) -> {
		res = this.add_new_membership_role.apply(req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);
		AccountObject group = table.get(groupID);
		AccountObject firstAdmin = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( firstAdmin != null && !group.getMemberRole(firstAdmin).equalsIgnoreCase("admin") ) // Set the creator as the admin
			group.setMember(firstAdmin, "admin");
		return res;
	};
	/// Does the actual setup for the API
	/// Given the API Builder, and the namespace prefix
	///
	/// @param  API builder to add the required functions
	/// @param  Path to assume
	public void setupApiBuilder(ApiBuilder builder, String path) {
    super.setupApiBuilder(builder, path);
		builder.filter(path+"account/*", account_bundle_check);
		builder.filter(path+"account/*reset*/*", account_password_check);
		builder.filter(path+"account/account_info*", account_info_check);
		builder.filter(path+"account/new/*", complexity_bundle_check);
		builder.filter(path+"account/admin/*", admin_bundle_check);

		// builder.put(path+API_GROUP_ADMIN_ADD_MEM_ROLE, group_first_role_check);
		builder.filter(path+"group/*", group_bundle_check);
		builder.filter(path+"group/get*", group_get_info_check);
		builder.filter(path+"group/admin/*", group_admin_bundle_check);

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

	// Single Check Methods
	// Check if user is logged in
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_user_logged_in = (req, res) -> {
		AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao != null )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if current user is a group admin
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_current_user_group_admin = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( currentUser == null )
			return Boolean.FALSE;
		String originalUserID = req.getString(REQ_USER_ID);
		req.put(REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		String role = res.getString(RES_SINGLE_RETURN_VALUE, "");
		res.put(RES_ERROR, null);
		req.put(REQ_USER_ID, originalUserID);
		if ( role.equalsIgnoreCase("admin") )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if user is a member of the group
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_current_user_a_member = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( currentUser == null )
			return Boolean.FALSE;
		String originalUserID = req.getString(REQ_USER_ID);
		req.put(REQ_USER_ID, currentUser._oid());
		res = this.getMemberRoleFromGroup.apply(req, res);
		String role = res.getString(RES_SINGLE_RETURN_VALUE, "");
		res.put(RES_ERROR, null);
		req.put(REQ_USER_ID, originalUserID);
		if ( !role.isEmpty() )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if current user is editing self
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_current_user_self = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String userID = req.getString(REQ_USER_ID, "");
		if ( currentUser != null && userID.equalsIgnoreCase(currentUser._oid()) )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if current user is editing self group
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_current_user_self_group = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		String groupID = req.getString(REQ_GROUP_ID, "");
		if ( currentUser != null && groupID.equalsIgnoreCase(currentUser._oid()) )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if it is super user
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_current_user_superuser = (req, res) -> {
		AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
		if ( currentUser != null && currentUser.isSuperUser() )
			return Boolean.TRUE;
		return Boolean.FALSE;
	};

	// Check if it is creating a group
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_create_group = (req, res) -> {
		boolean isGroup = req.getBoolean(REQ_IS_GROUP, false);
		return isGroup;
	};

	// Check if userID exist
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_user_id_exist = (req, res) -> {
		return ( req.getString(REQ_USER_ID) != null ) ? Boolean.TRUE : Boolean.FALSE;
	};

	// Check if userName exist
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_user_name_exist = (req, res) -> {
		return ( req.getString(REQ_USERNAME) != null ) ? Boolean.TRUE : Boolean.FALSE;
	};

	// Check password complexity
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_password_satisfied = (req, res) -> {
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$", password = "";
		Pattern p = Pattern.compile(passwordRegex);
		String[] passwordList = new String[]{REQ_PASSWORD, REQ_NEW_PASSWORD, REQ_REPEAT_PASSWORD};
		for ( String password_string : passwordList ) {
			password = req.getString(password_string, "");
			Matcher m = p.matcher(password);
			if ( !password.isEmpty() && !m.matches()) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	};

	// Check email format
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_email_satisfied = (req, res) -> {
		String email = req.getString(REQ_USERNAME,"");
		boolean isGroup = req.getBoolean(REQ_IS_GROUP, false);
		if ( email.isEmpty() || (!isEmailFormat(email) && !isGroup) ) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	};

	// Check meta object exist
	private BiFunction<ApiRequest, ApiResponse, Boolean> is_meta_exist = (req, res) -> {
		Object metaObjRaw = req.get(REQ_META);
		return ( metaObjRaw == null ) ? Boolean.FALSE : Boolean.TRUE;
	};
}
