package picoded.mmData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import picoded.jCache.*;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;

public class MetaObject {
	
	/// The constructor, based on the JStack values, and the object parent name
	public MetaObject(JCache[] jCacheArr, JSql[] jSqlArr, String mapName, String objName) {
		jCacheStack = jCacheArr;
		jSqlStack = jSqlArr;
		
		metaMapName = mapName;
		metaObjName = objName;
	}
	
	//--------------------------------------------------//
	// Protected variables, containing the node setup   //
	//--------------------------------------------------//
	
	protected JCache[] jCacheStack = null;
	protected JSql[] jSqlStack = null;
	
	protected String metaMapName = null;
	protected String metaObjName = null;
	
	protected String jCacheMetaKey(String metaName) {
		return metaObjName + "$" + metaName;
	}
	
	protected static Object NullObject = new Object();
	
	//--------------------------------------------------//
	// Helper funciton, handling the stack segments     //
	//--------------------------------------------------//
	
	/*
	public Object getFromCache(String metaName) {

		int i = 0;
		ConcurrentMap<String, Object> cMap;
		Object ret;

		String cacheMetaKey = jCacheMetaKey(metaName);

		for (i = 0; i < jCacheStack.length; ++i) {
			if (jCacheStack[i] == null) {
				continue;
			}

			cMap = jCacheStack[i].getMap(metaMapName);

			ret = cMap.get(cacheMetaKey);

			if (ret != null) {

			}
		}

	}

	//--------------------------------------------------//
	// The main meta get and put functions              //
	//--------------------------------------------------//

	public Object get(String metaName) {
		for (i = 0; i < jSqlStack.length; ++i) {

		}
	}
	 */
}
