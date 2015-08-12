package picoded.webTemplateEngines;

public class JsonKeys {
	
	////////////////////////////////////////////////
	//
	//  Input value handling
	//
	////////////////////////////////////////////////
	
	// Field name to read and write values to
	public static String FIELD = "field";
	
	// Default value to extract from json map, if field name value does not exists
	public static String DEFAULT = "default";
	
	////////////////////////////////////////////////
	//
	//  HTML parameters handling
	//
	////////////////////////////////////////////////
	
	// The field to setup automated class loading
	public static String AUTO_CLASS = "class";
	
	// The field to setup the class SPECFIC
	public static String INPUT_CLASS = "inputClass";
	
	////////////////////////////////////////////////
	//
	//  HTML style overwrites
	//
	////////////////////////////////////////////////
	
	// Input css overwrites
	public static String INPUT_CSS = "inputCss";
	
	
	
	
	
	////////////////////////////////////////////////
	//
	//  HTML DOM types
	//
	////////////////////////////////////////////////
	
	// Standard html DIV
	public static String DIV = "div";
	
	// The header class type
	public static String DOM_HEADER = "h3";
	
	
	
	
	
	
	////////////////////////////////////////////////
	//
	//  TO-REFACTOR
	//
	////////////////////////////////////////////////
	
	// Inner HTML injection (discouraged from use)
	public static String HTML_INJECTION = "innerHTML"; 
	
	public static String TEXT = "text";
	public static String TITLE = "title";
	public static String TYPE = "type";
	public static String SUBNODES = "subnodes";
	public static String DROPDOWN = "dropdown";
	public static String LABEL = "label";
	public static String WRAPPER = "wrapper";
	public static String OPTIONS = "options";
	public static String DROPDOWN_WITHOTHERS = "dropdownWithOthers";
	public static String OTHERS_OPTION = "othersOption";
	
	public static String CUSTOMCLASS = "class";
	public static String WRAPPER_CLASS = "wrapperClass";
	public static String LABEL_CLASS = "labelClass";
	public static String PDFDISPLAY_CLASS = "pdfDisplayClass";
	public static String PDFOUTPUT_CLASS = "pdfOutputClass";
	public static String CHILD_CLASS = "childClass";
	
	public static String WRAPPER_CSS = "wrapperCss";
	public static String LABEL_CSS = "labelCss";
	
}
