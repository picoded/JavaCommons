package picoded.JStack;

import java.util.*;

// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.JSql.struct.*;
import picoded.JCache.struct.*;
import picoded.JStack.struct.*;

/// JStack provides various common data storage format, that utalizes a combination of
/// JCache, and JSql instances implementation.
///
/// The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
public class JStack extends JStruct implements JStackLayer {
	
	//----------------------------------------------
	// Readonly internal variables
	//----------------------------------------------
	
	/// Internal JCache layers stack used
	protected JStackLayer[] _stackLayers = null;
	
	/// Final getter for JStackLayers 
	public final JStackLayer[] stackLayers() {
		return _stackLayers;
	}
	
	/// JStruct layering internal var
	protected JStruct[] _structLayers = null;
	
	/// Final getter for JStruct layers
	public final JStruct[] structLayers() {
		if (_structLayers != null) {
			return _structLayers;
		}
		
		int len = stackLayers().length;
		JStruct[] ret = new JStruct[len];
		for (int i = 0; i < len; ++i) {
			JStackLayer layer = _stackLayers[i];
			if (layer instanceof JStruct) {
				ret[i] = (JStruct) layer;
			} else if (layer instanceof JSql) {
				ret[i] = new JSqlStruct((JSql) layer);
			} else if (layer instanceof JCache) {
				ret[i] = new JCacheStruct((JCache) layer);
			}
		}
		
		return (_structLayers = ret);
	}
	
	// Table prefixing filter ??
	
	// Object created timestamp prefix filter ??
	
	// Object GUID prefix filter ??
	
	//----------------------------------------------
	// Constructor
	//----------------------------------------------
	
	public JStack(JStackLayer inLayer) {
		_stackLayers = new JStackLayer[] { inLayer };
	}
	
	public JStack(JStackLayer[] inStack) {
		_stackLayers = inStack;
	}
	
	//----------------------------------------------
	// Teardown 
	//----------------------------------------------
	
	public void disposeStackLayers() throws JStackException {
		try {
			for( JStackLayer oneLayer : _stackLayers ) {
				oneLayer.dispose();
			}
		} catch (Exception e) {
			throw new JStackException(e);
		}
	}
	
	//----------------------------------------------
	// MetaTable, KeyValueMap handling
	//----------------------------------------------
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	protected KeyValueMap setupKeyValueMap(String name) {
		return new JStack_KeyValueMap(this, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	protected MetaTable setupMetaTable(String name) {
		return new JStack_MetaTable(this, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns AccountTable
	protected AccountTable setupAccountTable(String name) {
		return new AccountTable(this, name);
	}
	
}
