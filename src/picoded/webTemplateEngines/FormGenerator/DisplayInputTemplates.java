package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picoded.conv.RegexUtils;
import picoded.struct.CaseInsensitiveHashMap;

import com.mysql.jdbc.StringUtils;

public class DisplayInputTemplates {
	
	////////////////////////////////////////////////
	//
	//  TO-REFACTOR
	//
	////////////////////////////////////////////////
	
	protected static FormInputInterface div = (node)->{
		return FormInputTemplates.displayDiv(node, "pfi_div");
	};
	
	protected static FormInputInterface header = (node)->{ 
		return FormInputTemplates.displayDiv(node, "pfi_header");
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{ 
		return FormInputTemplates.displayDiv(node, "pfi_select");
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface input_text = (node)->{
		return FormInputTemplates.displayDiv(node, "pfi_inputText");
	};
	
	protected static FormInputInterface raw_html = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		return sb;
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new CaseInsensitiveHashMap<String, FormInputInterface>();
		
		// Wildcard fallback
		defaultTemplates.put("*", FormInputTemplates.div);
		
		// Standard divs
		defaultTemplates.put(JsonKeys.DIV, FormInputTemplates.div);
		defaultTemplates.put(JsonKeys.TITLE, FormInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, FormInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, FormInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, FormInputTemplates.raw_html);
		//defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, dropdownWithOthers);
		
		return defaultTemplates;
	}
	
}
