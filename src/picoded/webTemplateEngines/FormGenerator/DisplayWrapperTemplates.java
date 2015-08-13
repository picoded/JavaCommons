package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayWrapperTemplates {
	
	public static FormWrapperInterface default_pdf=(node)->{
		return FormWrapperTemplates.standardDivWrapper(node, true);
	};
	
	public static FormWrapperInterface none_pdf=(node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(true) );
		return ret;
	};
	
	public static Map<String, FormWrapperInterface> defaultWrapperTemplates(){
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", default_pdf);
		
		defaultTemplates.put("div", default_pdf);
		defaultTemplates.put("none", none_pdf);
		
		return defaultTemplates;
	}
}
