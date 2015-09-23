package picoded.webTemplateEngines.FormGenerator;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.JSML.*;

import com.amazonaws.util.StringUtils;
///
/// Web templating engine that helps define and convert a JSON styled template, into the actual web form
///
///
/// ### Code process flow
///
/// The following ANSCII digram illustrates the process flow that the code follows for 
///
/// + FormGenerator
/// + FormNode
/// + FormWrapperInterface
/// + FormInputInterface
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
///  +----------------+    (Via input/wrapperInterfaceMap)
///  +  FormGenerator |----------------------------------------\
///  +----------------+                                        |
///        |                                                   |
///        V                                                   |
///  +-----------------------------------------+               |
///  | FormGenerator.build(),                  |               |
///  | Form Definition is (Map<String,Object>) |               |
///  +-----------------------------------------+               |
///        |                                                   |
///        V                                                   |
///  +-----------+                                             |
///  +  FormNode |                                             |
///  +-----------+                                             |
///        |                                                   |
///        V         +-----------------+                       |
///  +-----------+   | For each child  |                       |  
///  + FulHtml() |<--| Iterate through |                       |
///  +-----------+   +-----------------+                       | 
///        |                  ^                                |
///        V                  |                                |
///  +--------------------------------------------------+      |
///  |              FormWrapperInterface                |      |
///  |                                                  |<-----+
///  | The respective interface called is selected      |      |
///  | based on its type, and taken from FormGenerator  |      |
///  | This also handles the insertion of labels        |      |
///  | & child wrappers string                          |      |
///  +--------------------------------------------------+      | 
///        |                                                   |
///        V                                                   |
///  +-------------+                                           |  
///  + InputHtml() |                                           |
///  +-------------+                                           | 
///        |                                                   |
///        V                                                   |
///  +--------------------------------------------------+      |
///  |                FormInputInterface                |      |
///  |                                                  |<-----/
///  | The respective interface called is selected      |      
///  | based on its type, and taken from FormGenerator  |    
///  +--------------------------------------------------+     
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///

public class FormGenerator {
	
	/////////////////////////////////////////////////////////////////////////
	//
	// Internal vars, used in the FormGenerator class
	//
	/////////////////////////////////////////////////////////////////////////
	
	private Map<String, FormWrapperInterface> customFormWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customFormInputTemplates = new HashMap<String, FormInputInterface>();
	
	private Map<String, FormWrapperInterface> customDisplayWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customDisplayInputTemplates = new HashMap<String, FormInputInterface>();
	
	/////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	/////////////////////////////////////////////////////////////////////////
	
	public FormGenerator() {
		setupDefaultFormTemplates();
	}
	
	private void setupDefaultFormTemplates() {
		customFormWrapperTemplates = FormWrapperTemplates.defaultWrapperTemplates();
		customFormInputTemplates = FormInputTemplates.defaultInputTemplates();
		
		customDisplayWrapperTemplates = DisplayWrapperTemplates.defaultWrapperTemplates();
		customDisplayInputTemplates = DisplayInputTemplates.defaultInputTemplates();
	}
	
	/////////////////////////////////////////////////////////////////////////
	//
	// Internal vars Accessor
	//
	/////////////////////////////////////////////////////////////////////////
	
	/// Returns the inputMap storing all the various FormInputInterface
	///
	/// @params {boolean} displayOnly  - Returns the varient for read only display mode (eg: PDF)
	///
	/// @returns {Map<String, FormInputInterface>} interface map
	public Map<String, FormInputInterface> inputInterfaceMap(boolean displayOnly) {
		if (displayOnly) {
			return customDisplayInputTemplates;
		} else {
			return customFormInputTemplates;
		}
	}
	
	/// Returns the wrapperMap storing all the various FormWrapperInterface
	///
	/// @params {boolean} displayOnly  - Returns the map, in which display only interfaces are provided
	///
	/// @returns {Map<String, FormInputInterface>} interface map
	public Map<String, FormWrapperInterface> wrapperInterfaceMap(boolean displayOnly) {
		if (displayOnly) {
			return customDisplayWrapperTemplates;
		} else {
			return customFormWrapperTemplates;
		}
	}
	
	/// Returns the FormInputInterface, given the display mode and type
	/// This will automatically fallback to default interface if the requested type is not found
	///
	/// @params {boolean} displayOnly  - Returns the varient for read only display mode (eg: PDF)
	/// @params {String} type          - String representing the input type to request for
	///
	/// @returns {FormInputInterface} interface function
	public FormInputInterface inputInterface(boolean displayOnly, String type) {
		FormInputInterface ret = inputInterfaceMap(displayOnly).get(type);
		if (ret != null) {
			return ret;
		}
		return inputInterfaceMap(displayOnly).get("*");
	}
	
