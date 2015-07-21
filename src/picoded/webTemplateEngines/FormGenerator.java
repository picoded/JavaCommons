package picoded.webTemplateEngines;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import picoded.conv.GenericConvert;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

///
/// Web templating engine that helps define and convert a JSON styled template, into the actual web form
///
/// @TODO
/// + Skip/ShowOnFilter, a common filter would be PDF
///
/// @Future Stuff
/// + Use a tokenizer system? that is more performance efficent??, instead of multiple string/stringBuilder
///
public class FormGenerator {
	
	
	
	/// Helps escape html dom parameter quotes, in an "optimal" way
	protected static String escapeParameterQuote( String val ) {
		boolean hasSingleQuote = val.contains("\'");
		boolean hasDoubleQuote = val.contains("\"");
		
		if( hasSingleQuote && hasDoubleQuote ) {
			//No choice, escape double quotes, and use them
			return "\""+val.replaceAll("\\\\", "\\\\").replaceAll("\"","\\\"")+"\"";
		} else if( hasDoubleQuote ) {
			return "\'"+val+"\'";
		} else if( hasSingleQuote ) {
			return "\""+val+"\"";
		} //else { //quoteless, use single quotes
		return "\'"+val+"\'";
		//}
	}
	
	/// List of parameters that are blocked from automated standard pass
	protected static List<String> blockedStandardParameters = Arrays.asList( new String[] { "type", "class", "style", "labelStyle", "inputStyle" } );
	
	/// Pass forward the standard parameters, and filter out the protected ones
	protected static Map<String,Object> standardParameterPass( 
		Map<String,Object> parameterMap, 
		GenericConvertMap<String,Object> inFormat, 
		String classStr 
	) {
		
		for (Map.Entry<String, Object> entry : inFormat.entrySet()) {
			if( blockedStandardParameters.contains(entry.getKey()) ) {
				continue;
			}
			inFormat.put( entry.getKey(), entry.getValue() );
		}
		String subVal;
		
		// Handles custom class string
		if( classStr == null ) {
			classStr = "";
		}
		subVal = inFormat.getString("class");
		
		if( subVal != null && subVal.length() > 0 ) {
			classStr = classStr + " " + subVal;
		}
		
		if(classStr.length() > 0) {
			parameterMap.put("class", classStr);
		}
		
		return parameterMap;
	}
	
	/// Construct the standard HtmlWrapper, for input label, and info boxes 
	protected static String inputHtmlWrapper( String objType, GenericConvertMap<String,Object> format, Map<String,Object> parameterMap, String inputHtml ) {
		return htmlNodeGenerator( "div", null, parameterMap, format.getString("title", "title") + format.getString("value", "") );
	}
	
	/// Wraps a single string builder, with the given parameter type, and DOM node name, and innerHTML.
	/// Note that the innerHTML is reused for the output if given. If there is a need to reuse the innerHTML,
	/// it is suggested to use the string varient instead.
	public static String htmlNodeGenerator( String nodeType, String parameterMapString, Map<String,Object> parameterMap, String innerHTML ) {
		StringBuilder domNode = new StringBuilder("<"+nodeType);
		
		if( parameterMapString != null && parameterMapString.length() > 0 ) {
			domNode.append(" ").append(parameterMapString);
		}
		
		if( parameterMap != null ) {
			for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
				domNode
					.append(" ")
					.append(entry.getKey())
					.append("=")
					.append( escapeParameterQuote( GenericConvert.toString(entry.getValue(), "") ) );
			}
		}
		domNode.append(">");
		
		if( innerHTML != null && innerHTML.length() > 0 ) {
			domNode.append(innerHTML);
		}
		
		domNode.append("</").append(nodeType).append(">");
		
		return domNode.toString();
	}
	
	/// Convert a single object node, into its dom object
	public static String templateObjectToHtml( Map<String,Object> inFormat ) {
		GenericConvertMap<String,Object> format = ProxyGenericConvertMap.ensureGenericConvertMap(inFormat);
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		
		// Gets the object type
		String objType = format.getString( "type", "textBox" );
		
		if( objType.equals("title") ) {
			standardParameterPass( parameterMap, format, "node title" );
			return htmlNodeGenerator( "h3", null, parameterMap, format.getString("title", "title") + format.getString("value", "") );
		} else if( objType.equals("option") ) {
			
		}
		
		//
		return "";
	}
	
	//------------------------sam stuff below-----------------------
	public static String htmlTypeKey = "type";
	public static String htmlTextKey = "text";
	
	public static String divKey = "div";
	public static String titleTagKey = "title";
	public static String dropDownListKey = "dropDown";
	public static String inputFieldKey = "inputField";
	
	protected static String htmlDivTagName = "div";
	protected static String htmlTitleTagName = "h";
	protected static String htmlDropDownTagName = "select";
	protected static String htmlInputFieldTagName = "input";
	
	private Map<String, FormWrapperInterface> customFormWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customFormInputTemplates = new HashMap<String, FormInputInterface>();
	
	public FormGenerator(){
		setupDefaultFormTemplates();
	}
	
	private void setupDefaultFormTemplates(){
		customFormWrapperTemplates = FormWrapperTemplates.defaultWrapperTemplates();
		customFormInputTemplates = FormInputTemplates.defaultInputTemplates();
	}
	
	public void addCustomFormWrapperTemplate(String key, FormWrapperInterface customWrapperTemplate){
		customFormWrapperTemplates.put(key, customWrapperTemplate);
	}
	
	public void addCustomFormInputTemplate(String key, FormInputInterface customInputTemplate){
		customFormInputTemplates.put(key, customInputTemplate);
	}
	
	public String applyTemplating(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			htmlBuilder.append(applyTemplating(node));
		}
		
		return htmlBuilder.toString();
	}
	
	public String applyTemplating(FormNode node){
		String nodeType = node.getString(htmlTypeKey, htmlDivTagName);
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString("wrapper", "div");
		
		formWrappers = customFormWrapperTemplates.get(wrapperType).apply(node);
		String formTextData = customFormInputTemplates.get(nodeType).apply(node);
		
		//get inner data for children
		StringBuilder innerData = new StringBuilder("");
		if(node.childCount() > 0){
			innerData.append(applyTemplating(node.children()));
		}
		
		String finalNodeValue = formWrappers[0]+formTextData+innerData.toString()+formWrappers[1];
		return finalNodeValue;
	}
	
	public static String generateInputClass(FormNode node){
		StringBuilder sb = new StringBuilder("class=\"");
		if(node.containsKey("inputClass")){
			sb.append(node.getString("inputClass") + "\"");
		} else {
			sb.append("pf_"+node.getString("type")+"Class\"");
		}
		return sb.toString();
	}
}
