package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.Map;

public class PDFWrapperTemplates {
	
	public static FormWrapperInterface default_pdf=(node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
		StringBuilder prefix = new StringBuilder();
		
		StringBuilder classBuilder = new StringBuilder(" class=\"pf_div");
		FormGenerator.getCustomClass(node, classBuilder, JsonKeys.CUSTOMCLASS, "pfw_");
		FormGenerator.getCustomClass(node, classBuilder, JsonKeys.WRAPPER_CLASS, "");
		classBuilder.append("\"");
		
		String classString = classBuilder.toString();
		String cssString = FormGenerator.getWrapperCssString(node);
		prefix.append("<"+HtmlTag.DIV+""+classString+cssString+">\n");	
		
		//generating suffix
		StringBuilder suffix = new StringBuilder("</"+HtmlTag.DIV+">\n");
		
		prefixSuffix[0] = prefix.toString();
		prefixSuffix[1] = suffix.toString();
		
		StringBuilder ret = new StringBuilder(prefixSuffix[0]);
		
		
		ret.append(prefixSuffix[1]);
		return ret;
	};
	
	public static FormWrapperInterface none_pdf=(node)->{
		StringBuilder ret = new StringBuilder();
		
		return ret;
	};
	
	public static Map<String, FormWrapperInterface> defaultPDFWrapperTemplates(){
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", default_pdf);
		
		defaultTemplates.put("div", default_pdf);
		defaultTemplates.put("none", none_pdf);
		
		return defaultTemplates;
	}
}
