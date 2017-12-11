package picoded.servlet.api.module;

public class ApiModuleConstantStrings {
	
	// Every API end point is expected to have a .result
	public static final String RESULT = "result";
	
	// Or a .ERROR for fatal exceptions
	public static final String ERROR = "ERROR";
	
	// .INFO provides additional textual description information for either .result 
	// or .ERROR as a stack trace
	public static final String INFO = "INFO";
	
	// String Escape indicator, enabled by default in all CommonApiModule result processing
	public static final String STRING_ESCAPE = "stringEscape";
	
	// Boolean indicator inside config, used to enable / disable StringEscape api.after filtering
	public static final String STRING_ESCAPE_AFTER_FILTER = "stringEscapeAfterFilter";
	
	// Unless named otherwise, all object id is set as _oid
	public static final String OBJECT_ID = "_oid";
	
	//--------------------------------------------------------------------
	//
	// Everything below here is NOT yet approved
	//
	//--------------------------------------------------------------------
	
	// Error code for known/expected errors that will be handled by the consumer
	public static final String ERROR_CODE = "ERROR_CODE";
	
}
