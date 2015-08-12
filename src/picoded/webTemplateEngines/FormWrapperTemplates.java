package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.Map;

/// FormWrapperTemplates
///
/// Default implmentation of various FormWrapperInterface used in FormGenerator.
///
public class FormWrapperTemplates {
	
	/// divWrapper
	///
	/// Does a basic div wrapper
	protected static FormWrapperInterface divWrapper = (node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
		StringBuilder prefix = new StringBuilder();
		
		//new class string goodness
		StringBuilder classString = new StringBuilder(" class=\"pf_div");
		FormGenerator.getCustomClass(node, classString, JsonKeys.CUSTOMCLASS, "pfw_");
		
		classString.append("\"");
		
		String cssString = FormGenerator.getWrapperCssString(node);
		prefix.append("<"+HtmlTag.DIV+""+classString+cssString+">\n");	 
		
		
		// Adds the label
		//---------------------------------------------------------------
		String labelValue = node.label();
		String fieldValue = node.getFieldName();
		if(!labelValue.isEmpty()){
			StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
			FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
			FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.LABEL_CLASS, "");
			labelClassBuilder.append("\"");
			
			prefix.append("<"+HtmlTag.DIV+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.DIV+">");
		}
		
		//generating suffix
		StringBuilder suffix = new StringBuilder("</"+HtmlTag.DIV+">\n");
		
		prefixSuffix[0] = prefix.toString();
		prefixSuffix[1] = suffix.toString();
		
		return prefixSuffix;
	};
	
	protected static FormWrapperInterface none = (node)->{
		String[] prefixSuffix = new String[]{"", ""};
		
		return prefixSuffix;
	};
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("div", FormWrapperTemplates.divWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		
		return defaultTemplates;
	}
}
