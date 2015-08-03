package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.Map;

public class FormWrapperTemplates {
	
	//TODO
	/*
	 * raw wrapper to inject raw html
	 */
	
	protected static FormWrapperInterface defaultWrapper = (node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
		StringBuilder prefix = new StringBuilder();
		String classString = FormGenerator.getWrapperClassString(node);
		String cssString = FormGenerator.getWrapperCssString(node);
		prefix.append("<"+HtmlTag.DIV+""+classString+cssString+">\n");	
		
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
		
		defaultTemplates.put("div", FormWrapperTemplates.defaultWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		
		return defaultTemplates;
	}
}
