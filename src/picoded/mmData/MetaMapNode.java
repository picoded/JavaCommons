package picoded.mmData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import picoded.jCache.*;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;

public class MetaMapNode {
	
	/// The constructor, based on the JStack values, and the object parent name
	public MetaMapNode(JCache[] jCacheArr, JSql[] jSqlArr, String mapName, String objName) {
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
	
}
