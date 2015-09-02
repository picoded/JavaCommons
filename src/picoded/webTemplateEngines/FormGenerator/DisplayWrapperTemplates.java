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
		return TableWrapperTemplates.tableWrapper_horizontal(node, true);
	};
	
	protected static FormWrapperInterface verticalTableWrapper = (node)->{
		return TableWrapperTemplates.tableWrapper_vertical(node, true);
	};
	
	protected static FormWrapperInterface imageWrapper = (node)->{
		return FormWrapperTemplates.imageWrapper(node, true);
	};
	
	protected static FormWrapperInterface signatureWrapper = (node)->{
		return FormWrapperTemplates.signatureWrapper(node, true);
	};
	
	protected static FormWrapperInterface datePickerWrapper = (node)->{
		return FormWrapperTemplates.datePickerWrapper(node, true);
	};
	
	/// noneWrapper
	///
	/// No wrappers
	protected static FormWrapperInterface none = (node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(true) );
		return ret;
	};
	
	/// JMTE wrapper implementation
	protected static FormWrapperInterface jmteWrapper = (node)->{
		return FormWrapperTemplates.JMTEWrapper(node, true);
	};
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", DisplayWrapperTemplates.divWrapper);
		
		defaultTemplates.put("div", DisplayWrapperTemplates.divWrapper);
		defaultTemplates.put("for", DisplayWrapperTemplates.forWrapper);
		defaultTemplates.put("none", DisplayWrapperTemplates.none);
		defaultTemplates.put("checkbox", DisplayWrapperTemplates.checkboxWrapper);
		defaultTemplates.put("table", DisplayWrapperTemplates.tableWrapper);
		defaultTemplates.put("verticalTable", DisplayWrapperTemplates.verticalTableWrapper);
		defaultTemplates.put("image", DisplayWrapperTemplates.imageWrapper);
		defaultTemplates.put("signature", DisplayWrapperTemplates.signatureWrapper);
		defaultTemplates.put("date", DisplayWrapperTemplates.datePickerWrapper);
		
		defaultTemplates.put("jmte", DisplayWrapperTemplates.jmteWrapper);
		
		return defaultTemplates;
	}
}
