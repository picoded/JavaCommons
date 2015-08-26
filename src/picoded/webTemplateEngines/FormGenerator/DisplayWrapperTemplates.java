package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayWrapperTemplates {
	
	/// divWrapper
	///
	/// Does a basic div wrapper
	protected static FormWrapperInterface divWrapper = (node)->{
		return FormWrapperTemplates.standardDivWrapper(node, true);
	};
	
	/// forWrapper
	///
	/// Does a basic div wrapper
	protected static FormWrapperInterface forWrapper = (node)->{
		return FormWrapperTemplates.forListWrapper(node, true);
	};
	
	protected static FormWrapperInterface checkboxWrapper = (node)->{
		return FormWrapperTemplates.checkboxWrapper(node, true);
	};
	
	protected static FormWrapperInterface tableWrapper = (node)->{
		return FormWrapperTemplates.tableWrapper(node, true);
	};
	
	/// noneWrapper
	///
	/// No wrappers
	protected static FormWrapperInterface none = (node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(true) );
		return ret;
	};
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", DisplayWrapperTemplates.divWrapper);
		
		defaultTemplates.put("div", DisplayWrapperTemplates.divWrapper);
		defaultTemplates.put("for", DisplayWrapperTemplates.forWrapper);
		defaultTemplates.put("none", DisplayWrapperTemplates.none);
		defaultTemplates.put("checkbox", DisplayWrapperTemplates.checkboxWrapper);
		defaultTemplates.put("table", DisplayWrapperTemplates.tableWrapper);
		defaultTemplates.put("verticalTable", DisplayWrapperTemplates.tableWrapper);
		
		return defaultTemplates;
	}
}
