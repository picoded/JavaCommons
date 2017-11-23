package picoded.dstack.stack;

// Java imports
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// Library imports
import picoded.dstack.*;
import picoded.dstack.core.*;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import picoded.core.struct.UnsupportedDefaultMap;

// Third party imports
import org.apache.commons.lang3.RandomUtils;

/**
 * The DStack, for handling all the data stuff
 *
 * provides various common data storage format, that utalizes a combination of
 * JCache, and JSql instances implementation.
 *
 * The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of DataTables.
 **/
public class DStackImplementation extends Core_CommonStack {
	
	//-----------------------------------------------------------
	//
	// Constructor
	//
	//-----------------------------------------------------------
	
	/**
	 * The stack of backend supported
	 **/
	protected List<CommonStack> _stackLayers = new ArrayList<CommonStack>();
	
	/**
	 * Blank constructor
	 **/
	public DStackImplementation() {
		//
	}
	
	/**
	 * DStack setup with a single stack node
	 *
	 * @param  A single stack implementation
	 **/
	public DStackImplementation(CommonStack single) {
		_stackLayers.add(single);
		validateStackLayers();
	}
	
	/**
	 * DStack setup with a list of stack
	 * @param A list of stacks
	 **/
	public DStackImplementation(List<CommonStack> list) {
		_stackLayers.addAll(list);
		validateStackLayers();
	}
	
	/**
	 * Validates the internal stacklayers, throw an exception if not valid
	 */
	protected void validateStackLayers() {
		// @TODO : Actually validate
	}
	
	//-----------------------------------------------------------
	//
	// Closure, of DStack connections (ie: JSQL)
	//
	//-----------------------------------------------------------
	
	/**
	 * Perform any required connection / file handlers / etc closure
	 * This is to clean up any "resource" usage if needed.
	 * 
	 * This proxy the closure call to the actual underlying implementation
	 */
	public void close() {
		for (CommonStack layer : _stackLayers) {
			layer.close();
		}
	}
	
	//-----------------------------------------------------------
	//
	// Stack management
	//
	//-----------------------------------------------------------
	
	//-----------------------------------------------------------
	//
	// Underlying implementation
	//
	//-----------------------------------------------------------
	
	/**
	 * Common structure initialization interface, to be overwritten by actual implementation
	 *
	 * @param   Type of structure to setup
	 * @param   Name used to initialize the structure
	 *
	 * @return  The CommonStructure that was initialized
	 **/
	public CommonStructure initializeStructure(String type, String name) {
		
		// Empty DStack error
		if (_stackLayers.size() == 0) {
			throw new RuntimeException(
				"Empty DStack, add desired underlying implementation (like JSQL)");
		}
		
		// Single DStack optimization
		if (_stackLayers.size() == 1) {
			CommonStack single = _stackLayers.get(0);
			if (!(single instanceof Core_CommonStack)) {
				throw new RuntimeException(
					"DStack layer implmentation is not based of Core_CommonStack");
			}
			
			return ((Core_CommonStack) single).initializeStructure(type, name);
		}
		
		// Actual DStack setup
		throw new RuntimeException("Multiple Dstack layers is not supported yet =(");
	}
	
}
