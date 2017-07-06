package picoded.dstack.stack;

// Java imports
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// Library imports
import picoded.dstack.*;
import picoded.dstack.core.*;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.struct.UnsupportedDefaultMap;

// Third party imports
import org.apache.commons.lang3.RandomUtils;

/**
* The DStack, for handling all the data stuff
*
* provides various common data storage format, that utalizes a combination of
* JCache, and JSql instances implementation.
*
* The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
**/
public class DStack_Stack extends Core_CommonStack {

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
	public DStack_Stack() {
		//
	}

	/**
	* DStack setup with a single stack node
	*
	* @param  A single stack implementation
	**/
	public DStack_Stack(CommonStack single) {
		_stackLayers.add(single);
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
		if( _stackLayers.size() == 0 ) {
			throw new RuntimeException("Empty DStack, add desired underlying implementation (like JSQL)");
		}

		// Single DStack optimization
		if( _stackLayers.size() == 1 ) {
			CommonStack single = _stackLayers.get(0);
			if( !(single instanceof Core_CommonStack ) ) {
				throw new RuntimeException("DStack layer implmentation is not based of Core_CommonStack");
			}

			return ((Core_CommonStack)single).initializeStructure(type, name);
		}

		// Actual DStack setup
		throw new RuntimeException("Multiple Dstack layers is not supported yet =(");
	}

}
