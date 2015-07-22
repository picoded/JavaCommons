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
	
	private Map<String, FormWrapperInterface> customFormWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customFormInputTemplates = new HashMap<String, FormInputInterface>();
	
	public FormGenerator(){
		setupDefaultFormTemplates();
	}
	
	private void setupDefaultFormTemplates(){
		customFormWrapperTemplates = FormWrapperTemplates.defaultWrapperTemplates();
		customFormInputTemplates = FormInputTemplates.defaultInputTemplates();
	}
	
	public FormWrapperInterface addCustomFormWrapperTemplate(String key, FormWrapperInterface customWrapperTemplate){
		return customFormWrapperTemplates.put(key, customWrapperTemplate);
	}
	
	public FormInputInterface addCustomFormInputTemplate(String key, FormInputInterface customInputTemplate){
		return customFormInputTemplates.put(key, customInputTemplate);
	}
	
	public String applyTemplating(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			htmlBuilder.append(applyTemplating(node));
		}
		
		return htmlBuilder.toString();
	}
	
	public String applyTemplating(FormNode node){
		String nodeType = node.getString("type", "div");
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString("wrapper", "default");
		
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
	
	public static String getWrapperClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("wrapperClass")){
			sb.append(node.getString("wrapperClass") + "\"");
		} else {
			sb.append("pf_"+node.getString("type")+"Class\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("labelClass")){
			sb.append(node.getString("labelClass") + "\"");
		} else {
			sb.append("pf_labelClass\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("inputClass")){
			sb.append(node.getString("inputClass") + "\"");
		} else {
			sb.append("pf_inputClass\"");
		}
		
		return sb.toString();
	}
	
	public static String getWrapperCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("wrapperCss")){
			sb.append(" style=\""+node.getString("wrapperCss")+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("inputCss")){
			sb.append(" style=\""+node.getString("inputCss")+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("labelCss")){
			sb.append(" style=\""+node.getString("labelCss")+"\"");
		}
		
		return sb.toString();
	}
}
