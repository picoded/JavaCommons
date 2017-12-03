package picoded.servlet.api.module.account;

import java.util.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.dstack.module.account.*;
import picoded.servlet.api.module.dstack.*;
import picoded.dstack.*;
import picoded.core.conv.ConvertJSON;
import picoded.core.conv.StringEscape;
import picoded.core.conv.GenericConvert;
import java.util.function.BiFunction;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;
import picoded.core.common.SystemSetupInterface;

/**
 * Account table group API
 **/
public class AccountGroupApi extends CommonApiModule {
	
	/**
	 * The AccountTable reference
	 **/
	protected AccountTable table = null;
	
	/**
	 * The DataTableApi reference
	 **/
	protected DataTableApi dataTableApi = null;
	
	/**
	 * Static ERROR MESSAGES
	 **/
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	
	/**
	 * Setup the account login API
	 *
	 * @param  AccountTable to use
	 **/
	public AccountGroupApi(AccountTable inTable) {
		table = inTable;
		dataTableApi = new DataTableApi(inTable.accountDataTable());
	}
	
	/**
	 * Internal subsystem array, used to chain up setup commands
	 *
	 * @TODO : Chain up accountTable internalSubsystemArray to its internal objects
	 *
	 * @return  Array containing the AccountTable used
	 */
	protected SystemSetupInterface[] internalSubsystemArray() {
		return new SystemSetupInterface[] {};
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   DataTable info proxy
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	private static String list_GET_and_POST_inner(
		int draw, int start,
	int length, String[] headers, String query, String[] queryArgs, String orderBy,
	String[] insideGroup_any, String[] hasGroupRole_any, String groupStatus,
	boolean sanitiseOutput, boolean asObject) throws RuntimeException {
	
	List<Object> ret = new ArrayList<Object>();
	
	if (_DataTableObj == null) {
		return ConvertJSON.fromObject(ret);
	}
	
	try {
		if (headers != null && headers.length > 0) {
			DataObject[] metaObjs = null;
			AccountObject[] fullUserArray = null;
			
			if ((insideGroup_any == null || insideGroup_any.length == 0)
				&& (hasGroupRole_any == null || hasGroupRole_any.length == 0)) {
				//do normal query
				DataTable accountDataTable = _DataTableObj.accountDataTable();
				
				if (accountDataTable == null) {
					return ConvertJSON.fromObject(ret);
				}
				
				if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
					metaObjs = accountDataTable.query(null, null, orderBy, start, length);
				} else {
					metaObjs = accountDataTable.query(query, queryArgs, orderBy, start, length);
				}
				
				List<AccountObject> retUsers = new ArrayList<AccountObject>();
				for (DataObject metaObj : metaObjs) {
					AccountObject ao = _DataTableObj.get(metaObj._oid()); //a single account
					// System.out.println(ao._oid()+" ahwejakwekawej<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
					retUsers.add(ao);
				}
				
				fullUserArray = retUsers.toArray(new AccountObject[retUsers.size()]);
			} else {
				//do filtered query
				fullUserArray = _DataTableObj.getUsersByGroupAndRole(insideGroup_any,
					hasGroupRole_any);
			}
			
			if (fullUserArray == null || fullUserArray.length == 0) {
				return ConvertJSON.fromObject(ret);
			}
			
			//group status filtering
			if (groupStatus != null) {
				List<AccountObject> filteredUsers = new ArrayList<AccountObject>();
				for (AccountObject ao : fullUserArray) {
					if (groupStatus.equalsIgnoreCase("group")) {
						if (ao.isGroup()) {
							filteredUsers.add(ao);
						}
					} else if (groupStatus.equalsIgnoreCase("user")) {
						if (!ao.isGroup()) {
							filteredUsers.add(ao);
						}
					}
				}
				
				fullUserArray = filteredUsers.toArray(new AccountObject[filteredUsers.size()]);
			}
			
			for (AccountObject ao : fullUserArray) {
				List<Object> rowAsList = new ArrayList<Object>();
				Map<String, Object> rowAsObject = new HashMap<String, Object>();
				for (String header : headers) {
					
					if (header.equalsIgnoreCase("names")) {
						if (ao != null) {
							Set<String> aoNames = ao.getLoginNameSet();
							
							if (sanitiseOutput) {
								aoNames.clear();
								for (String name : ao.getLoginNameSet()) {
									aoNames.add(StringEscape.commonHtmlEscapeCharacters(name));
								}
							}
							
							if (aoNames != null) {
								List<String> aoNameList = new ArrayList<String>(aoNames);
								if (asObject)
									rowAsObject.put(header, aoNameList);
								else
									rowAsList.add(aoNameList);
							}
						}
					} else {
						Object rawVal = ao.get(header); //this used to be metaObj.get
						if (header.equalsIgnoreCase("_oid"))
							header = "id";
						if (sanitiseOutput && rawVal instanceof String) {
							String stringVal = GenericConvert.toString(rawVal);
							if (asObject)
								rowAsObject.put(header, stringVal);
							else
								rowAsList.add(stringVal);
						} else {
							if (asObject)
								rowAsObject.put(header, rawVal);
							else
								rowAsList.add(rawVal);
						}
						
					}
				}
				if (asObject)
					ret.add(rowAsObject);
				else
					ret.add(rowAsList);
			}
		}
	} catch (Exception e) {
		throw new RuntimeException("list_GET_and_POST_inner() ", e);
	}
	
	return ConvertJSON.fromObject(ret);
	}
	 */
	
}