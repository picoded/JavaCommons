package picoded.mmData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import picoded.jCache.*;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;

public class MetaMap {
	
	/// The constructor, based on the JStack values
	public MetaMap(JCache[] jCacheArr, JSql[] jSqlArr, String mapName) {
		jCacheStack = jCacheArr;
		jSqlStack = jSqlArr;
		
		metaMapName = mapName;
		//metaObjName = objName;
	}
	
	//--------------------------------------------------//
	// Protected variables, containing the node setup   //
	//--------------------------------------------------//
	
	protected JCache[] jCacheStack = null;
	protected JSql[] jSqlStack = null;
	
	protected String metaMapName = null;
	
	//protected String metaObjName = null;
	
	/// Gets and returns the metamap object (does not check if exists)
	public MetaObject getMetaObject(String objName) {
		return new MetaObject(jCacheStack, jSqlStack, metaMapName, objName);
	}
	
	/// Convinent wrappers
	public MetaObject get(String objName) {
		return getMetaObject(objName);
	}
}
