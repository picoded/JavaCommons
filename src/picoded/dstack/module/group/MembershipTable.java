package picoded.dstack.module.group;

import java.util.*;
import java.util.function.BiFunction;

import picoded.dstack.*;
import picoded.dstack.module.*;
import picoded.core.conv.*;
import picoded.core.struct.*;
import org.apache.commons.lang3.ArrayUtils;

import static picoded.servlet.api.module.account.AccountConstantStrings.*;

/**
 * MembershipTable used to provide group ownership based functionality,
 * against existing data table compatible backend.
 **/
public class MembershipTable extends ModuleStructure implements DataTable {
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Underlying data structures
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Handles the storage of group data.
	 * 
	 * Note: Consider this the "PRIMARY TABLE"
	 *
	 * DataTable<GroupOID, DataObject>
	 **/
	protected DataTable groupTable = null;
	
	/**
	 * Member DataTable, to link. Note that this is designed,
	 * intentionally to work with DataTable compatible interface.
	 *
	 * DataTable<AccountOID, DataObject>
	 **/
	protected DataTable memberTable = null;
	
	/**
	 * Handles the storage of group to partipant link.
	 *
	 * DataTable<MembershipOID, DataObject>
	 **/
	protected DataTable membershipTable = null;
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Constructor setup : Setup the actual tables, with the various names
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Blank constructor, used for more custom extensions
	 **/
	public MembershipTable() {
		super();
	}

	/**
	 * MembershipTable constructor using DataTable
	 * 
	 * @param  inGroup         group table to build / extend from
	 * @param  inMember        partipant table to build / extend from
	 * @param  membership      primary membership table to link the group / partipant table
	 **/
	public MembershipTable(
		DataTable inGroup,
		DataTable inMember,
		DataTable membership
	) {
		super();
		groupTable       = inGroup;
		memberTable      = inMember;
		membershipTable  = membership;
	}
	
	//----------------------------------------------------------------
	//
	//  Internal CommonStructure management
	//
	//----------------------------------------------------------------
	
