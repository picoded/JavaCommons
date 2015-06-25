package picoded.JStack;

import java.util.*;

// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.struct.CaseInsensitiveHashMap;

/// JStack provides various common data storage format, that utalizes a combination of
/// JCache, and JSql instances implementation.
///
/// The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
public class JStack extends JStackLayer {
	
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
	}
	
	public JStack(JStackLayer[] inStack) {
		stackLayers = inStack;
	}
	
	/*
	public JStack(JStackLayer inLayer, String inNamespace) {
		stackLayers = new JStackLayer[] { inLayer };
		setTablePrefix(inNamespace);
	}
	
	public JStack(JStackLayer[] inStack, String inNamespace) {
		stackLayers = inStack;
		setTablePrefix(inNamespace);
	}
	 */
	
	protected CaseInsensitiveHashMap<String,JStackData> cachedMetaTable = new CaseInsensitiveHashMap<String,JStackData>();
	protected CaseInsensitiveHashMap<String,JStackData> cachedKeyValueMap = new CaseInsensitiveHashMap<String,JStackData>();
	protected CaseInsensitiveHashMap<String,JStackData> cachedAccountTable = new CaseInsensitiveHashMap<String,JStackData>();
	
	//----------------------------------------------
	// JStack modules
	//----------------------------------------------
	
	public MetaTable getMetaTable(String tableName) {
		return new MetaTable(this, tableName);
	}
	
	public KeyValueMap getKeyValueMap(String tableName) {
		return new KeyValueMap(this, tableName);
	}
	
	public AccountTable getAccountTable(String tableName) {
		return new AccountTable(this, tableName);
	}
	
	//----------------------------------------------
	// JStack automated setup of cached tables
	//----------------------------------------------
	
	/// Gets all the sub stack
	protected List< CaseInsensitiveHashMap<String,JStackData> > JStackData_stack() {
		List< CaseInsensitiveHashMap<String,JStackData>> ret = new ArrayList< CaseInsensitiveHashMap<String,JStackData>>();
		
		ret.add( cachedMetaTable );
		ret.add( cachedKeyValueMap );
		ret.add( cachedAccountTable );
		
		return ret;
	}
	
	/// This does the setup called on all the cached tables, created via get calls
	public void setup() throws JStackException {
		List< CaseInsensitiveHashMap<String,JStackData> > l =  JStackData_stack();
		for (CaseInsensitiveHashMap<String,JStackData> m : l) {
			for (Map.Entry<String, JStackData> e : m.entrySet()) {
				e.getValue().stackSetup();
			}
		}
	}
	
	/// This does the teardown called on all the cached tables, created via get calls
	public void teardown() throws JStackException {
		List< CaseInsensitiveHashMap<String,JStackData> > l =  JStackData_stack();
		for (CaseInsensitiveHashMap<String,JStackData> m : l) {
			for (Map.Entry<String, JStackData> e : m.entrySet()) {
				e.getValue().stackTeardown();
			}
		}
	}
	
}
