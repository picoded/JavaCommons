package picoded.webTemplateEngines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormWrapperTemplates {
	
	protected static FormWrapperInterface defaultWrapper = (node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
		StringBuilder prefix = new StringBuilder();
		String classString = FormGenerator.getWrapperClassString(node);
		String cssString = FormGenerator.getWrapperCssString(node);
		prefix.append("<div"+classString+cssString+">");	
		
		if(node.containsKey("label")){
			String labelName = node.getString("label");
			String labelClassString = FormGenerator.getLabelClassString(node);
			String labelCssString = FormGenerator.getLabelCssString(node);
			prefix.append("<div"+labelClassString+labelCssString+">"+labelName+"</div>");
		}
		
		//generating suffix
		StringBuilder suffix = new StringBuilder("</div>");
		
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
		
		defaultTemplates.put("default", FormWrapperTemplates.defaultWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		
		return defaultTemplates;
	}
}