	/// Returns the FormWrapperInterface, given the display mode and type
	/// This will automatically fallback to default interface if the requested type is not found
	///
	/// @params {boolean} displayOnly  - Returns the varient for read only display mode (eg: PDF)
	/// @params {String} type          - String representing the wrapper type to request for
	///
	/// @returns {FormWrapperInterface} interface function
	public FormWrapperInterface wrapperInterface(boolean displayOnly, String type) {
		FormWrapperInterface ret = wrapperInterfaceMap(displayOnly).get(type);
		if (ret != null) {
			return ret;
		}
		return wrapperInterfaceMap(displayOnly).get("*");
	}
	
	////////////////////////////////////////////////
	//
	// JSML FormSet, linked if formset exists
	//
	////////////////////////////////////////////////
	
	/// Inner protected vars
	protected JSMLFormSet formSetObj = null;
	
	/// FormSet Setter
	public void setFormSet(JSMLFormSet set) {
		formSetObj = set;
	}
	
	/// FormSet Getter
	public JSMLFormSet getFormSet() {
		return formSetObj;
	}
	
	/////////////////////////////////////////////////////////////////////////
	//
	// To generate and run
	//
	/////////////////////////////////////////////////////////////////////////
	
	/// Builds the template and run the form generator
	///
	/// @params  {Map<String,Object>}  format       - The JSML format object to generate the form/display
	/// @params  {Map<String,Object>}  data         - The Data map to extract value from
	/// @params  {boolean}             displayOnly  - Display mode, html read only or form
	///
	/// @returns {StringBuilder} the full returning HTML
	public StringBuilder build(Map<String, Object> format, Map<String, Object> data, boolean displayOnly) {
		FormNode rootNode = new FormNode(this, format, data);
		rootNode.setFormSet(getFormSet());
		
		return rootNode.fullHtml(displayOnly);
	}
	
	/// Builds the template and run the form generator
	///
	/// @params  {List<Map<String,Object>>}  format       - The JSML format object to generate the form/display
	/// @params  {Map<String,Object>}        data         - The Data map to extract value from
	/// @params  {boolean}                   displayOnly  - Display mode, html read only or form
	///
	/// @returns {StringBuilder} the full returning HTML
	// public StringBuilder build( List<Map<String,Object>> format, Map<String,Object> data, boolean displayOnly ) {
	// 	Map<String,Object> divWrap = new HashMap<String,Object>();
	// 	divWrap.put("type", "none");
	// 	divWrap.put("children", format );
	// 	
	// 	return build(divWrap, data, displayOnly);
	// }
	
	public StringBuilder build(List<Object> format, Map<String, Object> data, boolean displayOnly) {
		Map<String, Object> divWrap = new HashMap<String, Object>();
		divWrap.put("type", "none");
		divWrap.put("children", format);
		
		return build(divWrap, data, displayOnly);
	}
	
