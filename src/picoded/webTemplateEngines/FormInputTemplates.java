package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FormInputTemplates {
	public static String selectOptionKey = "option";

	protected static FormInputInterface titleInput = (node)->{ 
		String text = node.getString(FormGenerator.htmlTextKey, "");
		//do any additional stuff as needed
		return text; 
	};
	
	protected static FormInputInterface divInput = (node)->{
		//do any additional stuff as needed
		return ""; 
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface selectInput = (node)->{
		StringBuilder sb = new StringBuilder();
		if(node.containsKey(selectOptionKey)){
			Object dropDownObject = node.get(selectOptionKey);
			
			if(dropDownObject instanceof HashMap<?, ?>){
				LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)node.get(selectOptionKey);
				for(String key:dropDownListOptions.keySet()){
					sb.append("<option value=\""+key+"\">"+dropDownListOptions.get(key)+"</option>");
				}
			} else if(dropDownObject instanceof List<?>){
				List<String> dropDownOptions = (List<String>)dropDownObject;
				for(String str:dropDownOptions){
					sb.append("<option>"+str+"</option>");
				}
			}
		}
		
		return sb.toString();
	};
	
	//loltastic function name
	protected static FormInputInterface inputInput = (node)->{
		return "";
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put(FormGenerator.titleTagKey, FormInputTemplates.titleInput);
		defaultTemplates.put(FormGenerator.divKey, FormInputTemplates.divInput);
		defaultTemplates.put(FormGenerator.dropDownListKey, FormInputTemplates.selectInput);
		defaultTemplates.put(FormGenerator.inputFieldKey, FormInputTemplates.inputInput);
		
		return defaultTemplates;
	}
}
