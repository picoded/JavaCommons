package picoded.JStruct.internal;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import picoded.JStack.JStack;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;

/// JSql implmentation of MetaTable
///
public class JStack_MetaTable extends JStruct_MetaTable {
	
	/// Invalid constructor (throws exception)
	protected JStack_MetaTable() {
	}
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected Logger logger = Logger.getLogger(JStack_MetaTable.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JStack stackObj = null;
	
	/// The tablename for the key value pair map
	protected String stackTablename = null;
	
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
	protected JStruct_MetaTable[] implementationLayer = null;
	
	/// The cached structure implmentation layers reversed
	protected JStruct_MetaTable[] implementationLayersReversed = null;
	
	///
	/// Getting the implmentation layers
	/// This is used internally to iterate the KeyValueMap layers
	///
	public JStruct_MetaTable[] implementationLayers() {
		JStruct_MetaTable[] ret = new JStruct_MetaTable[0];
		try {
			if (implementationLayer != null) {
				return implementationLayer;
			}
			
			// Get the structure layers
			JStruct[] struct = stackObj.structLayers();
			ret = new JStruct_MetaTable[struct.length];
			
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
			
			return implementationLayer = ret;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
		return ret;
	}
	
	///
	/// Getting the implmentation layers reversed
	/// This is used internally to iterate the KeyValueMap layers reversed
	///
	public JStruct_MetaTable[] implementationLayers_reverse() {
		if (implementationLayersReversed != null) {
			return implementationLayersReversed;
		}
		
		// Get and reverse
		JStruct_MetaTable[] impleLayers = implementationLayers();
		int impleLength = impleLayers.length;
		JStruct_MetaTable[] ret = new JStruct_MetaTable[impleLength];
		for (int i = 0; i < impleLength; i++) {
			ret[i] = impleLayers[impleLength - 1 - i];
		}
		
		// Return the reversed PDF
		return implementationLayersReversed = ret;
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		for (JStruct_MetaTable i : implementationLayers()) {
			i.systemSetup();
		}
		
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	@Override
	public void systemTeardown() {
		for (JStruct_MetaTable i : implementationLayers()) {
			i.systemTeardown();
		}
		
	}
	
	// MetaObject MAP operations
	//----------------------------------------------
	
	/// Gets the full keySet
	@Override
	public Set<String> keySet() {
		Set<String> ret = null;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			//			if (i.keySet() != null) {
			ret = i.keySet();
			//			}
		}
		return ret;
	}
	
	/// Remove the node
	@Override
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
	@Override
	public Map<String, Object> metaObjectRemoteDataMap_get(String oid) {
		// Layers to fetch from
		JStruct_MetaTable[] layers = implementationLayers();
		
		// Final return obj
		Map<String, Object> ret = null;
		for (int i = 0; i < layers.length; ++i) {
			// Found a valid layer
			if ((ret = layers[i].metaObjectRemoteDataMap_get(oid)) != null) {
				// Iterate back upwards and populate the upper layers
				// And cache the layers inbetween =)
				//				i = i - 1; // Start immediately at previous layer
				for (; i >= 0; --i) {
					layers[i].metaObjectRemoteDataMap_update(oid, ret, null);
				}
				return ret;
			}
		}
		
		// Failure return
		return null;
	}
	
	/// Updates the actual backend storage of MetaObject 
	/// either partially (if supported / used), or completely
	@Override
	public void metaObjectRemoteDataMap_update(String oid, Map<String, Object> fullMap,
		Set<String> keys) {
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			i.metaObjectRemoteDataMap_update(oid, fullMap, keys);
		}
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
	@Override
	public String[] queryKeys(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		String[] ret = null;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			if (i.queryKeys(whereClause, whereValues, orderByStr, offset, limit).length > 0) {
				ret = i.queryKeys(whereClause, whereValues, orderByStr, offset, limit);
			}
		}
		return ret;
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
	@Override
	public MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return getArrayFromID(queryKeys(whereClause, whereValues, orderByStr, offset, limit), true);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	@Override
	public long queryCount(String whereClause, Object[] whereValues) {
		long ret = 0;
		for (JStruct_MetaTable i : implementationLayers_reverse()) {
			//			if (i.queryCount(whereClause, whereValues) >= 0) {
			ret = i.queryCount(whereClause, whereValues);
			//			}
		}
		return ret;
	}
	
}