	/// Builds the template and run the form generator
	///
	/// @params  {String}                    jsonFormatString  - The JSML format object in string format to generate the form/display
	/// @params  {Map<String,Object>}        data              - The Data map to extract value from
	/// @params  {boolean}                   displayOnly       - Display mode, html read only or form
	///
	/// @returns {StringBuilder} the full returning HTML
	public StringBuilder build(String jsonFormatString, Map<String, Object> data, boolean displayOnly) {
		if (jsonFormatString.startsWith("[")) {
			List<Object> jsonArray = ConvertJSON.toList(jsonFormatString);
			return build(jsonArray, data, displayOnly);
		} else {
			Map<String, Object> jsonObj = ConvertJSON.toMap(jsonFormatString);
			return build(jsonObj, data, displayOnly);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	//
	// To remove
	//
	/////////////////////////////////////////////////////////////////////////
	
	public FormWrapperInterface addCustomFormWrapperTemplate(String key, FormWrapperInterface customWrapperTemplate) {
		return customFormWrapperTemplates.put(key, customWrapperTemplate);
	}
	
	public FormInputInterface addCustomFormInputTemplate(String key, FormInputInterface customInputTemplate) {
		return customFormInputTemplates.put(key, customInputTemplate);
	}
	
	public String generatePDFReadyHTML(String jsonString, Map<String, Object> prefilledJSONData) {
		List<FormNode> formNodes = FormNode.createFromJSONString(this, jsonString, prefilledJSONData);
		String htmlString = generatePDFReadyHTML(formNodes);
		return htmlString;
	}
	
	public String generatePDFReadyHTML(Map<String, Object> jsonData, Map<String, Object> prefilledJSONData) {
		FormNode rootNode = new FormNode(this, jsonData, prefilledJSONData);
		rootNode.setFormSet(getFormSet());
		
		List<FormNode> formNodes = new ArrayList<FormNode>();
		formNodes.add(rootNode);
		String htmlString = generatePDFReadyHTML(formNodes);
		return htmlString;
	}
	
	protected String generatePDFReadyHTML(List<FormNode> nodes) {
		StringBuilder htmlBuilder = new StringBuilder();
		for (FormNode node : nodes) {
			String nodeHtml = generatePDFReadyHTML(node);
			htmlBuilder.append(nodeHtml);
		}
		
		return htmlBuilder.toString();
	}
	
	/// The critical recursive function
	protected String generatePDFReadyHTML(FormNode node) {
		String nodeType = node.getString(JsonKeys.TYPE, HtmlTag.DIV);
		String[] formWrappers = new String[] { "", "" };
		
		String wrapperType = node.getString(JsonKeys.WRAPPER, HtmlTag.DIV);
		
		//formWrappers = customPDFWrapperTemplates.get(wrapperType).apply(node);
		
		/// This is input data output
		StringBuilder formTextData = customDisplayInputTemplates.get(nodeType).apply(node);
		
		//get inner data for children
		StringBuilder innerData = new StringBuilder("");
		if (node.childCount() > 0) {
			innerData.append(generatePDFReadyHTML(node.children()));
		}
		
		String finalNodeValue = formWrappers[0] + formTextData + innerData.toString() + formWrappers[1];
		return finalNodeValue;
	}
	
	public String applyTemplating(String jsonString, Map<String, Object> prefilledJSONData) {
		List<FormNode> formNodes = FormNode.createFromJSONString(this, jsonString, prefilledJSONData);
		String htmlString = applyTemplating(formNodes);
		// htmlString = "<div class=\"pf_root\">"+htmlString+"</div>";
		return htmlString;
	}
	
	protected String applyTemplating(List<FormNode> nodes) {
		StringBuilder htmlBuilder = new StringBuilder();
		for (FormNode node : nodes) {
			htmlBuilder.append(applyTemplating(node));
		}
		
		return htmlBuilder.toString();
	}
	
	/// The critical recursive function
	protected String applyTemplating(FormNode node) {
		String nodeType = node.getString(JsonKeys.TYPE, HtmlTag.DIV);
		String[] formWrappers = new String[] { "", "" };
		
		String wrapperType = node.getString(JsonKeys.WRAPPER, HtmlTag.DIV);
		
		//formWrappers = customFormWrapperTemplates.get(wrapperType).apply(node);
		if (node.containsKey(JsonKeys.HTML_INJECTION)) {
			String rawHtml = node.getString(JsonKeys.HTML_INJECTION);
			String finalNodeValue = formWrappers[0] + rawHtml + formWrappers[1];
			return finalNodeValue;
		} else {
			
			/// This is input data output
			StringBuilder inputOutputData = customFormInputTemplates.get(nodeType).apply(node);
			
			//get inner data for children
			StringBuilder innerData = new StringBuilder("");
			if (node.childCount() > 0) {
				innerData.append("<div class=\"pf_childDiv");
				getCustomClass(node, innerData, JsonKeys.CUSTOMCLASS, "pfc_");
				getCustomClass(node, innerData, JsonKeys.CHILD_CLASS, "");
				innerData.append("\">\n");
				innerData.append(applyTemplating(node.children()));
				innerData.append("</div>\n");
			}
			
			String finalNodeValue = formWrappers[0] + inputOutputData + innerData.toString() + formWrappers[1];
			return finalNodeValue;
		}
	}
	
	public static void getCustomClass(FormNode node, StringBuilder sb, String jsonKey, String prefix) {
		if (node.containsKey(jsonKey)) {
			String wrapperClass = node.getString(jsonKey);
			String[] wrapperClassSplit = null;
			if (wrapperClass.contains(" ")) {
				wrapperClassSplit = wrapperClass.split(" ");
				for (String str : wrapperClassSplit) {
					if (!str.equals(" ")) {
						sb.append(" " + prefix + str);
					}
				}
			} else {
				sb.append(" " + prefix + wrapperClass);
			}
		}
	}
	
	public static String getWrapperCssString(FormNode node) {
		StringBuilder sb = new StringBuilder("");
		
		if (node.containsKey(JsonKeys.WRAPPER_CSS)) {
			sb.append(" style=\"" + node.getString(JsonKeys.WRAPPER_CSS) + "\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputCssString(FormNode node) {
		StringBuilder sb = new StringBuilder("");
		
		if (node.containsKey(JsonKeys.INPUT_CSS)) {
			sb.append(" style=\"" + node.getString(JsonKeys.INPUT_CSS) + "\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelCssString(FormNode node) {
		StringBuilder sb = new StringBuilder("");
		
		if (node.containsKey(JsonKeys.LABEL_CSS)) {
			sb.append(" style=\"" + node.getString(JsonKeys.LABEL_CSS) + "\"");
		}
		
		return sb.toString();
	}
}
