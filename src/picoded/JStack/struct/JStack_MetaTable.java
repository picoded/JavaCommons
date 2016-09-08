package picoded.JStack.struct;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.JSql.*;
import picoded.JSql.struct.internal.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.enums.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.JStack.*;
import picoded.JStack.struct.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

/// JSql implmentation of MetaTable
///
public class JStack_MetaTable extends JStruct_MetaTable {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected static Logger logger = Logger.getLogger(JStack_MetaTable.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	public JStack stackObj = null;
	
	/// The tablename for the key value pair map
	public String stackTablename = null;
	
	/// JStack setup
	public JStack_MetaTable(JStack inStack, String tablename) {
		super();
		stackObj = inStack;
		stackTablename = tablename;
	}
	
	///
	/// Internal implementation layers handling
	///--------------------------------------------------------------------------
	
	/// The cached structure implmentation layers
	public JStruct_MetaTable[] _implementationLayers = null;
	
	/// The cached structure implmentation layers reversed
	public JStruct_MetaTable[] _implementationLayers_reversed = null;
	
	///
	/// Getting the implmentation layers
	/// This is used internally to iterate the KeyValueMap layers
	///
	public JStruct_MetaTable[] implementationLayers() {
		if (_implementationLayers != null) {
			return _implementationLayers;
		}
		
		// Get the structure layers
		JStruct[] struct = stackObj.structLayers();
		JStruct_MetaTable[] ret = new JStruct_MetaTable[struct.length];
		
		// Fetch their respective key value map
		for (int a = 0; a < struct.length; ++a) {
			
			// Safety check
			if (struct[a] == null) {
				ret[a] = null;
				continue;
			}
			
			// Fetch the implementation
			ret[a] = (JStruct_MetaTable) struct[a].getMetaTable(stackTablename);
		}
		
		return (_implementationLayers = ret);
	}
	
	///
	/// Getting the implmentation layers reversed
	/// This is used internally to iterate the KeyValueMap layers reversed
	///
	public JStruct_MetaTable[] implementationLayers_reverse() {
		if (_implementationLayers_reversed != null) {
			return _implementationLayers_reversed;
		}
		
		// Get and reverse
		JStruct_MetaTable[] impleLayers = implementationLayers();
		int impleLength = impleLayers.length;
		JStruct_MetaTable[] ret = new JStruct_MetaTable[impleLength];
		for (int i = 0; i < impleLength; i++) {
			ret[i] = impleLayers[impleLength - 1 - i];
		}
		
		// Return the reversed PDF
		return _implementationLayers_reversed = ret;
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		for (JStruct_MetaTable i : implementationLayers()) {
			i.systemSetup();
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		for (JStruct_MetaTable i : implementationLayers()) {
			i.systemTeardown();
		}
	}
	
	// MetaObject MAP operations
	//----------------------------------------------
	
	/// Gets the full keySet
	public Set<String> keySet() {
		Set<String> ret = null;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			if ((ret = i.keySet()) != null) {
				return ret;
			}
		}
		return null;
	}
	
	/// Remove the node
	public MetaObject remove(Object key) {
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			i.remove(key);
		}
		return null;
	}
	
	///
	/// Internal functions, used by MetaObject
	///--------------------------------------------------------------------------
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null
	public Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		// Layers to fetch from
		JStruct_MetaTable[] layers = implementationLayers();
		
		// Final return obj
		Map<String, Object> ret = null;
		for (int i = 0; i < layers.length; ++i) {
			// Found a valid layer
			if ((ret = layers[i].metaObjectRemoteDataMap_get(_oid)) != null) {
				// Iterate back upwards and populate the upper layers
				// And cache the layers inbetween =)
				i = i - 1; // Start immediately at previous layer
				for (; i >= 0; --i) {
					layers[i].metaObjectRemoteDataMap_update(_oid, ret, null);
				}
				return ret;
			}
		}
		
		// Failure return
		return null;
	}
	
	/// Updates the actual backend storage of MetaObject 
	/// either partially (if supported / used), or completely
	public void metaObjectRemoteDataMap_update(String _oid, Map<String, Object> fullMap, Set<String> keys) {
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			i.metaObjectRemoteDataMap_update(_oid, fullMap, keys);
		}
		return;
	}
	
	/// 
	/// Query based 
	///--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects keys
	///
	/// CURRENTLY: It is entirely dependent on the whereValues object type to perform the relevent search criteria
	/// @TODO: Performs the search pattern using the respective type map
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The String[] array
	public String[] queryKeys(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
		String[] ret = null;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			if ((ret = i.queryKeys(whereClause, whereValues, orderByStr, offset, limit)) != null) {
				return ret;
			}
		}
		return null;
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
		return getArrayFromID(queryKeys(whereClause, whereValues, orderByStr, offset, limit), true);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	public long queryCount(String whereClause, Object[] whereValues) {
		long ret = 0;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			if ((ret = i.queryCount(whereClause, whereValues)) >= 0) {
				return ret;
			}
		}
		return 0;
	}
	
}
