package picoded.webTemplateEngines.FormGenerator;

public class JsonKeys {
	
	////////////////////////////////////////////////
	//
	//  Input and wrapper type handling
	//
	////////////////////////////////////////////////
	
	/// The type to assume for both input and wrapper
	public static String TYPE = "type";
	
	/// Overwrites of each input type
	public static String INPUT_TYPE = "inputType";
	
	/// Overwrites of each wrapper type
	public static String WRAPPER_TYPE = "wrapperType";
	
	////////////////////////////////////////////////
	//
	//  Input value handling
	//
	////////////////////////////////////////////////
	
	// Field name to read and write values to
	public static String FIELD = "field";
	
	// Default value to extract from json map, if field name value does not exists
	public static String DEFAULT = "default";
	
	// Options listing, used to list options in dropdown
	public static String OPTIONS = "options";

	////////////////////////////////////////////////
	//
	//  HTML parameters handling
	//
	////////////////////////////////////////////////
	
	// The field to setup automated class loading
	public static String AUTO_CLASS = "class";
	
	// The field to setup the class SPECFIC
	public static String INPUT_CLASS = "inputClass";
	
	// The input ID to use
	public static String INPUT_ID = "inputID";
	
	// Input css overwrites
	public static String INPUT_CSS = "inputCss";
	
	// The field to setup the class SPECFIC
	public static String WRAPPER_CLASS = "wrapperClass";
	
	// The input ID to use
	public static String WRAPPER_ID = "wrapperID";
	
	// Input css overwrites
	public static String WRAPPER_CSS = "wrapperCss";
	
	// The field to setup the class SPECFIC
	public static String LABEL_CLASS = "labelClass";
	
	// The input ID to use
	public static String LABEL_ID = "labelID";
	
	// Input css overwrites
	public static String LABEL_CSS = "labelCss";
	
	
	// The field to setup the class SPECFIC
	public static String CHILD_CLASS = "childClass";
	
	// The input ID to use
	public static String CHILD_ID = "childID";
	
	// Input css overwrites
	public static String CHILD_CSS = "childCss";
	
	//removal of Label types from inner children if needed
	public static String REMOVE_LABEL_FROM_SECOND_ITERATION = "removeLabel";
	
	//extra properties injection
	public static String EXTRA_WRAPPER_PROPERTIES = "HTML_wrapperAttributes";
	public static String EXTRA_CHILD_WRAPPER_PROPERTIES = "HTML_childWrapperAttributes";
	public static String EXTRA_INPUT_PROPERTIES = "HTML_inputAttributes";
	public static String EXTRA_LABEL_PROPERTIES = "HTML_labelAttributes";
	
	public static String EXTRA_WRAPPER_PROPERTIES_MAP = "HTML_wrapperAttributesMap";
	public static String EXTRA_CHILD_WRAPPER_PROPERTIES_MAP = "HTML_childWrapperAttributesMap";
	public static String EXTRA_INPUT_PROPERTIES_MAP = "HTML_inputAttributesMap";
	public static String EXTRA_LABEL_PROPERTIES_MAP = "HTML_labelAttributesMap";
	
	////////////////////////////////////////////////
	//
	//  JMTE json keys
	//
	////////////////////////////////////////////////
	
	public static String JMTE_GENERIC = "jmte_generic";
	public static String JMTE_DISPLAY = "jmte_display";
	public static String JMTE_INPUT = "jmte_input";
	
	////////////////////////////////////////////////
	//
	//  JSON Node Types (standard)
	//
	////////////////////////////////////////////////
	
	/// No wrapper? Used mainly for raw html mode
	public static String NONE = "none";
	
	/// Standard DIV wrapper / input
	public static String DIV = "div";
	
	////////////////////////////////////////////////
	//
	//  TO-REFACTOR
	//
	////////////////////////////////////////////////
	
	// Inner HTML injection (discouraged from use)
	public static String HTML_INJECTION = "innerHTML"; 
	
	// Html input textbox type
	public static String TEXTBOX = "textbox";
	
	public static String TEXT = "text";
	public static String TITLE = "title";
	public static String CHECKBOX = "checkbox";
	public static String SUBNODES = "subnodes";
	public static String DROPDOWN = "dropdown";
	public static String LABEL = "label";
	public static String WRAPPER = "wrapper";
	
	public static String DROPDOWN_WITHOTHERS = "dropdownWithOthers";
	public static String OTHERS_OPTION = "othersOption";
	public static String DROPDOWN_WITHOTHERS_TEXTFIELD = "textField";
	public static String FUNCTION_NAME = "functionName";
	
	public static String CUSTOMCLASS = "class";
	
	public static String PDFDISPLAY_CLASS = "pdfDisplayClass";
	public static String PDFOUTPUT_CLASS = "pdfOutputClass";
	
	
	
}