	/**
	 * Setup the list of local CommonStructure's
	 * this is used internally by setup/destroy/maintenance
	 **/
	protected List<CommonStructure> setupInternalStructureList() {
		// Check for missing items
		if( groupTable == null || memberTable == null || membershipTable == null ) {
			throw new RuntimeException("Missing group/member/membership table");
		}
		// Return it as a list
		return Arrays.asList( groupTable, memberTable, membershipTable );
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Membership based utility functions
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Validate group and member ID, throws runtime exception on failure
	 * 
	 * @param  groupID   group id to fetch from
	 * @param  memberID  member id to fetch from
	 */
	protected void validateMembership(String groupID, String memberID) {
		if( !groupTable.containsKey(groupID) ) {
			cleanupMembership(groupID, null);
			throw new RuntimeException("Missing/Invalid groupID : "+groupID);
		}
		if( !memberTable.containsKey(memberID) ) {
			cleanupMembership(null, memberID);
			throw new RuntimeException("Missing/Invalid memberID : "+memberID);
		}
	}

	/**
	 * Clean up membership objects, if the parent objects orphaned them
	 * 
	 * @param  groupID   group id to remove, effectively ignored if null
	 * @param  memberID  member id to remove, effectively ignored if null
	 */
	protected void cleanupMembership(String groupID, String memberID) {
		String[] ids = membershipTable.query_id("groupid=? OR memberid=?", new Object[] { groupID, memberID }, null);
		if( ids != null && ids.length > 0 ) {
			// Detecting more then one object match, remove obseleted items
			for(int i=0; i<ids.length; ++i) {
				membershipTable.remove(ids[i]);
			}
		}
	}

	/**
	 * Gets and return the membership object, only if it exists.
	 * 
	 * @param  groupID   group id to fetch from
	 * @param  memberID  member id to fetch from
	 * 
	 * @return  membership object ID
	 */
	protected String getMembershipID(String groupID, String memberID) {
		// Basic id validation
		validateMembership(groupID, memberID);

		// @CONSIDER : Adding key-value map caching layer to optimize group/memberid to membership-id
		// @CONSIDER : Collision removal checking by timestamp, where oldest wins
		String[] ids = membershipTable.query_id("groupid=? AND memberid=?", new Object[] { groupID, memberID }, "DESC _oid");
		if( ids != null && ids.length > 0 ) {
			// Detecting more then one object match, remove collision
			if( ids.length > 1 ) {
				for(int i=1; i<ids.length; ++i) {
					membershipTable.remove(ids[i]);
				}
			}
			return ids[0];
		}
		return null;
	}

	/**
	 * Utility function to cast membership DataObject to MembershipObject
	 * 
	 * @param  objList  array of data objects to cast
	 * 
	 * @return MembershipObject array
	 */
	protected MembershipObject[] castFromDataObject(DataObject[] objList) {
		if(objList == null) {
			return null;
		}

		MembershipObject[] ret = new MembershipObject[ objList.length ];
		for( int i=0; i<objList.length; ++i ) {
			ret[i] = new MembershipObject(this, objList[i]._oid());
		}
		return ret;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Basic add / get / remove membership
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Gets and return the membership object, only if it exists.
	 * 
	 * @param  groupID   group id to fetch from
	 * @param  memberID  member id to fetch from
	 * 
	 * @return  membership object (if found)
	 */
	public MembershipObject getMembership(String groupID, String memberID) {
		String id = getMembershipID(groupID, memberID);
		if( id != null ) {
			return new MembershipObject(this, id);
		}
		return null;
	}

	/**
	 * Add membership object, only if doesnt previously exists
	 * Else fetches the membership object.
	 * 
	 * @param  groupID   group id to fetch from
	 * @param  memberID  member id to fetch from
	 * 
	 * @return  membership object (if created / existed)
	 */
	public MembershipObject addMembership(String groupID, String memberID) {
		String id = getMembershipID(groupID, memberID);
		// Create and save a new membership object 
		// if it does not exists
		if( id == null ) {
			DataObject obj = membershipTable.newEntry();
			obj.put("groupid", groupID);
			obj.put("memberid", groupID);
			obj.saveAll();
		}
		// Get the newly saved membership object
		return getMembership(groupID, memberID);
	}

	/**
	 * Remove membership object, only if doesnt previously exists
	 * Else fetches the membership object.
	 * 
	 * @param  groupID   group id to fetch from
	 * @param  memberID  member id to fetch from
	 * 
	 * @return  membership object (if created / existed)
	 */
	public void removeMembership(String groupID, String memberID) {
		String id = getMembershipID(groupID, memberID);
		if( id != null ) {
			membershipTable.remove(id);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// basic Membership listing
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the list of membership given a group
	 * 
	 * @param  groupID   group id to fetch from
	 * 
	 * @return  list of relevent members
	 */
	public MembershipObject[] listMembership_fromGroup(String groupID) {
		return castFromDataObject ( groupTable.query("groupid=?", new Object[] { groupID }) );
	}

	/**
	 * Gets the list of membership given a member
	 * 
	 * @param  memberID  member id to fetch from
	 * 
	 * @return  list of relevent members
	 */
	public MembershipObject[] listMembership_fromMember(String memberID) {
		return castFromDataObject ( groupTable.query("memberid=?", new Object[] { memberID }) );
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Datatable based query
	//
	// @TODO : Refactor this out to a core.struct.query.AbstractMapMapQuery
	//
	///////////////////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////////////////////////
	// //
	// // Universal object listing =/
	// //
	// ///////////////////////////////////////////////////////////////////////////

	// /**
	//  * Does a rather complex multi-query, across group, member, and membership.
	//  * And return the chosen object type. 
	//  * 
	//  * For query / args pairs that are null, they are effectively wildcards.
	//  * 
	//  * @param  groupQuery        group based query
	//  * @param  groupArgs         group based query args
	//  * @param  memberQuery       member based query
	//  * @param  memberArgs        member based query args
	//  * @param  membershipQuery   membership based query
	//  * @param  membershipArgs    membership based query args
	//  * 
	//  * @return list of memberships, after all the various queries
	//  */
	// public List<MembershipObject> multiQuery(
	// 	String groupQuery,      Object[] groupArgs,
	// 	String memberQuery,     Object[] memberArgs,
	// 	String membershipQuery, Object[] membershipArgs
	// ) {
	// 	// Note that the query route changes 
	// 	// According to the parameters provided
	// 	// Under currently best guess assumptions
	// 	//
	// 	// @CONSIDER : Optimizing the query based on real usage
	// 	//             or alternatively with a hint parameter
	// 	if( groupQuery != null && groupArgs != null ) {
	// 		// If group query is provided, query will be executed
	// 		// in the following sequence, skipping if needed.
	// 		//
	// 		// 1. groupQuery
	// 		// 2. membershipQuery
	// 		// 3. memberQuery
	// 	} 
	// 	// DataObject[] groupList = null;
	// 	// DataObject[] memberList = null;
	// 	// // Build using group list first, if query is given
	// 	// if( groupQuery != null && groupArgs != null ) {
	// 	// 	groupList = groupTable.query(groupQuery, groupArgs);
	// 	// }
	// 	// // Build from member list
	// 	// if( memberList != null && memberArgs != null ) {
	// 	// 	// Group list is null, so use member query directly
	// 	// 	if( groupList == null ) {
	// 	// 	}
	// 	// }
	// 	return null;
	// }
}
