package picoded.JStack;

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
	
	protected CaseInsensitiveHashMap<String,MetaTable> cachedMetaTable = new CaseInsensitiveHashMap<String,MetaTable>();
	protected CaseInsensitiveHashMap<String,KeyValueMap> cachedKeyValueMap = new CaseInsensitiveHashMap<String,KeyValueMap>();
	protected CaseInsensitiveHashMap<String,AccountTable> cachedAccountTable = new CaseInsensitiveHashMap<String,AccountTable>();
	
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
	
	/// This does the setup called on all the cached tables, created via get calls
	public void setup() {
		
	}
	
	/// This does the teardown called on all the cached tables, created via get calls
	public void tearDown() {
		
	}
	
}
