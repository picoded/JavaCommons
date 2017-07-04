package picoded.servlet.api.module.account;

public class Account_Strings{
	/**
	* Static REQUEST VARS
	**/
	public static final String REQ_OID = "_oid";
	public static final String REQ_ACCOUNT_ID = "accountID";
	public static final String REQ_ACCOUNT_NAME = "accountName";
	public static final String REQ_ADD_LIST = "addList";
	public static final String REQ_REMOVE_LIST = "removeList";
	public static final String REQ_PASSWORD = "password";
	public static final String REQ_USERNAME = "username";
	public static final String REQ_REMEMBER_ME = "rememberMe";
	public static final String REQ_SANITISE_OUTPUT = "sanitiseOutput";
	public static final String REQ_DEFAULT_ROLES = "defaultRoles";
	public static final String REQ_GROUPNAME = "groupname";
	public static final String REQ_GROUP_ID = "groupID";
	public static final String REQ_IS_GROUP = "isGroup";
	public static final String REQ_HEADERS = "headers";
	public static final String REQ_META = "meta";
	public static final String REQ_ROLE = "role";

	/**
	* Static RESPONSE VARS
	**/
	// Single Value/Object Variables
	public static final String RES_ERROR = "ERROR";
	public static final String RES_RETURN = "return";
	public static final String RES_ACCOUNT_ID = "accountID";
	public static final String RES_SINGLE_RETURN_VALUE = "single";
	public static final String RES_GROUP_ID= "groupID";
	public static final String RES_IS_LOGIN = "isLogin";
	public static final String RES_REMEMBER_ME = "rememberMe";
	public static final String RES_META = "meta";
	public static final String RES_DRAW = "draw";
	// Data List Return Variables
	public static final String RES_HEADERS= "headers";
	public static final String RES_DATA = "data";
	public static final String RES_LOGIN_ID_LIST = "loginIDList";
	public static final String RES_LIST = "list";
	public static final String RES_FAIL_REMOVE = "failToRemove";
	public static final String RES_SUCCESS_REMOVE = "succeedRemove";
	public static final String RES_FAIL_ADD = "failToAdd";
	public static final String RES_SUCCESS_ADD = "succeedAdd";

	/**
	* Static PROPERTIES VARS
	**/
	public static final String PROPERTIES_ROLE = "role";
	public static final String PROPERTIES_MEMBERSHIP_ROLE = "membershipRoles";
	public static final String PROPERTIES_EMAIL = "email";
	public static final String PROPERTIES_IS_GROUP = "isGroup";
	public static final String PROPERTIES_OID = "_oid";
	public static final String PROPERTIES_NAMES = "name";

	/**
	* ERROR MESSAGES
	**/
	public static final String ERROR_NO_GROUP = "No group is found.";
	public static final String ERROR_NO_GROUP_ID = "No groupID is found.";
	public static final String ERROR_NO_USERNAME = "No username is supplied.";
	public static final String ERROR_NO_PASSWORD = "No password is supplied.";
	public static final String ERROR_NO_GROUPNAME = "No groupname is found.";
	public static final String ERROR_NO_LOGIN_PASSWORD = "Missing login password.";
	public static final String ERROR_NO_LOGIN_ID = "Missing login ID.";
	public static final String ERROR_FAIL_LOGIN = "Failed login (wrong password or invalid user?).";
	public static final String ERROR_NO_ROLE = "No role is found.";
	public static final String ERROR_NO_USER = "No user is found.";
	public static final String ERROR_DEFAULT = "There is an error";

	/**
	* ADDITIONAL Variables
	**/
	public static final String SPACE = " ";
	public static final String GROUP = "group";

}
