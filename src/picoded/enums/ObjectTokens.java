package picoded.enums;

/// Sometimes you just want objects which represents "something"
/// temporarily, and can be considered "equal" to itself.
///
/// Think of it as temp object, as they have no persistence between
/// JVM initiations run anyway. Use this in place of internal class
/// variables for increased readability, and consistency.
///
/// Note that EXTREME care should be taken if these objects are passed
/// between different classes. Make sure u contact both side respective
/// author as this can cause unexpected behaviour. 
public class ObjectTokens {
	
	/// Used to represent a pesudo NULL value, when actual NULL
	/// may have unintended implications
	public static final Object NULL = new Object();
	
}
