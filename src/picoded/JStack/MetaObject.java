package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;

/// Picoded imports
import picoded.conv.GUID;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.GenericConvertMap;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// Represents a single object node in the MetaTable collection.
///
/// NOTE: This class should not be initialized directly, but through MetaTable class
public class MetaObject extends CaseInsensitiveHashMap<String, Object> implements GenericConvertMap<String, Object> {
	
	/// Java serialversion uid: http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
	private static final long serialVersionUID = 42L;
	
	// Core variables
	//----------------------------------------------
	
	/// MetaTable used for the object
	protected MetaTable mTable = null;
	
	/// GUID used for the object
	protected String _oid = null;
	
	/// Written changes
	protected Map<String, Object> deltaDataMap = new CaseInsensitiveHashMap<String, Object>();
	
	/// Local data cache
	protected Map<String, Object> remoteDataMap = null;
	
	/// Query data cache
	protected Map<String, Object> queryDataMap = null;
	
	/// Boolean indicating if there was a data change, and the "combined storage" needed to be updated
	protected boolean combinedNeedsUpdate = true;
	
	// Constructor
	//----------------------------------------------
	
	/// Common setup function, across constructors
	protected void commonSetup(MetaTable inTable, String inOID, Map<String, Object> inRemoteData) {
		mTable = inTable;
		_oid = inOID;
		
		// Generates a GUID if not given
		if (_oid == null || _oid.length() < 22) {
			_oid = GUID.base58();
		}
		
		// Local data map
		remoteDataMap = inRemoteData;
		
		// Creates the combined map
		combinedMap();
	}
	
	/// Constructor, with metaTable and GUID (auto generated if null)
	protected MetaObject(MetaTable inTable, String inOID) {
		commonSetup(inTable, inOID, null);
	}
	
	/// Constructor, with metaTable and GUID (auto generated if null)
	protected MetaObject(MetaTable inTable, String inOID, Map<String, Object> inRemoteData) {
		commonSetup(inTable, inOID, inRemoteData);
	}
	
	// MetaObject ID
	//----------------------------------------------
	
	/// The object ID
	public String _oid() {
		return _oid;
	}
	
	// Utiltiy functions
	//----------------------------------------------
	
	/// Lazy load the current map
	protected void lazyLoad() {
		try {
			if (remoteDataMap == null) {
				remoteDataMap = mTable.lazyLoadGet(_oid);
			}
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
		if (remoteDataMap == null) {
			remoteDataMap = new CaseInsensitiveHashMap<String, Object>();
		}
	}
	
	/// Checks if the combinedNeedsUpdate flag is set, updating and returning it
	protected CaseInsensitiveHashMap<String, Object> combinedMap() {
		if (combinedNeedsUpdate) {
			super.clear();
			
			// Lazy load the object
			if (remoteDataMap == null) {
				lazyLoad();
			}
			
			super.putAll(remoteDataMap);
			
			super.putAll(deltaDataMap);
		}
		combinedNeedsUpdate = false;
		return this;
	}
	
	// Map functions
	//----------------------------------------------
	
	/// Associates the specified value with the specified key in this map.
	/// If the map previously contained a mapping for the key, the old value is replaced.
	///
	/// @TODO: Optimize the combineMap(), to trigger only on iterative gets
	///
	/// @param   key     key string with which the specified value is to be associated
	/// @param   value   value to be associated with the specified key
	///
	/// @returns  the previous value associated with key, or null if there was no mapping for key.
	///           (A null return can also indicate that the map previously associated null with key)
	@SuppressWarnings("unchecked")
	@Override
	public Object put(String key, Object value) {
		Object ori = get(key);
		deltaDataMap.put(key, value);
		
		combinedNeedsUpdate = true;
		combinedMap();
		
		return ori;
	}
	
	/// Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	/// More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.toLowerCase().equals(k)),
	/// then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
	/// A return value of null does not necessarily indicate that the map contains no mapping for the key;
	/// it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
	///
	/// @TODO: Optimize the combineMap(), to trigger only on iterative gets
	/// @TODO: Optimize to fetch from queryCache, without pulling the full object (if needed)
	///
	/// @param    key     the key whose associated value is to be returned
	///
	/// @returns  the value to which the specified key is mapped, or null if this map contains no mapping for the key
	@SuppressWarnings("unchecked")
	@Override
	public Object get(Object key) {
		//combinedMap();
		return super.get(key);
	}
	
	/// Copies all of the mappings from the specified map to this map.
	/// These mappings will replace any mappings that this map had for any of the keys currently in the specified map.
	///
	/// Note: Care should be taken when importing multiple case sensitive mappings, as the order which they are
	/// overwritten may not be predictable / in sequence.
	///
	/// @param    m     Original mappings to be stored in this map
	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		for (Map.Entry<?, ?> entry : m.entrySet()) {
			this.put((String) (entry.getKey()), (Object) (entry.getValue()));
		}
	}
	
	// MetaObject Specific functions
	//----------------------------------------------
	
	/// Save the delta changes to storage
	public void saveDelta() throws JStackException {
		Map<String, Object> cMap = combinedMap();
		
		mTable.updateMap(_oid, cMap, deltaDataMap.keySet());
		
		deltaDataMap = new CaseInsensitiveHashMap<String, Object>();
		remoteDataMap = new CaseInsensitiveHashMap<String, Object>();
		remoteDataMap.putAll(cMap);
	}
	
	/// Save all the configured data, ignore delta handling
	public void saveAll() throws JStackException {
		Map<String, Object> cMap = combinedMap();
		
		mTable.updateMap(_oid, cMap, null);
		
		deltaDataMap = new CaseInsensitiveHashMap<String, Object>();
		remoteDataMap = new CaseInsensitiveHashMap<String, Object>();
		remoteDataMap.putAll(cMap);
	}
	
}