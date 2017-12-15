package picoded.servlet.api.module.account;

import javax.mail.internet.*;

public class AccountConstantStrings {
	
	/// Common parameter names (standardised)
	public static final String ACCOUNT_ID = "accountID";
	public static final String LOGIN_NAME = "loginName";
	public static final String LOGIN_NAME_LIST = "loginNameList";
	
	/// Common error / info messages
	public static final String INFO_MISSING_LOGIN = "Missing login information (request is missing valid authentication)";
	
	//--------------------------------------------------------------------
	//
	// Everything below here is NOT yet approved
	//
	//--------------------------------------------------------------------
	
	/// Static REQUEST VARS
	public static final String OID = "_oid";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String ADD_LIST = "addList";
	public static final String REMOVE_LIST = "removeList";
	public static final String PASSWORD = "password";
	public static final String REMEMBER_ME = "rememberMe";
	public static final String SANITISE_OUTPUT = "sanitiseOutput";
	public static final String DEFAULT_ROLES = "defaultRoles";
	public static final String GROUPNAME = "groupname";
	public static final String GROUP_ID = "groupID";
	public static final String IS_GROUP = "isGroup";
	public static final String HEADERS = "headers";
	public static final String DATA = "data";
	public static final String ROLE = "role";
	public static final String UPDATE_MODE = "updateMode";
	public static final String OLD_PASSWORD = "oldPassword";
	public static final String REPEAT_PASSWORD = "repeatPassword";
	public static final String NEW_PASSWORD = "newPassword";
	public static final String NAME = "name";
	public static final String UPDATE = "update";
	public static final String TOKEN = "token";
	public static final String LOGINNAME = "loginName";
	public static final String LOGINNAMELIST = "loginNameList";
	public static final String USER_ID = "userID";
	
	// Rancher VARS
	public static final String EMAIL = "email";
	public static final String NODE_ID = "nodeID";
	public static final String AUTH_KEY = "authKey";
	public static final String ADMIN_PASS = "adminPass";
	
	/// Static RESPONSE VARS
	// Single Value/Object Variables
	public static final String RETURN = "return";
	public static final String SINGLE_RETURN_VALUE = "single";
	public static final String IS_LOGIN = "isLogin";
	public static final String DRAW = "draw";
	public static final String RECORDS_TOTAL = "recordsTotal";
	public static final String RECORDS_FILTERED = "recordsFiltered";
	public static final String SUCCESS = "success";
	// Data List Return Variables
	public static final String LIST = "list";
	public static final String FAIL_REMOVE = "failToRemove";
	public static final String SUCCESS_REMOVE = "succeedRemove";
	public static final String FAIL_ADD = "failToAdd";
	public static final String SUCCESS_ADD = "succeedAdd";
	
	/// Static PROPERTIES VARS
	public static final String PROPERTIES_ROLE = "role";
	public static final String PROPERTIES_MEMBERSHIP_ROLE = "membershipRoles";
	public static final String PROPERTIES_EMAIL = "email";
	public static final String PROPERTIES_IS_GROUP = "isGroup";
	public static final String PROPERTIES_OID = "_oid";
	public static final String PROPERTIES_NAME = "name";
	public static final String PROPERTIES_NODE_ID = "nodeID";
	public static final String PROPERTIES_ADMIN_PASS = "adminPass";
	public static final String PROPERTIES_STACK_NAME = "stackName";
	public static final String PROPERTIES_AUTH_KEY = "authKey";
	public static final String PROPERTIES_CREATE_STACK_TIMESTAMP = "createdStackTimeStamp";
	public static final String PROPERTIES_HOST_URL = "hostURL";
	public static final String PROPERTIES_STACK_ID = "stackID";
	
