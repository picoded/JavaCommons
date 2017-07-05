package picoded.dstack.jsql;

// Picoded imports
import picoded.dstack.jsql.connector.*;
import picoded.dstack.*;
import picoded.dstack.core.*;

/**
* Reference implementation of CommonStack
* This is done via a minimal implementation via internal data structures.
*
* Built ontop of the Core_CommonStack implementation.
**/
public class JSqlStack extends Core_CommonStack {

	/**
	* The respective JSQL connection
	**/
	protected JSql sqlConn = null;

	/**
	* Constructor setup with connection
	*
	* @param   The JSql connection to use
	**/
	public JSqlStack(JSql inConn) {
		sqlConn = inConn;
	}

	/**
	* Common structure initialization interface, to be overwritten by actual implementation
	*
	* @param   Type of structure to setup
	* @param   Name used to initialize the structure
	*
	* @return  The CommonStructure that was initialized
	**/
	public CommonStructure initializeStructure(String type, String name) {
		if ("MetaTable".equalsIgnoreCase(type)) {
			return new JSql_MetaTable(sqlConn, name);
		} else if ("KeyValueMap".equalsIgnoreCase(type)) {
			return new JSql_KeyValueMap(sqlConn, name);
		} else if ("AtomicLongMap".equalsIgnoreCase(type)) {
			return new JSql_AtomicLongMap(sqlConn, name);
		}
		return null;
	}

}
