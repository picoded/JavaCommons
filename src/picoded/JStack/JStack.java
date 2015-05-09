package picoded.JStack;

// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;

/// JStack provides various common data storage format, that utalizes a combination of
/// JCache, and JSql instances implementation.
///
/// The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
public class JStack {
	
	//----------------------------------------------
	// Readonly internal variables
	//----------------------------------------------
	
	/// Internal JCache layers stack used
	protected JCache[] JCacheStack = null;
	
	/// Internal JCache layers stack used
	public JCache[] JCacheStack() {
		return JCacheStack;
	}
	
	/// Internal JSql database stack used
	protected JSql[] JSqlStack = null;
	
	/// Internal JCache layers stack used
	public JSql[] JSqlStack() {
		return JSqlStack;
	}
	
	/// Internal namespace prefix, this is used for MetaTables / etc setup
	protected String namespace = "";
	
	/// Internal namespace prefix, this is used for MetaTables / etc setup
	public String namespace() {
		return namespace;
	}
	
	//----------------------------------------------
	// Constructor
	//----------------------------------------------
	
	public JStack(JCache[] inJCacheStack, JSql[] inJSqlStack, String inNamespace) {
		JCacheStack = inJCacheStack;
		JSqlStack = inJSqlStack;
		namespace = inNamespace;
		if (namespace == null) {
			namespace = "";
		}
	}
	
}