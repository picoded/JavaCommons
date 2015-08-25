package picoded.JStruct.internal;

/// Java imports
import java.util.*;

/// Picoded imports
import picoded.conv.*;
import picoded.struct.*;
import picoded.JStruct.*;
import picoded.enums.*;

/// Represents a single object node in the MetaTable collection.
///
/// This is intended, to handle local, delta, and remote data seperately.
/// Where its data layers can be visualized in the following order
///
/// + deltaDataMap                 - keeping track of local changes and removals
/// + remoteDataMap (incomplete)   - partial data map, used when only query data is needed
/// + remoteDataMap (complete)     - full data map, used when the incomplete map is insufficent
///
/// NOTE: This class should not be initialized directly, but through MetaTable class
public class JStruct_MetaObject implements MetaObject {
	
	// Core variables
	//----------------------------------------------
	
	/// MetaTable used for the object
	protected JStruct_MetaTable mainTable = null;
	
	/// GUID used for the object
	protected String _oid = null;
	
	/// Written changes, note that picoded.enums.ObjectTokens.NULL is used
	/// as a pesudo null value (remove)
	protected Map<String, Object> deltaDataMap = new HashMap<String, Object>();
	
	/// Used to indicate if the full remoteDataMap is given. 
	/// This is used when incomplete query data is given first
	protected boolean isCompleteRemoteDataMap = false;
	
	/// Local data cache
	protected Map<String, Object> remoteDataMap = null;
	
	// Constructor
	//----------------------------------------------
	
	/// Common setup function, across constructors
	protected void commonSetup(JStruct_MetaTable inTable, String inOID, Map<String, Object> inRemoteData, boolean isCompleteData) {
		mainTable = inTable;
	
		// Generates a GUID if not given
		if (inOID == null || inOID.length() < 22) {
			// Issue a GUID
			_oid = GUID.base58();
			
			// D= GUID collision check for LOLZ
			remoteDataMap = mainTable.metaObjectRemoteDataMap_get(_oid);
			if( remoteDataMap == null ) {
				remoteDataMap = new HashMap<String,Object>();
			}
			
			if(remoteDataMap.size() > 0 ) {
				if( remoteDataMap.size() == 1 && remoteDataMap.get("_oid") != null ) {
					// ignore if its just _oid
				} else {
					throw new RuntimeException("GUID Collision (╯°□°）╯︵ ┻━┻ : "+_oid);
				}
			}
			
			// Ensure oid is savable, needed to save blank objects
			deltaDataMap.put("_oid", _oid);
			
			isCompleteRemoteDataMap = true;
			
		} else {
			// _oid setup
			_oid = inOID;
			
			// Loading remote data map, only valid if _oid is given
			remoteDataMap = inRemoteData;
			if(remoteDataMap != null) {
				isCompleteRemoteDataMap = isCompleteData;
			}
		}
		
	}
	
	/// Constructor, with metaTable and GUID (auto generated if null)
	public JStruct_MetaObject(JStruct_MetaTable inTable, String inOID) {
		commonSetup(inTable, inOID, null, false);
	}
	
	/// Constructor, with metaTable and GUID (auto generated if null)
	public JStruct_MetaObject(JStruct_MetaTable inTable, String inOID, Map<String, Object> inRemoteData, boolean isCompleteData) {
		commonSetup(inTable, inOID, inRemoteData, isCompleteData);
	}
	
	// MetaObject ID
	//----------------------------------------------
	
	/// The object ID
	public String _oid() {
		return _oid;
	}
	
	// Utiltiy functions
	//----------------------------------------------
	
	/// Ensures the complete remote data map is loaded
	protected void ensureCompleteRemoteDataMap() {
		if( remoteDataMap == null || isCompleteRemoteDataMap == false ) {
			remoteDataMap = mainTable.metaObjectRemoteDataMap_get(_oid);
			isCompleteRemoteDataMap = true;
			
			if( remoteDataMap == null ) {
				remoteDataMap = new HashMap<String,Object>();
			}
		}
	}
	
	/// Collapse delta map to remote map
	protected void collapseDeltaToRemoteMap() {
		ensureCompleteRemoteDataMap();
		for( String key : deltaDataMap.keySet() ) {
			Object val = deltaDataMap.get(key);
			
			if( val == null || val == ObjectTokens.NULL ) {
				remoteDataMap.remove(key);
			} else {
				remoteDataMap.put( key, val );
			}
		}
		deltaDataMap = new HashMap<String, Object>();
	}
	
	/// Raw keyset, that is unfiltered
	protected Set<String> unfilteredForNullKeySet() {
		Set<String> unfilteredForNull = new HashSet<String>();
		
		ensureCompleteRemoteDataMap();
		unfilteredForNull.addAll( deltaDataMap.keySet() );
		unfilteredForNull.addAll( remoteDataMap.keySet() );
		
		return unfilteredForNull;
	}
	
	// Critical map functions
	//----------------------------------------------
	
	/// Gets and return its current value
	public Object get(Object key) {
		
		/// Get key operation
		if( key.toString().equalsIgnoreCase( "_oid") ) {
			return _oid;
		}
		
		Object ret = deltaDataMap.get(key);
		
		// Get from incomplete map
		if( ret == null && !isCompleteRemoteDataMap && remoteDataMap != null) {
			ret = remoteDataMap.get(key);
		}
		
		// Get from complete map
		if( ret == null ) {
			ensureCompleteRemoteDataMap();
			ret = remoteDataMap.get(key);
		}
		
		// Return null value
		if( ret == ObjectTokens.NULL ) {
			return null;
		}
		return ret;
	}
	
	/// Put and set its delta value, set null is considered "remove"
	public Object put(String key, Object value) {
		Object ret = get(key);
		
		if( value == null ) {
			deltaDataMap.put(key, ObjectTokens.NULL);
		} else {
			deltaDataMap.put(key, value);
		}
		
		return ret;
	}
	
	/// Remove operation
	public Object remove(Object key) {
		return put(key.toString(), null);
	}
	
	/// Gets and return valid keySet()
	public Set<String> keySet() {
		Set<String> unfilteredForNull = unfilteredForNullKeySet();
		Set<String> retSet = new HashSet<String>();
		
		for( String key : unfilteredForNull ) {
			if( key.equalsIgnoreCase("_oid") ) {
				continue;
			}
			
			if( get(key) != null ) {
				retSet.add(key);
			}
		}
		retSet.add("_oid");
		
		return retSet;
	} 
	
	// MetaObject save operations
	//----------------------------------------------
	
	/// Save the delta changes to storage
	public void saveDelta() {
		ensureCompleteRemoteDataMap();
		mainTable.metaObjectRemoteDataMap_update(_oid, this, deltaDataMap.keySet());
		collapseDeltaToRemoteMap();
	}
	
	/// Save all the configured data, ignore delta handling
	public void saveAll() {
		ensureCompleteRemoteDataMap();
		mainTable.metaObjectRemoteDataMap_update(_oid, this, unfilteredForNullKeySet());
		collapseDeltaToRemoteMap();
	}
	
	// To string operation : aids debugging
	//----------------------------------------------
	public String toString() {
		return ConvertJSON.fromMap(this);
	}
}
