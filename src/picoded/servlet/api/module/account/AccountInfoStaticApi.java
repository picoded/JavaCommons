package picoded.servlet.api.module.account;

import java.util.*;
import java.util.function.BiFunction;

import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.dmodule.account.*;
import picoded.servlet.api.module.dstack.*;
import picoded.dstack.*;
import picoded.core.conv.ConvertJSON;
import picoded.core.conv.StringEscape;
import picoded.core.conv.GenericConvert;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.MutablePair;
import picoded.core.struct.query.OrderBy;
import picoded.core.struct.query.Query;
import picoded.core.struct.GenericConvertHashMap;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;
import picoded.core.common.SystemSetupInterface;

/**
 * Account table group API as static funcitons
 **/
public class AccountInfoStaticApi {
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   AccountTable list vairent
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Account specific varient of the listing API.
	 * 
	 * Note that this is not as well optimized as standard listing query,
	 * As non-trival amount of additional resources is used to validate all results
	 * from the query and search filter. For the account type / insideGroupAny / hasGroupRoleAny
	 * query options.
	 * 
	 * A longer term "optimization" is to seperate out an "ownership" datatable module.
	 * This is relevent for other forms of "group-like" ownership.
	 * 
	 * @param  res        result object map to populate 'result' and return
	 * @param  aTable     AccountTable object to use
	 * 
	 * @param  fieldList  field list to return in the result
	 * @param  query      Requested Query filter
	 * @param  queryArgs  Requested Query filter arguments
	 * 
	 * @param  searchString     Search string to use
	 * @param  searchFieldList  List of fields to search
	 * @param  searchMode       Determines SQL query wildcard position, for the first word
	 *                          (Either prefix, suffix, or both), second word onwards always uses both
	 * 
	 * @param  insideGroupAny   Return results only for those inside the given group id's / name
	 * @param  hasGroupRoleAny  Return result for only those inside any of the group, with the specified role.
	 * 
	 * @param  start      Record start listing, 0-indexed
	 * @param  length     Number of records to return
	 * @param  orderBy    Result ordering to use
	 * @param  rowMode    result array row format, use either "array" or "object"
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | recordsFiltered | int                | Total amount of records, matching the query, type, group, role, and search    |
	 * | recordsTotal    | int (not critical) | Total amount of records, matching the query, type, group, role                |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | Array[Obj/Array]   | Array of row records, each row is represented as an array                     |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T list(T res, AccountTable aTable,
		String[] fieldList, String query, Object[] queryArgs, String searchString,
		String[] searchFieldList, String searchMode, 
		String[] insideGroupAny, String[] hasGroupRoleAny,
		int start, int length, String orderBy,
		String rowMode) {
		
		// No special group based filter, does standard data table query
		if( (insideGroupAny == null || insideGroupAny.length == 0) &&
			(hasGroupRoleAny == null || hasGroupRoleAny.length == 0)
		) {
			return DataTableStaticApi.list(res, aTable.accountDataTable(), fieldList, query, queryArgs, searchString, searchFieldList, searchMode, start, length, orderBy, rowMode);
		}
		
		// Does an initial query
		String[] oidList = aTable.accountDataTable().query_id(query, queryArgs, null);

		// Then filter it down by the insideGroup, and role
		//
		// @TODO : Deprecate this funciton
		AccountObject[] filteredAccounts = null; //aTable.filterUsersByGroupAndRole( oidList, insideGroupAny, hasGroupRoleAny );

		// Get the record total
		res.put("recordsTotal", filteredAccounts.length);

		// Time to build actual query, and search it down
		MutablePair<String, Object[]> queryPair = DataTableStaticApi.collapseSearchStringAndQuery(query, queryArgs,
		searchString, searchFieldList, searchMode, "AND");
		
		// Prepare the query, and order by filter
		Query filter = Query.build(queryPair.getLeft(), queryPair.getRight());
		OrderBy<AccountObject> order = new OrderBy<AccountObject>(orderBy);

		// Execute the query
		List<AccountObject> queriedList = Arrays.asList(filteredAccounts);
		List<AccountObject> filteredAndOrdered = filter.search(queriedList, order);

		// Get the filtered length
		res.put("recordsFiltered", filteredAndOrdered.size());

		// Actual list
		List<AccountObject> actualList = new ArrayList<AccountObject>();
		
		// Gets the subset list (using start and length)
		int maxIdx = start + length;
		for( int idx = start; idx < maxIdx; ++idx ) {
			actualList.add( filteredAndOrdered.get(idx) );
		}

		// Prepare the result and returns it
		res.put("result", DataTableStaticApi.formatDataObjectList(actualList.toArray(new AccountObject[] {}), fieldList, rowMode));
		return res;
	}

}