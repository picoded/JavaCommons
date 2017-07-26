package picoded.servlet.api.module.account;

public class Account_Strings{

  	/// Static REQUEST VARS
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
    public static final String REQ_USER_ID = "userID";
    public static final String REQ_UPDATE_MODE = "updateMode";
    public static final String REQ_OLD_PASSWORD = "oldPass";
    public static final String REQ_REPEAT_PASSWORD = "repeatPass";
    public static final String REQ_NEW_PASSWORD = "newPass";

    /// Static RESPONSE VARS
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
    public static final String RES_RECORDS_TOTAL = "recordsTotal";
    public static final String RES_RECORDS_FILTERED = "recordsFiltered";
    public static final String RES_UPDATE_MODE = "updateMode";
    public static final String RES_SUCCESS = "success";
    // Data List Return Variables
    public static final String RES_HEADERS= "headers";
    public static final String RES_DATA = "data";
    public static final String RES_LOGIN_ID_LIST = "loginIDList";
    public static final String RES_LIST = "list";
    public static final String RES_FAIL_REMOVE = "failToRemove";
    public static final String RES_SUCCESS_REMOVE = "succeedRemove";
    public static final String RES_FAIL_ADD = "failToAdd";
    public static final String RES_SUCCESS_ADD = "succeedAdd";

    /// Static PROPERTIES VARS
    public static final String PROPERTIES_ROLE = "role";
    public static final String PROPERTIES_MEMBERSHIP_ROLE = "membershipRoles";
    public static final String PROPERTIES_EMAIL = "email";
    public static final String PROPERTIES_IS_GROUP = "isGroup";
    public static final String PROPERTIES_OID = "_oid";
    public static final String PROPERTIES_NAMES = "name";

    /// ERROR MESSAGES
    public static final String ERROR_NO_GROUP = "No group is found.";
    public static final String ERROR_NO_GROUP_ID = "No groupID is found.";
    public static final String ERROR_NO_GROUPNAME = "No groupname is found.";
    public static final String ERROR_NOT_GROUP = "This is not a group.";
    public static final String ERROR_NO_USERNAME = "No username is supplied.";
    public static final String ERROR_NO_PASSWORD = "No password is supplied.";
    public static final String ERROR_NO_LOGIN_PASSWORD = "Missing login password.";
    public static final String ERROR_NO_LOGIN_ID = "Missing login ID.";
    public static final String ERROR_FAIL_LOGIN = "Failed login (wrong password or invalid user?).";
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
    public static final String ERROR_PASSWORD_COMPLEXITY = "The password must contain at least 1 uppercase, 1 lowercase and 1 number.";
    public static final String ERROR_INVALID_FORMAT_EMAIL = "The username is not an email.";
    public static final String ERROR_USER_NOT_LOGIN = "User is not logged in.";

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
    public static final String API_ACCOUNT_PASS_RESET = "account/do_password_reset";
    public static final String API_ACCOUNT_INFO_NAME = "account/account_info_by_Name";
    public static final String API_ACCOUNT_INFO_ID = "account/account_info_by_ID";
    public static final String API_ACCOUNT_ADMIN_REMOVE = "account/admin/remove";
    public static final String API_ACCOUNT_ADMIN_GET_U_G_LIST = "account/admin/get_user_or_group_list";
    public static final String API_ACCOUNT_UPDATE_U_INFO = "account/update_current_user_info";

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

}
