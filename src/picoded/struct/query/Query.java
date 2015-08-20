package picoded.struct.query;

import java.util.function.*;
import java.util.*;

///
/// 
/// 
/// 
///
public interface Query extends Predicate<Object> {
	
	/// The test operator, asserts if the element matches
	/// against its cached argument map value
	///
	/// @param   the object to test against
	///
	/// @returns  boolean indicating true / false
	boolean test(Object t);
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	boolean test(Object t, Map<String,Object>argMap);
	
}
