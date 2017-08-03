package picoded.dstack;

// Java imports
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// Library imports
import picoded.dstack.stack.*;
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
* The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of DataTables.
*
* See stack/DStackImplementation for actual details
**/
public class DStack extends DStackImplementation {

	/**
	* Blank constructor
	**/
	public DStack() {
		super();
	}

	/**
	* DStack setup with a single stack node
	*
	* @param  A single stack implementation
	**/
	public DStack(CommonStack single) {
		super(single);
	}

	/**
	* DStack setup with a list of stack
	* @param A list of stacks
	**/
	public DStack(List<CommonStack> list) {
		super(list);
	}

}
