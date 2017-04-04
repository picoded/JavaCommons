package picoded.dstack.core;

// Java imports
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Picoded imports
import picoded.conv.ConvertJSON;
import picoded.set.ObjectToken;
import picoded.struct.query.*;
import picoded.dstack.*;

///
/// Common base utility class of EventLogger
///
/// Does not actually implement its required feature,
/// but helps provide a common base line for all the various implementation.
///
abstract public class Core_EventLogger extends Core_DataStructure<String, String> implements
	EventLogger {
	
	//--------------------------------------------------------------------------
	// 
	// Constructor and maintenance
	//
	//--------------------------------------------------------------------------
	
	///
	/// Maintenance step call, however due to the nature of most implementation not 
	/// having any form of time "expirary", this call does nothing in most implementation.
	/// 
	/// As such im making that the default =)
	///
	@Override
	public void maintenance() {
		// Does nothing
	}
	
}