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
		return FormInputTemplates.displayDiv(node, "pfi_div pfi_display");
	};
	
	protected static FormInputInterface header = (node)->{ 
		return FormInputTemplates.displayDiv(node, "pfi_header pfi_display");
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{ 
		return FormInputTemplates.displayDiv(node, "pfi_select pfi_display");
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface input_text = (node)->{
		return FormInputTemplates.displayDiv(node, "pfi_inputText pfi_display");
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface input_textbox = (node)->{
		return FormInputTemplates.displayDiv(node, "pfi_inputTextBox pfi_display");
	};
	
	protected static FormInputInterface raw_html = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		return sb;
	};
	
	protected static FormInputInterface checkbox = (node)->{
		return FormInputTemplates.createCheckbox(node, true, "pf_div pf_displayCheckboxSet");
	};
	
	protected static FormInputInterface dropdown_WithOthers = (node)->{
		return FormInputTemplates.dropdownWithOthers(node, true);
	};
	
	protected static FormInputInterface table = (node)->{
		return FormInputTemplates.tableWrapper(node, true);
	};
	
	protected static FormInputInterface verticalTable = (node)->{
		return FormInputTemplates.verticalTable(node, true);
	};
	
	protected static FormInputInterface signature = (node)->{
		return FormInputTemplates.signature(node, true);
	};
	
	protected static FormInputInterface image = (node)->{
		return FormInputTemplates.image(node, true);
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new CaseInsensitiveHashMap<String, FormInputInterface>();
		
		// Wildcard fallback
		defaultTemplates.put("*", DisplayInputTemplates.div);
		
		// Standard divs
		defaultTemplates.put(JsonKeys.DIV, DisplayInputTemplates.div);
		defaultTemplates.put(JsonKeys.TITLE, DisplayInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, DisplayInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, DisplayInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.TEXTBOX, DisplayInputTemplates.input_textbox);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, DisplayInputTemplates.raw_html);
		defaultTemplates.put("checkbox", DisplayInputTemplates.checkbox);
		defaultTemplates.put("table", DisplayInputTemplates.table);
		defaultTemplates.put("verticalTable", DisplayInputTemplates.verticalTable);
		defaultTemplates.put("image", DisplayInputTemplates.image);
		defaultTemplates.put("signature", DisplayInputTemplates.signature);
		
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, DisplayInputTemplates.dropdown_WithOthers);
		
		return defaultTemplates;
	}
	
}
