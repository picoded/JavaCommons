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
		
		//put into function which takes in a stringbuilder
		getWrapperClass(node, classString);
		getCustomClass(node, classString);
		
		classString.append("\"");
		
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
	
	protected static void getWrapperClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.WRAPPER_CLASS)){
			String wrapperClass = node.getString(JsonKeys.WRAPPER_CLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" "+str);
					}
				}
			}else{
				sb.append(" "+wrapperClass);
			}
		}
	}
	
	protected static void getCustomClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.CUSTOMCLASS)){
			String wrapperClass = node.getString(JsonKeys.CUSTOMCLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" pf_w"+str);
					}
				}
			}else{
				sb.append(" pf_w"+wrapperClass);
			}
		}
	}
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("div", FormWrapperTemplates.divWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		
		return defaultTemplates;
	}
}
