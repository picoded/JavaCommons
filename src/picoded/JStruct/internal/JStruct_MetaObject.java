package picoded.JStruct.internal;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.conv.ConvertJSON;
/// Picoded imports
import picoded.conv.GUID;
import picoded.enums.ObjectTokens;

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
	// ----------------------------------------------
	
	// / MetaTable used for the object
	protected JStruct_MetaTable mainTable = null;
	
	// / GUID used for the object
	protected String _oid = null;
	
	// / Written changes, note that picoded.enums.ObjectTokens.NULL is used
	// / as a pesudo null value (remove)
	protected Map<String, Object> deltaDataMap = new HashMap<String, Object>();
	
	// / Used to indicate if the full remoteDataMap is given.
	// / This is used when incomplete query data is given first
	protected boolean isCompleteRemoteDataMap = false;
	
	// / Local data cache
	protected Map<String, Object> remoteDataMap = null;
	
	// Constructor
	// ----------------------------------------------
	
	// / Common setup function, across constructors
	protected void commonSetup(MetaTable inTable, String inOID, Map<String, Object> inRemoteData,
		boolean isCompleteData) {
		
		//		if (!(inTable instanceof JStruct_MetaTable)) {
		//			throw new RuntimeException("Requires MetaTable to be based off JStruct_MetaTable");
		//		}
		
		mainTable = (JStruct_MetaTable) inTable;
		
		// Generates a GUID if not given
		if (inOID == null || inOID.length() < 22) {
			// Issue a GUID
			if (_oid == null) {
				_oid = GUID.base58();
			}
			
			// D= GUID collision check for LOLZ
			remoteDataMap = mainTable.metaObjectRemoteDataMap_get(_oid);
			if (remoteDataMap == null) {
				remoteDataMap = new HashMap<String, Object>();
			}
			
			if (remoteDataMap.size() > 0) {
				if (remoteDataMap.size() == 1 && remoteDataMap.get("_oid") != null) {
					// ignore if its just _oid
				} else {
					// SERIOUSLY ?????
					throw new SecurityException("GUID Collision T_T Q_Q " + _oid);
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
			if (remoteDataMap != null) {
				isCompleteRemoteDataMap = isCompleteData;
			}
		}
		
	}
	
	// / Constructor, with metaTable and GUID (auto generated if null)
	public JStruct_MetaObject(MetaTable inTable, String inOID) {
		commonSetup(inTable, inOID, null, false);
	}
	
	// / Constructor, with metaTable and GUID (auto generated if null)
	public JStruct_MetaObject(MetaTable inTable, String inOID, Map<String, Object> inRemoteData,
		boolean isCompleteData) {
		commonSetup(inTable, inOID, inRemoteData, isCompleteData);
	}
	
	// MetaObject ID
	// ----------------------------------------------
	
	// / The object ID
	@Override
	public String _oid() {
		return _oid;
	}
	
	// Utiltiy functions
	// ----------------------------------------------
	
	// / Ensures the complete remote data map is loaded
	protected void ensureCompleteRemoteDataMap() {
		if (remoteDataMap == null || !isCompleteRemoteDataMap) {
			remoteDataMap = mainTable.metaObjectRemoteDataMap_get(_oid);
			isCompleteRemoteDataMap = true;
			
			if (remoteDataMap == null) {
				remoteDataMap = new HashMap<String, Object>();
			}
		}
	}
	
	// / Collapse delta map to remote map
	protected void collapseDeltaToRemoteMap() {
		ensureCompleteRemoteDataMap();
		for (Entry<String, Object> entry : deltaDataMap.entrySet()) {
			Object val = deltaDataMap.get(entry.getKey());
			if (val == null || val.equals(ObjectTokens.NULL)) {
				remoteDataMap.remove(entry.getKey());
			} else {
				remoteDataMap.put(entry.getKey(), val);
			}
		}
		deltaDataMap = new HashMap<String, Object>();
	}
	
	// / Raw keyset, that is unfiltered
	protected Set<String> unfilteredForNullKeySet() {
		Set<String> unfilteredForNull = new HashSet<String>();
		
		ensureCompleteRemoteDataMap();
		unfilteredForNull.addAll(deltaDataMap.keySet());
		unfilteredForNull.addAll(remoteDataMap.keySet());
		
		return unfilteredForNull;
	}
	
	// / The aggressive type conversion to int / double types
	// /
	// / @params The input object to convert
	// /
	// / @returns The converted Integer or Double object, else its default value
	protected Object agressiveNumericConversion(Object value) {
		if (value == null) {
			return value;
		}
		
		// Ignore byte[] array type conversion
		if (value instanceof byte[]) {
			return value;
		}
		
		Object ret = null;
		String strValue = value.toString();
		try {
			
			ret = Integer.valueOf(strValue);
		} catch (Exception e) {
			// Silent ignore
		}
		
		try {
			if (ret == null) {
				ret = Long.parseLong(strValue);
			}
		} catch (Exception e) {
			// Silent ignore
		}
		
		try {
			
			if (ret == null) {
				ret = Double.parseDouble(strValue);
			}
		} catch (Exception e) {
			// Silent ignore
		}
		if (ret != null && ret.toString().equals(strValue)) {
			return ret;
		}
		return value;
	}
	
	// Critical map functions
	// ----------------------------------------------
	
	// / Gets and return its current value
	@Override
	public Object get(Object key) {
		
		// / Get key operation
		if (key.toString().equalsIgnoreCase("_oid")) {
			return _oid;
		}
		
		Object ret = deltaDataMap.get(key);
		
		// Get from incomplete map
		if (ret == null && isCompleteRemoteDataMap && remoteDataMap != null) {
			ret = remoteDataMap.get(key);
		}
		
		// Get from complete map
		if (ret == null) {
			ensureCompleteRemoteDataMap();
			ret = remoteDataMap.get(key);
		}
		
		// Return null value
		if (ret != null && ret.equals(ObjectTokens.NULL)) {
			return ret;
		}
		return ret;
	}
	
	// / Put and set its delta value, set null is considered "remove"
	@Override
	public Object put(String key, Object value) {
		Object ret = get(key);
		
		// Object token null, cleared as null
		if (ret != null && ret.equals(ObjectTokens.NULL)) {
			ret = null;
		}
		
		// Aggressive numeric conversion
		value = agressiveNumericConversion(value);
		
		// If no values are changed, ignore delta
		if (value != null && value.equals(ret)) {
			return ret;
		}
		
		// Value comparision check, ignore if no change
		if (value != null && ret != null && value.getClass() == ret.getClass()) {
			return ret;
		}
		if (value == null) {
			deltaDataMap.put(key, ObjectTokens.NULL);
		} else {
			deltaDataMap.put(key, value);
		}
		
		return ret;
	}
	
	// / Remove operation
	@Override
	public Object remove(Object key) {
		return put(key.toString(), null);
	}
	
	// / Gets and return valid keySet()
	@Override
	public Set<String> keySet() {
		Set<String> unfilteredForNull = unfilteredForNullKeySet();
		Set<String> retSet = new HashSet<String>();
		
		for (String key : unfilteredForNull) {
			if (key.equalsIgnoreCase("_oid")) {
				continue;
			}
			
			if (get(key) != null && !get(key).equals(ObjectTokens.NULL)) {
				retSet.add(key);
			}
		}
		retSet.add("_oid");
		
		return retSet;
	}
	
	// MetaObject save operations
	// ----------------------------------------------
	
	// / Save the delta changes to storage
	@Override
	public void saveDelta() {
		ensureCompleteRemoteDataMap();
		mainTable.metaObjectRemoteDataMap_update(_oid, this, deltaDataMap.keySet());
		collapseDeltaToRemoteMap();
	}
	
	// / Save all the configured data, ignore delta handling
	@Override
	public void saveAll() {
		ensureCompleteRemoteDataMap();
		
		Set<String> keySet = new HashSet<String>(deltaDataMap.keySet());
		keySet.addAll(remoteDataMap.keySet());
		mainTable.metaObjectRemoteDataMap_update(_oid, this, keySet);
		
		collapseDeltaToRemoteMap();
		unfilteredForNullKeySet();
	}
	
	// To string operation : aids debugging
	// ----------------------------------------------
	@Override
	public String toString() {
		return ConvertJSON.fromMap(this);
	}
}