	/// ERROR MESSAGES
	public static final String ERROR_NO_GROUP = "No group is found.";
	public static final String ERROR_NO_GROUP_ID = "No groupID is found.";
	public static final String ERROR_NO_GROUPNAME = "No groupname is found.";
	public static final String ERROR_NOT_GROUP = "This is not a group.";
	public static final String ERROR_NO_LOGINNAME = "No loginname is found.";
	public static final String ERROR_NO_PASSWORD = "No password is found.";
	public static final String ERROR_NO_LOGIN_PASSWORD = "Missing login password.";
	public static final String ERROR_NO_LOGIN_ID = "Missing login ID.";
	public static final String ERROR_FAIL_LOGIN = "Invalid username or password.";
	public static final String ERROR_NO_ROLE = "No role is found.";
	public static final String ERROR_NO_USER = "No user is found.";
	public static final String ERROR_NO_USER_ID = "No userID is found.";
	public static final String ERROR_DEFAULT = " is found.";
	public static final String ERROR_NO_META = "No meta is found.";
	public static final String ERROR_NOT_IN_GROUP_OR_ROLE = "User is not in group or not in specified role.";
	public static final String ERROR_INVALID_FORMAT_JSON = "Invalid Format JSON";
	public static final String ERROR_PASS_NOT_EQUAL = "The passwords are not equal.";
	public static final String ERROR_NO_NEW_PASSWORD = "No new password found.";
	public static final String ERROR_NO_NEW_REPEAT_PASSWORD = "No new repeat password found.";
	public static final String ERROR_PASS_INCORRECT = "Old password is wrong.";
	public static final String ERROR_NO_PRIVILEGES = "User does not have privilege.";
	public static final String ERROR_PASSWORD_COMPLEXITY = "The password must contain at least 1 uppercase, 1 lowercase and 1 number with at least 6 characters long.";
	public static final String ERROR_INVALID_FORMAT_EMAIL = "Invalid email format.";
	public static final String ERROR_USER_NOT_LOGIN = "User is not logged in.";
	public static final String ERROR_NO_EMAIL = "No email address is found.";
	public static final String ERROR_NO_NODE_ID = "No node ID is found.";
	public static final String ERROR_EMAIL_EXISTS = "This email is in use.";
	public static final String ERROR_NO_NAME = "No name is found.";
	public static final String ERROR_NO_ACCOUNT_ID = "No account ID is found.";
	public static final String ERROR_LOGIN_NAME_EXISTS = "This username is in use.";
	public static final String ERROR_NO_DIFFERENT_EMAIL_ALLOW = "You cannot change the login name to a different email address.";
	public static final String ERROR_NO_EMAIL_LOGIN_NAME_FOUND = "One of the login names must contain the user's email.";
	/// ADDITIONAL Variables
	public static final String SPACE = " ";
	public static final String GROUP = "group";
	
	/// Check States Variables (FILTER API)
	public static final String SV_IS_SUPERUSER = "isSuperUser";
	public static final String SV_IS_ADMIN = "isAdmin";
	public static final String SV_IS_MEMBER = "isMember";
	public static final String SV_IS_SELF = "isSelf";
	public static final String SV_IS_SELF_GROUP = "isSelfGroup";
	public static final String SV_IS_LOGGED_IN = "isLoggedIn";
	public static final String SV_IS_CREATE_GROUP = "isCreateGroup";
	public static final String SV_IS_USER_ID = "isUserID";
	public static final String SV_IS_USER_NAME = "isUserName";
	public static final String SV_IS_PASSWORD_SATISFIED = "isPassFormat";
	public static final String SV_IS_EMAIL_SATISFIED = "isEmailFormat";
	public static final String SV_IS_META = "isMeta";
	
	/// API ENDPOINTS
	public static final String API_ACCOUNT_IS_LOGIN = "account/isLogin";
	public static final String API_ACCOUNT_LOGIN = "account/login";
	public static final String API_ACCOUNT_LOCKTIME = "account/lockTime";
	public static final String API_ACCOUNT_LOGOUT = "account/logout";
	public static final String API_ACCOUNT_NEW = "account/new";
	public static final String API_ACCOUNT_PASS_RESET = "account/password/reset";
	public static final String API_ACCOUNT_INFO = "account/info/get";
	public static final String API_ACCOUNT_ADMIN_REMOVE = "account/admin/remove";
	public static final String API_ACCOUNT_LIST = "account/info/list"; //otherwise there's a duplicate...
	public static final String API_ACCOUNT_SET_LOGIN_NAME = "account/info/loginname";
	
	public static final String API_GROUP_GRP_ROLES = "group/groupRoles";
	public static final String API_GROUP_GET_MEM_ROLE = "group/getMemberRole";
	public static final String API_GROUP_GET_LIST_GRP_ID_MEM = "group/getListOfGroupIDOfMember";
	public static final String API_GROUP_GET_LIST_GRP_OBJ_MEM = "group/getListOfGroupObjectOfMember";
	public static final String API_GROUP_GET_SINGLE_MEM_META = "group/get_single_member_meta";
	public static final String API_GROUP_UPDATE_MEM_META = "group/update_member_meta_info";
	public static final String API_GROUP_ADMIN_ADD_MEM_ROLE = "group/admin/addMembershipRole";
	public static final String API_GROUP_ADMIN_REM_MEM_ROLE = "group/admin/removeMembershipRole";
	public static final String API_GROUP_ADMIN_GET_MEM_LIST_INFO = "group/admin/get_member_list_info";
	public static final String API_GROUP_ADMIN_ADD_REM_MEM = "group/admin/add_remove_member";
	
	public static final String API_RANCHER_REGISTER = "account/rancherRegister";
	
	// Methods that shared across Account APIs
	// with help from http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
	public static boolean isEmailFormat(String inEmail) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(inEmail);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}
}
