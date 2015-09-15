package picoded.JStack;

import java.util.*;

// Picoded imports
import picoded.JSql.*;
import picoded.JStruct.*;
import picoded.JSql.struct.*;

/// JStack provides various common data storage format, that utalizes a combination of
/// JCache, and JSql instances implementation.
///
/// The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
public class JStack extends JSqlStruct implements JStackLayer {
	
	//----------------------------------------------
	// Readonly internal variables
	//----------------------------------------------
	
	/// Internal JCache layers stack used
	protected JStackLayer[] stackLayers = null;
	
	public JStackLayer[] stackLayers() {
		return stackLayers;
	}
	
	// Table prefixing filter ??
	
	// Object created timestamp prefix filter ??
	
	// Object GUID prefix filter ??
	
	//----------------------------------------------
	// Constructor
	//----------------------------------------------
	
	public JStack(JStackLayer inLayer) {
		stackLayers = new JStackLayer[] { inLayer };
		interimJSqlOnly();
	}
	
	public JStack(JStackLayer[] inStack) {
		stackLayers = inStack;
		interimJSqlOnly();
	}
	
	/// This is used to setup JStack as a JSQL only system.
	protected void interimJSqlOnly() {
		JStackLayer[] layers = stackLayers();
		
		JStackLayer last = layers[layers.length - 1];
		if (last instanceof JSql) {
			sqlObj = (JSql) last;
		} else {
			throw new RuntimeException("JStack currently supports a single JSql node (@TODO : Fix this)");
		}
	}
	
	//----------------------------------------------
	// MetaTable, KeyValueMap handling
	//----------------------------------------------
	
}
